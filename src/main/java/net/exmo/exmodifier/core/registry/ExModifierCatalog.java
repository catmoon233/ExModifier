package net.exmo.exmodifier.core.registry;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.compat.irons.IronSchoolElements;
import net.exmo.exmodifier.core.runtime.CustomEffectsRuntime;
import net.exmo.exmodifier.core.runtime.DefaultElementIndex;
import net.exmo.exmodifier.core.runtime.DefaultEntryIndex;
import net.exmo.exmodifier.events.ExEntryRegistryEvent;
import net.exmo.exmodifier.events.ExLevelRegistryEvent;
import net.exmo.exmodifier.events.ExRegisterExType;
import net.exmo.exmodifier.events.ExRegisterSlotEvent;
import net.exmo.exmodifier.events.OnElementRegisterEvent;
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
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Immutable snapshot of datapack definitions used by the public API and runtime. */
public final class ExModifierCatalog {
    public static final ExModifierCatalog EMPTY = new ExModifierCatalog(
            Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of());

    private static volatile ExModifierCatalog active = EMPTY;

    private final Map<ResourceLocation, ModifierEntryDefinition> entries;
    private final Map<ResourceLocation, SlotDefinition> slots;
    private final Map<ResourceLocation, ElementDefinition> elements;
    private final Map<ResourceLocation, QualityDefinition> qualities;
    private final Map<ResourceLocation, SuitDefinition> suits;
    private final Map<ResourceLocation, RefineMaterialDefinition> refineMaterials;
    private final Map<ResourceLocation, RefineRecordDefinition> refineRecords;
    private final Map<ResourceLocation, ItemTypeDefinition> itemTypes;
    private final Map<ResourceLocation, WashingMaterialDefinition> washingMaterials;
    private final Map<ResourceLocation, ItemLevelDefinition> itemLevels;
    private final Map<ResourceLocation, ResourceLocation> damageTypeToElement;

    public ExModifierCatalog(
            Map<ResourceLocation, ModifierEntryDefinition> entries,
            Map<ResourceLocation, SlotDefinition> slots,
            Map<ResourceLocation, ElementDefinition> elements,
            Map<ResourceLocation, QualityDefinition> qualities,
            Map<ResourceLocation, SuitDefinition> suits,
            Map<ResourceLocation, RefineMaterialDefinition> refineMaterials,
            Map<ResourceLocation, RefineRecordDefinition> refineRecords,
            Map<ResourceLocation, ItemTypeDefinition> itemTypes,
            Map<ResourceLocation, WashingMaterialDefinition> washingMaterials,
            Map<ResourceLocation, ItemLevelDefinition> itemLevels
    ) {
        this.entries = Map.copyOf(entries);
        this.slots = Map.copyOf(slots);
        this.elements = Map.copyOf(elements);
        this.qualities = Map.copyOf(qualities);
        this.suits = Map.copyOf(suits);
        this.refineMaterials = Map.copyOf(refineMaterials);
        this.refineRecords = Map.copyOf(refineRecords);
        this.itemTypes = Map.copyOf(itemTypes);
        this.washingMaterials = Map.copyOf(washingMaterials);
        this.itemLevels = Map.copyOf(itemLevels);
        HashMap<ResourceLocation, ResourceLocation> index = new HashMap<>();
        this.elements.forEach((id, definition) -> {
            for (ResourceLocation damageType : definition.damageTypes()) index.putIfAbsent(damageType, id);
            definition.school().ifPresent(school -> index.putIfAbsent(school, id));
        });
        this.damageTypeToElement = Map.copyOf(index);
    }

    public ExModifierCatalog(
            Map<ResourceLocation, ModifierEntryDefinition> entries,
            Map<ResourceLocation, SlotDefinition> slots,
            Map<ResourceLocation, ElementDefinition> elements,
            Map<ResourceLocation, QualityDefinition> qualities,
            Map<ResourceLocation, SuitDefinition> suits,
            Map<ResourceLocation, RefineMaterialDefinition> refineMaterials,
            Map<ResourceLocation, RefineRecordDefinition> refineRecords,
            Map<ResourceLocation, ItemTypeDefinition> itemTypes,
            Map<ResourceLocation, WashingMaterialDefinition> washingMaterials
    ) {
        this(entries, slots, elements, qualities, suits, refineMaterials, refineRecords, itemTypes, washingMaterials, Map.of());
    }

    public static ExModifierCatalog current() {
        return active;
    }

    public static void bind(ExModifierCatalog catalog) {
        active = catalog == null ? EMPTY : catalog;
    }

    public static void bind(RegistryAccess access) {
        bind(from(access).overlayElements(IronSchoolElements.discover()));
        DefaultElementIndex.bind(access);
        DefaultEntryIndex.bind(access);
        CustomEffectsRuntime.bind(access);
        fireRegistryEvents();
    }

