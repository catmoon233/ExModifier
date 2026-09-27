package net.exmo.exmodifier.events;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

public final class LivingSwingEvent extends Event {
    private final LivingEntity entity;

    public LivingSwingEvent(LivingEntity entity) {
        this.entity = entity;
    }

    public LivingEntity getEntity() {
        return entity;
    }
}
