package net.exmo.exmodifier.content.specialEffects;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class BanditEffect extends SpecialEffect {
    
    public BanditEffect() {
        super("bandit", null);
    }
    
    @Override
    public void onKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player killer)) return;
        
        // 检查是否为村民
        if (!(event.getEntity() instanceof Villager)) return;
        
        Level level = killer.level();
        if (level instanceof ServerLevel serverLevel) {
            // 掉落1颗绿宝石
            ItemEntity emerald = new ItemEntity(
                serverLevel,
                event.getEntity().getX(),
                event.getEntity().getY(),
                event.getEntity().getZ(),
                Items.EMERALD.getDefaultInstance()
            );
            serverLevel.addFreshEntity(emerald);
        }
    }
}
