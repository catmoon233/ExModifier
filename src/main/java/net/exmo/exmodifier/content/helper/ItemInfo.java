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

    public ModifierEntryHelper getModifierEntryHelper() {
        if (modifierEntryHelper==null) modifierEntryHelper = new ModifierEntryHelper(itemStack);
        return modifierEntryHelper;
    }
    public void reloadModifierEntryHelper(){
        modifierEntryHelper = new ModifierEntryHelper(itemStack);
    }
}