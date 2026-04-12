package net.exmo.exmodifier.content.specialEffects;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.util.TickCooldown;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

public class ThornsEffect extends SpecialEffect {
    
    private static final int COOLDOWN = 20 * 10; // 10秒 = 200刻
    
    public ThornsEffect() {
        super("thorns", null);
    }
    
    @Override
    public void hurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player))return;
        // 检查冷却时间
        if (!TickCooldown.cooldownOk(player, "thorns", COOLDOWN)) {
            return;
        }
        
        // 获取周围3格内的所有生物
        AABB searchBox = entity.getBoundingBox().inflate(3.0);
        List<LivingEntity> nearbyEntities = entity.level().getEntitiesOfClass(
            LivingEntity.class, 
            searchBox, 
            e -> e != entity && e.isAlive()
        );
        
        if (nearbyEntities.isEmpty()) {
            return;
        }
        
        // 对周围所有生物造成1点伤害
        for (LivingEntity nearby : nearbyEntities) {
            nearby.hurt(player.damageSources().generic(), 1.0F);
        }
        
        // 生成粒子效果（爆炸粒子）
        if (entity.level() instanceof ServerLevel serverLevel) {

            
            // 生成闪电火花效果
            serverLevel.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                entity.getX(),
                entity.getY() + entity.getBbHeight() * 0.5,
                entity.getZ(),
                12,
                0.8,
                0.8,
                0.8,
                0.2
            );
        }
        

    }
}
