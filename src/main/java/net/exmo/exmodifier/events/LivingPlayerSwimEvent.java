package net.exmo.exmodifier.events;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;

public class LivingPlayerSwimEvent extends TickEvent.PlayerTickEvent {
    public LivingPlayerSwimEvent(Phase phase, Player player) {
        super(phase, player);
    }
}