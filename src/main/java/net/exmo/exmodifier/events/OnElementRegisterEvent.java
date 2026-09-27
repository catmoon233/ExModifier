package net.exmo.exmodifier.events;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;

public final class OnElementRegisterEvent extends Event {
    private final ResourceLocation elementId;

    public OnElementRegisterEvent(ResourceLocation elementId) {
        this.elementId = elementId;
    }

    public ResourceLocation elementId() { return elementId; }
}
