package net.exmo.exmodifier.content.helper;

import net.exmo.exmodifier.api.ExModifierApi;
import net.exmo.exmodifier.core.component.UnlockedSlots;
import net.exmo.exmodifier.core.runtime.ModifierService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class ModifierSlotHelper {
    private final ItemStack stack;

    public ModifierSlotHelper(ItemStack stack) {
        this.stack = stack;
    }

    public static ModifierSlotHelper of(ItemStack stack) {
        return new ModifierSlotHelper(stack);
    }

    public UnlockedSlots slots() {
        return ExModifierApi.unlockedSlots(stack);
    }

    public boolean add(ResourceLocation slotId) {
        return ModifierService.addSlot(stack, slotId);
    }
}
