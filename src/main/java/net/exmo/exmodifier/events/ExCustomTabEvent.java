package net.exmo.exmodifier.events;

import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.Event;

public final class ExCustomTabEvent extends Event {
    private final CreativeModeTab.Output output;

    public ExCustomTabEvent(CreativeModeTab.Output output) {
        this.output = output;
    }

    public CreativeModeTab.Output output() { return output; }
}
