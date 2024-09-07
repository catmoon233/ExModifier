package net.exmo.exmodifier.events;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;

public class ExApplySuitEffectEvent extends PlayerEvent {
    public MobEffectInstance mobEffectInstance;
    public ExApplySuitEffectEvent(Player player) {
        super(player);
    }

    public ExApplySuitEffectEvent(Player player, MobEffectInstance mobEffectInstance) {
        super(player);
        this.mobEffectInstance = mobEffectInstance;
    }
}
