package net.exmo.exmodifier.events;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

public final class ModifierLivingHurtBaseDamage extends Event {
    private final LivingEntity entity;
    private float damage;

    public ModifierLivingHurtBaseDamage(LivingEntity entity, float damage) {
        this.entity = entity;
        this.damage = damage;
    }

    public LivingEntity getEntity() { return entity; }
    public float getDamage() { return damage; }
    public void setDamage(float damage) { this.damage = damage; }
}
