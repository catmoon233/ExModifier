package net.exmo.exmodifier.content.modifier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WashingMaterials {
    public String ItemId;
    public Item item;
    public int rarity;
    public List<ModifierEntry.Type> OnlyTypes = new ArrayList<>();

    public  boolean containTag(ItemStack stack){
        for (String tag : OnlyTags ){
            if (stack.is(ItemTags.create(new ResourceLocation(tag))))return true;
        }
        return false;
    }
    public boolean OnlyHasWashEntry = false;
    public int MinRandomTime;
    public int MaxRandomTime;
    public int additionEntry;
    public List<String> OnlyTags;
    public List<String> OnlyItems;

    public WashingMaterials(String itemId, int additionEntry, Item itemId1, int rarity) {
        this.ItemId = itemId;
        this.additionEntry = additionEntry;
        this.rarity = rarity;
        this.item = itemId1;
    }

    public WashingMaterials(String itemId, Item item, int rarity, int minRandomTime, int maxRandomTime) {
        ItemId = itemId;
        this.item = item;
        this.rarity = rarity;
        MinRandomTime = minRandomTime;
        MaxRandomTime = maxRandomTime;
    }
}
