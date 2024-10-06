package net.exmo.exmodifier.compat;

import net.exmo.exmodifier.content.event.MainEvent;
import net.exmo.exmodifier.util.AttriGether;
import net.exmo.exmodifier.util.ItemAttrUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber
public class weponlevelingCompat {
    private static final UUID AttackDamageUUID = UUID.fromString("867fad31-78e1-4a62-8d10-a9e3fd78ef0b");
    private static  AttriGether AttackDamage(double amout) {
      return   new AttriGether(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, new AttributeModifier(AttackDamageUUID, "WeaponLeveling Attack Damage", amout, AttributeModifier.Operation.ADDITION), EquipmentSlot.MAINHAND);
    }

    @SubscribeEvent
    public static void atChange(LivingEquipmentChangeEvent event) {
            if (ModList.get().isLoaded("weaponleveling")) {
               // Exmodifier.LOGGER.info("Load ca");
                if (event.getEntity() instanceof Player player) {
                    ItemStack stack  = event.getTo();
                    if (stack.getItem() instanceof ArmorItem) return;
                    if (stack.getItem() instanceof BowItem) return;
                    if (MainEvent.CommonEvent.hasAttr(stack)){
                        if (stack.getTag() ==null)return;
                    if (stack.getTag().contains("level")) {
                        if (stack.getTag().getInt("level") != stack.getTag().getInt("Oldlevel")) {
                            AttriGether attriGether1 = AttackDamage(((stack.getTag().getInt("level"))-1)*0.5);

                            //       Exmodifier.LOGGER.info("WeaponLeveling Compat");
                            ItemAttrUtil.removeAttributeModifier(stack, attriGether1.attribute, attriGether1.modifier, attriGether1.slot);
                            AttriGether attriGether = AttackDamage((stack.getTag().getInt("level"))*0.5);
                            stack.getTag().putInt("Oldlevel", stack.getTag().getInt("level"));

                            ItemAttrUtil.addItemAttributeModifier2(stack, attriGether.attribute, attriGether.modifier, attriGether.slot);
                        }
                    }
                    }
                }
            }
        }

}
