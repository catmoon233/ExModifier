package net.exmo.exmodifier.events;

import net.exmo.exmodifier.content.helper.ItemQualityHelper;
import net.exmo.exmodifier.content.refine.RefineHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public class ExRefineMaxCountEvent extends Event {
    public int maxCount;
    public ItemStack itemStack;
    public RefineHelper refineHelper;
    public ItemQualityHelper itemQualityHelper;

    public ExRefineMaxCountEvent(int maxCount, ItemStack itemStack, RefineHelper refineHelper, ItemQualityHelper itemQualityHelper) {
        this.maxCount = maxCount;
        this.itemStack = itemStack;
        this.refineHelper = refineHelper;
        this.itemQualityHelper = itemQualityHelper;
    }
    public int getMaxCount() {
        return maxCount;
    }
}
