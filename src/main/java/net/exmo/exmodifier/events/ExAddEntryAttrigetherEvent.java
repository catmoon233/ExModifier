package net.exmo.exmodifier.events;

import net.exmo.exmodifier.core.data.AttributeSpec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;

public final class ExAddEntryAttrigetherEvent extends Event {
    private final ResourceLocation entryId;
    private AttributeSpec spec;

    public ExAddEntryAttrigetherEvent(ResourceLocation entryId, AttributeSpec spec) {
        this.entryId = entryId;
        this.spec = spec;
    }

    public ResourceLocation entryId() { return entryId; }
    public AttributeSpec spec() { return spec; }
    public void spec(AttributeSpec spec) { this.spec = spec; }
}
