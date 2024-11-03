package net.exmo.exmodifier.content.quality;

import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.minecraft.world.item.ItemStack;

import javax.print.attribute.Attribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemQuality {
    public int rarity; //稀有度
    public List<ModifierEntry> entries = new ArrayList<>();
    public boolean cantRemoveEntry = false;
    public String id ;
    public String LocalDescription; //描述
    public List<String> items = new ArrayList<>();
    public Map<Attribute,Double> attributeLowerLimit = new HashMap<>();
    public boolean autoRefresh = false;
    public int refineNum = 0;
    public boolean isRandom = true;
    public List<String> materials = new ArrayList<>();
    public ItemQuality(int rarity, String id) {
        this.rarity = rarity;
        this.id = id;
    }

}