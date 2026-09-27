package net.exmo.exmodifier.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public final class ExItemDefaultEntry extends Event {
    private final ItemStack stack;
    private ResourceLocation entryId;

    public ExItemDefaultEntry(ItemStack stack, ResourceLocation entryId) {
        this.stack = stack;
        this.entryId = entryId;
    }

    public ItemStack stack() { return stack; }
    public ResourceLocation entryId() { return entryId; }
    public void entryId(ResourceLocation entryId) { this.entryId = entryId; }
}
