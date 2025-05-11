package net.exmo.exmodifier.content.refine;

import net.exmo.exmodifier.Config;
import net.exmo.exmodifier.content.helper.ExHelper;
import net.exmo.exmodifier.content.helper.ItemQualityHelper;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.quality.ItemQuality;
import net.exmo.exmodifier.content.type.ItemType;
import net.exmo.exmodifier.events.ExCanRefineEvent;
import net.exmo.exmodifier.events.ExRefineMaxCountEvent;
import net.exmo.exmodifier.init.ExAttribute;
import net.exmo.exmodifier.util.ExAttributeModifier;
import net.exmo.exmodifier.util.ExUtil;
import net.exmo.exmodifier.util.ItemAttrUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraftforge.common.MinecraftForge;

import java.text.DecimalFormat;
import java.util.List;

public class RefineHelper extends ExHelper {
    public RefineHelper(ItemStack itemStack) {
        super(itemStack);
    }

    public static RefineHelper of(ItemStack item) {
        return new RefineHelper(item);
    }

    public boolean validKey() {
        return ValidMainNbt() && getMainNbt().contains("refine");
    }
    public int getRefineLevel()
    {
        if(validKey())
        {
            return getMainNbt().getInt("refine");
        }
        return 0;
    }
    public int getRefineMaxLevel(){
        ItemQualityHelper itemQualityHelper = new ItemQualityHelper(itemStack);
        int maxCount = Config.max_refine;
        if (!itemQualityHelper.getQualityEntries().isEmpty()){
            for (ItemQuality itemQuality : itemQualityHelper.getQualityEntries()) {
                if (itemQuality ==null)continue;
                maxCount = Math.max(maxCount,itemQuality.Max_Refine);
            }
        }
        ExRefineMaxCountEvent exRefineMaxCountEvent = new ExRefineMaxCountEvent(maxCount,itemStack,this,itemQualityHelper);
        MinecraftForge.EVENT_BUS.post(exRefineMaxCountEvent);
        return exRefineMaxCountEvent.getMaxCount();
    }
    public boolean canRefine(ItemStack stack){
        boolean canRefine = stack.getItem() == itemStack.getItem();
        ExCanRefineEvent exCanRefineEvent = new ExCanRefineEvent(itemStack,stack,canRefine,this);
        MinecraftForge.EVENT_BUS.post(exCanRefineEvent);
        List<ItemQuality> qualityEntries = ItemQualityHelper.of(stack).getQualityEntries();
        if ((!qualityEntries.isEmpty() && qualityEntries.stream().anyMatch(itemQuality -> itemQuality.refineNeedSameStar)) ||Config.refine_system)
            if (RefineHelper.of(stack).getRefineLevel() != getRefineLevel()) exCanRefineEvent.canRefine  = false;
        return exCanRefineEvent.canRefine && getRefineLevel()<getRefineMaxLevel();
    }
    public  ExAttributeModifier getRefineModifier(int add) {
        ItemQualityHelper itemQualityHelper = new ItemQualityHelper(itemStack);
        //总和
        float amount = itemQualityHelper.getQualityEntries().stream().map(e->e.growValue).reduce(0f, Float::sum);
       return new ExAttributeModifier("exmodifier_refine", amount ==0 ? Config.refine_effect * add : amount, AttributeModifier.Operation.MULTIPLY_BASE);
    }
    public RefineHelper addRefine(boolean addAttribute,int add){
        if(ValidMainNbt())
        {
            int refine = getMainNbt().getInt("refine");
            EquipmentSlot[] equipmentSlot = getEquipmentSlot(itemStack);
            for (var slot : ModifierEntry.getType(itemStack).stream().map(ItemType::equipmentSlot)
                    .toArray(EquipmentSlot[][]::new)){
                for (EquipmentSlot slot1 : slot) {
                    Attribute attribute = getAttribute();
                    ItemAttrUtil.removeAttributeModifierNoAmout(itemStack, attribute, getRefineModifier(0), slot1);
                    if (add>0) ItemAttrUtil.addItemAttributeModifier2(itemStack, attribute, getRefineModifier(add+refine), slot1);                }
            }

            getMainNbt().putInt("refine",refine+add);
        }
        return this;
    }

