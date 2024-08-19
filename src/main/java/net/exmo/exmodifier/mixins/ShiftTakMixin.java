package net.exmo.exmodifier.mixins;

import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemCombinerMenu.class)
public abstract class ShiftTakMixin {

    @Inject(at = @org.spongepowered.asm.mixin.injection.At("RETURN"), method = "quickMoveStack", cancellable = true)
    public void onTake(Player p_39792_, int p_39793_, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack p_150646_ = cir.getReturnValue();
        if (p_150646_.getOrCreateTag().getBoolean("modifier_refresh")){
            p_150646_.getOrCreateTag().putString("exmodifier_armor_modifier_applied0","");
            p_150646_.getOrCreateTag().putString("exmodifier_armor_modifier_applied1","");
            p_150646_.getOrCreateTag().putString("exmodifier_armor_modifier_applied2","");
            p_150646_.getOrCreateTag().putBoolean("modifier_refresh", false);
            p_150646_.getOrCreateTag().putInt("exmodifier_armor_modifier_applied", 0);

            ModifierHandle.CommonEvent.RandomEntry(p_150646_, p_150646_.getOrCreateTag().getInt("modifier_refresh_rarity"), p_150646_.getOrCreateTag().getInt("modifier_refresh_add"));
            cir.setReturnValue(p_150646_);
        }
    }
}
