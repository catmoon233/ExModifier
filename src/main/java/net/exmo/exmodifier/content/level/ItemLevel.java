package net.exmo.exmodifier.content.level;

import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;
import java.util.List;

public class ItemLevel
{
    public String id ;
    public ModifierEntry.Type type;
    public List<String> OnlyItemIds = new ArrayList<>();
    public List<String> OnlyItemTags = new ArrayList<>();
    public int MaxLevel;
    public int DefaultLevel;
    public List<LevelAttriGether> attriGethers = new ArrayList<>();
    public String UpEvent ;
    public String XpAddValue ;


}