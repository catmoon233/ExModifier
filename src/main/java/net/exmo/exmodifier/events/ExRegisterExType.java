package net.exmo.exmodifier.events;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;

public final class ExRegisterExType extends Event {
    private final ResourceLocation typeId;

    public ExRegisterExType(ResourceLocation typeId) {
        this.typeId = typeId;
    }

    public ResourceLocation typeId() { return typeId; }
}
