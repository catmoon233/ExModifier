package net.exmo.exmodifier.content.helper.entity;

import net.exmo.exmodifier.util.module.ExEntityNbtAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

/**
 * Entity Helper基类 - 现在继承ExEntityNbtAccessor，提供统一的NBT访问方法。
 * 所有Entity子类Helper都可以使用 getMainNbt(), validMainNbt(), ensureMainNbt(),
 * getListTag(), addListItem(), removeFirstFromList() 等方法。
 */
public class BaseEntityHelper extends ExEntityNbtAccessor {
    /** @deprecated 使用 persistentData */
    @Deprecated
    public CompoundTag nbt;

    public BaseEntityHelper(LivingEntity entity) {
        super(entity);
        this.nbt = entity.getPersistentData();
    }

    // region 向后兼容的旧方法

    /** @deprecated 使用 ensureMainNbt() */
    @Deprecated
    public void createMainNbt() {
        ensureMainNbt();
    }

    /** @deprecated 使用 validMainNbt() */
    @Deprecated
    public boolean ValidMainNbt() {
        return validMainNbt();
    }

    // endregion
}
