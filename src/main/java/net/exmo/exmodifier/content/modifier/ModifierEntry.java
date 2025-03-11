package net.exmo.exmodifier.content.modifier;

import net.exmo.exmodifier.content.specialEffects.SpecialEffect;
import net.exmo.exmodifier.content.suit.ExSuit;
import net.exmo.exmodifier.content.suit.ExSuitHandle;
import net.exmo.exmodifier.content.type.ExType;
import net.exmo.exmodifier.content.type.ExTypeHandle;
import net.exmo.exmodifier.content.type.ItemType;
import net.exmo.exmodifier.util.CuriosUtil;
import net.exmo.exmodifier.util.ItemAttrUtil;
import net.exmo.exmodifier.util.WeightedUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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
import static net.exmo.exmodifier.content.type.ExType.*;
//@SerialClass
public class ModifierEntry {

    public Map<String,String> setting = new HashMap<>();
    public float weight;
    public boolean cantSelect = false;
    public boolean isRandom = true;
    public String icon ="";
    public boolean OnlyHasThisEntry = false;
    public String localDescription="";
    public int maxLevel=1;
    public List<String> Slots = new ArrayList<>();
    public  List<ItemType> types = new ArrayList<>();
    public List<String> specialTags = new ArrayList<>();
    public boolean isCuriosEntry =false;
    public float needFreshValue = 0;
    public String curiosType ="";
    public List<String> OnlyTags = new ArrayList<>();
    public List<String> OnlyItems = new ArrayList<>();
    public List<String> OnlyWashItems = new ArrayList<>();
    public List<String> Commands = new ArrayList<>();
    public String id;
    public String Expression="";
    public int RandomNum = 0;
    public List<ModifierAttriGether> attriGether = new java.util.ArrayList<>();

    public ModifierEntry(String id) {
        this.id = id;
    }
    public ModifierEntry(){}

    public List<String> getUnlessItemTags() {
        return UnlessItemTags;
    }

    public boolean containItem(ItemStack stack){
        boolean re = true;
        String itemId = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
        if (!OnlyItems.isEmpty()){
            re =false;
            for (String item : OnlyItems){
                if (item.equals(itemId)){
                    re = true;
                    break;
                }
            }
        }
        if (!UnlessItemIds.isEmpty()){
            for (String item : UnlessItemIds){
                if (item.equals(itemId)){
                    return false;
                }
            }
        }
        if (!OnlyTags.isEmpty()){
            if(!containTag(stack))return false;
        }
        if (!UnlessItemTags.isEmpty()){
            if (!unContainTag(stack))return false;
        }
        if (!containItemTypes(stack, types)){
            return  false;
        }
        return re;
    }






    public void setUnlessItemTags(List<String> unlessItemTags) {
        UnlessItemTags = unlessItemTags;
    }

    public List<String> getUnlessItemIds() {
        return UnlessItemIds;
    }

    public void setUnlessItemIds(List<String> unlessItemIds) {
        UnlessItemIds = unlessItemIds;
    }

    public List<String> UnlessItemTags = new ArrayList<>();
    public List<String> UnlessItemIds = new ArrayList<>();
//    public static boolean containItemTypes(ItemStack item, List<ItemType> onlyTypes) {
//        for (ItemType type : onlyTypes){
//            if (containItemType(item, type)) return true;
//        }
//        return false;
//    }

    @Override
    public String toString() {
        return "ModifierEntry{" +
                "setting=" + setting +
                ", weight=" + weight +
                ", cantSelect=" + cantSelect +
                ", isRandom=" + isRandom +
                ", icon='" + icon + '\'' +
                ", OnlyHasThisEntry=" + OnlyHasThisEntry +
                ", localDescription='" + localDescription + '\'' +
                ", maxLevel=" + maxLevel +
                ", Slots=" + Slots +
                ", type=" + types +
                ", specialTags=" + specialTags +
                ", isCuriosEntry=" + isCuriosEntry +
                ", needFreshValue=" + needFreshValue +
                ", curiosType='" + curiosType + '\'' +
                ", OnlyTags=" + OnlyTags +
                ", OnlyItems=" + OnlyItems +
                ", OnlyWashItems=" + OnlyWashItems +
                ", Commands=" + Commands +
                ", id='" + id + '\'' +
                ", Expression='" + Expression + '\'' +
                ", RandomNum=" + RandomNum +
                ", attriGether=" + attriGether +
                ", UnlessItemTags=" + UnlessItemTags +
                ", UnlessItemIds=" + UnlessItemIds +
                '}';
    }


