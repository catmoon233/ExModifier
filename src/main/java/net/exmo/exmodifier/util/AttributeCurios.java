package net.exmo.exmodifier.util;

import net.exmo.exmodifier.util.gether.AttrGether;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.tuple.Pair;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.ArrayList;
import java.util.List;

import static net.exmo.exmodifier.util.AttributeCuriosHandle.attributeCurios;

public class
AttributeCurios extends Item  implements ICurioItem {
	public boolean hasSkill = false;
	List<AttrGether> attrGethers ;
	public int Star_rating = 0;
	public boolean HideAttribute = false;
	public int CoolDown = 0;

	public boolean isp = false;
	public static boolean isWear(Item curios,Player player){
		return   CuriosUtil.isLivingWear(curios,player);
	}
	public List<Component> customTooltips = new ArrayList<>();
	public AttributeCurios(Properties p_41383_, List<AttrGether> attrGethers) {
		super(p_41383_);
		this.attrGethers = attrGethers;
		attributeCurios.add(this);

	}
	public  List<DynamicAttributeInstant> getDynamicAttributes(LivingEntity entity){
		return new ArrayList<>();
	};
	public String getAttributeID(Attribute attribute){
		return 	ExUtil.getAttributeID(attribute);
	}

	public String getAttributeID(RegistryObject<Attribute> attribute){
		return ExUtil.getAttributeID(attribute.get());
	}

	@Override
	public void curioTick(SlotContext slotContext, ItemStack stack) {
		ICurioItem.super.curioTick(slotContext, stack);
		List<DynamicAttributeInstant> dynamicAttributeInstants = getDynamicAttributes(slotContext.entity());
		for (DynamicAttributeInstant dynamicAttributeInstant : dynamicAttributeInstants) {
			if (dynamicAttributeInstant.type() == DynamicAttributeInstant.DynamicAttributeGeneratorType.TICK) {
				CuriosUtil.removeAttributeModifierAffix(stack, dynamicAttributeInstant.baseAttribute(), dynamicAttributeInstant.modifierName());
				Pair<SimpleAttrGather, Double> calculate = dynamicAttributeInstant.calculate(slotContext.entity());
				CuriosUtil.addSimpleAttributeModifierAffix(stack, calculate.getLeft(), calculate.getRight());
			}
		}

	}

	public List<Component> extraTooltip(List <Component> extraTooltip, ItemStack itemStack){
		return extraTooltip;
	};
	public void skill(Player player){
		if (!this. isp) player.getCooldowns().addCooldown(this, this.CoolDown);

	}


}
