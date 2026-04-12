package net.exmo.exmodifier.content.specialEffects;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.util.TickCooldown;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BloodthirstyEffect extends SpecialEffect {
    
    private static final int COOLDOWN = 20*2;
    
    public BloodthirstyEffect() {
        super("bloodthirsty", null);
    }
    
    @Override
    public void attackEntity(LivingHurtEvent event) {
        // 获取攻击者
        if (event.getSource().getEntity() instanceof Player attacker) {

            
            if (!TickCooldown.cooldownOk(attacker, "bloodthirsty", COOLDOWN))return;
            
            // 恢复1点生命值
            attacker.heal(1.0F);
            
            // 生成粒子效果（红色心形粒子）
            if (attacker.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                    ParticleTypes.HEART,
                    attacker.getX(),
                    attacker.getY() + attacker.getBbHeight() * 0.5,
                    attacker.getZ(),
                    2,
                    0.3,
                    0.3,
                    0.3,
                    0.1
                );
            }
            
        }
    }
    

}
