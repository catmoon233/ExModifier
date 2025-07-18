package net.exmo.exmodifier.events;


import mod.arcomit.emberthral.client.filter.Filter;
import net.exmo.exmodifier.Exmodifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;


public class ExCustomTabEvent extends Event implements IModBusEvent
{
    public ExCustomTabEvent() {
    }

    public void addTab(String tabName, ItemStack itemStack ){
      //  Exmodifier.itemGroups.add(new Filter(tabName,itemStack));
    }
}