    private static Attribute getAttribute() {
        Attribute attribute = ExUtil.getAttribute(Config.refine_attribute);
        return attribute;
    }

    public RefineHelper removeRefine(boolean removeAttribute,int remove){
        if(validKey())
        {
            int refine = getMainNbt().getInt("refine");
            EquipmentSlot[] equipmentSlot = getEquipmentSlot(itemStack);
            int add = refine - remove;
            for (EquipmentSlot slot : equipmentSlot){
                ItemAttrUtil.removeAttributeModifierNoAmout(itemStack, getAttribute(), getRefineModifier(remove), slot);
                if (add>0) ItemAttrUtil.addItemAttributeModifier2(itemStack, getAttribute(), getRefineModifier(add), slot);
            }
            getMainNbt().putInt("refine", add);
        }
        return this;
    }
    public RefineHelper setRefine(int refine){
        if(ValidMainNbt())
        {
            //int oldRefine = getMainNbt().getInt("refine");
            EquipmentSlot[] equipmentSlot = getEquipmentSlot(itemStack);
            for (EquipmentSlot slot : equipmentSlot){
                ItemAttrUtil.removeAttributeModifierNoAmout(itemStack, getAttribute(), getRefineModifier(0),slot);
            }
            if (refine>0) {
                for (EquipmentSlot slot : equipmentSlot) {
                    ItemAttrUtil.addItemAttributeModifier2(itemStack, getAttribute(), getRefineModifier(refine), slot);
                }
            }
            getMainNbt().putInt("refine", refine);
        }
        return this;
    }
    public Component getRefineTooltip(boolean forceDisplay) {
        if (validKey()) {
            int refine = getMainNbt().getInt("refine");
            if (getRefineMaxLevel() > 10) {
                // 使用暗星翻译键，参数为带星号的等级
                MutableComponent mutableComponent = Component.translatable("exmodifier.refine.tooltip.star.on").append(Component.literal("X" + getRefineLevel())).withStyle(ChatFormatting.GOLD);
                mutableComponent.append(" ").append(Component.translatable("exmodifier.refine.tooltip.star.off").append(Component.literal("X" + (getRefineMaxLevel()-getRefineLevel()))).withStyle(ChatFormatting.GOLD));
                return mutableComponent;
            } else {
                int refineMaxLevel = getRefineMaxLevel();
                var toReturn = Component.empty();
                for (int i = 0; i < refine; i++)
                    toReturn.append(Component.translatable("exmodifier.refine.tooltip.star.on"));

                for (int i = refine; i < refineMaxLevel; i++)
                    toReturn.append(Component.translatable("exmodifier.refine.tooltip.star.off"));
                return toReturn.withStyle(ChatFormatting.GOLD);
            }
        }
        if (ValidMainNbt() &&( Config.alaways_display_modifier_name_under_item_name || forceDisplay)){
            if (getRefineMaxLevel()>10){
                return Component.translatable("exmodifier.refine.tooltip.star.off").append(Component.literal("X" + getRefineMaxLevel())).withStyle(ChatFormatting.GOLD);
            }else {
                var toReturn = Component.empty();
                for (int i = 0; i < getRefineMaxLevel(); i++) {
                    toReturn.append(Component.translatable("exmodifier.refine.tooltip.star.off"));
                }
                return toReturn.withStyle(ChatFormatting.GOLD);
            }
        }
        // 默认情况使用亮星翻译键
        return null;
    }
    public Component getStarUpComponent(){
        if ( getRefineLevel() > 0 ){
            ExAttributeModifier exmodifierRefine1 = ItemAttrUtil.getAttributeModifierFromNamed("exmodifier_refine", itemStack);
            if (exmodifierRefine1 == null) return null;

            double exmodifierRefine = exmodifierRefine1.getAmount() * 100;
            DecimalFormat df = new DecimalFormat("0.###"); // 创建格式化器
            String formattedValue = df.format(exmodifierRefine); // 格式化数值

            return Component.translatable("exmodifier.refine.tooltip.star.up")
                    .append(Component.literal(formattedValue+"%"))
                    .append(Component.translatable(getAttribute().getDescriptionId()))
                    .withStyle(ChatFormatting.YELLOW);
        }
        return null;
    }

}
