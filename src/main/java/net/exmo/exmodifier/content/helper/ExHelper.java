package net.exmo.exmodifier.content.helper;

import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.CommonEvent.isValidForType;

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
    public EquipmentSlot getEquipmentSlot(ItemStack itemStack){
        if (itemStack.getItem() instanceof ArmorItem armorItem){
            return armorItem.getEquipmentSlot();
        }
        Map<ModifierEntry.Type, EquipmentSlot> typeSlotMap =new HashMap<>( Map.of(
                ModifierEntry.Type.HELMET, EquipmentSlot.HEAD,
                ModifierEntry.Type.CHESTPLATE, EquipmentSlot.CHEST,
                ModifierEntry.Type.BOOTS, EquipmentSlot.FEET,
                ModifierEntry.Type.LEGGINGS, EquipmentSlot.LEGS,
                ModifierEntry.Type.ARMOR, EquipmentSlot.CHEST,  // For ARMOR type, we'll dynamically set the slot based on the item
                ModifierEntry.Type.SHIELD, EquipmentSlot.OFFHAND,
                ModifierEntry.Type.BOW, EquipmentSlot.MAINHAND,
                ModifierEntry.Type.SWORD, EquipmentSlot.MAINHAND,
                ModifierEntry.Type.ATTACKABLE, EquipmentSlot.MAINHAND,
                ModifierEntry.Type.AXE, EquipmentSlot.MAINHAND
        ));
        typeSlotMap.put(ModifierEntry.Type.CROSSBOW, EquipmentSlot.MAINHAND);
        typeSlotMap.put(ModifierEntry.Type.ATTACKABLE, EquipmentSlot.MAINHAND);
        for (Map.Entry<ModifierEntry.Type, EquipmentSlot> entry : typeSlotMap.entrySet()) {
            ModifierEntry.Type type = entry.getKey();
            EquipmentSlot slot = entry.getValue();
           if ( isValidForType(itemStack, type))return slot;
    }
        return itemStack.getItem().getEquipmentSlot(itemStack);
    }
}