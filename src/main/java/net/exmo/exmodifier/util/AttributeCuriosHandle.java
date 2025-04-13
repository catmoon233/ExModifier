
package net.exmo.exmodifier.util;


import net.exmo.exmodifier.Config;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.helper.ModifierEntryHelper;
import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.events.ExCuriosAttributeTooltipEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
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
    public static class ForgeBusEvents {
        private static void setDeltaMovement(Player player,Vec3 vec3){
            player.move(net.minecraft.world.entity.MoverType.SELF, vec3);
        }


        public static void RenderCustomCuriosAttributes(ItemTooltipEvent event){
            if (event.getItemStack().getItem() instanceof AttributeCurios attributeCurios){
             //   if (attributeCurios.HideAttribute)return;
            }
            List<AttrGether> attributeModifiers = CuriosUtil.getAttributeModifiersAffix(event.getItemStack());
            List<Component> adds = new ArrayList<>();
            ExCuriosAttributeTooltipEvent event1 = new ExCuriosAttributeTooltipEvent(event.getEntity(), event.getItemStack(), event.getToolTip(), adds,attributeModifiers);
            MinecraftForge.EVENT_BUS.post(event1);
            if (!event1.attributeModifiers.isEmpty()){
                adds.add(Component.literal(""));
                adds.add(Component.translatable("attribute.curios.tooltip").withStyle(ChatFormatting.GOLD));


                for (AttrGether attrGether : event1.attributeModifiers) {
                    adds.add(attrGether.generateTooltipBase());
                }
                List<Component> tooltipADD = event1.tooltipADD;
                if (tooltipADD.size()>2) event.getToolTip().addAll(tooltipADD);

            }
        }

        @SubscribeEvent
        public static void exceptModifierAttributeDisplay(ExCuriosAttributeTooltipEvent event){
            boolean foldFlag = Config.entryFold && Screen.hasShiftDown();
            if (foldFlag ||!Config.entryFold) {
                List<ModifierEntry> modifierEntriesB = ModifierEntryHelper.of(event.itemStack).getModifierEntriesB();
                for (ModifierEntry modifierEntry : modifierEntriesB) {
                    for (ModifierAttriGether modifierAttriGether : modifierEntry.attriGether) {
                        event.attributeModifiers.removeIf(e -> e.attributeModifier.getName().equals(modifierAttriGether.modifier.getName()));
                    }
                }
            }
            }

        @SubscribeEvent
        public static void OnCurioChange2(ExCuriosAttributeTooltipEvent event) {
//            ModifierEntryHelper modifierEntryHelper = ModifierEntryHelper.of(event.itemStack);
//            for (var entry : modifierEntryHelper.getModifierEntriesB()) {
//                for (var attrGether : entry.attriGether) {
//
//                }
//            }
        }

        @SubscribeEvent
        public static void RenderTooltips(ItemTooltipEvent event) {
            ItemStack itemStack = event.getItemStack();
            if (itemStack.getItem() instanceof AttributeCurios a ) {
                if (!a.extraTooltip(new ArrayList<>(), itemStack).isEmpty()){
                    event.getToolTip().add(Component.literal(""));
                    if (!Screen.hasShiftDown()){
                        event.getToolTip().add(Component.translatable("attribute.curios.tooltip.shift").withStyle(ChatFormatting.GOLD));

                    }else {
                        event.getToolTip().addAll(a.extraTooltip(new ArrayList<>(), itemStack));
                    }
               //     event.getToolTip().add(Component.literal(""));
                }
                if (a.CoolDown !=0){
                    event.getToolTip().add(Component.translatable("attribute.curios.cooldown").append(Component.literal("§l§e " + a.CoolDown*0.05+ " " ).append(Component.translatable("attribute.curios.cooldown.second"))));
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

                if (!a.attrGethers.isEmpty() ||!a.HideAttribute ) {
                    if (a.customTooltips.isEmpty() )  event.getToolTip().add(Component.translatable("attribute.curios.tooltip").withStyle(ChatFormatting.GOLD));

                    for (AttrGether attrGether : a.attrGethers) {
                        if (attrGether != null) {
                            MutableComponent e = attrGether.generateTooltipBase();
                            if (e.equals(Component.translatable("exmodifier.tooltip.error3")))continue;
                            event.getToolTip().add(e);
//                            String as = "+";
//                            String as1 = "";
//                            String amouts = "";
//
//                            if (attrGether.attributeModifier.getAmount() < 0) as = "-";
//                            double amout = attrGether.attributeModifier.getAmount();
//                            if (attrGether.attributeModifier.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL)as+="%";
//
////                            if (attrGether.attribute instanceof RangedAttribute&&attrGether.attribute != TaskexModAttributes.DEFENSE.get() || attrGether.attributeModifier.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE || attrGether.attribute == RottenKeTimeModAttributes.FREEZINGPROBABILITY.get()||attrGether.attribute == RottenKeTimeModAttributes.DODGE.get()) {
////                                amout = amout * 100;
////                                as1 = "%";
////                            }
//
//                            amouts = String.valueOf(amout);
//                            if ((amout - (int) amout == 0)) amouts = String.valueOf((int) amout);
//                            event.getToolTip().add(Component.literal("\u00a79" + as + amouts + as1 + " ").append(Component.translatable((attrGether.attribute.getDescriptionId()))).withStyle(ChatFormatting.BLUE));
//

                        }
                    }

                }
                if (a.customTooltips !=null){
                    for (Component component : a.customTooltips) {
                        event.getToolTip().add(component);
                    }
                }
                RenderCustomCuriosAttributes(event);


        }else RenderCustomCuriosAttributes(event);
            }


        @SubscribeEvent
        public static void OnCurioChange(CurioChangeEvent event) {

            handleCurios(event);

        }


    }

    public static void handleCurios(CurioChangeEvent event) {
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
                    Exmodifier.LOGGER.debug("attributeModifiers0:" + attrGether.attributeModifier.getAmount());
                }
            }
        }
        if (!event.getTo().isEmpty()) {
            attributeModifiers = CuriosUtil.getAttributeModifiersAffix(event.getTo());
            if (!attributeModifiers.isEmpty()) {
                for (AttrGether attrGether : attributeModifiers) {
                    EntityAttrUtil.entityAddAttrTF(attrGether, (LivingEntity) event.getEntity(), EntityAttrUtil.WearOrTake.WEAR);
                    Exmodifier.LOGGER.debug("attributeModifiers1:" + attrGether.attributeModifier.getAmount());
                }
            }
        }
    }
}
