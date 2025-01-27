package net.exmo.exmodifier.events;

import net.exmo.exmodifier.content.slot.ModifierSlotHandle;
import net.exmo.exmodifier.content.slot.UnLockSlotItem;
import net.minecraftforge.eventbus.api.Event;

public class ExRegisterUnLockSlotEvent extends Event {
    public ExRegisterUnLockSlotEvent registerUnLockSlot(UnLockSlotItem unLockSlotItem){
        ModifierSlotHandle.registerUnLockSlotItem(unLockSlotItem);
        return this;
    }
}
