
package net.exmo.exmodifier.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.ForgeRegistries;

import java.text.DecimalFormat;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.percentAtr;
import static net.minecraft.world.item.ItemStack.ATTRIBUTE_MODIFIER_FORMAT;

public class AttrGether {
	public Attribute attribute;
	public AttributeModifier attributeModifier;
	public AttrGether(Attribute attribute, AttributeModifier attributeModifier) {
		this.attribute = attribute;
		this.attributeModifier = attributeModifier;
	}
	public MutableComponent generateTooltipBase()
	{

		AttributeModifier attributemodifier = this.getModifier();
		Attribute attribute = this.getAttribute();
		if (attribute == null)return new TranslatableComponent("exmodifier.tooltip.error1");
		if (attributemodifier ==null)return new TranslatableComponent("exmodifier.tooltip.error2");
		if (attributemodifier.getOperation() == null)return new TranslatableComponent("exmodifier.tooltip.error0");

		//    if (modifierAttriGether.slot==null)continue;
		//  Exmodifier.LOGGER.info(modifierAttriGether.getAttribute().getDescriptionId());
		//   if (!itemStack.getAttributeModifiersAffix(modifierAttriGether.slot).containsEntry(attribute, attributemodifier))continue;
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
			return ((new TextComponent(" ")).append(new TranslatableComponent("attribute.modifier.equals." + attributemodifier.getOperation().toValue(), new Object[]{ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslatableComponent(attribute.getDescriptionId())})).withStyle(ChatFormatting.DARK_GREEN));
		} else if (d0 > 0.0) {
			if (percent.equals("%")) return (new TranslatableComponent("add").append(amouta2).append(percent).append(" ").append(new TranslatableComponent(attribute.getDescriptionId())).withStyle(ChatFormatting.BLUE));
			else return ((new TranslatableComponent("attribute.modifier.plus." + attributemodifier.getOperation().toValue(), new Object[]{ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslatableComponent(attribute.getDescriptionId())})).withStyle(ChatFormatting.BLUE));
		} else if (d0 < 0.0) {
			d1 *= -1.0;
			if (percent.equals("%")) return (new TranslatableComponent("subtract").append(amouta2).append(percent).append(" ").append(new TranslatableComponent(attribute.getDescriptionId())).withStyle(ChatFormatting.RED));
			else  return ((new TranslatableComponent("attribute.modifier.take." + attributemodifier.getOperation().toValue(), new Object[]{ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslatableComponent(attribute.getDescriptionId())})).withStyle(ChatFormatting.RED));
		}
		return new TranslatableComponent("exmodifier.tooltip.error3");
	}

	private AttributeModifier getModifier() {
		return attributeModifier;
	}
	private Attribute getAttribute() {
		return attribute;
	}
}
