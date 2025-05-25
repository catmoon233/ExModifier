package net.exmo.exmodifier.content.modifier;

import com.google.gson.JsonElement;
import net.exmo.exmodifier.content.selected.ModifierItemSelector;
import net.exmo.exmodifier.content.specialEffects.SpecialEffect;
import net.exmo.exmodifier.content.suit.ExSuit;
import net.exmo.exmodifier.content.suit.ExSuitHandle;
import net.exmo.exmodifier.content.type.ExType;
import net.exmo.exmodifier.content.type.ExTypeHandle;
import net.exmo.exmodifier.content.type.ItemType;
import net.exmo.exmodifier.util.*;
import net.exmo.exmodifier.util.exSerialize.ExSerClass;
import net.exmo.exmodifier.util.exSerialize.ExSerialize;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.exmo.exmodifier.Exmodifier.MODID;
import static net.exmo.exmodifier.content.modifier.EntryItem.CommonEvent.df;
import static net.exmo.exmodifier.content.modifier.ModifierHandle.percentAtr;
import static net.minecraft.world.item.ItemStack.ATTRIBUTE_MODIFIER_FORMAT;
import static net.exmo.exmodifier.content.type.ExType.*;
//@SerialClass
public class ModifierEntry implements SelectorClass<ModifierItemSelector<ModifierEntry>> , ExSerClass<ModifierEntry> {

