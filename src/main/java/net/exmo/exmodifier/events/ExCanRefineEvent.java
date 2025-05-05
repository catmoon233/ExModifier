package net.exmo.exmodifier.events;

import net.exmo.exmodifier.content.refine.RefineHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public class ExCanRefineEvent extends Event {
    public ItemStack originalItemStack;
    public ItemStack targetItemStack;
    public boolean canRefine;
    public RefineHelper refineHelper;
    public boolean isCanRefine;
    public ExCanRefineEvent(ItemStack originalItemStack, ItemStack targetItemStack, boolean canRefine, RefineHelper refineHelper) {
        this.originalItemStack = originalItemStack;
        this.targetItemStack = targetItemStack;
        this.canRefine = canRefine;
        this.refineHelper = refineHelper;
    }
}
