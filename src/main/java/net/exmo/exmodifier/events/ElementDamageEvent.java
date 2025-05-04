package net.exmo.exmodifier.events;

import net.exmo.exmodifier.content.element.ExElement;
import net.exmo.exmodifier.content.element.ExElementInstant;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

public class ElementDamageEvent extends LivingHurtEvent {
    public List<ExElementInstant> elements;
    public ElementDamageEvent(LivingEntity entity, DamageSource source, float amount, List<ExElementInstant> elements) {
        super(entity, source, amount);
        this.elements = elements;
    }
}