    public static ExSerialize<ModifierEntry> ExSer = ExSerialize.create(ModifierEntry::new)
            .addStringListField("types",e->e.types.stream().map(ItemType::name).toList(),((modifierEntry, strings) -> modifierEntry.types = strings.stream().map(ModifierEntry::StringToType).toList())).
            addStringField("id",e->e.id,((modifierEntry, s) -> modifierEntry.id = s))
            .addStringField("type",(modifierEntry, s) -> modifierEntry.types.add(StringToType(s))).onlyRead()
            .addBooleanField("cantSelect",e->e.cantSelect,((modifierEntry, aBoolean) -> modifierEntry.cantSelect = aBoolean))
            .addBooleanField("isRandom",e->e.isRandom,((modifierEntry, aBoolean) -> modifierEntry.isRandom = aBoolean))
            .addStringField("icon",e->e.icon,((modifierEntry, s) -> modifierEntry.icon = s))
            .addStringListField("slots",e->e.Slots,((modifierEntry, strings) -> modifierEntry.Slots = strings)).
            addStringListField("specialTags",e->e.specialTags,((modifierEntry, strings) -> modifierEntry.specialTags = strings)).
            addStringField("localDescription",e->e.localDescription,((modifierEntry, s) -> modifierEntry.localDescription = s)).
            addIntField("maxLevel",e->e.maxLevel,((modifierEntry, integer) -> modifierEntry.maxLevel = integer))
            .addBooleanField("displayNameInItemName",e->e.displayNameInItemName,((modifierEntry, aBoolean) -> modifierEntry.displayNameInItemName = aBoolean))
            .addStringField("curiosType",e->e.curiosType,((modifierEntry, s) -> modifierEntry.curiosType = s))
            .addBooleanField("OnlyHasThisEntry",e->e.OnlyHasThisEntry,((modifierEntry, aBoolean) -> modifierEntry.OnlyHasThisEntry = aBoolean))
            .addFloatField("needFreshValue",e->e.needFreshValue,((modifierEntry, aFloat) -> modifierEntry.needFreshValue = aFloat))
            .addFloatField("weight",e->e.weight,((modifierEntry, aFloat) -> modifierEntry.weight = aFloat))
            .addStringListField("tags",e->e.tags.stream().map(TagKey::location).map(ResourceLocation::toString).toList(),((modifierEntry, strings) -> modifierEntry.tags = strings.stream().map(e->ExUtil.createOrGetModifierTagKey(new ResourceLocation(e))).toList()))
            .addStringField("Expression",e->e.Expression,((modifierEntry, s) -> modifierEntry.Expression = s))
            .addStringListField("exsuit",ModifierEntry::getExsuit,ModifierEntry::setExsuit)
            .addSubclass(modifierEntry -> modifierEntry.modifierItemSelector, (modifierEntry, modifierItemSelector) -> modifierEntry.modifierItemSelector = (ModifierItemSelector<ModifierEntry>) modifierItemSelector,ModifierItemSelector.ExSer)
            .addJsonObjectList("attriGethers",
                    modifierEntry -> ModifierAttriGether.ExSer.toJson(modifierEntry.attriGether).asList().stream().map(JsonElement::getAsJsonObject).toList(),
                    (modifierEntry, jsonObjects) -> {
                        modifierEntry.attriGether = jsonObjects.stream()
                                .flatMap(jsonObject -> ModifierAttriGether.ExSer.fromJson(jsonObject).stream()) // 关键修改点
                                .toList();
                    })
            .addJsonObjectList("attriGethers",
                    modifierEntry -> ModifierAttriGether.ExSer.toJson(modifierEntry.attriGether).asList().stream().map(JsonElement::getAsJsonObject).toList(),
                    (modifierEntry, jsonObjects) -> {
                        modifierEntry.attriGether = jsonObjects.stream()
                                .flatMap(jsonObject -> ModifierAttriGether.ExSer.fromJson(jsonObject).stream()) // 关键修改点
                                .toList();
                    }).onlyRead();
    public static final ResourceKey<Registry<ModifierEntry>> MODIFIER_KEY = ResourceKey.createRegistryKey( ResourceLocation.tryBuild(MODID,"modifier_entry"));
    public static final TagKey<ModifierEntry> defaultTag = TagKey.create(MODIFIER_KEY, new ResourceLocation(MODID, "refresh_default"));
    public Map<String, String> setting = new HashMap<>();
    public float weight;
    public boolean cantSelect = false;
    public boolean isRandom = true;
    public String icon = "";
    public String source = "";
    public List<String> entityTypes = new ArrayList<>();
    public List<String> exsuit = new ArrayList<>();
    public List<TagKey<ModifierEntry>> tags = new ArrayList<>();
    public boolean OnlyHasThisEntry = false;
    public String localDescription = "";
    public int maxLevel = 1;
    public List<String> Slots = new ArrayList<>();
    public List<ItemType> types = new ArrayList<>();
    public boolean isCuriosEntry = false;
    public float needFreshValue = 0;
    public String curiosType = "";
    public boolean displayNameInItemName = false;
    public String id;
    public String Expression = ""; //todo 这个没用
    public int RandomNum = 0;
    public ModifierItemSelector<ModifierEntry> modifierItemSelector = new ModifierItemSelector<ModifierEntry>();
    public List<ModifierAttriGether> attriGether = new java.util.ArrayList<>();
    public  List<String> specialTags = new ArrayList<>();


    public List<String> getExsuit() {
        return exsuit;
    }

    public ModifierEntry setExsuit(List<String> exsuit) {
        this.exsuit = exsuit;
        if (ModifierHandle.EEMatchQueue.containsKey(this.id)) {
            ModifierHandle.EEMatchQueue.get(this.id).addAll(exsuit);
        }else ModifierHandle.EEMatchQueue.put(this.id, exsuit);
        return this;
    }

    public ModifierEntry(String id) {
        this.id = id;
    }

    public ModifierEntry() {
    }

    @Override
    public ModifierItemSelector<ModifierEntry>  getModifierItemSelector() {
        return modifierItemSelector;
    }

    public boolean containItemType(ItemType type){
        return types.stream().map(ItemType::name).toList().contains(type.name());
    }

    public ModifierInstant toInstant() {
        return ModifierInstant.of(this);
    }

