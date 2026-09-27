package net.exmo.exmodifier.events;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;

public final class ExLevelRegistryEvent extends Event {
    private final ResourceLocation tableId;

    public ExLevelRegistryEvent(ResourceLocation tableId) {
        this.tableId = tableId;
    }

    public ResourceLocation tableId() { return tableId; }
}
