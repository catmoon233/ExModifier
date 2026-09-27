package net.exmo.exmodifier.events;

import net.exmo.exmodifier.api.AttributeContribution;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public final class ExApplyEntryAttrigetherEvent extends Event {
    private final ItemStack stack;
    private final AttributeContribution contribution;

    public ExApplyEntryAttrigetherEvent(ItemStack stack, AttributeContribution contribution) {
        this.stack = stack;
        this.contribution = contribution;
    }

    public ItemStack stack() { return stack; }
    public AttributeContribution contribution() { return contribution; }
}
