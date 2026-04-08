package net.exmo.exmodifier.util.module;

import net.exmo.exmodifier.Exmodifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * ItemStack NBT Helper基类 - 取代原ExHelper。
 * 提供基于ExNbtSchema的声明式NBT存取，统一管理 exmo_nbt 下的子TAG。
 * 面向向上移植设计，不依赖特定MC版本API。
 */
public abstract class ExItemNbtAccessor {
    public static final String EXMO_NBT = "exmo_nbt";

    public final ItemStack itemStack;

    protected ExItemNbtAccessor(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    // region 基础NBT操作

    public CompoundTag getMainNbt() {
        CompoundTag tag = itemStack.getTag();
        if (tag == null) return new CompoundTag();
        return tag.getCompound(EXMO_NBT);
    }

    public boolean validMainNbt() {
        CompoundTag tag = itemStack.getTag();
        return tag != null && tag.contains(EXMO_NBT);
    }

    public void ensureMainNbt() {
        CompoundTag tag = itemStack.getOrCreateTag();
        if (!tag.contains(EXMO_NBT)) {
            tag.put(EXMO_NBT, new CompoundTag());
        }
    }

    // endregion

    // region ListTag 通用操作

    /**
     * 获取 mainNbt 下指定key的ListTag（CompoundTag列表）
     */
    protected ListTag getListTag(String key) {
        return getMainNbt().getList(key, 10);
    }

    /**
     * 确保 mainNbt 下指定key存在ListTag
     */
    protected void ensureListTag(String key) {
        ensureMainNbt();
        CompoundTag main = getMainNbt();
        if (!main.contains(key)) {
            main.put(key, new ListTag());
        }
    }

    /**
     * 获取list的大小
     */
    protected int getListSize(String key) {
        if (!validMainNbt()) return 0;
        return getMainNbt().getList(key, 10).size();
    }

    /**
     * 通过Schema读取列表中的对象
     */
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

    /**
     * 通过Schema写入单个对象到列表
     */
    protected <T> void addListItem(String listKey, T item, ExNbtSchema<T> schema) {
        ensureMainNbt();
        ensureListTag(listKey);
        ListTag listTag = getListTag(listKey);
        listTag.add(schema.writeToNbt(item));
    }

    /**
     * 移除列表中满足条件的第一个元素
     */
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

    /**
     * 移除列表中满足条件的所有元素
     */
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

    /**
     * 按索引移除列表元素
     */
    protected boolean removeFromListAt(String listKey, int index) {
        if (!validMainNbt()) return false;
        ListTag listTag = getListTag(listKey);
        if (index < 0 || index >= listTag.size()) return false;
        listTag.remove(index);
        return true;
    }

    /**
     * 查找列表中满足条件的第一个元素
     */
    protected CompoundTag findInList(String listKey, Function<CompoundTag, Boolean> predicate) {
        if (!validMainNbt()) return null;
        ListTag listTag = getListTag(listKey);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag tag = listTag.getCompound(i);
            if (predicate.apply(tag)) return tag;
        }
        return null;
    }

    /**
     * 更新列表中满足条件的第一个元素的某个字段
     */
    protected <V> boolean updateInList(String listKey,
                                       Function<CompoundTag, Boolean> predicate,
                                       String fieldKey,
                                       java.util.function.BiConsumer<CompoundTag, String> updater) {
        CompoundTag found = findInList(listKey, predicate);
        if (found != null) {
            updater.accept(found, fieldKey);
            return true;
        }
        return false;
    }

    // endregion

    // region 便利方法

    /** 获取mainNbt下的int值 */
    protected int getMainInt(String key) {
        return getMainNbt().getInt(key);
    }

    /** 获取mainNbt下的double值 */
    protected double getMainDouble(String key) {
        return getMainNbt().getDouble(key);
    }

    /** 获取mainNbt下的String值 */
    protected String getMainString(String key) {
        return getMainNbt().getString(key);
    }

    /** 获取mainNbt下的boolean值 */
    protected boolean getMainBoolean(String key) {
        return getMainNbt().getBoolean(key);
    }

    /** 设置mainNbt下的int值 */
    protected void setMainInt(String key, int value) {
        ensureMainNbt();
        getMainNbt().putInt(key, value);
    }

    /** 设置mainNbt下的double值 */
    protected void setMainDouble(String key, double value) {
        ensureMainNbt();
        getMainNbt().putDouble(key, value);
    }

    /** 设置mainNbt下的String值 */
    protected void setMainString(String key, String value) {
        ensureMainNbt();
        getMainNbt().putString(key, value);
    }

    /** 设置mainNbt下的boolean值 */
    protected void setMainBoolean(String key, boolean value) {
        ensureMainNbt();
        getMainNbt().putBoolean(key, value);
    }

    /** 检查mainNbt下是否存在某key */
    protected boolean hasMainKey(String key) {
        return validMainNbt() && getMainNbt().contains(key);
    }

    // endregion
}
