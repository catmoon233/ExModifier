package net.exmo.exmodifier.events;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class ExRefreshEvent extends PlayerEvent {
    public int time ;
    public int rarity;
    public String washItem;
    public ExRefreshEvent(Player player, int time, int rarity, String washItem) {
        super(player);
        this.time = time;
        this.rarity = rarity;
        this.washItem = washItem;
        }
}
