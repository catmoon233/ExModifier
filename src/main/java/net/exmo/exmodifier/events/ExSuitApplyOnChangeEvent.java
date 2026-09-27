package net.exmo.exmodifier.events;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

public final class ExSuitApplyOnChangeEvent extends Event {
    private final LivingEntity entity;

    public ExSuitApplyOnChangeEvent(LivingEntity entity) {
        this.entity = entity;
    }

    public LivingEntity entity() { return entity; }
}
