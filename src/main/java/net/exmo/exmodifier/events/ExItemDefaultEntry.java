package net.exmo.exmodifier.events;

import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExItemDefaultEntry extends Event {


    public void addDefaultEntry(String itemId,List<ModifierEntry> modifierEntries){
        ModifierHandle.itemsDefaultEntry.put(itemId,modifierEntries);
    }
}