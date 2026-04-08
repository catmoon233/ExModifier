package net.exmo.exmodifier.util.module;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * LivingEntity NBT Helper基类 - 取代原BaseEntityHelper。
 * 提供基于ExNbtSchema的声明式NBT存取，统一管理 entity persistentData 下的 exmo_nbt。
 * 面向向上移植设计。
 */
public abstract class ExEntityNbtAccessor {
    public static final String EXMO_NBT = "exmo_nbt";

    public final LivingEntity entity;
    protected final CompoundTag persistentData;

    protected ExEntityNbtAccessor(LivingEntity entity) {
        this.entity = entity;
        this.persistentData = entity.getPersistentData();
    }

    // region 基础NBT操作

    public CompoundTag getMainNbt() {
        if (persistentData == null) return new CompoundTag();
        return persistentData.getCompound(EXMO_NBT);
    }

    public boolean validMainNbt() {
        return persistentData != null && persistentData.contains(EXMO_NBT);
    }

    public void ensureMainNbt() {
        if (!persistentData.contains(EXMO_NBT)) {
            persistentData.put(EXMO_NBT, new CompoundTag());
        }
    }

    // endregion

    // region ListTag 通用操作

    protected ListTag getListTag(String key) {
        return getMainNbt().getList(key, 10);
    }

    protected void ensureListTag(String key) {
        ensureMainNbt();
        CompoundTag main = getMainNbt();
        if (!main.contains(key)) {
            main.put(key, new ListTag());
        }
    }

    protected int getListSize(String key) {
        if (!validMainNbt()) return 0;
        return getMainNbt().getList(key, 10).size();
    }

    protected <T> List<T> readListItems(String listKey, ExNbtSchema<T> schema,
                                         Supplier<T> constructor,
                                         Function<CompoundTag, Boolean> validator) {
        List<T> result = new ArrayList<>();
        if (!validMainNbt()) return result;

        ListTag listTag = getListTag(listKey);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag itemTag = listTag.getCompound(i);
            if (validator == null || validator.apply(itemTag)) {
                T instance = constructor.get();
                schema.readFromNbt(itemTag, instance);
                result.add(instance);
            }
        }
        return result;
    }

    protected <T> void addListItem(String listKey, T item, ExNbtSchema<T> schema) {
        ensureMainNbt();
        ensureListTag(listKey);
        ListTag listTag = getListTag(listKey);
        listTag.add(schema.writeToNbt(item));
    }

    protected boolean removeFirstFromList(String listKey, Function<CompoundTag, Boolean> predicate) {
        if (!validMainNbt()) return false;
        ListTag listTag = getListTag(listKey);
        for (int i = 0; i < listTag.size(); i++) {
            if (predicate.apply(listTag.getCompound(i))) {
                listTag.remove(i);
                return true;
            }
        }
        return false;
    }

    protected int removeAllFromList(String listKey, Function<CompoundTag, Boolean> predicate) {
        if (!validMainNbt()) return 0;
        ListTag listTag = getListTag(listKey);
        int removed = 0;
        for (int i = listTag.size() - 1; i >= 0; i--) {
            if (predicate.apply(listTag.getCompound(i))) {
                listTag.remove(i);
                removed++;
            }
        }
        return removed;
    }

    protected CompoundTag findInList(String listKey, Function<CompoundTag, Boolean> predicate) {
        if (!validMainNbt()) return null;
        ListTag listTag = getListTag(listKey);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag tag = listTag.getCompound(i);
            if (predicate.apply(tag)) return tag;
        }
        return null;
    }

    // endregion

    // region 便利方法

    protected int getMainInt(String key) {
        return getMainNbt().getInt(key);
    }

    protected double getMainDouble(String key) {
        return getMainNbt().getDouble(key);
    }

    protected String getMainString(String key) {
        return getMainNbt().getString(key);
    }

    protected boolean getMainBoolean(String key) {
        return getMainNbt().getBoolean(key);
    }

    protected void setMainInt(String key, int value) {
        ensureMainNbt();
        getMainNbt().putInt(key, value);
    }

    protected void setMainDouble(String key, double value) {
        ensureMainNbt();
        getMainNbt().putDouble(key, value);
    }

    protected void setMainString(String key, String value) {
        ensureMainNbt();
        getMainNbt().putString(key, value);
    }

    protected void setMainBoolean(String key, boolean value) {
        ensureMainNbt();
        getMainNbt().putBoolean(key, value);
    }

    protected boolean hasMainKey(String key) {
        return validMainNbt() && getMainNbt().contains(key);
    }

    // endregion
}
