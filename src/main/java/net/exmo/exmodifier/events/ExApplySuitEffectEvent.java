package net.exmo.exmodifier.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

public final class ExApplySuitEffectEvent extends Event {
    private final LivingEntity entity;
    private final ResourceLocation suitId;
    private final String trigger;
    private boolean canceled;

    public ExApplySuitEffectEvent(LivingEntity entity, ResourceLocation suitId, String trigger) {
        this.entity = entity;
        this.suitId = suitId;
        this.trigger = trigger;
    }

    public LivingEntity entity() { return entity; }
    public ResourceLocation suitId() { return suitId; }
    public String trigger() { return trigger; }
    public boolean canceled() { return canceled; }
    public void cancel() { this.canceled = true; }
}
