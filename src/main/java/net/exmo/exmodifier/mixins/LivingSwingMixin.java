package net.exmo.exmodifier.mixins;

import net.exmo.exmodifier.events.LivingSwingEvent;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingSwingMixin {
    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;)V", at = @At("HEAD"))
    private void exmodifier$swing(CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new LivingSwingEvent((LivingEntity) (Object) this));
    }
}
