package net.exmo.exmodifier.events;

import net.exmo.exmodifier.content.modifier.WashingMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
@Cancelable
public class ExOnTableRefreshEntriesEvent extends Event {
    public WashingMaterials washingMaterials;
    public ItemStack stack;
    public ItemStack WashStack;
    public ItemStack output;


    public ExOnTableRefreshEntriesEvent(WashingMaterials washingMaterials, ItemStack stack, ItemStack washStack, ItemStack output) {
        this.washingMaterials = washingMaterials;
        this.stack = stack;
        WashStack = washStack;
        this.output = output;
    }
}
