package net.exmo.exmodifier.events;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

import java.util.List;

public final class ExCuriosAttributeTooltipEvent extends Event {
    private final ItemStack stack;
    private final List<Component> lines;

    public ExCuriosAttributeTooltipEvent(ItemStack stack, List<Component> lines) {
        this.stack = stack;
        this.lines = lines;
    }

    public ItemStack stack() { return stack; }
    public List<Component> lines() { return lines; }
}
