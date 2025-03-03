package net.exmo.exmodifier.content.helper;

import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.content.type.ItemType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.CommonEvent.isValidForType;
import static net.exmo.exmodifier.content.modifier.ModifierHandle.CommonEvent.typeSlotMap;

public class ExHelper {
    private CompoundTag nbt;
    public ItemStack itemStack;
    public static final String EXMO_NBT = "exmo_nbt";

    public ExHelper(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.nbt = itemStack.getTag();
    }
    public void createNbt(){
     nbt = itemStack.getOrCreateTag();
    }
    public void createMainNbt(){
        nbt.put(EXMO_NBT,new CompoundTag());
    }
    public boolean ValidMainNbt(){
        if (nbt==null)return false;
        return nbt.contains(EXMO_NBT);
    }
    public CompoundTag getMainNbt(){
        return nbt.getCompound(EXMO_NBT);
    }
    public EquipmentSlot[] getEquipmentSlot(ItemStack itemStack){
        if (itemStack.getItem() instanceof ArmorItem armorItem){
            return new EquipmentSlot[]{armorItem.getEquipmentSlot()};
        }
        Map<ItemType, EquipmentSlot[]> typeEquipmentSlotMap = typeSlotMap();


        for (Map.Entry<ItemType, EquipmentSlot[]> entry : typeEquipmentSlotMap.entrySet()) {
            ItemType type = entry.getKey();
            EquipmentSlot[] slot = entry.getValue();
           if ( isValidForType(itemStack, type))return slot;
    }
        return new EquipmentSlot[]{itemStack.getItem().getEquipmentSlot(itemStack)};
    }
}