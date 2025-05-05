package net.exmo.exmodifier.util.gether;

import net.exmo.exmodifier.util.ExAttributeModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.ForgeRegistries;

import java.text.DecimalFormat;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.percentAtr;
import static net.minecraft.world.item.ItemStack.ATTRIBUTE_MODIFIER_FORMAT;

public class AttriGether {
    public EquipmentSlot slot = null;
    public boolean IsAutoEquipmentSlot = false;
    public Attribute attribute;
    public ExAttributeModifier modifier;
    public AttriGether(Attribute attribute, ExAttributeModifier modifier,EquipmentSlot slot) {
        this.attribute = attribute;
        this.modifier = modifier;
        this.slot =     slot;
    }
//    public AttriGether setModifierUUID(UUID uuid){
//        this.modifier = new ExAttributeModifier(uuid, modifier.getName(), modifier.getAmount(), modifier.getOperation());
//        return this;
//    }
    public CompoundTag toNBT1() {
        CompoundTag tag = new CompoundTag();
        String sslot = "";
        if (slot != null)sslot = slot.getName();
        tag.putString("slot", sslot);
        tag.putBoolean("IsAutoEquipmentSlot", IsAutoEquipmentSlot);
        ResourceLocation key = ForgeRegistries.ATTRIBUTES.getKey(attribute);
        if (key!=null) {
            tag.putString("attribute", key.toString());
        }
        //if (modifier.getId()!=null)tag.putString("modifierId", modifier.getId().toString());
        tag.putString("modifierName", modifier.getName());
        tag.putDouble("modifierAmount", modifier.getAmount());
        if (modifier.getOperation()!=null)tag.putInt("modifierOperation", modifier.getOperation().toValue());

        return tag;
    }

    public static AttriGether fromNBT1(CompoundTag tag) {
        AttriGether attriGether = new AttriGether(null, null, null);
        String slot1 = tag.getString("slot");
        if (slot1.isEmpty()) attriGether.slot=null;
        else attriGether.slot = EquipmentSlot.byName(slot1);
        attriGether.IsAutoEquipmentSlot = tag.getBoolean("IsAutoEquipmentSlot");

        String attributeId = tag.getString("attribute");
        String modifierId1 = tag.getString("modifierId");
        attriGether.attribute = ForgeRegistries.ATTRIBUTES.getValue(ResourceLocation.tryParse(attributeId));
//        UUID modifierId=null;
//        if (!modifierId1.isEmpty()){
//            modifierId = UUID.fromString(modifierId1);
//        }else{
//            modifierId = UUID.randomUUID();
//        }
        String modifierName = tag.getString("modifierName");
        double modifierAmount = tag.getDouble("modifierAmount");
        net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation modifierOperation = net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.fromValue(tag.getInt("modifierOperation"));

        attriGether.modifier = new ExAttributeModifier( modifierName, modifierAmount, modifierOperation);

        return attriGether;
    }

    public AttriGether(Attribute attribute, ExAttributeModifier modifier) {
        this.attribute = attribute;
        this.modifier = modifier;
    }
    public AttriGether(Attribute attribute, ExAttributeModifier modifier,boolean isAutoEquipmentSlot) {
        this.attribute = attribute;
        this.modifier = modifier;
        this.IsAutoEquipmentSlot = isAutoEquipmentSlot;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public ExAttributeModifier getModifier() {
        return modifier;
    }
    public MutableComponent generateTooltipBase()
    {

        ExAttributeModifier attributemodifier = this.getModifier();
        Attribute attribute = this.getAttribute();
        if (attribute == null)return Component.translatable("exmodifier.tooltip.error1");
        if (attributemodifier ==null)return Component.translatable("exmodifier.tooltip.error2");
        //    if (modifierAttriGether.slot==null)continue;
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
            if (this.attribute.getDescriptionId().length() >=4){
                if (ForgeRegistries.ATTRIBUTES.getKey(attribute).toString().startsWith("twtp") ||ForgeRegistries.ATTRIBUTES.getKey(attribute).toString().startsWith("isfix") ) {
                    amouta2 = df.format(attributemodifier.getAmount()) ;
                }
            }
        }

        if (flag) {
            return ((Component.literal(" ")).append(Component.translatable("attribute.modifier.equals." + attributemodifier.getOperation().toValue(), new Object[]{ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(attribute.getDescriptionId())})).withStyle(ChatFormatting.DARK_GREEN));
        } else if (d0 > 0.0) {
            if (percent.equals("%")) return (Component.translatable("add").append(amouta2).append(percent).append(" ").append(Component.translatable(attribute.getDescriptionId())).withStyle(ChatFormatting.BLUE));
            else return ((Component.translatable("attribute.modifier.plus." + attributemodifier.getOperation().toValue(), new Object[]{ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(attribute.getDescriptionId())})).withStyle(ChatFormatting.BLUE));
        } else if (d0 < 0.0) {
            d1 *= -1.0;
            if (percent.equals("%")) return (Component.translatable("subtract").append(amouta2).append(percent).append(" ").append(Component.translatable(attribute.getDescriptionId())).withStyle(ChatFormatting.RED));
            else  return ((Component.translatable("attribute.modifier.take." + attributemodifier.getOperation().toValue(), new Object[]{ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(attribute.getDescriptionId())})).withStyle(ChatFormatting.RED));
        }
        return Component.translatable("exmodifier.tooltip.error3");
    }
}
