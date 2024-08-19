package net.exmo.exmodifier.content.suit;

import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.content.modifier.ModifierEntry;

import java.util.List;
import java.util.Map;

public class ExSuit {
    public String id;
    public ModifierEntry entry;
    Map<Integer,List< ModifierAttriGether>> attriGether;
    public ExSuit(String id, ModifierEntry entry, Map<Integer,List< ModifierAttriGether>> attriGether) {
        this.id = id;
        this.entry = entry;
        this.attriGether = attriGether;
    }
}
