package net.exmo.exmodifier.events;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;

public final class ExModifierPercentAttr extends Event {
    private final ResourceLocation attribute;
    private boolean percent;

    public ExModifierPercentAttr(ResourceLocation attribute, boolean percent) {
        this.attribute = attribute;
        this.percent = percent;
    }

    public ResourceLocation attribute() { return attribute; }
    public boolean percent() { return percent; }
    public void percent(boolean percent) { this.percent = percent; }
}
