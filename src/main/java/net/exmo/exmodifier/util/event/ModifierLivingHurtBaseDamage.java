package net.exmo.exmodifier.util.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

public class ModifierLivingHurtBaseDamage extends LivingEvent {
    public float damage;

    public ModifierLivingHurtBaseDamage(LivingEntity entity, float damage) {
        super(entity);
        this.damage = damage;
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }
}