    public static boolean containItemTypes(ItemStack stack, List<ItemType> types) {
        for (ItemType type : types) {
            if (type.compare(stack))return true;
        }
        return false;
    }
    public static boolean containItemType(ItemStack stack, ItemType type) {
        return type.compare(stack);
    }
    public static List<Component> GenerateTooltip(List<ModifierAttriGether> attriGethers, ItemStack itemStack) {
        List<Component> tooltips = new java.util.ArrayList<>();
        for (ModifierAttriGether modifierAttriGether : attriGethers) {
            AttributeModifier attributemodifier = modifierAttriGether.getModifier();
            Attribute attribute = modifierAttriGether.getAttribute();
            if (attribute == null)continue;
            if (attributemodifier ==null)continue;
            //    if (modifierAttriGether.slot==null)continue;
//            EquipmentSlot slot = modifierAttriGether.slot;
//            if (modifierAttriGether.IsAutoEquipmentSlot){
//                slot = ModifierEntry.TypeToEquipmentSlot(ModifierEntry.getType(itemStack));
//            }
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
                tooltips.add((Component.literal(" ")).append(Component.translatable("attribute.modifier.equals." + attributemodifier.getOperation().toValue(), new Object[]{ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(attribute.getDescriptionId())})).withStyle(ChatFormatting.DARK_GREEN));
            } else if (d0 > 0.0) {
                if (percent.equals("%")) tooltips.add(Component.translatable("add").append(amouta2).append(percent).append(" ").append(Component.translatable(attribute.getDescriptionId())).withStyle(ChatFormatting.BLUE));
                else tooltips.add((Component.translatable("attribute.modifier.plus." + attributemodifier.getOperation().toValue(), new Object[]{ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(attribute.getDescriptionId())})).withStyle(ChatFormatting.BLUE));
            } else if (d0 < 0.0) {
                d1 *= -1.0;
                if (percent.equals("%")) tooltips.add(Component.translatable("subtract").append(amouta2).append(percent).append(" ").append(Component.translatable(attribute.getDescriptionId())).withStyle(ChatFormatting.RED));
                else  tooltips.add((Component.translatable("attribute.modifier.take." + attributemodifier.getOperation().toValue(), new Object[]{ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(attribute.getDescriptionId())})).withStyle(ChatFormatting.RED));
            }
        }
        return tooltips;
    }
    public static EquipmentSlot TypeToEquipmentSlot(ItemType type) {
        if (type.equals(HAND.get()) || type.equals(MAINHAND.get()) || type.equals(BOW.get()) || type.equals(ATTACKABLE.get()) || type.equals(AXE.get())) {
            return EquipmentSlot.MAINHAND;
        } else if (type.equals(OFFHAND.get()) || type.equals(SHIELD.get())) {
            return EquipmentSlot.OFFHAND;
        } else if (type.equals(CHESTPLATE.get())) {
            return EquipmentSlot.CHEST;
        } else if (type.equals(LEGGINGS.get())) {
            return EquipmentSlot.LEGS;
        } else if (type.equals(HELMET.get())) {
            return EquipmentSlot.HEAD;
        } else if (type.equals(BOOTS.get())) {
            return EquipmentSlot.FEET;
        }
        return null;
    }
    public static ItemType StringToType(String type) {
        if (type.toLowerCase().startsWith("curios")) return ExType.CURIOS.get();
        for(var v : ExTypeHandle.values.values()){
            if(v.name().equalsIgnoreCase(type))return v;
        }
        /*switch (type) {
            case "ALL" -> {
                return ExType.ALL;
            }
            case "ATTACKABLE" -> {
                return ExType.ATTACKABLE;
            }
            case "ARMOR" -> {
                return ExType.ARMOR;
            }
            case "WEAPON" -> {
                return ExType.WEAPON;
            }
            case "HELMET" -> {
                return ExType.HELMET;
            }
            case "CHESTPLATE" -> {
                return ExType.CHESTPLATE;
            }
            case "LEGGINGS" -> {
                return ExType.LEGGINGS;
            }
            case "BOOTS" -> {
                return ExType.BOOTS;
            }
            case "RANGED" -> {
                return ExType.RANGED;
            }
            case "MISC" -> {
                return ExType.MISC;
            }
            case "FISHING_ROD" -> {
                return ExType.FISHING_ROD;
            }
            case "TRIDENT" -> {
                return ExType.TRIDENT;
            }
            case "CROSSBOW" -> {
                return ExType.CROSSBOW;
            }
            case "BOW" -> {
                return ExType.BOW;
            }
            case "SHIELD" -> {
                return ExType.SHIELD;
            }
            case "PICKAXE" -> {
                return ExType.PICKAXE;
            }
            case "AXE" -> {
                return ExType.AXE;
            }
            case "SHOVEL" -> {
                return ExType.SHOVEL;
            }
            case "HOE" -> {
                return ExType.HOE;
            }
            case "SWORD" -> {
                return ExType.SWORD;
            }
            case "TRINKET" -> {
                return ExType.TRINKET;
            }
            case "HAND" -> {
                return ExType.HAND;
            }
            case "OFFHAND" -> {
                return ExType.OFFHAND;
            }
            case "MAINHAND" -> {
                return ExType.MAINHAND;
            }
            case "OFFHAND_HAND" -> {
                return ExType.OFFHAND_HAND;
            }
            default -> {
                return ExType.UNKNOWN;
            }
        }
        */
        return ExType.UNKNOWN.get();
    }
    public static List<ItemType> getType(ItemStack stack) {
        List<ItemType> types = new ArrayList<>();
        for (var v : ExTypeHandle.values.values()){
            if (v.compare(stack)){
                types.add(v);
            }
        }
        return types;
//        if (stack.getItem() instanceof ArmorItem armorItem) {
//            switch (armorItem.getEquipmentSlot()) {
//                case CHEST -> {
//                    if (ModifierHandle.hasChestConfig) return CHESTPLATE.get();
//                }
//                case LEGS -> {
//                    if (ModifierHandle.hasLeggingsConfig) return LEGGINGS.get();
//                }
//                case FEET -> {
//                    if (ModifierHandle.hasBootsConfig) return BOOTS.get();
//                }
//                case HEAD -> {
//                    if (ModifierHandle.hasHelmetConfig) return HELMET.get();
//                }
//            }
//        }
//        if (stack.getItem() instanceof AxeItem) return AXE.get();
//        if (stack.getItem() instanceof BowItem) return BOW.get();
//        if (stack.getItem() instanceof CrossbowItem) return ExType.CROSSBOW;
//        if (stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE).stream().mapToDouble(attributeModifier -> attributeModifier.getAmount()).sum() > 0) return ExType.ATTACKABLE;
//        if (stack.getItem() instanceof SwordItem) return ExType.SWORD;
//        if (stack.getItem().isEdible()) return ExType.FISHING_ROD;
//        if (stack.getItem() instanceof ArmorItem) return ExType.ARMOR;
//        return ExType.UNKNOWN;
    }
    public static ItemType findTypeFormEntry(ModifierEntry modifierEntry)
    {
        for (ItemType type1 : ExTypeHandle.values.values())
        {
            if (type1.toString().substring(0,2).equals(modifierEntry.id.substring(0,2)))return type1;
        }
        return ExType.UNKNOWN.get();
    }
    public  boolean containTag(ItemStack stack){
        if (OnlyTags.isEmpty())return true;
        for (String tag : OnlyTags ){
            if (stack.is(ItemTags.create(new ResourceLocation(tag))))return true;
        }
        return false;
    }
    public boolean unContainTag(ItemStack stack){
        if (OnlyTags.isEmpty())return false;
        for (String tag : getUnlessItemTags() ){
            if (stack.is(ItemTags.create(new ResourceLocation(tag))))return false;
        }
        return true;
    }


    public float getWeight() {
        return weight;
    }

    public String getId() {
        return id;
    }
    public boolean hasSpecialEffect(SpecialEffect specialEffect){
        return specialTags.contains(specialEffect.id());
    }

    public List<Component> GenerateItemTooltip() {
        List<Component> list = new ArrayList<>();
        list.add(Component.translatable("modifier.entry.id").append(id));
        list.add(Component.translatable("modifier.entry.weight").append(String.valueOf(weight)));
        if (cantSelect) list.add(Component.translatable("modifier.entry.cant_select"));
        if (OnlyHasThisEntry) list.add(Component.translatable("modifier.entry.only_has_this_entry"));
        if (needFreshValue != 0)
            list.add(Component.translatable("modifier.entry.need_fresh_value").append(String.valueOf(needFreshValue)));
        if (!OnlyTags.isEmpty())
            list.add(Component.translatable("modifier.entry.only_tags").append(String.join(",", OnlyTags)));
        if (!OnlyItems.isEmpty())
            list.add(Component.translatable("modifier.entry.only_items").append(String.join(",", OnlyItems)));
        if (isRandom && RandomNum != 0)
            list.add(Component.translatable("modifier.entry.is_random").append(String.valueOf(RandomNum)));


        list.add(Component.translatable("modifier.entry.attribute_gather"));
        WeightedUtil<String> weightUtil = new WeightedUtil<>(
                attriGether.stream()
                        .collect(Collectors.toMap(
                                k -> k.getModifier().getName(),
                                ModifierAttriGether::getWeight,
                                (oldValue, newValue) -> newValue // 这里定义如何处理键冲突，例如这里选择保留旧值
                        ))
        );
        for (ModifierAttriGether attriGether1 : attriGether) {
            list.add(Component.literal(" §7¦ §r").append(attriGether1.GenerateTooltip(isRandom)).append(isRandom ? " §9(" + df.format(weightUtil.getProbability(attriGether1.getModifier().getName()) * 100) + "%)" : ""));
        }
        boolean hasSuit = false;

        for (ExSuit suit : ExSuitHandle.LoadExSuit.values().stream().filter(exSuit -> exSuit.entry.contains(this))
                .toList()) {
            if (suit.visible) {
                if (!hasSuit) {
                    list.add(Component.translatable("modifier.entry.suit"));
                    hasSuit = true;
                }
                list.add(Component.literal(" §7¦ §r").append(Component.translatable("modifier.entry.suit." + suit.id)));
            }
        }
        boolean hasTyoe = false;

        if (types.size() > 1) {

            for (
                    ItemType type : types
            ) {
                if (!hasTyoe) {
                    list.add(Component.translatable("modifier.entry.type"));
                    hasTyoe = true;
                }
                list.add(Component.literal(" §7¦ §r").append(Component.translatable("modifier.entry.type").append(type.name())));

            }

        }else {
            if (!types.isEmpty()) list.add(Component.translatable("modifier.entry.type").append(types.get(0).name()));

        }
        return list;
    }
}
