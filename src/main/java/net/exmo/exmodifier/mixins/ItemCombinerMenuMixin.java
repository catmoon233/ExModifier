package net.exmo.exmodifier.mixins;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ItemCombinerMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemCombinerMenu.class)
public abstract class ItemCombinerMenuMixin {

    @Shadow @Final protected Container inputSlots;

    @Shadow @Final protected Player player;

    @Inject(at = @At("HEAD"), method = "removed")
    public void removed(Player p_39790_, CallbackInfo ci) {
        p_39790_.getPersistentData().putBoolean("modifier_refresh_not_enough", false );
    }
    @Inject(at = @At("HEAD"), method = "slotsChanged")
    public void slotsChanged(Container p_39778_, CallbackInfo ci) {
      //  if (this.inputSlots.getItem(0).isEmpty() || this.inputSlots.getItem(1).isEmpty() )this.player.getPersistentData().putBoolean("modifier_refresh_not_enough", false);

    }
}