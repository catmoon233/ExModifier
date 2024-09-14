package net.exmo.exmodifier.events;

import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

public class ExEntryRegistryEvent extends Event {
    public List<ModifierEntry> entries;
    public void register(ModifierEntry entry) {
        entries.add(entry);
    }
    public ExEntryRegistryEvent(List<ModifierEntry> entries) {
        this.entries = entries;
    }


}
