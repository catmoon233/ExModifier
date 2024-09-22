package net.exmo.exmodifier.content.quality;

import net.minecraft.world.item.ItemStack;

import javax.print.attribute.Attribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemQuality {
    public int rarity; //稀有度
    public String id ;
    public String LocalDescription; //描述
    public List<String> items = new ArrayList<>();
    public Map<Attribute,Double> attributeLowerLimit = new HashMap<>();
    //你要怎么搞,新建文件还是修改原文件我去吃饭了
    //xing
    public ItemQuality(int rarity, String id) {
        this.rarity = rarity;
        this.id = id;
    }
    //你去写个handle吧

}