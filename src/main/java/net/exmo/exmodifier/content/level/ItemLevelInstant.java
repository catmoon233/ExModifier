package net.exmo.exmodifier.content.level;

import net.exmo.exmodifier.content.modifier.ModifierInstant;
import net.minecraft.nbt.CompoundTag;

public class ItemLevelInstant {
    public ItemLevel itemLevel;
    public int level;
    public int maxLevel;
    public CompoundTag data;
    public double xp;
    public double needXp;
    private boolean lock = false;

    public static ItemLevelInstant of(ItemLevel itemLevel) {
        return new ItemLevelInstant(itemLevel, itemLevel.getDefaultLevel(), itemLevel.getMaxLevel(), 0, ItemLevelHandle.generateLevelNeedXp(itemLevel, itemLevel.getDefaultLevel()));
    }

    public ItemLevelInstant(ItemLevel itemLevel, int level, int maxLevel, double xp, double needXp) {
        this.itemLevel = itemLevel;
        this.level = level;
        this.maxLevel = maxLevel;
        this.xp = xp;
        this.needXp = needXp;
    }

    public ItemLevelInstant lock() {
        this.lock = true;
        return this;
    }

    public ItemLevelInstant unlock() {
        this.lock = false;
        return this;
    }

    public ItemLevelInstant setLock(boolean lock) {
        this.lock = lock;
        return this;
    }

    public boolean isLock() {
        return lock;
    }

    public ItemLevel getItemLevel() {
        return itemLevel;
    }

    public void setItemLevel(ItemLevel itemLevel) {
        this.itemLevel = itemLevel;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public CompoundTag getData() {
        return data;
    }

    public void setData(CompoundTag data) {
        this.data = data;
    }

    public double getXp() {
        return xp;
    }

    public void setXp(double xp) {
        this.xp = xp;
    }

    public double getNeedXp() {
        return needXp;
    }

    public void setNeedXp(double needXp) {
        this.needXp = needXp;
    }
}