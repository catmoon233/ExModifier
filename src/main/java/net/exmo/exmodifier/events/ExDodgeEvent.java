package net.exmo.exmodifier.events;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;

public class ExDodgeEvent extends LivingEvent {
    public static enum resultType{
        MISS,
        HIT
    }
    public double dodgeValue;
    public double hitRate;
    public void setResult(resultType result){
        this.result = result ;
    }
    public resultType result;
    public LivingAttackEvent event;
    public ExDodgeEvent(LivingEntity entity, LivingAttackEvent event, double dodgeValue, double hitRate, resultType result) {
        super(entity);
        this.dodgeValue = dodgeValue;
        this.hitRate = hitRate;
        this.event = event;
        this.result = result;
    }
}