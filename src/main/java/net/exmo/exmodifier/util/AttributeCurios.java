/**
 * The code of this mod element is always locked.
 *
 * You can register new events in this class too.
 *
 * If you want to make a plain independent class, create it using
 * Project Browser -> New... and make sure to make the class
 * outside net.exmo.rottenketime as this package is managed by MCreator.
 *
 * If you change workspace package, modid or prefix, you will need
 * to manually adapt this file to these changes or remake it.
 *
 * This class will be added in the mod root package.
*/
package net.exmo.exmodifier.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.List;

import static net.exmo.exmodifier.util.AttributeCuriosHandle.attributeCurios;

public class    AttributeCurios extends Item {
	public boolean hasSkill = false;
 	List<AttrGether> attrGethers ;
	public int Star_rating = 0;
	public int CoolDown = 0;
	public boolean isp = false;
	public static boolean isWear(Item curios,Player player){
		return   (CuriosApi.getCuriosHelper().findEquippedCurio(curios, player).isPresent());

		}
	public List<Component> customTooltips = new ArrayList<>();
	public AttributeCurios(Properties p_41383_, List<AttrGether> attrGethers) {
		super(p_41383_);
		this.attrGethers = attrGethers;
		attributeCurios.add(this);

	}

	public void skill(Player player){
		if (!this.isp) player.getCooldowns().addCooldown(this, this.CoolDown);

	}


}
