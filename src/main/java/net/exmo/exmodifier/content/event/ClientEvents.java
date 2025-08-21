package net.exmo.exmodifier.content.event;

import net.exmo.exmodifier.Config;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.client.EntryItemRender;
import net.exmo.exmodifier.content.helper.ExElementHelper;
import net.exmo.exmodifier.content.helper.ItemQualityHelper;
import net.exmo.exmodifier.content.helper.ModifierEntryHelper;
import net.exmo.exmodifier.content.level.ItemLevelHandle;
import net.exmo.exmodifier.content.refine.RefineHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static net.exmo.exmodifier.Exmodifier.ENTRY_ITEM;

@Mod.EventBusSubscriber( value = Dist.CLIENT)
public class ClientEvents {
    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Exmodifier.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientEvents1 {
        @SubscribeEvent
        public static void registerItemDecoration(RegisterItemDecorationsEvent event) {
            event.register(ENTRY_ITEM.get(), new EntryItemRender());
        }
    }
//    @SubscribeEvent(priority = EventPriority.LOWEST)
//    public static void tooltipFixer(ItemTooltipEvent event) {
//        if (TooltipFixer.isFixing) {
//            TooltipFixer.isFixing = false;
//
//            ArrayList<Integer> fixList = new ArrayList<>();
//            TooltipFixer.fixList.forEach(equipmentSlot -> {
//                List<Component> toolTip = event.getToolTip();
//                for (int i = 0; i < toolTip.size(); i++) {
//                    Component component = toolTip.get(i);
//                    var contents = component.getContents();
//                    if (contents instanceof TranslatableContents m) {
//                        if (m.getKey().equals("item.modifiers." + equipmentSlot.getName())) {
//                            fixList.add(i);
//                        }
//                    }
//                }
//            });
//
//            try {
//                Collections.sort(fixList, Collections.reverseOrder());
//                for (Integer index : fixList) {
//                    if (index >= 0 && index < event.getToolTip().size()) {
//                        event.getToolTip().remove(index);
//                    }
//                }
//            } catch (Exception e) {
//                Exmodifier.LOGGER.error("fix error", e);
//            }
//
//            TooltipFixer.fixList.clear();
//        }
//    }

    @SubscribeEvent
    public static void TooltipChange(ItemTooltipEvent event) {

        ItemStack itemStack = event.getItemStack();
        if (itemStack.getTag() != null) {
            // if (!CuriosUtil.isCuriosItem(event.getItemStack())) {

            List<Component> toolTip1 = event.getToolTip();
            List<Component> toolTip = new ArrayList<>();
            boolean b = Config.entryFold && !Screen.hasShiftDown();
            Pair<List<Component>, Integer> listIntegerPair = null;
            if (!b) {
                listIntegerPair = MainEvent.CommonEvent.EntryInfoTooltip(itemStack, toolTip1, event.getEntity());
                if (!Config.ExMoTooltipRenderInRightValue) {
                    toolTip = listIntegerPair.getA();
                }
            }


            List<Component> tooo = new ArrayList<>();
            tooo.add(toolTip1.get(0));

            if (Config.refine_system){
                Component refineTooltip = RefineHelper.of(itemStack).getRefineTooltip(false);
                if (refineTooltip != null) {
                    tooo.add(refineTooltip);
                }
            }
            for (var a : ItemQualityHelper.of(itemStack).getQualityEntriesTooltip()) {
                if (a.isShowInHeadTooltip) {
                    //   tooo.set(0,a.mutableComponent.append(Component.literal(" §r")).append(toolTip1.get(0)));
                } else {
                    if (a.showModifierComponent){
                        tooo.add(Component.translatable("exmodifier.quality.modifier").append(a.mutableComponent));
                    }else tooo.add(a.mutableComponent);
                }
            }
            {
                var elementComponent = Component.empty();
                AtomicBoolean flag = new AtomicBoolean(false);
                ExElementHelper.of(itemStack).getElements().forEach(
                        exElementInstant -> {
                            if (!flag.get()) {
                                flag.set(true);
                                elementComponent.append(exElementInstant.getDesc());
                            } else {
                                elementComponent.append(" ").append(exElementInstant.getDesc());
                            }

                        }
                );

                if (flag.get()) tooo.add(elementComponent);
            }
            if (b) {
                if (!Config.entryShowUnderLevel) {
                    for (var aa : ModifierEntryHelper.of(itemStack).getModifierEntriesB()) {
                        if (aa.displayNameInItemName) continue;
                        tooo.add(Component.translatable(aa.getDescriptionId()));
                    }
                    tooo.add(Component.empty());
                }
            }
            tooo.addAll(ItemLevelHandle.genItemLevelInfo(itemStack));
            if (b) {
                if (Config.entryShowUnderLevel) {
                    for (var aa : ModifierEntryHelper.of(itemStack).getModifierEntries()) {
                        if (aa.getModifierEntry().displayNameInItemName || aa.getSlot().isPresent()) continue;
                        tooo.add(Component.translatable(aa.getModifierEntry().getDescriptionId()));
                    }

                }
            }
            for (int i = 1; i < toolTip1.size(); i++) {
                tooo.add(toolTip1.get(i));
            }
            if (!b) {
                if (Config.ExMoTooltipRenderInRightValue) {
                    for (int i = 0; i < listIntegerPair.getB(); i++) {
                        tooo.add(Component.empty());
                    }
                }
            }
            Component starUpComponent = RefineHelper.of(itemStack).getStarUpComponent();
            if (starUpComponent != null){
                tooo.add(starUpComponent);
            }
            if (tooo.size() <= 1) return;
            toolTip1.clear();
            toolTip1.addAll(tooo);
            //   }

        }
    }
}
