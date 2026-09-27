package net.exmo.exmodifier.core.runtime;

import net.exmo.exmodifier.api.ExModifierApi;
import net.exmo.exmodifier.core.data.DefaultItemEntryDefinition;
import net.exmo.exmodifier.core.registry.ExModifierDatapack;
import net.exmo.exmodifier.events.ExItemDefaultEntry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DefaultEntryIndex {
    private static volatile Map<ResourceLocation, List<ResourceLocation>> ITEMS = Map.of();
    private static volatile Map<ResourceLocation, Integer> LEVELS = Map.of();

    private DefaultEntryIndex() {}

    public static void bind(RegistryAccess access) {
        HashMap<ResourceLocation, List<ResourceLocation>> items = new HashMap<>();
        HashMap<ResourceLocation, Integer> levels = new HashMap<>();
        access.registry(ExModifierDatapack.DEFAULT_ITEM_ENTRIES).ifPresent(registry ->
                registry.holders().forEach(holder -> {
                    DefaultItemEntryDefinition definition = holder.value();
                    for (ResourceLocation item : definition.items()) {
                        items.put(item, definition.entries());
                        levels.put(item, definition.level());
                    }
                }));
        ITEMS = Map.copyOf(items);
        LEVELS = Map.copyOf(levels);
    }

    public static void applyItem(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !ExModifierApi.appliedModifiers(stack).isEmpty()) return;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        List<ResourceLocation> entries = ITEMS.get(id);
        if (entries == null || entries.isEmpty()) return;
        int level = LEVELS.getOrDefault(id, 1);
        for (ResourceLocation entry : entries) {
            ExItemDefaultEntry event = new ExItemDefaultEntry(stack, entry);
            NeoForge.EVENT_BUS.post(event);
            if (event.entryId() != null) {
                ModifierService.addEntry(stack, event.entryId(), level, java.util.Optional.empty());
            }
        }
    }
}
