package net.exmo.exmodifier.content.modifier;

import net.exmo.exmodifier.util.AttriGether;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

import java.util.List;

public class ModifierEntry {
    public float weight;
    public static enum Type {
        UNKNOWN,
        ATTACKABLE,
        ARMOR,
        WEAPON,
        HELMET,
        CHESTPLATE,
        LEGGINGS,
        BOOTS,
        RANGED,
        MISC,
        FISHING_ROD,
        TRIDENT,
        CROSSBOW,
        BOW,
        SHIELD,
        PICKAXE,
        AXE,
        SHOVEL,
        HOE,
        SWORD,
        TRINKET,
        HAND,
        OFFHAND,
        MAINHAND,
       OFFHAND_HAND,
    }
    public static Type StringToType(String type) {
        switch (type) {
            case "UNKNOWN" -> {
                return Type.UNKNOWN;
            }
            case "ATTACKABLE" -> {
                return Type.ATTACKABLE;
            }
            case "ARMOR" -> {
                return Type.ARMOR;
            }
            case "WEAPON" -> {
                return Type.WEAPON;
            }
            case "HELMET" -> {
                return Type.HELMET;
            }
            case "CHESTPLATE" -> {
                return Type.CHESTPLATE;
            }
            case "LEGGINGS" -> {
                return Type.LEGGINGS;
            }
            case "BOOTS" -> {
                return Type.BOOTS;
            }
            case "RANGED" -> {
                return Type.RANGED;
            }
            case "MISC" -> {
                return Type.MISC;
            }
            case "FISHING_ROD" -> {
                return Type.FISHING_ROD;
            }
            case "TRIDENT" -> {
                return Type.TRIDENT;
            }
            case "CROSSBOW" -> {
                return Type.CROSSBOW;
            }
            case "BOW" -> {
                return Type.BOW;
            }
            case "SHIELD" -> {
                return Type.SHIELD;
            }
            case "PICKAXE" -> {
                return Type.PICKAXE;
            }
            case "AXE" -> {
                return Type.AXE;
            }
            case "SHOVEL" -> {
                return Type.SHOVEL;
            }
            case "HOE" -> {
                return Type.HOE;
            }
            case "SWORD" -> {
                return Type.SWORD;
            }
            case "TRINKET" -> {
                return Type.TRINKET;
            }
            case "HAND" -> {
                return Type.HAND;
            }
            case "OFFHAND" -> {
                return Type.OFFHAND;
            }
            case "MAINHAND" -> {
                return Type.MAINHAND;
            }
            case "OFFHAND_HAND" -> {
                return Type.OFFHAND_HAND;
            }
            default -> {
                return Type.UNKNOWN;
            }
        }
    }
    public static Type getType(ItemStack stack) {
        if (ModifierHandle.hasChestConfig) {


            if (stack.getItem() instanceof ArmorItem armorItem) {
                if (armorItem.getSlot() == EquipmentSlot.CHEST)
                    return Type.CHESTPLATE;
            }
        }
        if (ModifierHandle.hasLeggingsConfig) {
            if (stack.getItem() instanceof ArmorItem armorItem) {
                if (armorItem.getSlot() == EquipmentSlot.LEGS)
                    return Type.LEGGINGS;
            }
        }
        if (ModifierHandle.hasBootsConfig) {
            if (stack.getItem() instanceof ArmorItem armorItem) {
                if (armorItem.getSlot() == EquipmentSlot.FEET)
                    return Type.BOOTS;
            }
        }
        if (ModifierHandle.hasHelmetConfig) {
            if (stack.getItem() instanceof ArmorItem armorItem) {
                if (armorItem.getSlot() == EquipmentSlot.HEAD)
                    return Type.HELMET;
            }
        }

        if (stack.getItem() instanceof AxeItem ){
            return Type.AXE;
        }
        if (stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE).stream()
                .mapToDouble(attributeModifier -> attributeModifier.getAmount()).sum() >0){
            return Type.ATTACKABLE;
        }
        if (stack.getItem() instanceof SwordItem item){
            return Type.SWORD;
        }
        if (stack.getItem().isEdible())
            return Type.FISHING_ROD;
        if (stack.getItem() instanceof ArmorItem)
            return Type.ARMOR;
        return Type.UNKNOWN;
    }
    public boolean isRandom;
    public Type type;
    public String id;
    public int RandomNum;
    public List<ModifierAttriGether> attriGether = new java.util.ArrayList<>();

    public float getWeight() {
        return weight;
    }

    public String getId() {
        return id;
    }
}
