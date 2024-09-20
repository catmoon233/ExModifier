package net.exmo.exmodifier.mixins;

import net.exmo.exmodifier.events.LivingSwingEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingSwingAction {

    @Inject(method = "swing*", at = @At("TAIL"))
    public void swing(InteractionHand hand, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new LivingSwingEvent((LivingEntity)(Object)this));
    }}