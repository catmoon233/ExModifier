package net.exmo.exmodifier.core.registry;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.core.data.DefaultEntityElementDefinition;
import net.exmo.exmodifier.core.data.DefaultItemElementDefinition;
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
import net.exmo.exmodifier.core.data.AttriEffectDefinition;
import net.exmo.exmodifier.core.data.DefaultItemEntryDefinition;
import net.exmo.exmodifier.core.data.DynamicAttributeDefinition;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

public final class ExModifierDatapack {
    public static final ResourceKey<Registry<ModifierEntryDefinition>> MODIFIER_ENTRIES = key("modifier_entry");
    public static final ResourceKey<Registry<SlotDefinition>> SLOTS = key("slot");
    public static final ResourceKey<Registry<ElementDefinition>> ELEMENTS = key("element");
    public static final ResourceKey<Registry<QualityDefinition>> QUALITIES = key("quality");
    public static final ResourceKey<Registry<SuitDefinition>> SUITS = key("suit");
    public static final ResourceKey<Registry<RefineMaterialDefinition>> REFINE_MATERIALS = key("refine_material");
    public static final ResourceKey<Registry<RefineRecordDefinition>> REFINE_RECORDS = key("refine_record");
    public static final ResourceKey<Registry<ItemTypeDefinition>> ITEM_TYPES = key("item_type");
    public static final ResourceKey<Registry<WashingMaterialDefinition>> WASHING_MATERIALS = key("washing_material");
    public static final ResourceKey<Registry<DefaultItemElementDefinition>> DEFAULT_ITEM_ELEMENTS = key("default_item_element");
    public static final ResourceKey<Registry<DefaultEntityElementDefinition>> DEFAULT_ENTITY_ELEMENTS = key("default_entity_element");
    public static final ResourceKey<Registry<ItemLevelDefinition>> ITEM_LEVELS = key("item_level");
    public static final ResourceKey<Registry<AttriEffectDefinition>> ATTR_EFFECTS = key("attri_effect");
    public static final ResourceKey<Registry<DefaultItemEntryDefinition>> DEFAULT_ITEM_ENTRIES = key("default_item_entry");
    public static final ResourceKey<Registry<DynamicAttributeDefinition>> DYNAMIC_ATTRIBUTES = key("dynamic_attribute");

    private ExModifierDatapack() {}

    private static <T> ResourceKey<Registry<T>> key(String path) {
        return ResourceKey.createRegistryKey(Exmodifier.id(path));
    }

    @SubscribeEvent
    public static void register(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(MODIFIER_ENTRIES, ModifierEntryDefinition.CODEC, ModifierEntryDefinition.CODEC);
        event.dataPackRegistry(SLOTS, SlotDefinition.CODEC, SlotDefinition.CODEC);
        event.dataPackRegistry(ELEMENTS, ElementDefinition.CODEC, ElementDefinition.CODEC);
        event.dataPackRegistry(QUALITIES, QualityDefinition.CODEC, QualityDefinition.CODEC);
        event.dataPackRegistry(SUITS, SuitDefinition.CODEC, SuitDefinition.CODEC);
        event.dataPackRegistry(REFINE_MATERIALS, RefineMaterialDefinition.CODEC, RefineMaterialDefinition.CODEC);
        event.dataPackRegistry(REFINE_RECORDS, RefineRecordDefinition.CODEC, RefineRecordDefinition.CODEC);
        event.dataPackRegistry(ITEM_TYPES, ItemTypeDefinition.CODEC, ItemTypeDefinition.CODEC);
        event.dataPackRegistry(WASHING_MATERIALS, WashingMaterialDefinition.CODEC, WashingMaterialDefinition.CODEC);
        event.dataPackRegistry(DEFAULT_ITEM_ELEMENTS, DefaultItemElementDefinition.CODEC, DefaultItemElementDefinition.CODEC);
        event.dataPackRegistry(DEFAULT_ENTITY_ELEMENTS, DefaultEntityElementDefinition.CODEC, DefaultEntityElementDefinition.CODEC);
        event.dataPackRegistry(ITEM_LEVELS, ItemLevelDefinition.CODEC, ItemLevelDefinition.CODEC);
        event.dataPackRegistry(ATTR_EFFECTS, AttriEffectDefinition.CODEC, AttriEffectDefinition.CODEC);
        event.dataPackRegistry(DEFAULT_ITEM_ENTRIES, DefaultItemEntryDefinition.CODEC, DefaultItemEntryDefinition.CODEC);
        event.dataPackRegistry(DYNAMIC_ATTRIBUTES, DynamicAttributeDefinition.CODEC, DynamicAttributeDefinition.CODEC);
    }
}
