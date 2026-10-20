package net.exmo.exmodifier.pack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.exmo.exmodifier.Exmodifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Set;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class PackNetwork {
    public static final int SYNC_LIMIT = 786432;
    private static final Set<UUID> OPEN = ConcurrentHashMap.newKeySet();
    public static Consumer<String> clientReceiver = json -> {};

    private PackNetwork() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToServer(Request.TYPE, Request.STREAM_CODEC, PackNetwork::onRequest);
        registrar.playToClient(Sync.TYPE, Sync.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> clientReceiver.accept(payload.json())));
    }

    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        OPEN.remove(event.getEntity().getUUID());
    }

    public static void request(String action, String body) {
        PacketDistributor.sendToServer(new Request(action, body == null ? "{}" : body));
    }

    public static void open(ServerPlayer player) {
        if (!PackAccess.allowed(player)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("command.exmodifier.pack.denied"));
            return;
        }
        OPEN.add(player.getUUID());
        send(player, snapshot(player.getServer(), true, true, "", "", "", ""));
    }

    public static void syncOpen(MinecraftServer server, boolean open, String message) {
        String json = snapshot(server, open, true, message, "", "", "");
        for (UUID id : OPEN) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) send(player, json);
        }
    }

    private static void onRequest(Request payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!PackAccess.allowed(player)) {
                send(player, snapshot(player.getServer(), false, false, "需要 OP 或单人存档主人", "", "", ""));
                return;
            }
            handle(player, payload.action(), payload.body());
        });
    }

    private static void handle(ServerPlayer player, String action, String body) {
        JsonObject json = parse(body);
        MinecraftServer server = player.getServer();
        PackStore store = PackLiveIndex.store(server);
        String pack = text(json, "pack");
        PackKind kind = PackKind.byFolder(text(json, "kind"));
        String selectPack = "";
        String selectKind = "";
        String selectId = "";
        PackResult result = switch (action == null ? "" : action) {
            case "open" -> {
                OPEN.add(player.getUUID());
                yield PackResult.ok("");
            }
            case "close" -> {
                OPEN.remove(player.getUUID());
                yield PackResult.ok("");
            }
            case "create" -> {
                PackResult created = store.create(text(json, "name"), text(json, "namespace"));
                if (created.ok()) selectPack = text(json, "name");
                yield created;
            }
            case "delete" -> store.delete(pack);
            case "remove" -> {
                ResourceLocation id = PackIds.qualify(text(json, "id"), store.namespace(pack));
                yield store.remove(pack, kind, id);
            }
            case "upsert" -> {
                ResourceLocation id = PackIds.qualify(text(json, "id"), store.namespace(pack));
                JsonElement document = parseElement(text(json, "json"));
                selectPack = pack;
                selectKind = kind == null ? "" : kind.folder();
                selectId = id == null ? "" : id.toString();
                yield store.upsert(pack, kind, id, document);
            }
            case "import" -> importRegistered(server, store, pack, kind, text(json, "id"), text(json, "as"));
            case "apply" -> PackApply.apply(server, pack, player);
            default -> PackResult.fail("未知操作");
        };
        if ("create".equals(action) && result.ok()) selectPack = text(json, "name");
        if ("import".equals(action) && result.ok()) {
            selectPack = pack;
            selectKind = kind == null ? "" : kind.folder();
            selectId = result.message().startsWith("id:") ? result.message().substring(3) : text(json, "as");
        }
        if (!"close".equals(action)) {
            OPEN.add(player.getUUID());
            send(player, snapshot(server, "open".equals(action), result.ok(), result.message(), selectPack, selectKind, selectId));
        }
    }

    private static PackResult resultHold(PackResult result) {
        return result;
    }

    private static PackResult importRegistered(MinecraftServer server, PackStore store, String pack, PackKind kind, String raw, String as) {
        if (kind == null) return PackResult.fail("未知类型");
        ResourceLocation from = locateImport(server, store, pack, kind, raw);
        JsonElement exported = from == null ? null : PackLiveIndex.export(server.registryAccess(), kind, from);
        if (exported == null && from != null) exported = store.find(kind, from).orElse(null);
        if (exported == null) return PackResult.fail("已注册内容里没有 " + raw);
        ResourceLocation target = as == null || as.isBlank() ? from : PackIds.qualify(as, store.namespace(pack));
        if (target == null) return PackResult.fail("目标 ID 不合法");
        PackResult saved = store.upsert(pack, kind, target, exported);
        return saved.ok() ? PackResult.ok("id:" + target) : saved;
    }

    private static ResourceLocation locateImport(MinecraftServer server, PackStore store, String pack, PackKind kind, String raw) {
        if (raw == null || raw.isBlank()) return null;
        if (raw.contains(":")) return PackIds.qualify(raw, store.namespace(pack));
        for (String namespace : List.of(store.namespace(pack), "exmodifier", "minecraft")) {
            ResourceLocation id = PackIds.qualify(raw, namespace);
            if (id == null) continue;
            if (PackLiveIndex.export(server.registryAccess(), kind, id) != null || store.find(kind, id).isPresent()) return id;
        }
        return PackIds.qualify(raw, store.namespace(pack));
    }

    public static String snapshot(MinecraftServer server, boolean open, boolean ok, String message, String selectPack, String selectKind, String selectId) {
        PackStore store = PackLiveIndex.store(server);
        JsonObject root = new JsonObject();
        root.addProperty("open", open);
        root.addProperty("ok", ok);
        root.addProperty("message", message == null ? "" : message);
        root.addProperty("selectPack", selectPack == null ? "" : selectPack);
        root.addProperty("selectKind", selectKind == null ? "" : selectKind);
        root.addProperty("selectId", selectId == null ? "" : selectId);
        JsonArray packs = new JsonArray();
        for (String name : store.packs()) {
            PackData data = store.load(name);
            JsonObject pack = new JsonObject();
            pack.addProperty("name", data.name());
            pack.addProperty("namespace", data.namespace());
            JsonArray documents = new JsonArray();
            for (PackDocument document : data.documents()) {
                JsonObject one = new JsonObject();
                one.addProperty("kind", document.kind().folder());
                one.addProperty("id", document.id().toString());
                one.addProperty("json", document.json().toString());
                documents.add(one);
            }
            pack.add("documents", documents);
            packs.add(pack);
        }
        root.add("packs", packs);
        JsonObject registered = new JsonObject();
        for (PackKind kind : PackKind.values()) {
            JsonArray ids = new JsonArray();
            for (String id : PackLiveIndex.registered(server.registryAccess(), kind)) ids.add(id);
            registered.add(kind.folder(), ids);
        }
        root.add("registered", registered);
        JsonObject extra = new JsonObject();
        extra.add("school", strings(PackLiveIndex.vanilla(server.registryAccess(), SuggestSource.SCHOOL)));
        extra.add("special", strings(PackLiveIndex.vanilla(server.registryAccess(), SuggestSource.SPECIAL)));
        extra.add("damage_type", strings(PackLiveIndex.vanilla(server.registryAccess(), SuggestSource.DAMAGE_TYPE)));
        root.add("extra", extra);
        String encoded = root.toString();
        if (encoded.length() <= SYNC_LIMIT) return encoded;
        root.remove("extra");
        encoded = root.toString();
        return encoded.length() <= SYNC_LIMIT ? encoded : "{\"open\":" + open + ",\"ok\":false,\"message\":\"包太大，无法同步到客户端\"}";
    }

    private static JsonArray strings(java.util.List<String> values) {
        JsonArray array = new JsonArray();
        int count = 0;
        for (String value : values) {
            if (count++ > 400) break;
            array.add(value);
        }
        return array;
    }

    private static void send(ServerPlayer player, String json) {
        PacketDistributor.sendToPlayer(player, new Sync(json));
    }

    private static JsonObject parse(String body) {
        try {
            JsonElement element = JsonParser.parseString(body == null || body.isBlank() ? "{}" : body);
            return element.isJsonObject() ? element.getAsJsonObject() : new JsonObject();
        } catch (RuntimeException ex) {
            return new JsonObject();
        }
    }

    private static JsonElement parseElement(String raw) {
        try {
            return JsonParser.parseString(raw == null || raw.isBlank() ? "{}" : raw);
        } catch (RuntimeException ex) {
            return new JsonObject();
        }
    }

    private static String text(JsonObject json, String key) {
        return json.has(key) && json.get(key).isJsonPrimitive() ? json.get(key).getAsString() : "";
    }

    public record Request(String action, String body) implements CustomPacketPayload {
        public static final Type<Request> TYPE = new Type<>(Exmodifier.id("pack_request"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Request> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.stringUtf8(64), Request::action,
                ByteBufCodecs.stringUtf8(SYNC_LIMIT), Request::body,
                Request::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record Sync(String json) implements CustomPacketPayload {
        public static final Type<Sync> TYPE = new Type<>(Exmodifier.id("pack_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Sync> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.stringUtf8(SYNC_LIMIT), Sync::json,
                Sync::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
