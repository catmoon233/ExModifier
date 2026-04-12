package net.exmo.exmodifier.content.specialEffects;

import net.exmo.exmodifier.util.TickCooldown;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class CorpsePoisonEffect extends SpecialEffect {
    
    private static final int COOLDOWN = 20; // 1秒冷却
    
    public CorpsePoisonEffect() {
        super("corpse_poison", null);
    }
    
    @Override
    public void attackEntity(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player attacker)) return;
        
        LivingEntity target = event.getEntity();
        
        // 检查是否为非亡灵生物
        if (target instanceof Monster monster && monster.isUndead()) return;
        if (target.getType().toString().contains("zombie") || 
            target.getType().toString().contains("skeleton") ||
            target.getType().toString().contains("wither")) {
            return;
        }
        
        if (!TickCooldown.cooldownOk(attacker, "corpse_poison", COOLDOWN)) return;
        
        // 施加5秒中毒效果
        target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
    }
}
