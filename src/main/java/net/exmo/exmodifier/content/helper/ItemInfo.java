package net.exmo.exmodifier.content.helper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class ItemInfo {
    private ModifierEntryHelper modifierEntryHelper;
    private   ItemLevelHelper itemLevelHelper;
    public ItemStack itemStack;
    public ItemInfo(ItemStack itemStack){
        this.itemStack = itemStack;
    }
    public static ItemInfo of(ItemStack itemStack){
        return new ItemInfo(itemStack);
    }

    public ModifierEntryHelper getModifierEntryHelper() {
        if (modifierEntryHelper==null) modifierEntryHelper = new ModifierEntryHelper(itemStack);
        return modifierEntryHelper;
    }
    public ModifierEntryHelper reloadModifierEntryHelper(){
        modifierEntryHelper = new ModifierEntryHelper(itemStack);
        return modifierEntryHelper;
    }
    public ItemLevelHelper getItemLevelHelper() {
        if (itemLevelHelper==null) itemLevelHelper = new ItemLevelHelper(itemStack);
        return itemLevelHelper;
    }
    public ItemLevelHelper reloadItemLevelHelper(){
        itemLevelHelper = new ItemLevelHelper(itemStack);
        return itemLevelHelper;
    }
}