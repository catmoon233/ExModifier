package net.exmo.exmodifier.content.modifier;

import net.exmo.exmodifier.content.helper.ItemLevelHelper;
import net.exmo.exmodifier.content.helper.ModifierEntryHelper;
import net.minecraft.nbt.CompoundTag;

public class ModifierInstant {
    private ModifierEntry modifierEntry;
    private int level;
    private boolean itemQualityLock;
    private CompoundTag data;
    public static ModifierInstant of (ModifierEntry modifierEntry, int level, CompoundTag data) {
        return new ModifierInstant(modifierEntry,level).setData(data);
    }
    public static ModifierInstant of (ModifierEntry modifierEntry) {
        return new ModifierInstant(modifierEntry);
    }
    public static ModifierInstant of (ModifierEntry modifierEntry, int level) {
        return new ModifierInstant(modifierEntry,level);
    }
    public ModifierInstant(ModifierEntry modifierEntry, int level) {
        this.modifierEntry = modifierEntry;
        this.level = level;
    }


    public ModifierInstant(ModifierEntry modifierEntry) {
        this.modifierEntry = modifierEntry;
        this.level = 1;
    }
    public ModifierInstant itemQualityLock() {
        this.setItemQualityLock(true);
        return this;
    }
    public ModifierEntry getModifierEntry() {
        return modifierEntry;
    }

    public void setModifierEntry(ModifierEntry modifierEntry) {
        this.modifierEntry = modifierEntry;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public CompoundTag getData() {
        return data;
    }

    public ModifierInstant setData(CompoundTag data) {
        this.data = data;
        return this;
    }
    public CompoundTag serializeNBT(){
        CompoundTag tag = new CompoundTag();
        tag.putString(ModifierEntryHelper.MEID,this.modifierEntry.id);
        tag.putInt("Level",this.level);
        tag.merge(this.data);
        return tag;
    }

    public boolean isItemQualityLock() {
        return itemQualityLock;
    }

    public ModifierInstant setItemQualityLock(boolean itemQualityLock) {
        this.itemQualityLock = itemQualityLock;
        return this;
    }
}