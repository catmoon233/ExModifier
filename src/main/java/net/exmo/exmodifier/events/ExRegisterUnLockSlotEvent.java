package net.exmo.exmodifier.events;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;

public final class ExRegisterUnLockSlotEvent extends Event {
    private final ResourceLocation slotId;

    public ExRegisterUnLockSlotEvent(ResourceLocation slotId) {
        this.slotId = slotId;
    }

    public ResourceLocation slotId() { return slotId; }
}
