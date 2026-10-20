package net.exmo.exmodifier.pack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.exmo.exmodifier.content.special.SpecialEffects;
import net.exmo.exmodifier.core.data.ElementDefinition;
import net.exmo.exmodifier.core.data.ModifierEntryDefinition;
import net.exmo.exmodifier.core.data.SuitDefinition;
import net.exmo.exmodifier.core.registry.ExModifierDatapack;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.storage.LevelResource;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

public final class PackLiveIndex {
    private PackLiveIndex() {}

    public static PackStore store(MinecraftServer server) {
        return new PackStore(server.getWorldPath(LevelResource.DATAPACK_DIR));
    }

    public static List<String> registered(RegistryAccess access, PackKind kind) {
        if (access == null || kind == null) return List.of();
        return keys(access, kind.registryKey());
    }

    public static List<String> vanilla(RegistryAccess access, SuggestSource source) {
        if (source == null) return List.of();
        return switch (source) {
            case BOOL -> PackSuggestions.booleans();
            case OPERATION -> PackSuggestions.operations();
            case UP_EVENT -> PackSuggestions.upEvents();
            case TRIGGER -> PackSuggestions.triggers();
            case ATTRIBUTE -> names(BuiltInRegistries.ATTRIBUTE.keySet());
            case ITEM -> names(BuiltInRegistries.ITEM.keySet());
            case ENTITY -> names(BuiltInRegistries.ENTITY_TYPE.keySet());
            case EFFECT -> names(BuiltInRegistries.MOB_EFFECT.keySet());
            case PARTICLE -> names(BuiltInRegistries.PARTICLE_TYPE.keySet());
            case ITEM_TAG -> BuiltInRegistries.ITEM.getTagNames().map(TagKey::location).map(ResourceLocation::toString).sorted().toList();
            case DAMAGE_TYPE -> access == null ? List.of() : keys(access, Registries.DAMAGE_TYPE);
            case SPECIAL -> specials(access);
            case SCHOOL -> schools(access);
            case ENTRY, SLOT, ELEMENT, QUALITY, ITEM_TYPE, SUIT, ATTRI_EFFECT, DYNAMIC_ATTRIBUTE ->
                    source.kind() == null || access == null ? List.of() : registered(access, source.kind());
            default -> List.of();
        };
    }

    public static List<String> suggest(MinecraftServer server, String pack, SuggestSource source) {
        if (server == null || source == null) return List.of();
        PackStore store = store(server);
        List<String> registered = new ArrayList<>(vanilla(server.registryAccess(), source));
        if (source == SuggestSource.SCHOOL || source == SuggestSource.SPECIAL || source == SuggestSource.DAMAGE_TYPE) {
            registered.addAll(fromPacks(store, null, source));
        }
        PackKind kind = source.kind();
        List<String> inPack = kind == null ? List.of() : store.ids(pack, kind);
        List<String> other = kind == null ? List.of() : store.idsExcept(pack, kind);
        return PackSuggestions.ids(registered, inPack, other);
    }

    public static List<String> fromPacks(PackStore store, String onlyPack, SuggestSource source) {
        if (store == null || source == null) return List.of();
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        for (String name : store.packs()) {
            if (onlyPack != null && !onlyPack.equals(name)) continue;
            for (PackDocument document : store.load(name).documents()) {
                collect(document.json(), source, ids);
            }
        }
        return List.copyOf(ids);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static JsonElement export(RegistryAccess access, PackKind kind, ResourceLocation id) {
        if (access == null || kind == null || id == null) return null;
        Optional registry = access.registry((ResourceKey) kind.registryKey());
        if (registry.isEmpty()) return null;
        Optional<Holder.Reference> holder = ((Registry) registry.get()).getHolder(id);
        if (holder.isEmpty()) return null;
        return (JsonElement) ((Codec) kind.codec()).encodeStart(JsonOps.INSTANCE, holder.get().value()).result().orElse(null);
    }

    private static List<String> specials(RegistryAccess access) {
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        SpecialEffects.knownIds().forEach(id -> ids.add(id.toString()));
        if (access == null) return List.copyOf(ids);
        access.registry(ExModifierDatapack.MODIFIER_ENTRIES).ifPresent(registry -> {
            for (ModifierEntryDefinition definition : registry) {
                definition.specialEffects().forEach(id -> ids.add(id.toString()));
            }
        });
        access.registry(ExModifierDatapack.SUITS).ifPresent(registry -> {
            for (SuitDefinition definition : registry) {
                for (var level : definition.levels()) {
                    level.specialEffects().forEach(id -> ids.add(id.toString()));
                }
            }
        });
        return List.copyOf(ids);
    }

    private static List<String> schools(RegistryAccess access) {
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        if (access == null) return List.of();
        access.registry(ExModifierDatapack.ELEMENTS).ifPresent(registry -> {
            for (ElementDefinition definition : registry) {
                definition.school().ifPresent(id -> ids.add(id.toString()));
            }
        });
        return List.copyOf(ids);
    }

    public static List<String> mentioned(JsonElement json, SuggestSource source) {
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        collect(json, source, ids);
        return List.copyOf(ids);
    }

    private static void collect(JsonElement json, SuggestSource source, LinkedHashSet<String> ids) {
        if (json == null || !json.isJsonObject()) return;
        JsonObject object = json.getAsJsonObject();
        switch (source) {
            case SCHOOL -> addPrimitive(object, "school", ids);
            case SPECIAL -> {
                addArray(object, "special_effects", ids);
                if (object.has("levels") && object.get("levels").isJsonArray()) {
                    object.getAsJsonArray("levels").forEach(level -> {
                        if (level.isJsonObject()) addArray(level.getAsJsonObject(), "special_effects", ids);
                    });
                }
            }
            case DAMAGE_TYPE -> addArray(object, "damage_types", ids);
            default -> {
            }
        }
    }

    private static void addPrimitive(JsonObject object, String key, LinkedHashSet<String> ids) {
        if (object.has(key) && object.get(key).isJsonPrimitive()) ids.add(object.get(key).getAsString());
    }

    private static void addArray(JsonObject object, String key, LinkedHashSet<String> ids) {
        if (!object.has(key) || !object.get(key).isJsonArray()) return;
        object.getAsJsonArray(key).forEach(element -> {
            if (element.isJsonPrimitive()) ids.add(element.getAsString());
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static List<String> keys(RegistryAccess access, ResourceKey key) {
        Optional registry = access.registry(key);
        if (registry.isEmpty()) return List.of();
        return names(((Registry) registry.get()).keySet());
    }

    private static List<String> names(Iterable<ResourceLocation> ids) {
        List<String> values = new ArrayList<>();
        for (ResourceLocation id : ids) values.add(id.toString());
        values.sort(String::compareTo);
        return values;
    }
}
