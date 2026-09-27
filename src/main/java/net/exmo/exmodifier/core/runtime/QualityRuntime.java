package net.exmo.exmodifier.core.runtime;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.api.ExModifierApi;
import net.exmo.exmodifier.core.data.QualityDefinition;
import net.exmo.exmodifier.core.registry.ExModifierCatalog;
import net.exmo.exmodifier.util.WeightedRandom;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class QualityRuntime {
    private QualityRuntime() {}

    public static void rollIfAbsent(ItemStack stack) {
        if (stack == null || stack.isEmpty() || ExModifierApi.qualityOn(stack).isPresent()) return;
        Optional<ResourceLocation> picked = pick(stack, stack.getItem().getDefaultInstance().getCount() >= 0 ? stack.getCount() : 1);
        ResourceLocation id = picked.orElse(Exmodifier.id("common"));
        apply(stack, id);
    }

    public static void apply(ItemStack stack, ResourceLocation qualityId) {
        if (stack == null || stack.isEmpty() || qualityId == null) return;
        ExModifierApi.setQuality(stack, qualityId);
        ExModifierCatalog.current().quality(qualityId).ifPresent(definition -> {
            for (ResourceLocation entry : definition.boundEntries()) {
                ModifierService.addEntry(stack, entry, 1, Optional.empty());
            }
        });
    }

    public static Optional<ResourceLocation> pick(ItemStack stack, int unused) {
        ExModifierCatalog catalog = ExModifierCatalog.current();
        Map<ResourceLocation, Float> weights = new HashMap<>();
        catalog.qualities().forEach((id, definition) -> {
            if (definition.weight() <= 0.0F) return;
            if (!matches(stack, definition)) return;
            weights.put(id, definition.weight());
        });
        RandomSource random = RandomSource.create(BuiltInRegistries.ITEM.getKey(stack.getItem()).hashCode() * 31L + ItemStack.hashItemAndComponents(stack));
        return WeightedRandom.pick(random, weights);
    }

    public static boolean matches(ItemStack stack, QualityDefinition definition) {
        if (!ItemTypeMatcher.matchesAnyType(stack, definition.itemTypes()) && !definition.itemTypes().isEmpty()) return false;
        return ItemTypeMatcher.matches(stack, definition.onlyItems(), definition.onlyTags());
    }
}
