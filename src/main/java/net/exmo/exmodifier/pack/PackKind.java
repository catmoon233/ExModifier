package net.exmo.exmodifier.pack;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.exmo.exmodifier.core.data.AttriEffectDefinition;
import net.exmo.exmodifier.core.data.DefaultEntityElementDefinition;
import net.exmo.exmodifier.core.data.DefaultItemElementDefinition;
import net.exmo.exmodifier.core.data.DefaultItemEntryDefinition;
import net.exmo.exmodifier.core.data.DynamicAttributeDefinition;
import net.exmo.exmodifier.core.data.ElementDefinition;
import net.exmo.exmodifier.core.data.ItemLevelDefinition;
import net.exmo.exmodifier.core.data.ItemTypeDefinition;
import net.exmo.exmodifier.core.data.ModifierEntryDefinition;
import net.exmo.exmodifier.core.data.QualityDefinition;
import net.exmo.exmodifier.core.data.RefineMaterialDefinition;
import net.exmo.exmodifier.core.data.RefineRecordDefinition;
import net.exmo.exmodifier.core.data.SlotDefinition;
import net.exmo.exmodifier.core.data.SuitDefinition;
import net.exmo.exmodifier.core.data.WashingMaterialDefinition;
import net.exmo.exmodifier.core.registry.ExModifierDatapack;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public enum PackKind {
    MODIFIER_ENTRY("modifier_entry", ExModifierDatapack.MODIFIER_ENTRIES, ModifierEntryDefinition.CODEC, SuggestSource.ENTRY),
    SLOT("slot", ExModifierDatapack.SLOTS, SlotDefinition.CODEC, SuggestSource.SLOT),
    ELEMENT("element", ExModifierDatapack.ELEMENTS, ElementDefinition.CODEC, SuggestSource.ELEMENT),
    QUALITY("quality", ExModifierDatapack.QUALITIES, QualityDefinition.CODEC, SuggestSource.QUALITY),
    SUIT("suit", ExModifierDatapack.SUITS, SuitDefinition.CODEC, SuggestSource.SUIT),
    REFINE_MATERIAL("refine_material", ExModifierDatapack.REFINE_MATERIALS, RefineMaterialDefinition.CODEC, SuggestSource.ITEM),
    REFINE_RECORD("refine_record", ExModifierDatapack.REFINE_RECORDS, RefineRecordDefinition.CODEC, SuggestSource.ITEM),
    ITEM_TYPE("item_type", ExModifierDatapack.ITEM_TYPES, ItemTypeDefinition.CODEC, SuggestSource.ITEM_TYPE),
    WASHING_MATERIAL("washing_material", ExModifierDatapack.WASHING_MATERIALS, WashingMaterialDefinition.CODEC, SuggestSource.ITEM),
    DEFAULT_ITEM_ELEMENT("default_item_element", ExModifierDatapack.DEFAULT_ITEM_ELEMENTS, DefaultItemElementDefinition.CODEC, SuggestSource.ITEM),
    DEFAULT_ENTITY_ELEMENT("default_entity_element", ExModifierDatapack.DEFAULT_ENTITY_ELEMENTS, DefaultEntityElementDefinition.CODEC, SuggestSource.ENTITY),
    ITEM_LEVEL("item_level", ExModifierDatapack.ITEM_LEVELS, ItemLevelDefinition.CODEC, SuggestSource.NONE),
    ATTRI_EFFECT("attri_effect", ExModifierDatapack.ATTR_EFFECTS, AttriEffectDefinition.CODEC, SuggestSource.ATTRI_EFFECT),
    DEFAULT_ITEM_ENTRY("default_item_entry", ExModifierDatapack.DEFAULT_ITEM_ENTRIES, DefaultItemEntryDefinition.CODEC, SuggestSource.ITEM),
    DYNAMIC_ATTRIBUTE("dynamic_attribute", ExModifierDatapack.DYNAMIC_ATTRIBUTES, DynamicAttributeDefinition.CODEC, SuggestSource.DYNAMIC_ATTRIBUTE);

    private final String folder;
    private final ResourceKey<? extends Registry<?>> registryKey;
    private final Codec<?> codec;
    private final SuggestSource idSource;

    PackKind(String folder, ResourceKey<? extends Registry<?>> registryKey, Codec<?> codec, SuggestSource idSource) {
        this.folder = folder;
        this.registryKey = registryKey;
        this.codec = codec;
        this.idSource = idSource;
    }

    public String folder() {
        return folder;
    }

    public String labelKey() {
        return "kind.exmodifier." + folder;
    }

    public ResourceKey<? extends Registry<?>> registryKey() {
        return registryKey;
    }

    public Codec<?> codec() {
        return codec;
    }

    public SuggestSource idSource() {
        return idSource;
    }

    public static PackKind byFolder(String raw) {
        if (raw == null) return null;
        String folder = raw.trim().toLowerCase(Locale.ROOT);
        for (PackKind kind : values()) {
            if (kind.folder.equals(folder) || kind.name().equalsIgnoreCase(folder)) return kind;
        }
        return null;
    }

    public List<PackField> fields() {
        return switch (this) {
            case MODIFIER_ENTRY -> List.of(
                    field("item_types", PackField.FieldType.ID_LIST, SuggestSource.ITEM_TYPE),
                    field("slots", PackField.FieldType.ID_LIST, SuggestSource.SLOT),
                    field("weight", PackField.FieldType.FLOAT, SuggestSource.NONE),
                    field("max_level", PackField.FieldType.INT, SuggestSource.NONE),
                    field("random", PackField.FieldType.BOOL, SuggestSource.BOOL),
                    field("cant_select", PackField.FieldType.BOOL, SuggestSource.BOOL),
                    field("attributes", PackField.FieldType.ATTRIBUTE_LIST, SuggestSource.ATTRIBUTE),
                    field("special_effects", PackField.FieldType.ID_LIST, SuggestSource.SPECIAL),
                    field("suits", PackField.FieldType.ID_LIST, SuggestSource.SUIT),
                    field("description_key", PackField.FieldType.STRING, SuggestSource.NONE));
            case SLOT -> List.of(
                    field("unlock_item", PackField.FieldType.ID, SuggestSource.ITEM),
                    field("xp_cost", PackField.FieldType.INT, SuggestSource.NONE),
                    field("consume_count", PackField.FieldType.INT, SuggestSource.NONE));
            case ELEMENT -> List.of(
                    field("damage_bonus", PackField.FieldType.FLOAT, SuggestSource.NONE),
                    field("damage_resist", PackField.FieldType.FLOAT, SuggestSource.NONE),
                    field("color", PackField.FieldType.COLOR, SuggestSource.NONE),
                    field("restrain", PackField.FieldType.FLOAT_MAP, SuggestSource.ELEMENT),
                    field("damage_types", PackField.FieldType.ID_LIST, SuggestSource.DAMAGE_TYPE),
                    field("school", PackField.FieldType.ID, SuggestSource.SCHOOL));
            case QUALITY -> List.of(
                    field("max_refine", PackField.FieldType.INT, SuggestSource.NONE),
                    field("tooltip_key", PackField.FieldType.STRING, SuggestSource.NONE),
                    field("weight", PackField.FieldType.FLOAT, SuggestSource.NONE),
                    field("rarity", PackField.FieldType.INT, SuggestSource.NONE),
                    field("grow_value", PackField.FieldType.FLOAT, SuggestSource.NONE),
                    field("auto_refresh", PackField.FieldType.BOOL, SuggestSource.BOOL),
                    field("cant_remove_entry", PackField.FieldType.BOOL, SuggestSource.BOOL),
                    field("show_in_head_tooltip", PackField.FieldType.BOOL, SuggestSource.BOOL),
                    field("refine_need_same_star", PackField.FieldType.BOOL, SuggestSource.BOOL),
                    field("bound_entries", PackField.FieldType.ID_LIST, SuggestSource.ENTRY),
                    field("only_items", PackField.FieldType.ID_LIST, SuggestSource.ITEM),
                    field("only_tags", PackField.FieldType.STRING_LIST, SuggestSource.ITEM_TAG),
                    field("item_types", PackField.FieldType.ID_LIST, SuggestSource.ITEM_TYPE));
            case SUIT -> List.of(
                    field("entries", PackField.FieldType.ID_LIST, SuggestSource.ENTRY),
                    field("levels", PackField.FieldType.SUIT_LEVEL_LIST, SuggestSource.NONE));
            case REFINE_MATERIAL -> List.of(
                    field("bonus", PackField.FieldType.DOUBLE, SuggestSource.NONE),
                    field("consume_count", PackField.FieldType.INT, SuggestSource.NONE));
            case REFINE_RECORD -> List.of(
                    field("quality", PackField.FieldType.ID, SuggestSource.QUALITY),
                    field("max_count", PackField.FieldType.INT, SuggestSource.NONE));
            case ITEM_TYPE -> List.of(
                    field("items", PackField.FieldType.STRING_LIST, SuggestSource.ITEM),
                    field("tags", PackField.FieldType.STRING_LIST, SuggestSource.ITEM_TAG));
            case WASHING_MATERIAL -> List.of(
                    field("consume_count", PackField.FieldType.INT, SuggestSource.NONE),
                    field("xp_cost", PackField.FieldType.INT, SuggestSource.NONE),
                    field("keep_entries", PackField.FieldType.INT, SuggestSource.NONE),
                    field("add_entries", PackField.FieldType.INT, SuggestSource.NONE),
                    field("min_weight", PackField.FieldType.FLOAT, SuggestSource.NONE));
            case DEFAULT_ITEM_ELEMENT -> List.of(
                    field("items", PackField.FieldType.ID_LIST, SuggestSource.ITEM),
                    field("elements", PackField.FieldType.INT_MAP, SuggestSource.ELEMENT));
            case DEFAULT_ENTITY_ELEMENT -> List.of(
                    field("entities", PackField.FieldType.ID_LIST, SuggestSource.ENTITY),
                    field("elements", PackField.FieldType.FLOAT_MAP, SuggestSource.ELEMENT));
            case ITEM_LEVEL -> List.of(
                    field("max_level", PackField.FieldType.INT, SuggestSource.NONE),
                    field("default_level", PackField.FieldType.INT, SuggestSource.NONE),
                    field("xp_per_level", PackField.FieldType.INT, SuggestSource.NONE),
                    field("attributes", PackField.FieldType.ATTRIBUTE_LIST, SuggestSource.ATTRIBUTE),
                    field("expression", PackField.FieldType.STRING, SuggestSource.NONE),
                    field("xp_add_expression", PackField.FieldType.STRING, SuggestSource.NONE),
                    field("up_event", PackField.FieldType.STRING, SuggestSource.UP_EVENT),
                    field("weight", PackField.FieldType.FLOAT, SuggestSource.NONE),
                    field("item_types", PackField.FieldType.ID_LIST, SuggestSource.ITEM_TYPE),
                    field("only_items", PackField.FieldType.ID_LIST, SuggestSource.ITEM),
                    field("only_tags", PackField.FieldType.STRING_LIST, SuggestSource.ITEM_TAG));
            case ATTRI_EFFECT -> List.of(
                    field("attributes", PackField.FieldType.ATTRIBUTE_LIST, SuggestSource.ATTRIBUTE),
                    field("particle", PackField.FieldType.ID, SuggestSource.PARTICLE),
                    field("description_key", PackField.FieldType.STRING, SuggestSource.NONE),
                    field("visible", PackField.FieldType.BOOL, SuggestSource.BOOL));
            case DEFAULT_ITEM_ENTRY -> List.of(
                    field("items", PackField.FieldType.ID_LIST, SuggestSource.ITEM),
                    field("entries", PackField.FieldType.ID_LIST, SuggestSource.ENTRY),
                    field("level", PackField.FieldType.INT, SuggestSource.NONE));
            case DYNAMIC_ATTRIBUTE -> List.of(
                    field("min", PackField.FieldType.DOUBLE, SuggestSource.NONE),
                    field("max", PackField.FieldType.DOUBLE, SuggestSource.NONE),
                    field("default", PackField.FieldType.DOUBLE, SuggestSource.NONE));
        };
    }

    public static PackField resolve(PackKind kind, String path) {
        if (kind == null || path == null || path.isBlank()) return null;
        String normalized = path.trim().replaceAll("\\.\\d+(?=\\.|$)", "");
        if (normalized.startsWith("levels.")) {
            String sub = normalized.substring("levels.".length());
            return switch (sub) {
                case "pieces" -> field("pieces", PackField.FieldType.INT, SuggestSource.NONE);
                case "trigger" -> field("trigger", PackField.FieldType.ID, SuggestSource.TRIGGER);
                case "attributes" -> field("attributes", PackField.FieldType.ATTRIBUTE_LIST, SuggestSource.ATTRIBUTE);
                case "effects" -> field("effects", PackField.FieldType.EFFECT_LIST, SuggestSource.EFFECT);
                case "commands" -> field("commands", PackField.FieldType.STRING_LIST, SuggestSource.NONE);
                case "special_effects" -> field("special_effects", PackField.FieldType.ID_LIST, SuggestSource.SPECIAL);
                default -> null;
            };
        }
        for (PackField field : kind.fields()) {
            if (field.key().equals(path.trim()) || field.key().equals(normalized)) return field;
        }
        return null;
    }

    public JsonElement emptyJson() {
        return JsonParser.parseString("{}");
    }

    public Optional<String> invalid(JsonElement json) {
        if (json == null || !json.isJsonObject()) return Optional.of("JSON 必须是对象");
        DataResult<?> result = codec.parse(JsonOps.INSTANCE, json);
        return result.error().map(error -> {
            String message = error.message();
            return message.length() > 240 ? message.substring(0, 240) : message;
        });
    }

    private static PackField field(String key, PackField.FieldType type, SuggestSource source) {
        return new PackField(key, type, source);
    }
}
