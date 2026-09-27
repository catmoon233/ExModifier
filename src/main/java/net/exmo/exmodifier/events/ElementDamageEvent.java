package net.exmo.exmodifier.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

import java.util.Map;

public final class ElementDamageEvent extends Event {
    private final LivingEntity victim;
    private final DamageSource source;
    private final float amount;
    private final Map<ResourceLocation, Integer> elements;

    public ElementDamageEvent(LivingEntity victim, DamageSource source, float amount, Map<ResourceLocation, Integer> elements) {
        this.victim = victim;
        this.source = source;
        this.amount = amount;
        this.elements = elements;
    }

    public LivingEntity victim() { return victim; }
    public DamageSource source() { return source; }
    public float amount() { return amount; }
    public Map<ResourceLocation, Integer> elements() { return elements; }
}
