package net.exmo.exmodifier.content.specialEffects;

import net.exmo.exmodifier.util.TickCooldown;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class HeavyStrikeEffect extends SpecialEffect {
    
    private static final int COOLDOWN = 20 * 5; // 5秒冷却
    private static final float EXTRA_DAMAGE = 3.0F;
    
    public HeavyStrikeEffect() {
        super("heavy_strike", null);
    }
    
    @Override
    public void attackEntity(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player attacker)) return;
        
        if (!TickCooldown.cooldownOk(attacker, "heavy_strike", COOLDOWN)) return;
        
        // 额外造成3点伤害
        event.setAmount(event.getAmount() + EXTRA_DAMAGE);
    }
}