    public ModifierInstant toInstant(int level) {
        return ModifierInstant.of(this, level);
    }
    public static boolean containItemTypes(ItemStack stack, List<ItemType> types) {
        for (ItemType type : types) {
            if (type.compare(stack))return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "ModifierEntry{" +
                "setting=" + setting +
                ", weight=" + weight +
                ", cantSelect=" + cantSelect +
                ", isRandom=" + isRandom +
                ", icon='" + icon + '\'' +
                ", source='" + source + '\'' +
                ", entityTypes=" + entityTypes +
                ", exsuit=" + exsuit +
                ", tags=" + tags +
                ", OnlyHasThisEntry=" + OnlyHasThisEntry +
                ", localDescription='" + localDescription + '\'' +
                ", maxLevel=" + maxLevel +
                ", Slots=" + Slots +
                ", types=" + types +
                ", isCuriosEntry=" + isCuriosEntry +
                ", needFreshValue=" + needFreshValue +
                ", curiosType='" + curiosType + '\'' +
                ", displayNameInItemName=" + displayNameInItemName +
                ", id='" + id + '\'' +
                ", Expression='" + Expression + '\'' +
                ", RandomNum=" + RandomNum +
                ", modifierItemSelector=" + modifierItemSelector +
                ", attriGether=" + attriGether +
                ", specialTags=" + specialTags +
                '}';
    }

    public String getDescriptionId(){
        return  "modifier.entry." + id.substring(2);
    }
    public static String getDescriptionId(String id){
        return  "modifier.entry." + id.substring(2);
    }




//    public static boolean containItemTypes(ItemStack item, List<ItemType> onlyTypes) {
//        for (ItemType type : onlyTypes){
//            if (containItemType(item, type)) return true;
//        }
//        return false;
//    }


    public static boolean containItemType(ItemStack stack, ItemType type) {
        return type.compare(stack);
    }

    public static List<Component> GenerateTooltip(List<ModifierAttriGether> attriGethers, ItemStack itemStack) {
        List<Component> tooltips = new java.util.ArrayList<>();
        for (ModifierAttriGether modifierAttriGether : attriGethers) {
            ExAttributeModifier attributemodifier = modifierAttriGether.getModifier();
            Attribute attribute = modifierAttriGether.getAttribute();
            if (attribute == null) continue;
            if (attributemodifier == null) continue;
            //    if (modifierAttriGether.slot==null)continue;
//            EquipmentSlot slot = modifierAttriGether.slot;
//            if (modifierAttriGether.IsAutoEquipmentSlot){
//                slot = ModifierEntry.TypeToEquipmentSlot(ModifierEntry.getType(itemStack));
//            }
            if (!ItemAttrUtil.hasAttributeModifierCompoundTagNoAmount(itemStack, attribute, attributemodifier, modifierAttriGether.slot))
                continue;
            //  Exmodifier.LOGGER.info(modifierAttriGether.getAttribute().getDescriptionId());
            //   if (!itemStack.getAttributeModifiers(modifierAttriGether.slot).containsEntry(attribute, attributemodifier))continue;
            double d0 = attributemodifier.getAmount();
            boolean flag = false;
            String percent = "";
            double d1;
            if (attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL && !percentAtr.contains(ExUtil.getAttributeID(attribute).toString())) {
                if ((attribute).equals(Attributes.KNOCKBACK_RESISTANCE)) {
                    d1 = d0 * 10.0;
                } else {
                    d1 = d0;
                }
            } else {
                d1 = d0 * 100.0;
            }
            String amouta2 = "";
            if (percentAtr.contains(ExUtil.getAttributeID(attribute).toString())) {
                percent = "%";
                DecimalFormat df = new DecimalFormat("#.####");
                amouta2 = df.format(attributemodifier.getAmount() * 100);
                if (modifierAttriGether.attribute.getDescriptionId().length() >= 4) {
                    if (ExUtil.getAttributeID(attribute).toString().startsWith("twtp") || ExUtil.getAttributeID(attribute).toString().startsWith("isfix")) {
                        amouta2 = df.format(attributemodifier.getAmount());
                    }
                }
            }

            if (flag) {
                tooltips.add((Component.literal(" ")).append(Component.translatable("attribute.modifier.equals." + attributemodifier.getOperation().toValue(), new Object[]{ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(attribute.getDescriptionId())})).withStyle(ChatFormatting.DARK_GREEN));
            } else if (d0 > 0.0) {
                if (percent.equals("%"))
                    tooltips.add(Component.translatable("add").append(amouta2).append(percent).append(" ").append(Component.translatable(attribute.getDescriptionId())).withStyle(ChatFormatting.BLUE));
                else
                    tooltips.add((Component.translatable("attribute.modifier.plus." + attributemodifier.getOperation().toValue(), new Object[]{ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(attribute.getDescriptionId())})).withStyle(ChatFormatting.BLUE));
            } else if (d0 < 0.0) {
                d1 *= -1.0;
                if (percent.equals("%"))
                    tooltips.add(Component.translatable("subtract").append(amouta2).append(percent).append(" ").append(Component.translatable(attribute.getDescriptionId())).withStyle(ChatFormatting.RED));
                else
                    tooltips.add((Component.translatable("attribute.modifier.take." + attributemodifier.getOperation().toValue(), new Object[]{ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(attribute.getDescriptionId())})).withStyle(ChatFormatting.RED));
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
        for (var v : ExTypeHandle.itemTypes.values()) {
            if (v.name().equalsIgnoreCase(type)) return v;
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
        for (var v : ExTypeHandle.itemTypes.values()) {
            if (v.compare(stack)) {
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

    public static ItemType findTypeFormEntry(ModifierEntry modifierEntry) {
        for (ItemType type1 : ExTypeHandle.itemTypes.values()) {
            if (type1.toString().substring(0, 2).equals(modifierEntry.id.substring(0, 2))) return type1;
        }
        return ExType.UNKNOWN.get();
    }


    public float getWeight() {
        return weight;
    }

    public String getId() {
        return id;
    }

    @Override
    public ResourceLocation getResId() {
        return ResourceLocation.isValidNamespace(getId()) ? new ResourceLocation(getId()) : new ResourceLocation(MODID, getId());
    }

    public boolean hasSpecialEffect(SpecialEffect specialEffect) {
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
        var onlyTags = getModifierItemSelector().getOnlyTags();
        if(!onlyTags.isEmpty())  list.add(Component.translatable("modifier.entry.only_tags").append(String.join(",", onlyTags)));
        var onlyItems = getModifierItemSelector().getOnlyItems();
        if(!onlyItems.isEmpty())  list.add(Component.translatable("modifier.entry.only_items").append(String.join(",", onlyItems)));

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

            if (this.types.size() > 1) {
                list.add(Component.translatable("modifier.entry.type"));
                for (
                        ItemType type : types
                ) {
                    list.add(Component.literal(" §7¦ §r").append(type.name()));

                }

            } else {
                if (!types.isEmpty())
                    list.add(Component.translatable("modifier.entry.type").append(types.get(0).name()));

            }
            if (this.tags.size() > 1) {
                list.add(Component.translatable("modifier.entry.tag"));
                for (
                        var tag : tags
                ) {
                    list.add(Component.literal(" §7¦ §r").append(tag.toString()));

                }

            } else {
                if (!tags.isEmpty())
                    list.add(Component.translatable("modifier.entry.tag").append(tags.get(0).toString()));

            }
            list.add(Component.translatable("modifier.entry.source").append(source));
            return list;

    }

    public boolean hasDefaultTag() {
        return tags.stream().anyMatch(tagKey -> tagKey.toString().equals(defaultTag.toString()));
    }

    @Override
    public ExSerialize<ModifierEntry> getExSerialize() {
        return (ExSer);
    }
}
