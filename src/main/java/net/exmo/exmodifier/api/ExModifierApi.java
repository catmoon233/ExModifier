package net.exmo.exmodifier.api;

import net.exmo.exmodifier.core.attachment.EntityElements;
import net.exmo.exmodifier.core.attachment.ExModifierAttachments;
import net.exmo.exmodifier.core.component.AppliedModifier;
import net.exmo.exmodifier.core.component.AppliedModifiers;
import net.exmo.exmodifier.core.component.EntryBook;
import net.exmo.exmodifier.core.component.ExModifierComponents;
import net.exmo.exmodifier.core.component.ItemElements;
import net.exmo.exmodifier.core.component.ItemLevelState;
import net.exmo.exmodifier.core.component.ItemQualityRef;
import net.exmo.exmodifier.core.component.RefineState;
import net.exmo.exmodifier.core.component.UnlockedSlots;
import net.exmo.exmodifier.core.data.AttributeSpec;
import net.exmo.exmodifier.core.data.ModifierEntryDefinition;
import net.exmo.exmodifier.core.registry.ExModifierCatalog;
import net.exmo.exmodifier.compat.curios.CuriosEquipment;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stable public query surface. Does not depend on GUI or mixin internals.
 * Item queries read data components; entity element queries read attachments.
 */
public final class ExModifierApi {
    private ExModifierApi() {}

    public static AppliedModifiers appliedModifiers(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return AppliedModifiers.EMPTY;
        DataComponentType<AppliedModifiers> type = componentOrNull(ExModifierComponents.MODIFIERS);
        if (type == null) return AppliedModifiers.EMPTY;
        return stack.getOrDefault(type, AppliedModifiers.EMPTY);
    }

    public static List<AppliedModifierView> modifiersOn(AppliedModifiers applied) {
        if (applied == null || applied.isEmpty()) return List.of();
        List<AppliedModifierView> views = new ArrayList<>(applied.values().size());
        for (AppliedModifier modifier : applied.values()) {
            views.add(new AppliedModifierView(modifier.entryId(), modifier.level(), modifier.slotId(), modifier.locked()));
        }
        return List.copyOf(views);
    }

    public static List<AppliedModifierView> modifiersOn(ItemStack stack) {
        return modifiersOn(appliedModifiers(stack));
    }

    public static List<AppliedModifierView> modifiersOnEquipped(LivingEntity entity) {
        if (entity == null) return List.of();
        List<AppliedModifierView> views = new ArrayList<>();
        for (ItemStack stack : equipped(entity)) views.addAll(modifiersOn(stack));
        return List.copyOf(views);
    }

    public static List<AttributeContribution> attributesOn(AppliedModifiers applied) {
        if (applied == null || applied.isEmpty()) return List.of();
        ExModifierCatalog catalog = ExModifierCatalog.current();
        List<AttributeContribution> contributions = new ArrayList<>();
        for (AppliedModifier modifier : applied.values()) {
            Optional<ModifierEntryDefinition> definition = catalog.entry(modifier.entryId());
            if (definition.isEmpty()) continue;
            for (AttributeSpec spec : definition.get().attributes()) {
                contributions.add(new AttributeContribution(
                        spec.attribute(), spec.amountAt(modifier.level()), spec.operation(), modifier.entryId()));
            }
        }
        return List.copyOf(contributions);
    }

    public static List<AttributeContribution> attributesOn(ItemStack stack) {
        return attributesOn(appliedModifiers(stack));
    }

    public static List<AttributeContribution> attributesOnEquipped(LivingEntity entity) {
        if (entity == null) return List.of();
        List<AttributeContribution> contributions = new ArrayList<>();
        for (ItemStack stack : equipped(entity)) contributions.addAll(attributesOn(stack));
        return List.copyOf(contributions);
    }

    public static Map<ResourceLocation, Integer> elementsOn(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Map.of();
        DataComponentType<ItemElements> type = componentOrNull(ExModifierComponents.ELEMENTS);
        if (type == null) return Map.of();
        return stack.getOrDefault(type, ItemElements.EMPTY).amounts();
    }

    public static Map<ResourceLocation, Float> elementsOn(LivingEntity entity) {
        if (entity == null) return Map.of();
        return entity.getData(ExModifierAttachments.ENTITY_ELEMENTS).amounts();
    }

