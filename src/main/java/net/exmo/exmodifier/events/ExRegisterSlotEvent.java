package net.exmo.exmodifier.events;

import net.exmo.exmodifier.content.slot.ModifierSlot;
import net.exmo.exmodifier.content.slot.ModifierSlotHandle;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;
import java.util.Map;

public class ExRegisterSlotEvent extends Event {
    public ExRegisterSlotEvent registerSlot(ResourceLocation resourceLocation,ModifierSlot slot){
        ModifierSlotHandle.registerSlots.put(resourceLocation,slot);
        return this;
    }
    public ExRegisterSlotEvent registerSlot(String s,ModifierSlot slot){
        ModifierSlotHandle.registerSlots.put(ResourceLocation.tryParse(s),slot);
        return this;
    }

    public ExRegisterSlotEvent registerSlot1(Map<ResourceLocation,ModifierSlot> slot){
        slot.forEach(ModifierSlotHandle::registerSlot);
        return this;
    }
    public ExRegisterSlotEvent registerSlot2(Map<String,ModifierSlot> slot){
        slot.forEach((k,v)->{
            ModifierSlotHandle.registerSlot(new ResourceLocation(k),v);
        });
        return this;
    }
}
