package net.exmo.exmodifier.content.helper;

import net.minecraft.world.item.ItemStack;

/** Facade over the component helpers for a single stack. */
public final class ItemInfo {
    private final ItemStack stack;

    public ItemInfo(ItemStack stack) {
        this.stack = stack;
    }

    public static ItemInfo of(ItemStack stack) {
        return new ItemInfo(stack);
    }

    public ItemStack stack() { return stack; }
    public ModifierEntryHelper entries() { return ModifierEntryHelper.of(stack); }
    public ItemQualityHelper quality() { return ItemQualityHelper.of(stack); }
    public ItemLevelHelper level() { return ItemLevelHelper.of(stack); }
    public ExElementHelper elements() { return ExElementHelper.of(stack); }
    public ModifierSlotHelper slots() { return ModifierSlotHelper.of(stack); }
}
