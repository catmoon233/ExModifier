package net.exmo.exmodifier.compat;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.util.AttriGether;
import net.exmo.exmodifier.util.ItemAttrUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;

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
                if (event.getEntity() instanceof Player player){
                    ItemStack stack = player.getMainHandItem();
                    if (stack.getItem() instanceof ArmorItem)return;
                    if (stack.getItem() instanceof BowItem)return;
                    if (stack.getOrCreateTag().getInt("level") != stack.getOrCreateTag().getInt("Oldlevel"))
                    {
                 //       Exmodifier.LOGGER.info("WeaponLeveling Compat");
                        stack.getOrCreateTag().putInt("Oldlevel", stack.getOrCreateTag().getInt("level"));
                        AttriGether attriGether = AttackDamage((stack.getOrCreateTag().getInt("level")-stack.getOrCreateTag().getInt("Oldlevel")) *0.5);
                        ItemAttrUtil.addItemAttributeModifier2(stack, attriGether.attribute, attriGether.modifier, attriGether.slot);
                    }
                }
            }
        }

}
