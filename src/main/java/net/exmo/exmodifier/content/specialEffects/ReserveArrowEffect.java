package net.exmo.exmodifier.content.specialEffects;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;

public class ReserveArrowEffect extends SpecialEffect {
    
    public ReserveArrowEffect() {
        super("reserve_arrow", null);
    }
    
    @Override
    public void onUseItem(LivingEntityUseItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        ItemStack item = event.getItem();
        
        // 检查是否为主手弓且没有无限附魔
        if (!item.is(player.getMainHandItem().getItem())) return;
        if (item.getEnchantmentLevel(Enchantments.INFINITY_ARROWS) > 0) return;
        
        // 消耗2次耐久度替代箭矢的逻辑在射箭事件中处理
        // 这里仅作标记
    }
}
