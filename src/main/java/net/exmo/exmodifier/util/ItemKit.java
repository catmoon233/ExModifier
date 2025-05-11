
package net.exmo.exmodifier.util;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ItemKit {
    public int slot;
    public EquipmentSlot equipmentSlot = null;
    public ItemStack itemStack;
    public int Count;
    //	public ItemKit(int slot, ItemStack itemStack, int count) {
//		this.slot = slot;
//		this.itemStack = itemStack;
//		this.Count = count;
//	}
    public boolean firstHas = true;

    public ItemKit(int slot, String s, int count) throws Exception {
        this.slot = slot;
        this.Count = count;
        this.itemStack = CustomItemUtil.getItemFromString(s);
    }

    public ItemKit(EquipmentSlot equipmentSlot, String s, int count) throws Exception {
        this.equipmentSlot = equipmentSlot;
        this.Count = count;
        this.itemStack = CustomItemUtil.getItemFromString(s);
    }

    public ItemKit(EquipmentSlot equipmentSlot, ItemStack s, int count) throws Exception {
        this.equipmentSlot = equipmentSlot;
        this.Count = count;
        this.itemStack = s;
    }

    public ItemKit(int slot, String s, int count, boolean firstHas) throws Exception {
        this.firstHas = firstHas;
        this.slot = slot;
        this.Count = count;
        this.itemStack = CustomItemUtil.getItemFromString(s);
    }

    public ItemKit(EquipmentSlot equipmentSlot, String s, int count, boolean firstHas) throws Exception {
        this.firstHas = firstHas;
        this.equipmentSlot = equipmentSlot;
        this.Count = count;
        this.itemStack = CustomItemUtil.getItemFromString(s);
    }
}
