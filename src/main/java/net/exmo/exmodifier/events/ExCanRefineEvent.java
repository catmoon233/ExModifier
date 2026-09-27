package net.exmo.exmodifier.events;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public final class ExCanRefineEvent extends Event {
    private final ItemStack gear;
    private final ItemStack material;
    private boolean canRefine;

    public ExCanRefineEvent(ItemStack gear, ItemStack material, boolean canRefine) {
        this.gear = gear;
        this.material = material;
        this.canRefine = canRefine;
    }

    public ItemStack gear() { return gear; }
    public ItemStack material() { return material; }
    public boolean canRefine() { return canRefine; }
    public void canRefine(boolean canRefine) { this.canRefine = canRefine; }
}
