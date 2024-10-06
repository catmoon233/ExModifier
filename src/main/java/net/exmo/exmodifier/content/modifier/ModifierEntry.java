package net.exmo.exmodifier.content.modifier;

import net.exmo.exmodifier.content.suit.ExSuit;
import net.exmo.exmodifier.content.suit.ExSuitHandle;
import net.exmo.exmodifier.util.CuriosUtil;
import net.exmo.exmodifier.util.ItemAttrUtil;
import net.exmo.exmodifier.util.WeightedUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.ForgeRegistries;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.exmo.exmodifier.content.modifier.EntryItem.CommonEvent.df;
import static net.exmo.exmodifier.content.modifier.ModifierHandle.percentAtr;
import static net.minecraft.world.item.ItemStack.ATTRIBUTE_MODIFIER_FORMAT;

public class ModifierEntry {
    public Map<String,String> setting = new HashMap<>();
    public float weight =0;
    public boolean cantSelect = false;

    public static boolean containItemTypes(ItemStack item, List<Type> onlyTypes) {
        for (Type type : onlyTypes){
            if (containItemType(item, type)) return true;
        }
        return false;
    }

    // public double level;
    public static enum Type {
        CURIOS,
        ALL,
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
        ;


        public record values() {
            public static final List<Type> values = List.of(Type.values());
        }
    }
    public static boolean containItemType(ItemStack stack, Type type){
        if (type == Type.ALL)return true;
        if (type == Type.SHIELD) {
            return stack.getUseAnimation() == UseAnim.BLOCK;
        }
        if (type == Type.BOW) return stack.getItem() instanceof BowItem || stack.getUseAnimation() == UseAnim.BOW;

        if (type == Type.ARMOR){
            return stack.getItem() instanceof ArmorItem;
        }
        if (type == Type.WEAPON){
            return stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem;
        }
        if (type== Type.ATTACKABLE){
           if ( stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE).stream()
                    .mapToDouble(AttributeModifier::getAmount).sum() >0)
               return true;
        }
        if (type ==Type.CURIOS)
            return CuriosUtil.isCuriosItem(stack);
        return false;
    }
    public static List<Component> GenerateTooltip(List<ModifierAttriGether> attriGethers, ItemStack itemStack) {
        List<Component> tooltips = new java.util.ArrayList<>();
        for (ModifierAttriGether modifierAttriGether : attriGethers) {
            AttributeModifier attributemodifier = modifierAttriGether.getModifier();
            Attribute attribute = modifierAttriGether.getAttribute();
            if (attribute == null)continue;
            if (attributemodifier ==null)continue;
            //    if (modifierAttriGether.slot==null)continue;
            EquipmentSlot slot = modifierAttriGether.slot;
            if (modifierAttriGether.IsAutoEquipmentSlot){
                slot = ModifierEntry.TypeToEquipmentSlot(ModifierEntry.getType(itemStack));
            }
            if (!ItemAttrUtil.hasAttributeModifierCompoundTagNoAmount(itemStack, attribute, attributemodifier, modifierAttriGether.slot))continue;
            //  Exmodifier.LOGGER.info(modifierAttriGether.getAttribute().getDescriptionId());
            //   if (!itemStack.getAttributeModifiers(modifierAttriGether.slot).containsEntry(attribute, attributemodifier))continue;
            double d0 = attributemodifier.getAmount();
            boolean flag = false;
            String percent = "";
            double d1;
            if (attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL  &&!percentAtr.contains(ForgeRegistries.ATTRIBUTES.getKey(attribute).toString())) {
                if ((attribute).equals(Attributes.KNOCKBACK_RESISTANCE)) {
                    d1 = d0 * 10.0;
                } else {
                    d1 = d0;
                }
            } else {
                d1 = d0 * 100.0;
            }
            String amouta2 = "";
            if (percentAtr.contains(ForgeRegistries.ATTRIBUTES.getKey(attribute).toString())){
                percent = "%";
                DecimalFormat df = new DecimalFormat("#.####");
                amouta2 = df.format(attributemodifier.getAmount() * 100);
                if (modifierAttriGether.attribute.getDescriptionId().length() >=4){
                    if (ForgeRegistries.ATTRIBUTES.getKey(attribute).toString().startsWith("twtp") ||ForgeRegistries.ATTRIBUTES.getKey(attribute).toString().startsWith("isfix") ) {
                        amouta2 = df.format(attributemodifier.getAmount()) ;
                    }
                }
            }

            if (flag) {
                tooltips.add((new TextComponent(" ")).append(new TranslatableComponent("attribute.modifier.equals." + attributemodifier.getOperation().toValue(), new Object[]{ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslatableComponent(attribute.getDescriptionId())})).withStyle(ChatFormatting.DARK_GREEN));
            } else if (d0 > 0.0) {
                if (percent.equals("%")) tooltips.add(new TranslatableComponent("add").append(amouta2).append(percent).append(" ").append(new TranslatableComponent(attribute.getDescriptionId())).withStyle(ChatFormatting.BLUE));
                else tooltips.add((new TranslatableComponent("attribute.modifier.plus." + attributemodifier.getOperation().toValue(), new Object[]{ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslatableComponent(attribute.getDescriptionId())})).withStyle(ChatFormatting.BLUE));
            } else if (d0 < 0.0) {
                d1 *= -1.0;
                if (percent.equals("%")) tooltips.add(new TranslatableComponent("subtract").append(amouta2).append(percent).append(" ").append(new TranslatableComponent(attribute.getDescriptionId())).withStyle(ChatFormatting.RED));
                else  tooltips.add((new TranslatableComponent("attribute.modifier.take." + attributemodifier.getOperation().toValue(), new Object[]{ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslatableComponent(attribute.getDescriptionId())})).withStyle(ChatFormatting.RED));
            }
        }
        return tooltips;
    }
    public static EquipmentSlot TypeToEquipmentSlot(Type type) {
        switch (type) {
            case HAND, MAINHAND ,BOW,ATTACKABLE,AXE-> {
                return EquipmentSlot.MAINHAND;
            }
            case OFFHAND, OFFHAND_HAND,SHIELD -> {
                return EquipmentSlot.OFFHAND;
            }
            case CHESTPLATE -> {
                return EquipmentSlot.CHEST;
            }
            case LEGGINGS -> {
                return EquipmentSlot.LEGS;
            }
            case HELMET -> {
                return EquipmentSlot.HEAD;
            }
            case BOOTS -> {
                return EquipmentSlot.FEET;
            }

        }
        return null;
    }
    public static Type StringToType(String type) {
        if (type.toLowerCase().startsWith("curios")) return Type.CURIOS;
        for(var v : Type.values()){
            if(v.toString().equals(type))return v;
        }
        /*switch (type) {
            case "ALL" -> {
                return Type.ALL;
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
        */
        return Type.UNKNOWN;
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
    public static Type findTypeFormEntry(ModifierEntry modifierEntry)
    {
        for (Type type1 : Type.values())
        {
            if (type1.toString().substring(0,2).equals(modifierEntry.id.substring(0,2)))return type1;
        }
        return Type.UNKNOWN;
    }
    public  boolean containTag(ItemStack stack){
        for (String tag : OnlyTags ){
            if (stack.is(ItemTags.create(new ResourceLocation(tag))))return true;
        }
        return false;
    }
    public boolean isRandom = true;
    public boolean OnlyHasThisEntry = false;
    public Type type;
    public boolean isCuriosEntry =false;
    public float needFreshValue = 0;
    public String curiosType;
    public List<String> OnlyTags = new ArrayList<>();
    public List<String> OnlyItems = new ArrayList<>();
    public List<String> OnlyWashItems = new ArrayList<>();
    public List<String> Commands = new ArrayList<>();
    public String id;
    public int RandomNum;
    public List<ModifierAttriGether> attriGether = new java.util.ArrayList<>();

    public float getWeight() {
        return weight;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "ModifierEntry{" +
                "weight=" + weight +
                ", isRandom=" + isRandom +
                ", OnlyHasThisEntry=" + OnlyHasThisEntry +
                ", type=" + type +
                ", isCuriosEntry=" + isCuriosEntry +
                ", curiosType='" + curiosType + '\'' +
                ", OnlyTags=" + OnlyTags +
                ", OnlyItems=" + OnlyItems +
                ", OnlyWashItems=" + OnlyWashItems +
                ", id='" + id + '\'' +
                ", RandomNum=" + RandomNum +
                ", attriGether=" + attriGether +
                '}';
    }
    public List<Component> GenerateItemTooltip()
    {
        List<Component> list = new ArrayList<>();
        list.add(new TranslatableComponent("modifiler.entry.id").append(id));
        list.add(new TranslatableComponent("modifiler.entry.weight").append(String.valueOf(weight)));
        if (cantSelect) list.add(new TranslatableComponent("modifiler.entry.cant_select"));
        if (OnlyHasThisEntry) list.add(new TranslatableComponent("modifiler.entry.only_has_this_entry"));
        if (needFreshValue!=0) list.add(new TranslatableComponent("modifiler.entry.need_fresh_value").append(String.valueOf(needFreshValue)));
        if (!OnlyTags.isEmpty()) list.add(new TranslatableComponent("modifiler.entry.only_tags").append(String.join(",",OnlyTags)));
        if (!OnlyItems.isEmpty()) list.add(new TranslatableComponent("modifiler.entry.only_items").append(String.join(",",OnlyItems)));
        if (isRandom) list.add(new TranslatableComponent("modifiler.entry.is_random").append(String.valueOf(RandomNum)));
        list.add(new TranslatableComponent("modifiler.entry.type").append(type.toString()));
        list.add(new TranslatableComponent("modifiler.entry.attribute_gather"));
        //list.add(new TextComponent("§7["));
        WeightedUtil<String> weightUtil = new WeightedUtil<>(
                attriGether.stream()
                        .collect(Collectors.toMap(
                                k -> k.getModifier().getName(),
                                ModifierAttriGether::getWeight,
                                (oldValue, newValue) -> newValue // 这里定义如何处理键冲突，例如这里选择保留旧值
                        ))
        );
        for (ModifierAttriGether attriGether1 : attriGether){
            list.add(new TextComponent(" §7¦ §r").append(attriGether1.GenerateTooltip(isRandom)).append(isRandom ? " §9("+df.format(weightUtil.getProbability(attriGether1.getModifier().getName())*100)+"%)" : ""));
        }
        //list.add(new TextComponent("§7]"));
        boolean hasSuit = false;

        for (ExSuit suit : ExSuitHandle.LoadExSuit.values().stream().filter(exSuit -> exSuit.entry.contains(this))
                .toList()) {
            if (suit.visible) {
                if (!hasSuit){
                    list.add(new TranslatableComponent("modifiler.entry.suit"));
                 //   list.add(new TextComponent("§7["));
                    hasSuit=true;
                }
                list.add(new TextComponent(" §7¦ §r").append(new TranslatableComponent("modifiler.entry.suit." + suit.id)));
            }
        }
       // if (hasSuit)list.add(new TextComponent("§7]"));


        return list;
    }
}
