package net.exmo.exmodifier.content.event;

import com.mojang.datafixers.util.Either;
import dev.shadowsoffire.placebo.events.ItemUseEvent;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.config;
import net.exmo.exmodifier.content.modifier.EntryItem;
import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.content.suit.ExSuit;
import net.exmo.exmodifier.content.suit.ExSuitHandle;
import net.exmo.exmodifier.events.*;
import net.exmo.exmodifier.network.ExModifiervaV;
import net.exmo.exmodifier.util.CuriosUtil;
import net.exmo.exmodifier.util.EntityAttrUtil;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
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
import java.util.Objects;
import java.util.stream.Collectors;

import static net.exmo.exmodifier.Exmodifier.*;
import static net.exmo.exmodifier.config.refresh_time;
import static net.exmo.exmodifier.content.event.MainEvent.CommonEvent.init;
import static net.exmo.exmodifier.content.modifier.ModifierHandle.CommonEvent.*;
import static net.exmo.exmodifier.content.modifier.ModifierHandle.getEntrysFromItemStack;
import static net.exmo.exmodifier.util.EntityAttrUtil.WearOrTake.TAKE;
import static net.exmo.exmodifier.util.EntityAttrUtil.WearOrTake.WEAR;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class MainEvent {

//    @SubscribeEvent
//    public static void FmlLoad(FMLCommonSetupEvent event) throws IOException {
//        init();
//
//
//    } 已内置到 Exmodifier.java

    @Mod.EventBusSubscriber
    public static class CommonEvent {

        @SubscribeEvent
        public static void CuriosChange(CurioChangeEvent event) {
            if (!(event.getEntity() instanceof Player player))return;

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
            if (stack.getTag()!=null){
                if (stack.getTag().getInt("exmodifier_armor_modifier_applied") > 0) {

                    if (stack.getOrCreateTag().getString("exmodifier_armor_modifier_applied0").equals("UNKNOWN")) {
                        tooltip.add(Component.translatable("null"));
                        tooltip.add(Component.translatable("modifiler.entry.UNKNOWN"));
                    } else {
                        for (ModifierEntry modifierEntry : getEntrysFromItemStack(stack)) {
                            // Exmodifier.LOGGER.debug("modifier id:" + modifierEntry.id);
                            if (!config.compact_tooltip) tooltip.add(Component.translatable("null"));
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
        public static void ApplySuitEffect(Player player, ExSuit.Trigger trigger){
            // Retrieve player capability once and exit early if not present
            player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                List<MobEffectInstance> mobEffectsToAdd = new ArrayList<>();
                CommandSourceStack commandSourceStack;
                if (player.level() instanceof  ServerLevel serverLevel){
                 commandSourceStack =  new CommandSourceStack(
                        CommandSource.NULL,
                        player.position(),
                        player.getRotationVector(),
                        serverLevel,
                        4,
                        player.getName().getString(),
                        player.getDisplayName(),
                        serverLevel.getServer(),
                        player
                );
                } else {
                    commandSourceStack = null;
                }

                for (String suitId : capability.Suits){
                    ExSuit suit = ExSuitHandle.LoadExSuit.get(suitId);
                    if (suit == null)continue;
                    int suitLevel = ExSuitHandle.GetSuitLevel(player, suitId);
                    for (int level = 1; level <= suitLevel; level++) {
                        //事件触发器在此 !!!!!!!!!!!!!!!!!!!
                        if (suit.getTriggers().get(level) != trigger)continue;
                        // Run commands if present for the current suit level
                        List<String> commands = suit.getCommands().get(level);
                        if (commands != null && !player.level().isClientSide() && player.getServer() != null &&commandSourceStack!=null) {
                            commands.forEach(command -> player.getServer().getCommands().performPrefixedCommand(commandSourceStack, command));
                        }

                        // Add MobEffects if present for the current suit level
                        List<MobEffectInstance> effects = suit.getEffect().get(level);
                        if (effects != null) {
                            effects.stream()
                                    .filter(Objects::nonNull)
                                    .forEach(mobEffectInstance -> {
                                        MobEffectInstance existingEffect = player.getEffect(mobEffectInstance.getEffect());
                                        if (existingEffect == null || existingEffect.getAmplifier() < mobEffectInstance.getAmplifier()) {
                                            mobEffectsToAdd.add(new MobEffectInstance(mobEffectInstance));
                                        }
                                    });
                        }
                    }
                }

                // Apply collected effects, firing an event for each one
                mobEffectsToAdd.forEach(mobEffectInstance -> {
                    ExApplySuitEffectEvent applySuitEffectEvent = new ExApplySuitEffectEvent(player, mobEffectInstance);
                    MinecraftForge.EVENT_BUS.post(applySuitEffectEvent);
                    if (!applySuitEffectEvent.isCanceled()) {
                        player.addEffect(applySuitEffectEvent.mobEffectInstance);
                    }
                });
            });
        }
        @SubscribeEvent
        public static void PlayerHurtAndAttack(LivingHurtEvent event){
            if ((event.getEntity() instanceof Player player))ApplySuitEffect(player, ExSuit.Trigger.ON_HURT);
            if ((event.getSource().getEntity() instanceof Player player))ApplySuitEffect(player, ExSuit.Trigger.ATTACK);
        }
        @SubscribeEvent
        public static void PlayerJump(LivingEvent.LivingJumpEvent event){
            if ((event.getEntity() instanceof Player player))ApplySuitEffect(player, ExSuit.Trigger.JUMP);
        }
        @SubscribeEvent
        public static void PlayerDeathAndKill(LivingDeathEvent event){
            if ((event.getEntity() instanceof Player player))ApplySuitEffect(player, ExSuit.Trigger.DIE);
            if ((event.getSource().getEntity() instanceof Player player))ApplySuitEffect(player, ExSuit.Trigger.KILL);
        }
        @SubscribeEvent
        public static void PlayerProjectile(ProjectileImpactEvent event){
            if ((event.getEntity() instanceof Player player))ApplySuitEffect(player, ExSuit.Trigger.PROJECTILE_HIT);
        }
        @SubscribeEvent
        public static void PlayerShoot(ArrowLooseEvent event){
           ApplySuitEffect(event.getEntity(), ExSuit.Trigger.SHOOT);
        }
        @SubscribeEvent
        public static void PlayerMove(MovementInputUpdateEvent event){
             ApplySuitEffect(event.getEntity(), ExSuit.Trigger.MOVECHANGE);

        }
        @SubscribeEvent
        public static void PlayerSwing(LivingSwingEvent event){
            if ((event.getEntity() instanceof Player player))   ApplySuitEffect(player, ExSuit.Trigger.SWING);
        }
        @SubscribeEvent
        public static void PlayerCrit(CriticalHitEvent event){
       ApplySuitEffect(event.getEntity(), ExSuit.Trigger.CRIT);
        }
        @SubscribeEvent
        public static void PlayerDodge(ExDodgeEvent event){
            if ((event.getEntity() instanceof Player player))
                if (event.result == ExDodgeEvent.resultType.MISS)
                    ApplySuitEffect(player, ExSuit.Trigger.DODGE);

        }
        @SubscribeEvent
        public static void PlayerEat(ItemUseEvent event){
            if (event.getEntity()==null)return;
            if ( event.getItemStack().getFoodProperties(event.getEntity())!=null) ApplySuitEffect(event.getEntity(), ExSuit.Trigger.EAT);

        }
//        @SubscribeEvent
//        public static void PlayerDamage(LivingDamageEvent event){
//            if ((event.getEntity() instanceof Player player))ApplySuitEffect(player, ExSuit.Trigger.ATTACK);
//        } 该用法不稳定 已移植hurt
        @SubscribeEvent
        public static void PlayerLiving(TickEvent.PlayerTickEvent event) {
            Player player = event.player;
            if (player.level().isClientSide) {
                return;
            }
            ApplySuitEffect(player, ExSuit.Trigger.TICK);

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
                boolean isExSuitOperate = false;
                if (!event.getEntity().level().isClientSide) {


                    ItemStack stack = event.getTo();
                   // Exmodifier.LOGGER.debug(event.getFrom().toString());
                    List<String> curiosSlots = CuriosUtil.getSlotsFromItemstack(stack);
                    if (!curiosSlots.isEmpty()){
//                        if (player.getPersistentData().getBoolean("LoginGamea")) {
//                            player.getPersistentData().putBoolean("LoginGamea", false);
//                            return;
//                        }
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
//                if (event.getEntity().level()().isClientSide) {
//                    player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
//                        if (!capability.Sitemstack.isEmpty()) {
//                            player.setItemSlot(event.getSlot(), capability.Sitemstack);
//                            capability.Sitemstack = ItemStack.EMPTY;
//                            capability.syncPlayerVariables(player);
//                        }
//                    });
//                }
                isExSuitOperate=    SuitOperate((Player) event.getEntity(), event.getTo(), event.getFrom());
                MinecraftForge.EVENT_BUS.post(new ExAfterArmorChange(event, isExSuitOperate));
            }

        }

        public static boolean SuitOperate(@NotNull Player player, ItemStack stack1, ItemStack stack2) {

            if (player.level().isClientSide) return false;
            boolean flag = false;
            flag =  handleStack(player, stack1, EntityAttrUtil.WearOrTake.WEAR);
            if (handleStack(player, stack2, EntityAttrUtil.WearOrTake.TAKE))flag = true;
            return flag;
        }

        private static boolean handleStack(Player player, ItemStack stack, EntityAttrUtil.WearOrTake effectType) {
            boolean flag = false;
            if (!hasAttr(stack)) return false;

            CompoundTag tag = stack.getTag();
            if (tag == null || tag.getInt("exmodifier_armor_modifier_applied") <= 0) return false;

            int effectMultiplier = effectType == WEAR ? 1 : -1;

            for (int i = 0; ; i++) {
                String modifier = tag.getString("exmodifier_armor_modifier_applied" + i);
                if (modifier.isEmpty()) break;
                List<String> founds = new ArrayList<>();
                List<ExSuit> suits = ExSuitHandle.FindExSuit(modifier);
                for (ExSuit suit : suits) {
                    if (founds.contains(suit.id))continue;
                    founds.add(suit.id);
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
                    flag =true;


                    int suitLevel = ExSuitHandle.GetSuitLevel(player, suit.id);
                    List<ModifierAttriGether> attriGethers = suit.attriGether.get(effectType == WEAR ? suitLevel : suitLevel + 1);

                    if (attriGethers != null) {
                        for (ModifierAttriGether attrGether : attriGethers.stream().filter(attrGether -> attrGether.getOnlyItems().isEmpty()).toList()) {
                            Exmodifier.LOGGER.debug("items : "+attrGether.getOnlyItems().toString());
                            //    if (attrGether.getOnlySlots() ==null|| attrGether.getOnlySlots().isEmpty()) {

                            ExApplySuitAttrigetherEvent event1 = new ExApplySuitAttrigetherEvent(player, stack, effectType, attrGether);
                            try {
                                Exmodifier.LOGGER.debug("Apply Suit AttriGether: " + attrGether.attribute.getDescriptionId() + " " + attrGether.modifier.getOperation().toString() + " " + attrGether.modifier.getAmount());
                            }catch (Exception e){System.out.println(e);}
                            MinecraftForge.EVENT_BUS.post(event1);
                            if (!event1.isCanceled()) EntityAttrUtil.entityAddAttrTF(event1.attriGether.attribute, event1.attriGether.getModifier(),event1.player,event1.effectType);
                            //  }
                        }
                    }
                    ExSuitApplyOnChangeEvent event = new ExSuitApplyOnChangeEvent(player, suit, i, effectType);
                    MinecraftForge.EVENT_BUS.post(event);
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
            return flag;
        }
        public static void init() throws IOException {
            ModifierHandle.readConfig();
            ExSuitHandle.readConfig();
            ModifierHandle.EEMatchQueueHandle();

        }

        @SubscribeEvent
        public static void atReload(AddReloadListenerEvent event) throws IOException {

            init();

        }
    }
}

