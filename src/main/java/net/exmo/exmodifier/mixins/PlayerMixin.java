package net.exmo.exmodifier.mixins;

import net.exmo.exmodifier.events.LivingPlayerSwimEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin {
    @Inject(method = "updateSwimming", at = @At("HEAD"))
    public void updateSwimming(CallbackInfo ci){
        MinecraftForge.EVENT_BUS.post(new LivingPlayerSwimEvent(TickEvent.Phase.START, (Player)(Object)this));
    }
}

