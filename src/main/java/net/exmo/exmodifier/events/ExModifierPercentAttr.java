package net.exmo.exmodifier.events;

import net.minecraftforge.eventbus.api.Event;

import java.util.List;

public class ExModifierPercentAttr extends Event {
    public List<String> attrs;
    public ExModifierPercentAttr (List<String> attrs)
    {
        this.attrs = attrs;
    }
}
