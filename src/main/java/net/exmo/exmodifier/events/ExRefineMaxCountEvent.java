package net.exmo.exmodifier.events;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public final class ExRefineMaxCountEvent extends Event {
    private final ItemStack stack;
    private int maxCount;

    public ExRefineMaxCountEvent(ItemStack stack, int maxCount) {
        this.stack = stack;
        this.maxCount = maxCount;
    }

    public ItemStack stack() { return stack; }
    public int maxCount() { return maxCount; }
    public void maxCount(int maxCount) { this.maxCount = maxCount; }
}
