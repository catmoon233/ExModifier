package net.exmo.exmodifier.content.quality;

import net.minecraft.world.item.ItemStack;

import javax.print.attribute.Attribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemQuality {
    public int rarity;
    public String LocalDescription;
    public List<String> items = new ArrayList<>();
    public Map<Attribute,Double> attributeLowerLimit = new HashMap<>();
    //你要怎么搞,新建文件还是修改原文件我去吃饭了
    //xing
    public ItemQuality(int rarity, String LocalDescription, List<String> items) {
        this.rarity = rarity;
        this.LocalDescription = LocalDescription;
        this.items = items;
    }
    //你去写个handle吧

}