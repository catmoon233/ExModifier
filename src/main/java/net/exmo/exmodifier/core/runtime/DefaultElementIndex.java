package net.exmo.exmodifier.core.runtime;

import net.exmo.exmodifier.api.ExModifierApi;
import net.exmo.exmodifier.core.attachment.EntityElements;
import net.exmo.exmodifier.core.component.ItemElements;
import net.exmo.exmodifier.core.data.DefaultEntityElementDefinition;
import net.exmo.exmodifier.core.data.DefaultItemElementDefinition;
import net.exmo.exmodifier.core.registry.ExModifierDatapack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/** Datapack defaults applied once when an item/entity has no elements yet. */
public final class DefaultElementIndex {
    private static volatile DefaultElementIndex active = new DefaultElementIndex(Map.of(), Map.of());

    private final Map<ResourceLocation, Map<ResourceLocation, Integer>> items;
    private final Map<ResourceLocation, Map<ResourceLocation, Float>> entities;

    public DefaultElementIndex(Map<ResourceLocation, Map<ResourceLocation, Integer>> items,
                               Map<ResourceLocation, Map<ResourceLocation, Float>> entities) {
        this.items = Map.copyOf(items);
        this.entities = Map.copyOf(entities);
    }

    public static void bind(RegistryAccess access) {
        HashMap<ResourceLocation, Map<ResourceLocation, Integer>> itemMap = new HashMap<>();
        HashMap<ResourceLocation, Map<ResourceLocation, Float>> entityMap = new HashMap<>();
        access.registry(ExModifierDatapack.DEFAULT_ITEM_ELEMENTS).ifPresent(registry ->
                registry.holders().forEach(holder -> {
                    DefaultItemElementDefinition definition = holder.value();
                    for (ResourceLocation item : definition.items()) itemMap.put(item, definition.elements());
                }));
        access.registry(ExModifierDatapack.DEFAULT_ENTITY_ELEMENTS).ifPresent(registry ->
                registry.holders().forEach(holder -> {
                    DefaultEntityElementDefinition definition = holder.value();
                    for (ResourceLocation entity : definition.entities()) entityMap.put(entity, definition.elements());
                }));
        active = new DefaultElementIndex(itemMap, entityMap);
    }

    public static void applyItem(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !ExModifierApi.elementsOn(stack).isEmpty()) return;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        Map<ResourceLocation, Integer> elements = active.items.get(id);
        if (elements == null || elements.isEmpty()) return;
        ItemElements next = ItemElements.EMPTY;
        for (var entry : elements.entrySet()) next = next.with(entry.getKey(), entry.getValue());
        ExModifierApi.setElements(stack, next);
    }

    public static void applyEntity(LivingEntity entity) {
        if (entity == null || !ExModifierApi.elementsOn(entity).isEmpty()) return;
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        Map<ResourceLocation, Float> elements = active.entities.get(id);
        if (elements == null || elements.isEmpty()) return;
        EntityElements next = EntityElements.EMPTY;
        for (var entry : elements.entrySet()) next = next.with(entry.getKey(), entry.getValue());
        ExModifierApi.setEntityElements(entity, next);
    }

    public static void applyQuality(ItemStack stack) {
        QualityRuntime.rollIfAbsent(stack);
    }
}
