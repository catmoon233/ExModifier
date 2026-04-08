package net.exmo.exmodifier.events;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;

public class LivingSwingEvent extends LivingEvent {
    public LivingSwingEvent(LivingEntity entity) {
        super(entity);
    }
    public float swingProgress(){
        if (getEntity() instanceof Player player){
            return player.getAttackStrengthScale(0);
        }
        return 1;
    }

}