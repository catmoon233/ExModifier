package net.exmo.exmodifier.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.List;

import static net.exmo.exmodifier.util.AttributeCuriosHandle.attributeCurios;

public class
AttributeCurios extends Item {
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

	public List<Component> extraTooltip(List <Component> extraTooltip, ItemStack itemStack){
		return extraTooltip;
	};
	public void skill(Player player){
		if (!this.isp) player.getCooldowns().addCooldown(this, this.CoolDown);

	}


}
