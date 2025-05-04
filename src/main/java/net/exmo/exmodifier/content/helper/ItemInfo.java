package net.exmo.exmodifier.content.helper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class ItemInfo {
    private ModifierEntryHelper modifierEntryHelper;
    private ItemLevelHelper itemLevelHelper;
    private ModifierSlotHelper modifierSlotHelper;
    private ItemQualityHelper itemQualityHelper;
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
    public ModifierSlotHelper getModifierSlotHelper() {
        if (modifierSlotHelper==null) modifierSlotHelper = new ModifierSlotHelper(itemStack);
        return modifierSlotHelper;
    }
    public ItemQualityHelper getItemQualityHelper() {
        if (itemQualityHelper==null) itemQualityHelper = new ItemQualityHelper(itemStack);
        return itemQualityHelper;
    }
    public ItemLevelHelper reloadItemLevelHelper(){
        itemLevelHelper = new ItemLevelHelper(itemStack);
        return itemLevelHelper;
    }
}