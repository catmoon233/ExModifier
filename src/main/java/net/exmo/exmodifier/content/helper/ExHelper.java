package net.exmo.exmodifier.content.helper;

import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.content.type.ItemType;
import net.exmo.exmodifier.util.module.ExItemNbtAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.CommonEvent.isValidForType;
import static net.exmo.exmodifier.content.modifier.ModifierHandle.CommonEvent.typeSlotMap;

/**
 * ItemStack Helper基类 - 现在继承ExItemNbtAccessor，提供统一的NBT访问方法。
 * 所有子类Helper都可以使用 getMainNbt(), validMainNbt(), ensureMainNbt(),
 * getListTag(), addListItem(), removeFirstFromList() 等方法。
 *
 * 同时保留向后兼容的旧方法名。
 */
public class ExHelper extends ExItemNbtAccessor {

    public ExHelper(ItemStack itemStack) {
        super(itemStack);
    }

    // region 向后兼容的旧方法（委托到ExItemNbtAccessor）

    /** @deprecated 使用 itemStack.getOrCreateTag() 或 ensureMainNbt() */
    @Deprecated
    public void createNbt() {
        itemStack.getOrCreateTag();
    }

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

    public EquipmentSlot[] getEquipmentSlot(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ArmorItem armorItem) {
            return new EquipmentSlot[]{armorItem.getEquipmentSlot()};
        }
        Map<ItemType, EquipmentSlot[]> typeEquipmentSlotMap = typeSlotMap();

        for (Map.Entry<ItemType, EquipmentSlot[]> entry : typeEquipmentSlotMap.entrySet()) {
            ItemType type = entry.getKey();
            EquipmentSlot[] slot = entry.getValue();
            if (isValidForType(itemStack, type)) return slot;
        }
        return new EquipmentSlot[]{itemStack.getItem().getEquipmentSlot(itemStack)};
    }
}