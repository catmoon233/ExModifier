package net.exmo.exmodifier.content.modifier;

import net.minecraft.nbt.CompoundTag;

public class ModifierInstant {
    private ModifierEntry modifierEntry;
    private int level;
    private CompoundTag data;
    public ModifierInstant(ModifierEntry modifierEntry, int level) {
        this.modifierEntry = modifierEntry;
        this.level = level;
    }

    public ModifierInstant(ModifierEntry modifierEntry) {
        this.modifierEntry = modifierEntry;
        this.level = 1;
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
}