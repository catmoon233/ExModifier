package net.exmo.exmodifier.events;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public final class ExRefreshEvent extends Event {
    private final Player player;
    private final ItemStack gear;
    private final ItemStack material;
    private int addEntries;
    private int rarity;

    public ExRefreshEvent(Player player, ItemStack gear, ItemStack material, int addEntries, int rarity) {
        this.player = player;
        this.gear = gear;
        this.material = material;
        this.addEntries = addEntries;
        this.rarity = rarity;
    }

    public Player player() { return player; }
    public ItemStack gear() { return gear; }
    public ItemStack material() { return material; }
    public int addEntries() { return addEntries; }
    public void addEntries(int addEntries) { this.addEntries = addEntries; }
    public int rarity() { return rarity; }
    public void rarity(int rarity) { this.rarity = rarity; }
}
