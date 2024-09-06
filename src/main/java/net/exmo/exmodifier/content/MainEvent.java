package net.exmo.exmodifier.content;

import com.mojang.datafixers.util.Either;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.config;
import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.content.suit.ExSuit;
import net.exmo.exmodifier.content.suit.ExSuitHandle;
import net.exmo.exmodifier.network.ExModifiervaV;
import net.exmo.exmodifier.util.CuriosUtil;
import net.exmo.exmodifier.util.EntityAttrUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
            if (CuriosUtil.isCuriosItem(stack)) {
                boolean addf = false;
                for (ModifierEntry modifierEntry : getEntrysFromItemStack(stack)) {
                    if (stack.getOrCreateTag().getString("exmodifier_armor_modifier_applied0").equals("UNKNOWN")) {
                        event.getTooltipElements().add(Either.left(Component.translatable("null")));
                        event.getTooltipElements().add(Either.left(Component.translatable("modifiler.entry.UNKNOWN")));
                    } else {
                        if (!addf) {
                            addf = true;
                            event.getTooltipElements().add(Either.left(Component.translatable("null")));
                            event.getTooltipElements().add(Either.left(Component.translatable("modifiler.entry")));
                        }
                        event.getTooltipElements().add(Either.left((Component.translatable("modifiler.entry." + modifierEntry.id.substring(2)))));

                    }
                }
            }
        }

        public static List<Component> ItemToolTipsChange(ItemStack stack, List<Component> tooltip, Player player) {
            if (MainEvent.CommonEvent.hasAttr(stack)) {
                if (stack.getOrCreateTag().getInt("exmodifier_armor_modifier_applied") > 0) {

                    if (stack.getOrCreateTag().getString("exmodifier_armor_modifier_applied0").equals("UNKNOWN")) {
                        tooltip.add(Component.translatable("null"));
                        tooltip.add(Component.translatable("modifiler.entry.UNKNOWN"));
                    } else {
                        for (ModifierEntry modifierEntry : getEntrysFromItemStack(stack)) {
                            if (!config.compact_tooltip) tooltip.add(Component.translatable("null"));
                            tooltip.addAll(generateEntryTooltip(modifierEntry, player, stack));

                        }
                    }

                }
            }
            return tooltip;
        }

        @SubscribeEvent
        public static void AtJoinGame(PlayerEvent.PlayerLoggedInEvent event) {
            event.getEntity().getPersistentData().putBoolean("LoginGamea", true);
        }

        @SubscribeEvent
        public static void OutGame(PlayerEvent.PlayerLoggedOutEvent event) {
            event.getEntity().getPersistentData().putBoolean("LoginGamea", false);
        }

        @SubscribeEvent
        public static void PlayerLiving(TickEvent.PlayerTickEvent event) {

            Player player = event.player;
            if (player.level().isClientSide) return;
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
                player.addEffect(mobEffectInstance);
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
                if (player.getPersistentData().getBoolean("LoginGamea")) {
                    player.getPersistentData().putBoolean("LoginGamea", false);
                    return;
                }

                if (!event.getEntity().level().isClientSide) {

                    ItemStack stack = event.getTo();
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

                        if (hasAttr(stack)) {
                            if (stack.getTag() == null || (!stack.getTag().contains("exmodifier_armor_modifier_applied"))) {
                                RandomEntry(stack, 0, refresh_time,"none");
                                if (stack.getTag() != null) {
                                    if (stack.getTag().contains("exmodifier_armor_modifier_applied")) {

                                        player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                                            capability.Sitemstack = stack;
                                            capability.syncPlayerVariables(player);
                                        });
                                    }
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
                if (event.getEntity().level().isClientSide) {
                    player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                        if (!capability.Sitemstack.isEmpty()) {
                            player.setItemSlot(event.getSlot(), capability.Sitemstack);
                            capability.Sitemstack = ItemStack.EMPTY;
                            capability.syncPlayerVariables(player);
                        }
                    });
                }
                SuitOperate((Player) event.getEntity(), event.getTo(), event.getFrom());
            }

        }

        public static void SuitOperate(Player player, ItemStack stack1, ItemStack stack2) {


            if (player.level().isClientSide) return;

            if (hasAttr(stack1)) {
                if (stack1.getTag() != null && stack1.getTag().contains("exmodifier_armor_modifier_applied")) {
                    stack1.getAttributeModifiers(EquipmentSlot.CHEST).forEach((slot, attributeModifiers) -> {
                        Exmodifier.LOGGER.debug("Slot:" + slot + " " + attributeModifiers + " " + attributeModifiers.getName() + " " + attributeModifiers.getId());
                    });

                    for (int i = 0; true; i++) {
                        String a = stack1.getOrCreateTag().getString("exmodifier_armor_modifier_applied" + i); //获取武器的词条e
                        if (a.isEmpty()) break; //如果没有则退出
                        Exmodifier.LOGGER.debug("Suit a" + a);
                        List<ExSuit> suits = ExSuitHandle.FindExSuit(a); //找到相关的套装
                        for (ExSuit suit : suits) { //遍历找到的套装
                            Exmodifier.LOGGER.debug("Suit found" + suit.id);
                            //  player.getPersistentData().putInt(suit.id, player.getPersistentData().getInt(suit.id)+1); //套装效果加一
                            if (suit.setting.containsKey("excludeArmorInHand")) {
                                if (suit.setting.get("excludeArmorInHand").equals("true")) {
                                    if (stack1.getItem() instanceof ArmorItem) continue;
                                }
                            }
                            ExSuitHandle.addSuitLevel(player, suit.id, 1);
                            Exmodifier.LOGGER.debug("Suit found amout" + ExSuitHandle.GetSuitLevel(player, suit.id));
                            List<ModifierAttriGether> attriGethers = suit.attriGether.get(ExSuitHandle.GetSuitLevel(player, suit.id));
                            if (attriGethers == null) continue;
                            for (ModifierAttriGether attrGether : attriGethers) { //应用套装效果
                                EntityAttrUtil.entityAddAttrTF(attrGether.attribute, attrGether.getModifier(), player, WEAR);
                                //     Exmodifier.LOGGER.debug("Suit apply attrgether" + attrGether.attribute.getDescriptionId());
                            }

                            player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> { //添加套装列表
                                if (ExSuitHandle.GetSuitLevel(player, suit.id) > 0) {
                                    List<String> aaa = capability.Suits;
                                    if (!aaa.contains(suit.id)) aaa.add(suit.id);
                                    capability.Suits = aaa;
                                    capability.syncPlayerVariables(player);
                                } else if (ExSuitHandle.GetSuitLevel(player, suit.id) <= 0) {
                                    List<String> aaa = capability.Suits;
                                    if (aaa.contains(suit.id)) aaa.remove(suit.id);
                                    capability.Suits = aaa;
                                    capability.syncPlayerVariables(player);
                                }
                            });
                        }
                    }

                }
            }
            if (hasAttr(stack2)) {
                if (stack2.getTag() != null) {
                    if (stack2.getOrCreateTag().getInt("exmodifier_armor_modifier_applied") <= 0) {
                        return;
                    }
                    if (stack2.isEmpty())
                        return; //!!!!这里有返回！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
                    for (int i = 0; true; i++) {
                        String a = stack2.getOrCreateTag().getString("exmodifier_armor_modifier_applied" + i);
                        if (a.isEmpty()) break;

                        List<ExSuit> suits = ExSuitHandle.FindExSuit(a);

                        for (ExSuit suit : suits) {
                            Exmodifier.LOGGER.debug("Suit ab" + a);
                            // player.getPersistentData().putInt(suit.id, player.getPersistentData().getInt(suit.id)-1);
                            if (suit.setting.containsKey("excludeArmorInHand")) {
                                if (suit.setting.get("excludeArmorInHand").equals("true")) {
                                    if (stack2.getItem() instanceof ArmorItem) continue;
                                }
                            }
                            ExSuitHandle.RemoveSuitLevel(player, suit.id, 1);
                            Exmodifier.LOGGER.debug("Suit found amout" + ExSuitHandle.GetSuitLevel(player, suit.id));

                            List<ModifierAttriGether> attriGethers = suit.attriGether.get(ExSuitHandle.GetSuitLevel(player, suit.id) + 1);
                            if (attriGethers == null) continue;
                            for (ModifierAttriGether attrGether : attriGethers) { //移除套装效果
                                EntityAttrUtil.entityAddAttrTF(attrGether.attribute, attrGether.getModifier(), player, TAKE);
                                //      Exmodifier.LOGGER.debug("Suit remove attrgether" + attrGether.attribute.getDescriptionId());
                            }
                            player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                                if (ExSuitHandle.GetSuitLevel(player, suit.id) > 0) {
                                    List<String> aaa = capability.Suits;
                                    if (!aaa.contains(suit.id)) aaa.add(suit.id);
                                    capability.Suits = aaa;
                                    capability.syncPlayerVariables(player);
                                } else if (ExSuitHandle.GetSuitLevel(player, suit.id) <= 0) {
                                    List<String> aaa = capability.Suits;
                                    if (aaa.contains(suit.id)) aaa.remove(suit.id);
                                    capability.Suits = aaa;
                                    capability.syncPlayerVariables(player);
                                }
                            });
                        }

                    }
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

