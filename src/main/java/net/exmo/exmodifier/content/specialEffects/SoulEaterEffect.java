package net.exmo.exmodifier.content.specialEffects;

import net.exmo.exmodifier.util.TickCooldown;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class SoulEaterEffect extends SpecialEffect {
    
    public SoulEaterEffect() {
        super("soul_eater", null);
    }
    
    @Override
    public void attackEntity(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player attacker)) return;
        
        LivingEntity target = event.getEntity();
        
        // 检查目标最大生命值是否低于攻击者的5%
        double attackerMaxHealth = attacker.getMaxHealth();
        double targetMaxHealth = target.getMaxHealth();
        
        if (targetMaxHealth <= attackerMaxHealth * 0.05) {
            // 直接击杀
            target.setHealth(0);
            target.die(attacker.damageSources().playerAttack(attacker));
        }
    }
}
