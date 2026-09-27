package net.exmo.exmodifier.events;

import net.exmo.exmodifier.api.AttributeContribution;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

public final class ExApplySuitAttrigetherEvent extends Event {
    private final LivingEntity entity;
    private final ResourceLocation suitId;
    private final AttributeContribution contribution;

    public ExApplySuitAttrigetherEvent(LivingEntity entity, ResourceLocation suitId, AttributeContribution contribution) {
        this.entity = entity;
        this.suitId = suitId;
        this.contribution = contribution;
    }

    public LivingEntity entity() { return entity; }
    public ResourceLocation suitId() { return suitId; }
    public AttributeContribution contribution() { return contribution; }
}
