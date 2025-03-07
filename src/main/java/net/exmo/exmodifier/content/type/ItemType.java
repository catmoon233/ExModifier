package net.exmo.exmodifier.content.type;

import net.exmo.exmodifier.util.ItemSelector;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;

public record ItemType(String name, ItemSelector itemSelector, EquipmentSlot... equipmentSlot) {
    public EquipmentSlot[] getEquipmentSlot() {
        return equipmentSlot;
    }

    //我写handle去了 可
    //直接用ExType new一个
    //ExType无法动态注册 即通过config注册
    //开个handle 移过去
    //塞enum里，enum是一个特殊类 ?
    //你这样写 拓展性差 构造写不了什么
    //怎么了
}
