package net.exmo.exmodifier.events;

import net.exmo.exmodifier.content.level.ItemLevel;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

public class ExLevelRegistryEvent extends Event {
    public List<ItemLevel> itemLevel;
    public ExLevelRegistryEvent(List<ItemLevel> itemLevel) {
        this.itemLevel = itemLevel;
    }
}