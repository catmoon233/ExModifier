package net.exmo.exmodifier.content.helper;

import net.exmo.exmodifier.api.ExModifierApi;
import net.exmo.exmodifier.core.runtime.QualityRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class ItemQualityHelper {
    private final ItemStack stack;

    public ItemQualityHelper(ItemStack stack) {
        this.stack = stack;
    }

    public static ItemQualityHelper of(ItemStack stack) {
        return new ItemQualityHelper(stack);
    }

    public Optional<ResourceLocation> quality() {
        return ExModifierApi.qualityOn(stack);
    }

    public void set(ResourceLocation qualityId) {
        QualityRuntime.apply(stack, qualityId);
    }

    public void rollIfAbsent() {
        QualityRuntime.rollIfAbsent(stack);
    }
}
