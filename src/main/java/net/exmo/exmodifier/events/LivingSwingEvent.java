package net.exmo.exmodifier.events;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

public class LivingSwingEvent extends LivingEvent {
    public LivingSwingEvent(LivingEntity entity) {
        super(entity);
    }
}