    public static Optional<ResourceLocation> qualityOn(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Optional.empty();
        DataComponentType<ItemQualityRef> type = componentOrNull(ExModifierComponents.QUALITY);
        if (type == null) return Optional.empty();
        ItemQualityRef ref = stack.get(type);
        return ref == null ? Optional.empty() : Optional.of(ref.qualityId());
    }

    public static UnlockedSlots unlockedSlots(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return UnlockedSlots.EMPTY;
        DataComponentType<UnlockedSlots> type = componentOrNull(ExModifierComponents.SLOTS);
        if (type == null) return UnlockedSlots.EMPTY;
        return stack.getOrDefault(type, UnlockedSlots.EMPTY);
    }

    public static RefineState refineState(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return RefineState.EMPTY;
        DataComponentType<RefineState> type = componentOrNull(ExModifierComponents.REFINE);
        if (type == null) return RefineState.EMPTY;
        return stack.getOrDefault(type, RefineState.EMPTY);
    }

    public static Optional<EntryBook> entryBook(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Optional.empty();
        DataComponentType<EntryBook> type = componentOrNull(ExModifierComponents.ENTRY_BOOK);
        if (type == null) return Optional.empty();
        return Optional.ofNullable(stack.get(type));
    }

    public static Optional<ItemLevelState> itemLevel(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Optional.empty();
        DataComponentType<ItemLevelState> type = componentOrNull(ExModifierComponents.ITEM_LEVEL);
        if (type == null) return Optional.empty();
        return Optional.ofNullable(stack.get(type));
    }

    public static void setModifiers(ItemStack stack, AppliedModifiers modifiers) {
        if (stack == null || stack.isEmpty()) return;
        stack.set(ExModifierComponents.modifiers(), modifiers == null ? AppliedModifiers.EMPTY : modifiers);
    }

    public static void setElements(ItemStack stack, ItemElements elements) {
        if (stack == null || stack.isEmpty()) return;
        stack.set(ExModifierComponents.ELEMENTS.get(), elements == null ? ItemElements.EMPTY : elements);
    }

    public static void setQuality(ItemStack stack, ResourceLocation qualityId) {
        if (stack == null || stack.isEmpty() || qualityId == null) return;
        stack.set(ExModifierComponents.QUALITY.get(), new ItemQualityRef(qualityId));
    }

    public static void setSlots(ItemStack stack, UnlockedSlots slots) {
        if (stack == null || stack.isEmpty()) return;
        stack.set(ExModifierComponents.SLOTS.get(), slots == null ? UnlockedSlots.EMPTY : slots);
    }

    public static void setItemLevel(ItemStack stack, ItemLevelState state) {
        if (stack == null || stack.isEmpty()) return;
        stack.set(ExModifierComponents.ITEM_LEVEL.get(), state);
    }

    public static void setEntityElements(LivingEntity entity, EntityElements elements) {
        if (entity == null) return;
        entity.setData(ExModifierAttachments.ENTITY_ELEMENTS, elements == null ? EntityElements.EMPTY : elements);
    }

    public static List<ItemStack> equipped(LivingEntity entity) {
        if (entity == null) return List.of();
        List<ItemStack> stacks = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (!stack.isEmpty()) stacks.add(stack);
        }
        stacks.addAll(CuriosEquipment.equipped(entity));
        return stacks;
    }

    public static Map<ResourceLocation, Integer> countSuitPieces(LivingEntity entity) {
        LinkedHashMap<ResourceLocation, Integer> counts = new LinkedHashMap<>();
        for (AppliedModifierView view : modifiersOnEquipped(entity)) {
            ExModifierCatalog.current().entry(view.entryId()).ifPresent(definition -> {
                for (ResourceLocation suit : definition.suits()) {
                    counts.merge(suit, 1, Integer::sum);
                }
            });
        }
        return counts;
    }

    @SuppressWarnings("unchecked")
    private static <T> DataComponentType<T> componentOrNull(net.neoforged.neoforge.registries.DeferredHolder<DataComponentType<?>, DataComponentType<T>> holder) {
        try {
            return holder.get();
        } catch (RuntimeException ignored) {
            return null;
        }
    }
}
