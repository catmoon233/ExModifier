package net.exmo.exmodifier.content.helper;

import net.exmo.exmodifier.api.ExModifierApi;
import net.exmo.exmodifier.core.component.ItemElements;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public final class ExElementHelper {
    private final ItemStack stack;

    public ExElementHelper(ItemStack stack) {
        this.stack = stack;
    }

    public static ExElementHelper of(ItemStack stack) {
        return new ExElementHelper(stack);
    }

    public Map<ResourceLocation, Integer> amounts() {
        return ExModifierApi.elementsOn(stack);
    }

    public void set(ResourceLocation id, int amount) {
        ExModifierApi.setElements(stack, new ItemElements(ExModifierApi.elementsOn(stack)).with(id, amount));
    }
}
