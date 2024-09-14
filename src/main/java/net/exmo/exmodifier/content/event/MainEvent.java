package net.exmo.exmodifier.content.event;

import com.mojang.datafixers.util.Either;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.config;
import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.content.suit.ExSuit;
import net.exmo.exmodifier.content.suit.ExSuitHandle;
import net.exmo.exmodifier.events.ExApplySuitAttrigetherEvent;
import net.exmo.exmodifier.events.ExApplySuitEffectEvent;
import net.exmo.exmodifier.events.ExSuitApplyOnChangeEvent;
import net.exmo.exmodifier.network.ExModifiervaV;
import net.exmo.exmodifier.util.CuriosUtil;
import net.exmo.exmodifier.util.EntityAttrUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.exmo.exmodifier.config.refresh_time;
import static net.exmo.exmodifier.content.modifier.ModifierHandle.CommonEvent.*;
import static net.exmo.exmodifier.content.modifier.ModifierHandle.getEntrysFromItemStack;
import static net.exmo.exmodifier.util.EntityAttrUtil.WearOrTake.TAKE;
import static net.exmo.exmodifier.util.EntityAttrUtil.WearOrTake.WEAR;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class MainEvent {
    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) throws IOException {
        ModifierHandle.readConfig();
        ExSuitHandle.readConfig();


    }

    @Mod.EventBusSubscriber
    public static class CommonEvent {
        @SubscribeEvent
        public static void CuriosChange(CurioChangeEvent event) {
            if (!(event.getEntity() instanceof Player player))return;
            if (player.getPersistentData().getBoolean("LoginGamea")) {
                player.getPersistentData().putBoolean("LoginGamea", false);
                return;
            }
            ItemStack stack = event.getTo();
            if (stack.getTag() == null || (!stack.getTag().contains("exmodifier_armor_modifier_applied"))) {
                RandomEntryCurios(stack, 0, refresh_time,"none");
            }
            if (stack.getTag() != null) {
                if (stack.getTag().contains("modifier_refresh")) {
                    if (stack.getTag().getBoolean("modifier_refresh")) {
                        stack.getOrCreateTag().putBoolean("modifier_refresh", false);
                        stack.getOrCreateTag().putInt("exmodifier_armor_modifier_applied", 0);

                        RandomEntryCurios(stack, stack.getOrCreateTag().getInt("modifier_refresh_rarity"), stack.getOrCreateTag().getInt("modifier_refresh_add"),stack.getOrCreateTag().getString("wash_item"));
                    }
                }
            }
            SuitOperate((Player) event.getEntity(), event.getTo(), event.getFrom());
        }

        @SubscribeEvent
        public static void CuriosTooltipChange(RenderTooltipEvent.GatherComponents event) {
            ItemStack stack = event.getItemStack();
            if (CuriosApi.getCuriosHelper().getCurio(stack).isPresent()) {
                boolean addf = false;
                for (ModifierEntry modifierEntry : getEntrysFromItemStack(stack)) {
                    if (stack.getOrCreateTag().getString("exmodifier_armor_modifier_applied0").equals("UNKNOWN")) {
                        event.getTooltipElements().add(Either.left(new TranslatableComponent("null")));
                        event.getTooltipElements().add(Either.left(new TranslatableComponent("modifiler.entry.UNKNOWN")));
                    } else {
                        if (!addf) {
                            addf = true;
                            event.getTooltipElements().add(Either.left(new TranslatableComponent("null")));
                            event.getTooltipElements().add(Either.left(new TranslatableComponent("modifiler.entry")));
                        }
                        event.getTooltipElements().add(Either.left((new TranslatableComponent("modifiler.entry." + modifierEntry.id.substring(2)))));

                    }
                }
            }
        }

        public static List<Component> ItemToolTipsChange(ItemStack stack, List<Component> tooltip, Player player) {
            if (stack.getTag()!=null){
                if (stack.getTag().getInt("exmodifier_armor_modifier_applied") > 0) {

                    if (stack.getOrCreateTag().getString("exmodifier_armor_modifier_applied0").equals("UNKNOWN")) {
                        tooltip.add(new TranslatableComponent("null"));
                        tooltip.add(new TranslatableComponent("modifiler.entry.UNKNOWN"));
                    } else {
                        for (ModifierEntry modifierEntry : getEntrysFromItemStack(stack)) {
                            // Exmodifier.LOGGER.debug("modifier id:" + modifierEntry.id);
                            if (!config.compact_tooltip) tooltip.add(new TranslatableComponent("null"));
                            tooltip.addAll(generateEntryTooltip(modifierEntry, player, stack));

                        }
                    }

                }
            }

            return tooltip;
        }
        @SubscribeEvent
        public static void tooltip(ItemTooltipEvent event){

        }
        @SubscribeEvent
        public static void AtJoinGame(PlayerEvent.PlayerLoggedInEvent event) {
            Player player = (Player) event.getEntity();
            player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {

                Map<String,Integer> map = capability.SuitsNum ;
                capability.SuitsNum = map.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> 0
                        ));
                capability.syncPlayerVariables(player);
            });
        }

        @SubscribeEvent
        public static void OutGame(PlayerEvent.PlayerLoggedOutEvent event) {

        }

        @SubscribeEvent
        public static void PlayerLiving(TickEvent.PlayerTickEvent event) {

            Player player = event.player;
            if (player.level.isClientSide) return;
            List<MobEffectInstance> addMobEffects = new ArrayList<>();
            player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                for (String id : capability.Suits) {
                    ExSuit suit = ExSuitHandle.LoadExSuit.get(id);
                    for (int i = 1; i <= ExSuitHandle.GetSuitLevel(player, id); i++) {
                        // 获取效果列表，检查是否为null
                        Map<Integer, List<MobEffectInstance>> effectMap = suit.getEffect();
                        if (effectMap != null) {
                            List<MobEffectInstance> effects = effectMap.get(i);
                            if (effects != null) {
                                for (MobEffectInstance mobEffectInstance : effects) {
                                    if (mobEffectInstance != null) {
                                        if (player.hasEffect(mobEffectInstance.getEffect())) {
                                            if (player.getEffect(mobEffectInstance.getEffect()).getAmplifier() < mobEffectInstance.getAmplifier()) {
                                                addMobEffects.add(new MobEffectInstance(mobEffectInstance));
                                            }
                                        } else {
                                            addMobEffects.add(new MobEffectInstance(mobEffectInstance));

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            });
            for (MobEffectInstance mobEffectInstance : addMobEffects) {
                ExApplySuitEffectEvent event1 = new ExApplySuitEffectEvent(player, mobEffectInstance);
                MinecraftForge.EVENT_BUS.post(event1);
                if (!event1.isCanceled()) player.addEffect(event1.mobEffectInstance);
            }
        }

        public static boolean hasAttr(ItemStack stack) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (!stack.getAttributeModifiers(slot).isEmpty()) {
                    return true;

                }
            }
            return false;
        }

        @SubscribeEvent
        public static void armorChange(LivingEquipmentChangeEvent event) {
            if (event.getEntity() instanceof Player player) {
                //   Exmodifier.LOGGER.info(event.getTo().getDescriptionId());
//                if (player.getPersistentData().getBoolean("LoginGamea")) {
//                    player.getPersistentData().putBoolean("LoginGamea", false);
//                    return;
//                }

                if (!event.getEntity().level.isClientSide) {


                    ItemStack stack = event.getTo();
                    Exmodifier.LOGGER.debug(event.getFrom().toString());
                    List<String> curiosSlots = CuriosUtil.getSlotsFromItemstack(stack);
                    if (!curiosSlots.isEmpty()){
                        if (player.getPersistentData().getBoolean("LoginGamea")) {
                            player.getPersistentData().putBoolean("LoginGamea", false);
                            return;
                        }
                        if (stack.getTag() == null || (!stack.getTag().contains("exmodifier_armor_modifier_applied"))) {
                            RandomEntryCurios(stack, 0, refresh_time, "none");
                        }
                        if (stack.getTag() != null) {
                            if (stack.getTag().contains("modifier_refresh")) {
                                if (stack.getTag().getBoolean("modifier_refresh")) {
                                    stack.getOrCreateTag().putBoolean("modifier_refresh", false);
                                    stack.getOrCreateTag().putInt("exmodifier_armor_modifier_applied", 0);

                                    RandomEntryCurios(stack, stack.getOrCreateTag().getInt("modifier_refresh_rarity"), stack.getOrCreateTag().getInt("modifier_refresh_add"),stack.getOrCreateTag().getString("wash_item"));
                                }
                            }
                        }
                        SuitOperate((Player) event.getEntity(), event.getTo(), event.getFrom());
                    }else {

                        if (hasAttr(stack)||stack.getItem() instanceof ShieldItem) {
                            if (stack.getTag() == null || (!stack.getTag().contains("exmodifier_armor_modifier_applied"))) {
                                RandomEntry(stack, 0, refresh_time,"none");
                                if (stack.getTag() != null) {
//                                    if (stack.getTag().contains("exmodifier_armor_modifier_applied")) {
//
//                                        player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
//                                            capability.Sitemstack = stack;
//                                            capability.syncPlayerVariables(player);
//                                        });
//                                    }
                                }
                            }
                            if (stack.getTag() != null) {
                                if (stack.getTag().contains("modifier_refresh")) {
                                    if (stack.getTag().getBoolean("modifier_refresh")) {
                                        stack.getOrCreateTag().putBoolean("modifier_refresh", false);
                                        stack.getOrCreateTag().putInt("exmodifier_armor_modifier_applied", 0);

                                        RandomEntry(stack, stack.getOrCreateTag().getInt("modifier_refresh_rarity"), stack.getOrCreateTag().getInt("modifier_refresh_add"),stack.getOrCreateTag().getString("wash_item"));
                                    }
                                }
                            }
                        }
                    }
                }
//                if (event.getEntity().level().isClientSide) {
//                    player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
//                        if (!capability.Sitemstack.isEmpty()) {
//                            player.setItemSlot(event.getSlot(), capability.Sitemstack);
//                            capability.Sitemstack = ItemStack.EMPTY;
//                            capability.syncPlayerVariables(player);
//                        }
//                    });
//                }
                SuitOperate((Player) event.getEntity(), event.getTo(), event.getFrom());
            }

        }

        public static void SuitOperate(@NotNull Player player, ItemStack stack1, ItemStack stack2) {

            if (player.level.isClientSide) return;

            handleStack(player, stack1, EntityAttrUtil.WearOrTake.WEAR);
            handleStack(player, stack2, EntityAttrUtil.WearOrTake.TAKE);
        }

        private static void handleStack(Player player, ItemStack stack, EntityAttrUtil.WearOrTake effectType) {
            if (!hasAttr(stack)) return;

            CompoundTag tag = stack.getTag();
            if (tag == null || tag.getInt("exmodifier_armor_modifier_applied") <= 0) return;

            int effectMultiplier = effectType == WEAR ? 1 : -1;

            for (int i = 0; ; i++) {
                String modifier = tag.getString("exmodifier_armor_modifier_applied" + i);
                if (modifier.isEmpty()) break;

                List<ExSuit> suits = ExSuitHandle.FindExSuit(modifier);
                for (ExSuit suit : suits) {
                    if (effectType == WEAR && suit.setting.getOrDefault("excludeArmorInHand", "false").equals("true") && stack.getItem() instanceof ArmorItem) {
                        continue;
                    }

                    if (effectType == TAKE && suit.setting.getOrDefault("excludeArmorInHand", "false").equals("true") && stack.getItem() instanceof ArmorItem) {
                        continue;
                    }

                    if (effectType == WEAR) {
                        ExSuitHandle.addSuitLevel(player, suit.id, 1);
                    } else {
                        ExSuitHandle.RemoveSuitLevel(player, suit.id, 1);
                    }

                    ExSuitApplyOnChangeEvent event = new ExSuitApplyOnChangeEvent(player, suit, i, effectType);
                    MinecraftForge.EVENT_BUS.post(event);

                    int suitLevel = ExSuitHandle.GetSuitLevel(player, suit.id);
                    List<ModifierAttriGether> attriGethers = suit.attriGether.get(effectType == WEAR ? suitLevel : suitLevel + 1);

                    if (attriGethers != null) {
                        for (ModifierAttriGether attrGether : attriGethers) {
                            if (attrGether.OnlyItems.isEmpty()) {
                                ExApplySuitAttrigetherEvent event1 = new ExApplySuitAttrigetherEvent(player, stack, effectType, attrGether);
                                MinecraftForge.EVENT_BUS.post(event1);
                                if (!event1.isCanceled()) EntityAttrUtil.entityAddAttrTF(event1.attriGether.attribute, event1.attriGether.getModifier(),event1.player,event1.effectType);
                            }
                        }
                    }

                    player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                        List<String> suitsList = capability.Suits;
                        if (suitLevel > 0 && !suitsList.contains(suit.id)) {
                            suitsList.add(suit.id);
                        } else if (suitLevel <= 0 && suitsList.contains(suit.id)) {
                            suitsList.remove(suit.id);
                        }
                        capability.Suits = suitsList;
                        capability.syncPlayerVariables(player);
                    });
                }
            }
        }

        @SubscribeEvent
        public static void atReload(AddReloadListenerEvent event) throws IOException {

            ModifierHandle.readConfig();
            ExSuitHandle.readConfig();

        }
    }
}

