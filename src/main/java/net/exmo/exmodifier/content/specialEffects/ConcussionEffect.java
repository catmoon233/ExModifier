package net.exmo.exmodifier.content.specialEffects;

import net.exmo.exmodifier.util.TickCooldown;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class ConcussionEffect extends SpecialEffect {
    
    private static final int COOLDOWN = 20 * 20; // 20秒冷却
    
    public ConcussionEffect() {
        super("concussion", null);
    }
    
    @Override
    public void hurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        if (!TickCooldown.cooldownOk(player, "concussion", COOLDOWN)) return;
        
        // 每秒恢复1点生命值的效果由其他机制处理
        // 这里只负责设置冷却
    }
}
