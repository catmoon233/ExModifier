package net.exmo.exmodifier.pack;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.exmo.exmodifier.content.command.TokenArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public final class PackCommands {
    private PackCommands() {}

    public static LiteralArgumentBuilder<CommandSourceStack> packLiteral() {
        return fill(Commands.literal("pack"));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> fill(LiteralArgumentBuilder<CommandSourceStack> root) {
        return root.executes(ctx -> openOrHelp(ctx.getSource()))
                .then(Commands.literal("help").executes(ctx -> help(ctx.getSource())))
                .then(Commands.literal("gui").executes(ctx -> gui(ctx.getSource())))
                .then(Commands.literal("list").executes(ctx -> list(ctx.getSource())))
                .then(Commands.literal("create")
                        .then(Commands.argument("name", TokenArgument.token())
                                .executes(ctx -> create(ctx, ""))
                                .then(Commands.argument("namespace", TokenArgument.token())
                                        .executes(ctx -> create(ctx, arg(ctx, "namespace"))))))
                .then(Commands.literal("delete")
                        .then(Commands.argument("pack", TokenArgument.token()).suggests(suggestPacks())
                                .executes(PackCommands::delete)))
                .then(Commands.literal("apply")
                        .executes(ctx -> apply(ctx, ""))
                        .then(Commands.argument("pack", TokenArgument.token()).suggests(suggestPacks())
                                .executes(ctx -> apply(ctx, arg(ctx, "pack")))))
                .then(Commands.literal("edit")
                        .then(Commands.argument("pack", TokenArgument.token()).suggests(suggestPacks())
                                .then(entry())));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> entry() {
        return Commands.literal("entry")
                .then(Commands.literal("list")
                        .executes(ctx -> listEntries(ctx, null))
                        .then(Commands.argument("kind", TokenArgument.token()).suggests(suggestKinds())
                                .executes(ctx -> listEntries(ctx, kind(ctx)))))
                .then(Commands.literal("show")
                        .then(kindId(ctx -> show(ctx))))
                .then(Commands.literal("new")
                        .then(kindId(PackCommands::createEntry, false)))
                .then(Commands.literal("remove")
                        .then(kindId(PackCommands::removeEntry, true)))
                .then(Commands.literal("import")
                        .then(Commands.argument("kind", TokenArgument.token()).suggests(suggestKinds())
                                .then(Commands.argument("id", TokenArgument.token()).suggests(suggestRegistered())
                                        .executes(PackCommands::importEntry)
                                        .then(Commands.argument("as", TokenArgument.token())
                                                .executes(PackCommands::importEntry)))))
                .then(Commands.literal("set")
                        .then(kindIdField("value", PackCommands::setValue)))
                .then(Commands.literal("add")
                        .then(kindIdField("value", PackCommands::addValue)))
                .then(Commands.literal("take")
                        .then(kindIdField("value", PackCommands::takeValue)))
                .then(Commands.literal("attribute")
                        .then(Commands.literal("add")
                                .then(afterPackId(Commands.argument("field", TokenArgument.token()).suggests(suggestFields())
                                        .then(Commands.argument("attribute", TokenArgument.token()).suggests(suggestSource(SuggestSource.ATTRIBUTE))
                                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                                                        .then(Commands.argument("operation", TokenArgument.token()).suggests(suggestSource(SuggestSource.OPERATION))
                                                                .executes(ctx -> attribute(ctx, 0.0D))
                                                                .then(Commands.argument("per_level", DoubleArgumentType.doubleArg())
                                                                        .executes(ctx -> attribute(ctx, DoubleArgumentType.getDouble(ctx, "per_level"))))))))))
                        .then(Commands.literal("remove")
                                .then(kindIdField("index", PackCommands::takeValue))))
                .then(Commands.literal("effect")
                        .then(Commands.literal("add")
                                .then(afterPackId(Commands.argument("field", TokenArgument.token()).suggests(suggestFields())
                                        .then(Commands.argument("effect", TokenArgument.token()).suggests(suggestSource(SuggestSource.EFFECT))
                                                .then(Commands.argument("duration", IntegerArgumentType.integer(1))
                                                        .then(Commands.argument("amplifier", IntegerArgumentType.integer(0))
                                                                .executes(PackCommands::effect))))))))
                .then(Commands.literal("level")
                        .then(Commands.literal("add")
                                .then(Commands.argument("id", TokenArgument.token()).suggests(suggestSuitIds())
                                        .then(Commands.argument("pieces", IntegerArgumentType.integer(1))
                                                .executes(ctx -> level(ctx, "exmodifier:tick"))
                                                .then(Commands.argument("trigger", TokenArgument.token()).suggests(suggestSource(SuggestSource.TRIGGER))
                                                        .executes(ctx -> level(ctx, arg(ctx, "trigger")))))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("id", TokenArgument.token()).suggests(suggestSuitIds())
                                        .then(Commands.argument("index", IntegerArgumentType.integer(0))
                                                .executes(PackCommands::removeLevel)))))
                .then(Commands.literal("map")
                        .then(Commands.literal("put")
                                .then(afterPackId(Commands.argument("field", TokenArgument.token()).suggests(suggestFields())
                                        .then(Commands.argument("key", TokenArgument.token()).suggests(suggestValues())
                                                .then(Commands.argument("number", DoubleArgumentType.doubleArg())
                                                        .executes(PackCommands::mapPut))))))
                        .then(Commands.literal("remove")
                                .then(kindIdField("key", PackCommands::mapRemove))));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> kindId(
            java.util.function.Function<CommandContext<CommandSourceStack>, Integer> action, boolean packIds) {
        var id = Commands.argument("id", TokenArgument.token()).suggests(packIds ? suggestPackIds() : suggestNewIds());
        if (action != null) id.executes(action::apply);
        return Commands.argument("kind", TokenArgument.token()).suggests(suggestKinds()).then(id);
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> kindId(
            java.util.function.Function<CommandContext<CommandSourceStack>, Integer> action) {
        return kindId(action, true);
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> afterPackId(
            com.mojang.brigadier.builder.ArgumentBuilder<CommandSourceStack, ?> next) {
        var id = Commands.argument("id", TokenArgument.token()).suggests(suggestPackIds()).then(next);
        return Commands.argument("kind", TokenArgument.token()).suggests(suggestKinds()).then(id);
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> kindIdField(
            String valueName, java.util.function.Function<CommandContext<CommandSourceStack>, Integer> action) {
        return afterPackId(Commands.argument("field", TokenArgument.token()).suggests(suggestFields())
                .then(Commands.argument(valueName, TokenArgument.token()).suggests(suggestValues())
                        .executes(action::apply)));
    }

    private static int openOrHelp(CommandSourceStack source) {
        if (source.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) return gui(source);
        return help(source);
    }

    private static int help(CommandSourceStack source) {
        String[] lines = {
                "/exmodifier pack gui  打开可视化编辑器",
                "/exmodifier pack create <包名> [命名空间]",
                "/exmodifier pack edit <包> entry new <类型> <id>",
                "/exmodifier pack edit <包> entry add <类型> <id> <字段> <值>",
                "字段值会提示已注册词条，以及本包里还没重载的词条",
                "/exmodifier pack edit <包> entry attribute add <类型> <id> <字段> <属性> <数值> <运算> [每级]",
                "/exmodifier pack apply <包>"
        };
        for (String line : lines) source.sendSuccess(() -> Component.literal(line), false);
        return 1;
    }

    private static int gui(CommandSourceStack source) {
        if (!(source.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) {
            source.sendFailure(Component.translatable("command.exmodifier.pack.player_only"));
            return 0;
        }
        PackNetwork.open(player);
        return 1;
    }

    private static int list(CommandSourceStack source) {
        List<String> packs = PackLiveIndex.store(source.getServer()).packs();
        source.sendSuccess(() -> Component.translatable("command.exmodifier.pack.list", packs.isEmpty() ? "-" : String.join(", ", packs)), false);
        return packs.size();
    }

    private static int create(CommandContext<CommandSourceStack> ctx, String namespace) {
        PackResult result = PackLiveIndex.store(ctx.getSource().getServer()).create(arg(ctx, "name"), namespace);
        return feedback(ctx.getSource(), result);
    }

    private static int delete(CommandContext<CommandSourceStack> ctx) {
        return feedback(ctx.getSource(), PackLiveIndex.store(ctx.getSource().getServer()).delete(arg(ctx, "pack")));
    }

    private static int apply(CommandContext<CommandSourceStack> ctx, String pack) {
        return feedback(ctx.getSource(), PackApply.apply(ctx.getSource().getServer(), pack.isBlank() ? null : pack,
                ctx.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer player ? player : null));
    }

    private static int listEntries(CommandContext<CommandSourceStack> ctx, PackKind kind) {
        PackStore store = PackLiveIndex.store(ctx.getSource().getServer());
        String pack = arg(ctx, "pack");
        List<String> lines = new ArrayList<>();
        for (PackDocument document : store.load(pack).documents()) {
            if (kind == null || document.kind() == kind) lines.add(document.kind().folder() + " " + document.id());
        }
        ctx.getSource().sendSuccess(() -> Component.literal(lines.isEmpty() ? "(空)" : String.join("\n", lines)), false);
        return lines.size();
    }

    private static int show(CommandContext<CommandSourceStack> ctx) {
        return withDoc(ctx, (store, pack, kind, id, json) -> {
            ctx.getSource().sendSuccess(() -> Component.literal(json.toString()), false);
            return PackResult.ok(id.toString());
        });
    }

    private static int createEntry(CommandContext<CommandSourceStack> ctx) {
        PackStore store = PackLiveIndex.store(ctx.getSource().getServer());
        PackKind kind = kind(ctx);
        ResourceLocation id = PackIds.qualify(arg(ctx, "id"), store.namespace(arg(ctx, "pack")));
        if (kind == null || id == null) return fail(ctx.getSource(), "类型或 ID 不合法");
        return feedback(ctx.getSource(), store.upsert(arg(ctx, "pack"), kind, id, kind.emptyJson()));
    }

    private static int removeEntry(CommandContext<CommandSourceStack> ctx) {
        return withDoc(ctx, (store, pack, kind, id, json) -> store.remove(pack, kind, id));
    }

    private static int importEntry(CommandContext<CommandSourceStack> ctx) {
        PackStore store = PackLiveIndex.store(ctx.getSource().getServer());
        PackKind kind = kind(ctx);
        if (kind == null) return fail(ctx.getSource(), "未知类型");
        ResourceLocation from = resolveRegistered(ctx, kind, arg(ctx, "id"));
        JsonElement exported = from == null ? null : PackLiveIndex.export(ctx.getSource().getServer().registryAccess(), kind, from);
        if (exported == null) return fail(ctx.getSource(), "已注册内容里没有这个 ID");
        String as = arg(ctx, "as");
        ResourceLocation target = as.isBlank() ? from : PackIds.qualify(as, store.namespace(arg(ctx, "pack")));
        if (target == null) return fail(ctx.getSource(), "目标 ID 不合法");
        return feedback(ctx.getSource(), store.upsert(arg(ctx, "pack"), kind, target, exported));
    }

    private static int setValue(CommandContext<CommandSourceStack> ctx) {
        return withDoc(ctx, (store, pack, kind, id, json) -> {
            PackField field = PackKind.resolve(kind, arg(ctx, "value").isEmpty() ? arg(ctx, "field") : arg(ctx, "field"));
            String path = arg(ctx, "field");
            field = PackKind.resolve(kind, path);
            if (field == null) return PackResult.fail("未知字段");
            if (field.isList() || field.isMap() || field.type() == PackField.FieldType.ATTRIBUTE_LIST
                    || field.type() == PackField.FieldType.EFFECT_LIST || field.type() == PackField.FieldType.SUIT_LEVEL_LIST) {
                return PackResult.fail("列表或属性请用 add / attribute / effect / map / level");
            }
            JsonObject object = PackMutations.object(json);
            String value = resolveRef(ctx, field.source(), arg(ctx, "value"));
            if (value == null) return PackResult.fail("引用不唯一，请补全命名空间");
            if (!PackMutations.setScalar(object, field, path, value)) return PackResult.fail("值无法解析");
            return store.upsert(pack, kind, id, object);
        });
    }

    private static int addValue(CommandContext<CommandSourceStack> ctx) {
        return withDoc(ctx, (store, pack, kind, id, json) -> {
            String path = arg(ctx, "field");
            PackField field = PackKind.resolve(kind, path);
            if (field == null || !(field.type() == PackField.FieldType.ID_LIST || field.type() == PackField.FieldType.STRING_LIST)) {
                return PackResult.fail("add 只用于 ID 列表或字符串列表");
            }
            String value = resolveRef(ctx, field.source(), arg(ctx, "value"));
            if (value == null) return PackResult.fail("引用不唯一，请补全命名空间");
            JsonObject object = PackMutations.object(json);
            PackMutations.add(object, path, new JsonPrimitive(value));
            return store.upsert(pack, kind, id, object);
        });
    }

    private static int takeValue(CommandContext<CommandSourceStack> ctx) {
        return withDoc(ctx, (store, pack, kind, id, json) -> {
            String path = arg(ctx, "field");
            String raw = arg(ctx, "value").isBlank() ? arg(ctx, "index") : arg(ctx, "value");
            JsonObject object = PackMutations.object(json);
            if (!PackMutations.remove(object, path, raw)) return PackResult.fail("没有这项");
            return store.upsert(pack, kind, id, object);
        });
    }

    private static int attribute(CommandContext<CommandSourceStack> ctx, double perLevel) {
        return withDoc(ctx, (store, pack, kind, id, json) -> {
            String path = arg(ctx, "field");
            PackField field = PackKind.resolve(kind, path);
            if (field == null || field.type() != PackField.FieldType.ATTRIBUTE_LIST) return PackResult.fail("不是属性列表");
            String attribute = resolveRef(ctx, SuggestSource.ATTRIBUTE, arg(ctx, "attribute"));
            if (attribute == null) return PackResult.fail("属性不唯一");
            JsonObject object = PackMutations.object(json);
            PackMutations.addAttribute(object, path, attribute, DoubleArgumentType.getDouble(ctx, "amount"), perLevel, arg(ctx, "operation"), "");
            return store.upsert(pack, kind, id, object);
        });
    }

    private static int effect(CommandContext<CommandSourceStack> ctx) {
        return withDoc(ctx, (store, pack, kind, id, json) -> {
            String path = arg(ctx, "field");
            PackField field = PackKind.resolve(kind, path);
            if (field == null || field.type() != PackField.FieldType.EFFECT_LIST) return PackResult.fail("不是效果列表");
            String effect = resolveRef(ctx, SuggestSource.EFFECT, arg(ctx, "effect"));
            if (effect == null) return PackResult.fail("效果不唯一");
            JsonObject object = PackMutations.object(json);
            PackMutations.addEffect(object, path, effect, IntegerArgumentType.getInteger(ctx, "duration"),
                    IntegerArgumentType.getInteger(ctx, "amplifier"), false, false);
            return store.upsert(pack, kind, id, object);
        });
    }

    private static int level(CommandContext<CommandSourceStack> ctx, String trigger) {
        PackStore store = PackLiveIndex.store(ctx.getSource().getServer());
        String pack = arg(ctx, "pack");
        ResourceLocation id = PackIds.qualify(arg(ctx, "id"), store.namespace(pack));
        JsonElement json = id == null ? null : store.read(pack, PackKind.SUIT, id).orElse(null);
        if (json == null) return fail(ctx.getSource(), "套装不在包内");
        String resolved = resolveRef(ctx, SuggestSource.TRIGGER, trigger);
        JsonObject object = PackMutations.object(json);
        PackMutations.addSuitLevel(object, IntegerArgumentType.getInteger(ctx, "pieces"), resolved == null ? trigger : resolved);
        return feedback(ctx.getSource(), store.upsert(pack, PackKind.SUIT, id, object));
    }

    private static int removeLevel(CommandContext<CommandSourceStack> ctx) {
        PackStore store = PackLiveIndex.store(ctx.getSource().getServer());
        String pack = arg(ctx, "pack");
        ResourceLocation id = PackIds.qualify(arg(ctx, "id"), store.namespace(pack));
        JsonElement json = id == null ? null : store.read(pack, PackKind.SUIT, id).orElse(null);
        if (json == null) return fail(ctx.getSource(), "套装不在包内");
        JsonObject object = PackMutations.object(json);
        if (!PackMutations.remove(object, "levels", Integer.toString(IntegerArgumentType.getInteger(ctx, "index")))) {
            return fail(ctx.getSource(), "没有这个档位");
        }
        return feedback(ctx.getSource(), store.upsert(pack, PackKind.SUIT, id, object));
    }

    private static int mapPut(CommandContext<CommandSourceStack> ctx) {
        return withDoc(ctx, (store, pack, kind, id, json) -> {
            String path = arg(ctx, "field");
            PackField field = PackKind.resolve(kind, path);
            if (field == null || !field.isMap()) return PackResult.fail("不是映射字段");
            String key = resolveRef(ctx, field.source(), arg(ctx, "key"));
            if (key == null) return PackResult.fail("键不唯一");
            JsonObject object = PackMutations.object(json);
            double number = DoubleArgumentType.getDouble(ctx, "number");
            PackMutations.putMap(object, path, key, field.type() == PackField.FieldType.INT_MAP
                    ? new JsonPrimitive((int) number) : new JsonPrimitive(number));
            return store.upsert(pack, kind, id, object);
        });
    }

    private static int mapRemove(CommandContext<CommandSourceStack> ctx) {
        return withDoc(ctx, (store, pack, kind, id, json) -> {
            JsonObject object = PackMutations.object(json);
            if (!PackMutations.removeMap(object, arg(ctx, "field"), arg(ctx, "key"))) return PackResult.fail("没有这个键");
            return store.upsert(pack, kind, id, object);
        });
    }

    private interface Editor {
        PackResult apply(PackStore store, String pack, PackKind kind, ResourceLocation id, JsonElement json);
    }

    private static int withDoc(CommandContext<CommandSourceStack> ctx, Editor editor) {
        PackStore store = PackLiveIndex.store(ctx.getSource().getServer());
        String pack = arg(ctx, "pack");
        PackKind kind = kind(ctx);
        ResourceLocation id = PackIds.qualify(arg(ctx, "id"), store.namespace(pack));
        if (kind == null || id == null) return fail(ctx.getSource(), "类型或 ID 不合法");
        JsonElement json = store.read(pack, kind, id).orElse(null);
        if (json == null || !json.isJsonObject()) return fail(ctx.getSource(), "条目不在包内。先 new，或 import 已注册内容");
        return feedback(ctx.getSource(), editor.apply(store, pack, kind, id, json));
    }

    private static String resolveRef(CommandContext<CommandSourceStack> ctx, SuggestSource source, String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return null;
        if (trimmed.contains(":") || source == null || source == SuggestSource.NONE || source == SuggestSource.BOOL
                || source == SuggestSource.OPERATION || source == SuggestSource.UP_EVENT) {
            return trimmed;
        }
        MinecraftServer server = ctx.getSource().getServer();
        String pack = arg(ctx, "pack");
        List<String> options = PackLiveIndex.suggest(server, pack, source);
        String namespace = PackLiveIndex.store(server).namespace(pack);
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.equals(trimmed) || option.endsWith(":" + trimmed)) matches.add(option);
        }
        if (matches.size() == 1) return matches.get(0);
        if (matches.size() > 1) {
            String preferred = namespace + ":" + trimmed;
            if (matches.contains(preferred)) return preferred;
            if (matches.contains("exmodifier:" + trimmed)) return "exmodifier:" + trimmed;
            if (matches.contains("minecraft:" + trimmed)) return "minecraft:" + trimmed;
            return null;
        }
        if (source == SuggestSource.ITEM || source == SuggestSource.EFFECT || source == SuggestSource.ENTITY
                || source == SuggestSource.ATTRIBUTE || source == SuggestSource.PARTICLE || source == SuggestSource.DAMAGE_TYPE
                || source == SuggestSource.ITEM_TAG) {
            return "minecraft:" + trimmed;
        }
        return namespace + ":" + trimmed;
    }

    private static ResourceLocation resolveRegistered(CommandContext<CommandSourceStack> ctx, PackKind kind, String raw) {
        MinecraftServer server = ctx.getSource().getServer();
        String namespace = PackLiveIndex.store(server).namespace(arg(ctx, "pack"));
        ResourceLocation direct = raw.contains(":") ? PackIds.qualify(raw, namespace) : null;
        if (direct != null && PackLiveIndex.export(server.registryAccess(), kind, direct) != null) return direct;
        ResourceLocation local = PackIds.qualify(raw, namespace);
        if (local != null && PackLiveIndex.export(server.registryAccess(), kind, local) != null) return local;
        ResourceLocation minecraft = PackIds.qualify(raw, "minecraft");
        if (minecraft != null && PackLiveIndex.export(server.registryAccess(), kind, minecraft) != null) return minecraft;
        ResourceLocation mod = PackIds.qualify(raw, "exmodifier");
        if (mod != null && PackLiveIndex.export(server.registryAccess(), kind, mod) != null) return mod;
        return null;
    }

    private static int feedback(CommandSourceStack source, PackResult result) {
        if (result.ok()) {
            source.sendSuccess(() -> Component.literal(result.message()), false);
            return 1;
        }
        return fail(source, result.message());
    }

    private static int fail(CommandSourceStack source, String message) {
        source.sendFailure(Component.literal(message));
        return 0;
    }

    private static PackKind kind(CommandContext<CommandSourceStack> ctx) {
        return PackKind.byFolder(arg(ctx, "kind"));
    }

    private static String arg(CommandContext<?> ctx, String name) {
        try {
            return ctx.getArgument(name, String.class);
        } catch (IllegalArgumentException ex) {
            return "";
        }
    }

    private static SuggestionProvider<CommandSourceStack> suggestPacks() {
        return (ctx, builder) -> SharedSuggestionProvider.suggest(PackLiveIndex.store(ctx.getSource().getServer()).packs(), builder);
    }

    private static SuggestionProvider<CommandSourceStack> suggestKinds() {
        return (ctx, builder) -> SharedSuggestionProvider.suggest(java.util.Arrays.stream(PackKind.values()).map(PackKind::folder).toList(), builder);
    }

    private static SuggestionProvider<CommandSourceStack> suggestPackIds() {
        return (ctx, builder) -> SharedSuggestionProvider.suggest(ids(ctx, true), builder);
    }

    private static SuggestionProvider<CommandSourceStack> suggestNewIds() {
        return (ctx, builder) -> SharedSuggestionProvider.suggest(ids(ctx, false), builder);
    }

    private static SuggestionProvider<CommandSourceStack> suggestRegistered() {
        return (ctx, builder) -> {
            PackKind kind = kind(ctx);
            List<String> registered = kind == null ? List.of() : PackLiveIndex.registered(ctx.getSource().getServer().registryAccess(), kind);
            SuggestSource extra = kind == null ? SuggestSource.NONE : kind.idSource();
            if (extra == SuggestSource.ITEM || extra == SuggestSource.ENTITY) {
                registered = new ArrayList<>(registered);
                registered.addAll(PackLiveIndex.vanilla(ctx.getSource().getServer().registryAccess(), extra));
            }
            return SharedSuggestionProvider.suggest(registered, builder);
        };
    }

    private static SuggestionProvider<CommandSourceStack> suggestSuitIds() {
        return (ctx, builder) -> SharedSuggestionProvider.suggest(
                PackLiveIndex.store(ctx.getSource().getServer()).ids(arg(ctx, "pack"), PackKind.SUIT), builder);
    }

    private static SuggestionProvider<CommandSourceStack> suggestFields() {
        return (ctx, builder) -> {
            PackKind kind = kind(ctx);
            if (kind == null) return builder.buildFuture();
            JsonElement json = PackLiveIndex.store(ctx.getSource().getServer())
                    .read(arg(ctx, "pack"), kind, PackIds.qualify(arg(ctx, "id"), PackLiveIndex.store(ctx.getSource().getServer()).namespace(arg(ctx, "pack"))))
                    .orElse(null);
            return SharedSuggestionProvider.suggest(PackSuggestions.fieldPaths(kind, json), builder);
        };
    }

    private static SuggestionProvider<CommandSourceStack> suggestValues() {
        return (ctx, builder) -> {
            PackField field = PackKind.resolve(kind(ctx), arg(ctx, "field"));
            SuggestSource source = field == null ? SuggestSource.NONE : field.source();
            return SharedSuggestionProvider.suggest(PackLiveIndex.suggest(ctx.getSource().getServer(), arg(ctx, "pack"), source), builder);
        };
    }

    private static SuggestionProvider<CommandSourceStack> suggestSource(SuggestSource source) {
        return (ctx, builder) -> SharedSuggestionProvider.suggest(PackLiveIndex.suggest(ctx.getSource().getServer(), arg(ctx, "pack"), source), builder);
    }

    private static List<String> ids(CommandContext<CommandSourceStack> ctx, boolean packOnly) {
        PackKind kind = kind(ctx);
        if (kind == null) return List.of();
        PackStore store = PackLiveIndex.store(ctx.getSource().getServer());
        String pack = arg(ctx, "pack");
        if (packOnly) return store.ids(pack, kind);
        List<String> merged = new ArrayList<>(PackSuggestions.ids(
                PackLiveIndex.registered(ctx.getSource().getServer().registryAccess(), kind),
                store.ids(pack, kind),
                store.idsExcept(pack, kind)));
        if (kind.idSource() == SuggestSource.ITEM || kind.idSource() == SuggestSource.ENTITY) {
            merged.addAll(PackLiveIndex.vanilla(ctx.getSource().getServer().registryAccess(), kind.idSource()));
        }
        return merged;
    }
}
