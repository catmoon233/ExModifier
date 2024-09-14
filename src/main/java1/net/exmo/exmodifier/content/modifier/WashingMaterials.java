package net.exmo.exmodifier.content.modifier;

import net.minecraft.world.item.Item;

public class WashingMaterials {
    public String ItemId;
    public Item item;
    public int rarity;
    public int additionEntry;

    public WashingMaterials(String itemId, int additionEntry, Item itemId1, int rarity) {
        this.ItemId = itemId;
        this.additionEntry = additionEntry;
        this.rarity = rarity;
        this.item = itemId1;
    }
}
