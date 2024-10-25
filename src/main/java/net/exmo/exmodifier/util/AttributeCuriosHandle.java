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

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.util.event.AttrGether;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class AttributeCuriosHandle {
    public static List<AttributeCurios> attributeCurios = new ArrayList<>();

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        for (AttributeCurios attributeCurio : attributeCurios) {
            Exmodifier.LOGGER.debug("AttributeCurios Register => " + ForgeRegistries.ITEMS.getKey(attributeCurio).toString());
        }
    }

    @Mod.EventBusSubscriber
    private static class ForgeBusEvents {
        private static void setDeltaMovement(Player player,Vec3 vec3){
            player.move(net.minecraft.world.entity.MoverType.SELF, vec3);
        }


        @SubscribeEvent
        public static void RenderCustomCuriosAttributes(ItemTooltipEvent event){
            List<AttrGether> attributeModifiers = CuriosUtil.getAttributeModifiersAffix(event.getItemStack());
            if (!attributeModifiers.isEmpty()){
                event.getToolTip().add(Component.literal(""));
                event.getToolTip().add(Component.translatable("attribute.curios.tooltip").withStyle(ChatFormatting.GOLD));
                for (AttrGether attrGether : attributeModifiers) {
                    event.getToolTip().add(attrGether.generateTooltipBase());
                }
            }
        }

        @SubscribeEvent
        public static void RenderTooltips(ItemTooltipEvent event) {
            if (event.getItemStack().getItem() instanceof AttributeCurios a ) {
                if (a.CoolDown !=0){
                    event.getToolTip().add(Component.literal("attribute.curios.cooldown").append(Component.literal("§l§e " + a.CoolDown*0.05+ " " ).append(Component.literal("attribute.curios.cooldown.second"))));
                }

                if (a.Star_rating != 0) {
                    String star = "★";
                    String emptyStar = "☆";
                    String starString = "";
                    if (a.Star_rating > 5){
                        for (int i = 0; i < a.Star_rating; i++) {
                            starString = starString + star;
                        }
                        for (int i = 0; i < 10 - a.Star_rating; i++) {
                            starString = starString + emptyStar;
                        }
                    }else {
                        for (int i = 0; i < a.Star_rating; i++) {
                            starString = starString + star;
                        }
                        for (int i = 0; i < 5 - a.Star_rating; i++) {
                            starString = starString + emptyStar;
                        }
                    }
                    event.getToolTip().add(Component.literal("§l§e" + starString));
                }
                event.getToolTip().add(Component.literal(""));
            if (a.customTooltips.isEmpty() ) event.getToolTip().add(Component.translatable("attribute.curios.tooltip").withStyle(ChatFormatting.GOLD));
                if (a.attrGethers !=null) {
                    for (AttrGether attrGether : a.attrGethers) {
                        if (attrGether != null) {
                            String as = "+";
                            String as1 = "";
                            String amouts = "";

                            if (attrGether.attributeModifier.getAmount() < 0) as = "-";
                            double amout = attrGether.attributeModifier.getAmount();


//                            if (attrGether.attribute instanceof RangedAttribute&&attrGether.attribute != TaskexModAttributes.DEFENSE.get() || attrGether.attributeModifier.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE || attrGether.attribute == RottenKeTimeModAttributes.FREEZINGPROBABILITY.get()||attrGether.attribute == RottenKeTimeModAttributes.DODGE.get()) {
//                                amout = amout * 100;
//                                as1 = "%";
//                            }

                            amouts = String.valueOf(amout);
                            if ((amout - (int) amout == 0)) amouts = String.valueOf((int) amout);
                            event.getToolTip().add(Component.literal("\u00a79" + as + amouts + as1 + " ").append(Component.literal((attrGether.attribute.getDescriptionId()))).withStyle(ChatFormatting.BLUE));


                        }
                    }

                }
                if (a.customTooltips !=null){
                    for (Component component : a.customTooltips) {
                        event.getToolTip().add(component);
                    }
                }

        }
            }

        @SubscribeEvent
        public static void OnCurioChange(CurioChangeEvent event) {

            if (event.getFrom().getItem() instanceof AttributeCurios a) {
                if (attributeCurios.contains(a)) {
                    if (a.attrGethers !=null) {
                        for (AttrGether attrGether : a.attrGethers) {
                            if (attrGether!=null)      EntityAttrUtil.entityAddAttrTF(attrGether,  event.getEntity(), EntityAttrUtil.WearOrTake.TAKE);
                        }
                    }
                }
            }
            if (event.getTo().getItem() instanceof AttributeCurios a) {
                if (attributeCurios.contains(a)) {
                    if (a.attrGethers !=null) {

                        for (AttrGether attrGether : a.attrGethers) {
                            if (attrGether !=null) EntityAttrUtil.entityAddAttrTF(attrGether, event.getEntity(), EntityAttrUtil.WearOrTake.WEAR);
                        }
                    }
                }
            }
            //util
            List<AttrGether> attributeModifiers;
            if (!event.getFrom().isEmpty()) {
                attributeModifiers = CuriosUtil.getAttributeModifiersAffix(event.getFrom());
                if (!attributeModifiers.isEmpty()) {
                    for (AttrGether attrGether : attributeModifiers) {
                        EntityAttrUtil.entityAddAttrTF(attrGether, (Player) event.getEntity(), EntityAttrUtil.WearOrTake.TAKE);
                        Exmodifier.LOGGER.Logger.debug("attributeModifiers0:" + attrGether.attributeModifier.getAmount());
                    }
                }
            }
            if (!event.getTo().isEmpty()) {
                attributeModifiers = CuriosUtil.getAttributeModifiersAffix(event.getTo());
                if (!attributeModifiers.isEmpty()) {
                    for (AttrGether attrGether : attributeModifiers) {
                        EntityAttrUtil.entityAddAttrTF(attrGether, (LivingEntity) event.getEntity(), EntityAttrUtil.WearOrTake.WEAR);
                        Exmodifier.LOGGER.Logger.debug("attributeModifiers1:" + attrGether.attributeModifier.getAmount());
                    }
                }
            }

        }


    }
}
