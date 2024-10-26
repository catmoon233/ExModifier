package net.exmo.exmodifier.mixins;

import net.exmo.exmodifier.init.ExAttribute;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class DigSpeedMixin {
    @Inject(method = "getDigSpeed" ,at = @At("RETURN"), cancellable = true,remap = false)
    public void getDigSpeed(BlockState p_36282_, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        Player player = (Player) (Object) this;
        cir.setReturnValue((float) ((float) (double) cir.getReturnValueF() * player.getAttributeValue(ExAttribute.DIG_SPEED.get())));
    }
}