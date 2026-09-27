package net.exmo.exmodifier.events;

import net.exmo.exmodifier.core.data.AttributeSpec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;

public final class ExAddSuitAttrigetherEvent extends Event {
    private final ResourceLocation suitId;
    private AttributeSpec spec;

    public ExAddSuitAttrigetherEvent(ResourceLocation suitId, AttributeSpec spec) {
        this.suitId = suitId;
        this.spec = spec;
    }

    public ResourceLocation suitId() { return suitId; }
    public AttributeSpec spec() { return spec; }
    public void spec(AttributeSpec spec) { this.spec = spec; }
}
