package net.exmo.exmodifier.events;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

public final class LivingPlayerSwimEvent extends Event {
    private final Player player;

    public LivingPlayerSwimEvent(Player player) {
        this.player = player;
    }

    public Player player() { return player; }
}
