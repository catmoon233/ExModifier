package net.exmo.exmodifier.events;

import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.Event;

public class ExAfterArmorChange extends Event {
    public LivingEquipmentChangeEvent event;
    public boolean isSuitOperate = false;

    public ExAfterArmorChange(LivingEquipmentChangeEvent event) {
        this.event = event;
    }


    public ExAfterArmorChange(LivingEquipmentChangeEvent event, boolean isSuitOperate) {
        this.event = event;
        this.isSuitOperate = isSuitOperate;
    }
}
