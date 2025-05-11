
package net.exmo.exmodifier.util.gether;

import net.exmo.exmodifier.util.ExUtil;
import net.exmo.exmodifier.util.exSerialize.ExSerialize;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.ForgeRegistries;

import java.text.DecimalFormat;
import java.util.UUID;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.percentAtr;
import static net.minecraft.world.item.ItemStack.ATTRIBUTE_MODIFIER_FORMAT;

public class AttrGether {
	public Attribute attribute;
	public AttributeModifier attributeModifier;
	public static final ExSerialize<AttrGether> exSerialize = ExSerialize.create(()->new AttrGether(null,new AttributeModifier(UUID.randomUUID(),"",0, AttributeModifier.Operation.ADDITION)))
			.addStringField("attribute",attrGether ->
				ExUtil.getAttributeID(attrGether.attribute)
			,(k,v)->{
				k.attribute = ExUtil.getAttribute(v);
			})
			.addStringField("modifier_name",attrGether -> attrGether.attributeModifier.getName(),(k,v)->{
				k.attributeModifier = new AttributeModifier(k.attributeModifier.getId(),v,0, AttributeModifier.Operation.ADDITION);
			})
			.addFloatField("modifier_amount",attrGether -> (float) attrGether.attributeModifier.getAmount(),(k, v)->{
				k.attributeModifier = new AttributeModifier(k.attributeModifier.getId(),k.attributeModifier.getName(),v.doubleValue(), k.attributeModifier.getOperation());
			})
			.addStringField("modifier_operation",attrGether -> attrGether.attributeModifier.getOperation().name(),(k,v)->{
				k.attributeModifier = new AttributeModifier(k.attributeModifier.getId(),k.attributeModifier.getName(),0, AttributeModifier.Operation.valueOf(v));
			})
			.addStringField("modifier_uuid",attrGether -> attrGether.attributeModifier.getId().toString(),(k,v)->{
				k.attributeModifier = new AttributeModifier(UUID.fromString(v),k.attributeModifier.getName(),k.attributeModifier.getAmount(), k.attributeModifier.getOperation());
			})
			;

	public AttrGether(Attribute attribute, AttributeModifier attributeModifier) {
		this.attribute = attribute;
		this.attributeModifier = attributeModifier;
	}
	public MutableComponent generateTooltipBase()
	{

		AttributeModifier attributemodifier = this.getModifier();
		Attribute attribute = this.getAttribute();
		if (attribute == null)return Component.translatable("exmodifier.tooltip.error1");
		if (attributemodifier ==null)return Component.translatable("exmodifier.tooltip.error2");
		if (attributemodifier.getOperation() == null)return Component.translatable("exmodifier.tooltip.error0");

		//    if (modifierAttriGether.slot==null)continue;
		//  Exmodifier.LOGGER.info(modifierAttriGether.getAttribute().getDescriptionId());
		//   if (!itemStack.getAttributeModifiersAffix(modifierAttriGether.slot).containsEntry(attribute, attributemodifier))continue;
		double d0 = attributemodifier.getAmount();
		boolean flag = false;
		String percent = "";
		double d1;
		if (attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL  &&!percentAtr.contains(ExUtil.getAttributeID(attribute).toString())) {
			if ((attribute).equals(Attributes.KNOCKBACK_RESISTANCE)) {
				d1 = d0 * 10.0;
			} else {
				d1 = d0;
			}
		} else {
			d1 = d0 * 100.0;
		}
		String amouta2 = "";
		if (percentAtr.contains(ExUtil.getAttributeID(attribute).toString())){
			percent = "%";
			DecimalFormat df = new DecimalFormat("#.####");
			amouta2 = df.format(attributemodifier.getAmount() * 100);
			if (this.attribute.getDescriptionId().length() >=4){
				if (ExUtil.getAttributeID(attribute).toString().startsWith("twtp") ||ExUtil.getAttributeID(attribute).toString().startsWith("isfix") ) {
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

	public AttributeModifier getModifier() {
		return attributeModifier;
	}
	public Attribute getAttribute() {
		return attribute;
	}

	public AttrGether setModifierUUID(UUID uuid) {
		this.attributeModifier = new AttributeModifier(uuid, attributeModifier.getName(), attributeModifier.getAmount(), attributeModifier.getOperation());
		return this;
	}
}
