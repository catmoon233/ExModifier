package net.exmo.exmodifier.events;

import net.exmo.exmodifier.core.data.AttributeSpec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;

import java.util.ArrayList;
import java.util.List;

public final class ExAddSuitAttrigethersEvent extends Event {
    private final ResourceLocation suitId;
    private final List<AttributeSpec> specs;

    public ExAddSuitAttrigethersEvent(ResourceLocation suitId, List<AttributeSpec> specs) {
        this.suitId = suitId;
        this.specs = new ArrayList<>(specs == null ? List.of() : specs);
    }

    public ResourceLocation suitId() { return suitId; }
    public List<AttributeSpec> specs() { return specs; }
}
