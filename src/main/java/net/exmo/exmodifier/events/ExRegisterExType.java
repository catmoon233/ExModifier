package net.exmo.exmodifier.events;

import net.exmo.exmodifier.content.type.ExTypeHandle;
import net.exmo.exmodifier.content.type.ItemType;
import net.minecraftforge.eventbus.api.Event;

public class ExRegisterExType extends Event {

    public ExRegisterExType() {
    }
    public void register(ItemType type){
        ExTypeHandle.registerItemType( type);
    }
}
