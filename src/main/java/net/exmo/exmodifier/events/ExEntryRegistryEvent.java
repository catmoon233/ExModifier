package net.exmo.exmodifier.events;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;

public final class ExEntryRegistryEvent extends Event {
    private final ResourceLocation entryId;

    public ExEntryRegistryEvent(ResourceLocation entryId) {
        this.entryId = entryId;
    }

    public ResourceLocation entryId() { return entryId; }
}
