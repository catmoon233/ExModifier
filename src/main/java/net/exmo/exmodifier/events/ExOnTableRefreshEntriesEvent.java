package net.exmo.exmodifier.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

import java.util.ArrayList;
import java.util.List;

public final class ExOnTableRefreshEntriesEvent extends Event {
    private final ItemStack gear;
    private final List<ResourceLocation> pool;

    public ExOnTableRefreshEntriesEvent(ItemStack gear, List<ResourceLocation> pool) {
        this.gear = gear;
        this.pool = new ArrayList<>(pool == null ? List.of() : pool);
    }

    public ItemStack gear() { return gear; }
    public List<ResourceLocation> pool() { return pool; }
}