    private static void fireRegistryEvents() {
        ExModifierCatalog catalog = current();
        catalog.entries().keySet().forEach(id -> NeoForge.EVENT_BUS.post(new ExEntryRegistryEvent(id)));
        catalog.slots().keySet().forEach(id -> NeoForge.EVENT_BUS.post(new ExRegisterSlotEvent(id)));
        catalog.elements().keySet().forEach(id -> NeoForge.EVENT_BUS.post(new OnElementRegisterEvent(id)));
        catalog.itemTypes().keySet().forEach(id -> NeoForge.EVENT_BUS.post(new ExRegisterExType(id)));
        catalog.itemLevels().keySet().forEach(id -> NeoForge.EVENT_BUS.post(new ExLevelRegistryEvent(id)));
    }

    public static ExModifierCatalog from(RegistryAccess access) {
        return new ExModifierCatalog(
                copy(access, ExModifierDatapack.MODIFIER_ENTRIES),
                copy(access, ExModifierDatapack.SLOTS),
                copy(access, ExModifierDatapack.ELEMENTS),
                copy(access, ExModifierDatapack.QUALITIES),
                copy(access, ExModifierDatapack.SUITS),
                copy(access, ExModifierDatapack.REFINE_MATERIALS),
                copy(access, ExModifierDatapack.REFINE_RECORDS),
                copy(access, ExModifierDatapack.ITEM_TYPES),
                copy(access, ExModifierDatapack.WASHING_MATERIALS),
                copy(access, ExModifierDatapack.ITEM_LEVELS)
        );
    }

    public ExModifierCatalog overlayElements(Map<ResourceLocation, ElementDefinition> extra) {
        if (extra == null || extra.isEmpty()) return this;
        HashMap<ResourceLocation, ElementDefinition> merged = new HashMap<>(elements);
        extra.forEach(merged::putIfAbsent);
        if (merged.size() == elements.size()) return this;
        return new ExModifierCatalog(entries, slots, merged, qualities, suits, refineMaterials, refineRecords, itemTypes, washingMaterials, itemLevels);
    }

    private static <T> Map<ResourceLocation, T> copy(RegistryAccess access, net.minecraft.resources.ResourceKey<Registry<T>> key) {
        Optional<Registry<T>> registry = access.registry(key);
        if (registry.isEmpty()) return Map.of();
        HashMap<ResourceLocation, T> map = new HashMap<>();
        registry.get().holders().forEach(holder -> map.put(holder.key().location(), holder.value()));
        return map;
    }

    public Optional<ModifierEntryDefinition> entry(ResourceLocation id) {
        return Optional.ofNullable(entries.get(id));
    }

    public Map<ResourceLocation, ModifierEntryDefinition> entries() {
        return Collections.unmodifiableMap(entries);
    }

    public Optional<SlotDefinition> slot(ResourceLocation id) {
        return Optional.ofNullable(slots.get(id));
    }

    public Map<ResourceLocation, SlotDefinition> slots() {
        return Collections.unmodifiableMap(slots);
    }

    public Optional<ElementDefinition> element(ResourceLocation id) {
        return Optional.ofNullable(elements.get(id));
    }

    public Map<ResourceLocation, ElementDefinition> elements() {
        return Collections.unmodifiableMap(elements);
    }

    public ResourceLocation elementForDamage(DamageSource source) {
        if (source == null) return null;
        ResourceLocation type = source.typeHolder().unwrapKey().map(key -> key.location()).orElse(null);
        if (type == null) return null;
        ResourceLocation mapped = damageTypeToElement.get(type);
        if (mapped != null) return mapped;
        return elements.containsKey(type) ? type : null;
    }

    public Optional<QualityDefinition> quality(ResourceLocation id) {
        return Optional.ofNullable(qualities.get(id));
    }

    public Map<ResourceLocation, QualityDefinition> qualities() {
        return Collections.unmodifiableMap(qualities);
    }

    public Optional<SuitDefinition> suit(ResourceLocation id) {
        return Optional.ofNullable(suits.get(id));
    }

    public Map<ResourceLocation, SuitDefinition> suits() {
        return Collections.unmodifiableMap(suits);
    }

    public Optional<WashingMaterialDefinition> washing(ResourceLocation id) {
        return Optional.ofNullable(washingMaterials.get(id));
    }

    public Map<ResourceLocation, WashingMaterialDefinition> washingMaterials() {
        return Collections.unmodifiableMap(washingMaterials);
    }

    public Optional<RefineMaterialDefinition> refineMaterial(ResourceLocation id) {
        return Optional.ofNullable(refineMaterials.get(id));
    }

    public Optional<RefineRecordDefinition> refineRecord(ResourceLocation id) {
        return Optional.ofNullable(refineRecords.get(id));
    }

    public Map<ResourceLocation, ItemTypeDefinition> itemTypes() {
        return Collections.unmodifiableMap(itemTypes);
    }

    public Optional<ItemLevelDefinition> itemLevel(ResourceLocation id) {
        return Optional.ofNullable(itemLevels.get(id));
    }

    public Map<ResourceLocation, ItemLevelDefinition> itemLevels() {
        return Collections.unmodifiableMap(itemLevels);
    }
}
