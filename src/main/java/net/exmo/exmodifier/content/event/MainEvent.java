package net.exmo.exmodifier.content.event;

import net.exmo.exmodifier.Config;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.element.*;
import net.exmo.exmodifier.content.modifier.RefreshContainTagHandle;
import net.exmo.exmodifier.content.client.LanguageLoader;
import net.exmo.exmodifier.content.event.parameter.EventParameter;
import net.exmo.exmodifier.content.helper.*;
import net.exmo.exmodifier.content.level.ItemLevelHandle;
import net.exmo.exmodifier.content.modifier.*;
import net.exmo.exmodifier.content.quality.ItemQualityHandle;
import net.exmo.exmodifier.content.refine.RefineHandle;
import net.exmo.exmodifier.content.refine.RefineItemRecordPreparableReloadListener;
import net.exmo.exmodifier.content.selected.BaseItemSelected;
import net.exmo.exmodifier.content.slot.ModifierSlotHandle;
import net.exmo.exmodifier.content.suit.ExSuit;
import net.exmo.exmodifier.content.suit.ExSuitHandle;
import net.exmo.exmodifier.content.suit.ExSuitPreparableReloadListener;
import net.exmo.exmodifier.content.type.ExType;
import net.exmo.exmodifier.content.type.ExTypeHandle;
import net.exmo.exmodifier.content.resources.ZipHandle;
import net.exmo.exmodifier.events.*;
import net.exmo.exmodifier.network.ExModifiervaV;
import net.exmo.exmodifier.network.sync.lang.LangMessage;
import net.exmo.exmodifier.util.*;
import net.exmo.exmodifier.util.gether.AttriGetherNormal;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
//import net.minecraftforge.client.eventC.MovementInputUpdateEvent;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.GrindstoneEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static net.exmo.exmodifier.Config.refresh_time;
import static net.exmo.exmodifier.commands.ExModifierReloadCommand.sendUpdatedModifiersToClients;
import static net.exmo.exmodifier.content.element.ExElementHandle.FoundDefaultElementConfigs;
import static net.exmo.exmodifier.content.element.ExElementHandle.FoundEntityDefaultElementConfigs;
import static net.exmo.exmodifier.content.level.ItemLevelHandle.ItemLevelRefresh;
import static net.exmo.exmodifier.content.modifier.ModifierHandle.CommonEvent.*;
import static net.exmo.exmodifier.content.modifier.ModifierHandle.itemsDefaultEntry;
import static net.exmo.exmodifier.util.EntityAttrUtil.WearOrTake.TAKE;
import static net.exmo.exmodifier.util.EntityAttrUtil.WearOrTake.WEAR;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class MainEvent {

//    @SubscribeEvent
//    public static void FmlLoad(FMLCommonSetupEvent eventC) throws IOException {
//        init();
//
//
//    } 已内置到 Exmodifier.java

    @Mod.EventBusSubscriber
    public static class CommonEvent {
        public static List<String> UnMatchingModIDs = new ArrayList<>();

        static {
            UnMatchingModIDs.add("umapyoi");
        }




        @SubscribeEvent
        public static void CuriosChange(CurioChangeEvent event) {

            if (!(event.getEntity() instanceof Player player)) return;

            ItemStack stack = event.getTo();
            for (String s : UnMatchingModIDs) {
                if (ForgeRegistries.ITEMS.getKey(stack.getItem()).toString().startsWith(s)) return;
            }
            ModifierEntryHelper modifierEntryHelper = ModifierEntryHelper.of(stack);
            if (stack.getTag() == null || modifierEntryHelper.getModifierEntriesSize() <= 0) {
                RandomEntryCurios(stack, 0, refresh_time, "none");
            }
            if (stack.getTag() != null) {
                if (stack.getTag().contains("modifier_refresh")) {
                    if (stack.getTag().getBoolean("modifier_refresh")) {
                        stack.getTag().remove("modifier_refresh");
                        stack.getTag().remove("UNKNOWN");

                        //  stack.getTag().putInt("exmodifier_armor_modifier_applied", 0);

                        RandomEntryCurios(stack, stack.getOrCreateTag().getInt("modifier_refresh_rarity"), stack.getOrCreateTag().getInt("modifier_refresh_add"), stack.getTag().getString("wash_item"));
                        AttributeCuriosHandle.handleCurios(new CurioChangeEvent(player, event.getIdentifier(), event.getSlotIndex(), event.getFrom(), stack));
                    }
                }
            }
            SuitOperate(player, event.getTo(), event.getFrom());

        }

        //@SubscribeEvent
//        public static void CuriosTooltipChange(RenderTooltipEvent.GatherComponents event) {
//            ItemStack stack = event.getItemStack();
//            if (stack.getTag()==null)return;
//            if (CuriosUtil.isCuriosItem(stack)) {
//                boolean addf = false;
//                for (ModifierEntry exElement : new ModifierEntryHelper(stack).getModifierEntriesB()) {
//                    if (stack.getTag().getBoolean("UNKNOWN")) {
//                        event.getTooltipElements().add(Either.left(Component.translatable("null")));
//                        event.getTooltipElements().add(Either.left(Component.translatable("modifier.entry.UNKNOWN")));
//                    } else {
//                        if (!addf) {
//                            addf = true;
//                            event.getTooltipElements().add(Either.left(Component.translatable("null")));
//                            event.getTooltipElements().add(Either.left(Component.translatable("modifier.entry")));
//                            for (ExSuit suit : ExSuitHandle.LoadExSuit.itemTypes().stream().filter(exSuit -> exSuit.entry.contains(exElement))
//                                    .toList()) {
//                                if (suit.visible) {
//
//                                    event.getTooltipElements().add(Either.left(Component.translatable("modifier.entry.suit." + suit.Id)));
//                                    if (!suit.LocalDescription.isEmpty())
//                                        event.getTooltipElements().add(Either.left(Component.translatable(suit.LocalDescription)));
//
//                                    //.append(Component.translatable("modifier.entry.suit.color"))
//                                }
//                            }
//                        }
//                        event.getTooltipElements().add(Either.left((Component.translatable("modifier.entry." + exElement.Id.substring(2)))));
//
//                    }
//                }
//            }
//        }
        @SubscribeEvent
        public static void iLevelAttriGetherModifier(ExApplyEntryAttrigetherEvent event) {
            if (event.attriGether.Expression != null && !event.attriGether.Expression.isEmpty()) {
                ItemStack stack = event.stack;
                ExAttributeModifier modifier = event.attriGether.getModifier();
                int level = event.modifierInstant.getLevel();
                Exmodifier.LOGGER.debug("iLevelAttriGetherModifier: " + event.attriGether.Expression + " level: " + level);
                DynamicExpressionEvaluator dynamicExpressionEvaluator = new DynamicExpressionEvaluator();
                dynamicExpressionEvaluator.setVariable("level", level);
                dynamicExpressionEvaluator.setVariable("l", level);
                double amount = dynamicExpressionEvaluator.evaluate(event.attriGether.Expression);
                event.attriGether.modifier.setAmount(amount);
            }
        }

        public static Pair<List<Component>, Integer> EntryInfoTooltip(ItemStack stack, List<Component> tooltip, Player player) {
            if (stack.getTag() != null) {
                ModifierEntryHelper modifierEntryHelper = ModifierEntryHelper.of(stack);
                if (stack.getTag().getBoolean("UNKNOWN")) {
                    tooltip.add(Component.translatable("null"));
                    tooltip.add(Component.translatable("modifier.entry.UNKNOWN"));
                } else {
                    if (modifierEntryHelper.getModifierEntriesSize() > 0) {


                        for (ModifierInstant modifierEntry : new ItemInfo(stack).getModifierEntryHelper().getModifierEntries()) {
                            if (modifierEntry.getSlot().isPresent()) continue;
                            // Exmodifier.LOGGER.debug("modifier Id:" + exElement.Id);
                            if (!Config.compact_tooltip) tooltip.add(Component.translatable("null"));
                            tooltip.addAll(generateEntryTooltip(modifierEntry, player, stack, false));

                        }
                    }
                    if (stack.getTag().getBoolean("can_add_max"))
                        tooltip.add(Component.translatable("modifier.entry.can_add_max"));

                }
            }

            return new Pair<>(tooltip, tooltip.size());
        }


//      public   static int tick =0;
//        @SubscribeEvent
//        public static void ServerTick(TickEvent.ServerTickEvent event) {
//            tick++;
//            if (tick % 20 == 0) {
//                MinecraftServer server = event.getServer();
//                List<ServerPlayer> players = server.getPlayerList().getPlayers();
//
//                // 使用并行流优化实体计数
//                List<ServerLevel> serverLevels =new ArrayList<>();
//                server.getAllLevels().forEach(serverLevels::add);
//                AtomicInteger entityCount = new AtomicInteger();
//                serverLevels.parallelStream().map(ServerLevel::getEntities).map(e-> entityCount.addAndGet(1));
//
//                RealTimeWebServer.onlinePlayers.set(players);
//                RealTimeWebServer.entityCount.set( entityCount.get());
//            }
//        }

        @SubscribeEvent
        public static void AtJoinGame(PlayerEvent.PlayerLoggedInEvent event) {
            Player player = (Player) event.getEntity();
            player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {

                Map<String, Integer> map = capability.SuitsNum;
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

        public static float damageBoost = 1;
        public static float damageNumber = 0;
        public static boolean hasDamageBoost = false;
        public static boolean hasDamageNumber = false;
        public static void ApplySuitEffect(Player player, ExSuit.Trigger trigger) {
            if (player == null) return;
            if (player. level().isClientSide)return;
            // Retrieve player capability once and exit early if not present
            player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                List<MobEffectInstance> mobEffectsToAdd = new ArrayList<>();
                CommandSourceStack commandSourceStack;
                if (player.level() instanceof ServerLevel serverLevel) {
                    commandSourceStack = new CommandSourceStack(
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

                Map<String, Integer> suitsNum = capability.SuitsNum;
                for (var entry : suitsNum.entrySet()) {
                    String suitId = entry.getKey();
                    int suitLevel = entry.getValue();
                    ExSuit suit = ExSuitHandle.LoadExSuit.get(suitId);
                    if (suit == null) {
                        suitsNum.remove( suitId);
                        capability.SuitsNum =  suitsNum;
                        capability.syncPlayerVariables(player);
                        continue;
                    }
                    for (int level = 1; level <= suitLevel; level++) {
                        //事件触发器在此 !!!!!!!!!!!!!!!!!!!
                        if (suit.getTriggers().get(level) != trigger) continue;
                        // Run commands if present for the current suit level
                        List<String> commands = suit.getCommands().get(level);
                        if (commands != null && !player.level().isClientSide() && player.getServer() != null && commandSourceStack != null) {
                            int finalLevel = level;
                            commands.forEach(command -> {
                                if (trigger == ExSuit.Trigger.ATTACK || trigger == ExSuit.Trigger.PROJECTILE_HIT) {
                                    String string = player.getPersistentData().getString("hurtentity-uuid");
                                    if (!string.equals("null")) {
                                        command = command.replace("$(hurtentity)", string);
                                    }
                                }
                                if (cheekEvent.livingHurtEvent !=null) {
                                    if (hasDamageBoost) {
                                        //获取其后的数值
                                        cheekEvent.livingHurtEvent.setAmount((float) (cheekEvent.livingHurtEvent.getAmount() * damageBoost));
                                         damageBoost = 1;
                                        hasDamageBoost = false;

                                    }
                                    if (hasDamageNumber) {
                                        //获取其后的数值
                                        cheekEvent.livingHurtEvent.setAmount((float) (cheekEvent.livingHurtEvent.getAmount() + damageNumber));
                                        damageNumber = 0;
                                        hasDamageBoost = false;

                                    }
                                }
                                command = command.replace("$(level)", finalLevel + "");
                                player.getServer().getCommands().performPrefixedCommand(commandSourceStack, command);

                            });
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

                // Apply collected effects, firing an eventC for each one
                mobEffectsToAdd.forEach(mobEffectInstance -> {
                    ExApplySuitEffectEvent applySuitEffectEvent = new ExApplySuitEffectEvent(player, mobEffectInstance);
                    MinecraftForge.EVENT_BUS.post(applySuitEffectEvent);
                    if (!applySuitEffectEvent.isCanceled()) {
                        player.addEffect(applySuitEffectEvent.mobEffectInstance);
                    }
                });
            });
        }

        public static void addx(Player player, List<EventParameter<?>> eventParameters, String name) {
            ItemLevelHandle.ItemAddXpAuto(player, eventParameters, name);

        }

        @Mod.EventBusSubscriber
        public static class cheekEvent {
            private static LivingHurtEvent livingHurtEvent;
            @SubscribeEvent
            public static void PlayerHurtAndAttack(LivingHurtEvent event) {
                if (event.getSource().is(DamageTypes.GENERIC_KILL))return;
                if ((event.getEntity() instanceof Player player)) {
                    List<EventParameter<?>> eventParameters = new ArrayList<>();
                    eventParameters.add(new EventParameter<>("amount", event.getAmount()));
                    eventParameters.add(new EventParameter<>("max_health", player.getAttributeValue(Attributes.MAX_HEALTH)));
                    addx(player, eventParameters, "ON_HURT");
                    livingHurtEvent = event;
                    ApplySuitEffect(player, ExSuit.Trigger.ON_HURT);
                    livingHurtEvent = null;
                }
                if ((event.getSource().getEntity() instanceof Player player)) {
                    List<EventParameter<?>> eventParameters = new ArrayList<>();
                    eventParameters.add(new EventParameter<>("amount", event.getAmount()));
                    eventParameters.add(new EventParameter<>("max_health", player.getAttributeValue(Attributes.MAX_HEALTH)));
                    addx(player, eventParameters, "ATTACK");
                    if (event.getEntity() != null)
                        player.getPersistentData().putString("hurtentity-uuid", event.getEntity().getUUID().toString());
                    livingHurtEvent = event;
                    ApplySuitEffect(player, ExSuit.Trigger.ATTACK);
                    livingHurtEvent = null;
                    player.getPersistentData().putString("hurtentity-uuid", "null");
                }
            }

            @SubscribeEvent
            public static void PlayerJump(LivingEvent.LivingJumpEvent event) {
                if ((event.getEntity() instanceof Player player)) {
                    List<EventParameter<?>> eventParameters = new ArrayList<>();
                    addx(player, eventParameters, "JUMP");
                    ApplySuitEffect(player, ExSuit.Trigger.JUMP);
                }
            }

            @SubscribeEvent
            public static void Digger(BlockEvent.BreakEvent event) {
                Player player = event.getPlayer();
                List<EventParameter<?>> eventParameters = new ArrayList<>();
                addx(player, eventParameters, "DIG");
                ApplySuitEffect(player, ExSuit.Trigger.DIG);

            }

            @SubscribeEvent
            public static void PlayerDeathAndKill(LivingDeathEvent event) {
                if ((event.getEntity() instanceof Player player)) {
                    List<EventParameter<?>> eventParameters = new ArrayList<>();
                    addx(player, eventParameters, "DIE");
                    ApplySuitEffect(player, ExSuit.Trigger.DIE);
                }
                if ((event.getSource().getEntity() instanceof Player player)) {
                    List<EventParameter<?>> eventParameters = new ArrayList<>();
                    addx(player, eventParameters, "KILL");
                    ApplySuitEffect(player, ExSuit.Trigger.KILL);
                }
            }

            @SubscribeEvent
            public static void PlayerProjectile(ProjectileImpactEvent event) {
                if ((event.getProjectile().getOwner() instanceof Player player)) {
                    List<EventParameter<?>> eventParameters = new ArrayList<>();
                    if (event.getEntity() != null)
                        player.getPersistentData().putString("hurtentity-uuid", event.getEntity().getUUID().toString());
                    addx(player, eventParameters, "PROJECTILE_HIT");
                    ApplySuitEffect(player, ExSuit.Trigger.PROJECTILE_HIT);
                }
            }

            @SubscribeEvent
            public static void PlayerShoot(ArrowLooseEvent event) {
                List<EventParameter<?>> eventParameters = new ArrayList<>();
                addx(event.getEntity(), eventParameters, "SHOOT");
                ApplySuitEffect(event.getEntity(), ExSuit.Trigger.SHOOT);
            }

            //        @SubscribeEvent
//        public static void PlayerMove(MovementInputUpdateEvent eventC){
//             ApplySuitEffect(eventC.getEntity(), ExSuit.Trigger.MOVECHANGE);
//
//        }
            @SubscribeEvent
            public static void PlayerSwing(LivingSwingEvent event) {
                if ((event.getEntity() instanceof Player player)) {
                    List<EventParameter<?>> eventParameters = new ArrayList<>();
                    addx(player, eventParameters, "SWING");
                    ApplySuitEffect(player, ExSuit.Trigger.SWING);
                }
            }

            @SubscribeEvent
            public static void PlayerCrit(CriticalHitEvent event) {
                List<EventParameter<?>> eventParameters = new ArrayList<>();
                eventParameters.add(new EventParameter<>("amount", event.getDamageModifier()));
                Player player = event.getEntity();
                addx(player, eventParameters, "CRIT");
                ApplySuitEffect(player, ExSuit.Trigger.CRIT);
            }

            @SubscribeEvent
            public static void PlayerDodge(ExDodgeEvent event) {
                if ((event.getEntity() instanceof Player player))
                    if (event.result == ExDodgeEvent.resultType.MISS) {
                        List<EventParameter<?>> eventParameters = new ArrayList<>();
                        addx(player, eventParameters, "DODGE");
                        ApplySuitEffect(player, ExSuit.Trigger.DODGE);
                    }

            }

            @SubscribeEvent
            public static void PlayerUseItem(LivingEntityUseItemEvent event) {
                if (event.getEntity() instanceof Player player) {
                    List<EventParameter<?>> eventParameters = new ArrayList<>();
                    addx(player, eventParameters, "ON_USE");
                    ApplySuitEffect(player, ExSuit.Trigger.ON_USE);
                }
            }

            @SubscribeEvent
            public static void PlayerSwim(LivingPlayerSwimEvent event) {
                List<EventParameter<?>> eventParameters = new ArrayList<>();
                Player player = event.player;
                addx(player, eventParameters, "SWIM");
                ApplySuitEffect(player, ExSuit.Trigger.SWIM);
            }

            //
//        @SubscribeEvent
//        public static void PlayerEat(Item eventC){
//            if (eventC.getEntity()==null)return;
//            if ( eventC.getItemStack().getFoodProperties(eventC.getEntity())!=null) ApplySuitEffect(eventC.getEntity(), ExSuit.Trigger.EAT);
//
//        }
//        @SubscribeEvent
//        public static void PlayerDamage(LivingDamageEvent eventC){
//            if ((eventC.getEntity() instanceof Player player))ApplySuitEffect(player, ExSuit.Trigger.ATTACK);
//        } 该用法不稳定 已移植hurt
            @SubscribeEvent
            public static void PlayerLiving(TickEvent.PlayerTickEvent event) {
                Player player = event.player;
                if (player.level().isClientSide) {
                    return;
                }
                ApplySuitEffect(player, ExSuit.Trigger.TICK);

            }


        }

        @SubscribeEvent
        public static void grind(GrindstoneEvent.OnPlaceItem event) {
            handleArmorChangeExpectSuit(event.getOutput(), false);
        }

        public static boolean hasAttrOrBow(ItemStack stack) {
            if (stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem) return true;
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (!stack.getAttributeModifiers(slot).isEmpty()) {
                    return true;

                }
            }
            return false;
        }

        public static boolean handleArmorChange(Player player, ItemStack fromStack, ItemStack toStack, boolean isClientSide) throws ScriptException {
            if (handleArmorChangeExpectSuit(toStack, isClientSide)) return false;
            boolean isExSuitOperate;
            isExSuitOperate = SuitOperate(player, toStack, fromStack);
            return isExSuitOperate;

        }

        private static boolean handleArmorChangeExpectSuit(ItemStack toStack, boolean isClientSide) {
            // boolean isExSuitOperate = false;
            if (!isClientSide) {
                ItemInfo itemInfo = ItemInfo.of(toStack);
                ModifierEntryHelper modifierEntryHelper = itemInfo.getModifierEntryHelper();
                ModifierEntryHelper.moveOldEntry(toStack);
                ItemLevelHelper.moveOldLevel(toStack);
                String string = ExUtil.getItemID(toStack);
                for (String s : UnMatchingModIDs) {
                    if (string.startsWith(s)) return true;
                }
//                if (itemsDefaultEntry.containsKey(string)) {
//                    for (ModifierEntry exElement : itemsDefaultEntry.get(string)) {
//                        new ModifierEntryHelper(toStack).addModifierEntry(ModifierInstant.of(ModifierEntryHelper.getEntry(exElement.id)), true, true);
//                    }
//                }

                if (CuriosUtil.isCuriosItem2(toStack,false)) {
                    if (toStack.getTag() == null || modifierEntryHelper.getModifierEntriesSize() <= 0) {
                        RandomEntryCurios(toStack, 0, refresh_time, "none");
                    }
                    if (toStack.getTag() != null) {
                        if (toStack.getTag().contains("modifier_refresh")) {
                            if (toStack.getTag().getBoolean("modifier_refresh")) {
                                toStack.getTag().remove("modifier_refresh");
                                toStack.getTag().remove("UNKNOWN");
                                RandomEntryCurios(toStack, toStack.getTag().getInt("modifier_refresh_rarity"), toStack.getTag().getInt("modifier_refresh_add"), toStack.getTag().getString("wash_item"));
                            }
                        }
                    }
                } else {
                    if (!toStack.getTags().filter(e -> RefreshContainTagHandle.refreshContainTag.contains(e.toString())).toList().isEmpty() ||
                            RefreshContainItemHandle.refreshContainItem.contains(string) ||
                            hasAttrOrBow(toStack) && !ModifierEntry.getType(toStack).stream().filter(e -> e != ExType.UNKNOWN.get()).toList().isEmpty() && toStack.getItem().getMaxStackSize(toStack) == 1) {
                        if (toStack.getTag() == null || modifierEntryHelper.getModifierEntriesSize() <= 0) {
                            ModifierSlotHelper modifierSlotHelper = ModifierSlotHelper.of(toStack);
                            if (Config.FirstAddSlots && !modifierSlotHelper.validList()) {
                                modifierSlotHelper.addSlot(ModifierSlotHandle.getSlot(ResourceLocation.tryParse("exmodifier:front")));
                                modifierSlotHelper.addSlot(ModifierSlotHandle.getSlot(ResourceLocation.tryParse("exmodifier:centre")));
                            }
                            RandomEntry(toStack, 0, refresh_time, "none", 0);
                        }
                        if (toStack.getTag() != null) {
                            if (toStack.getTag().contains("modifier_refresh")) {
                                if (toStack.getTag().getBoolean("modifier_refresh")) {
                                    toStack.getTag().remove("modifier_refresh");
                                    toStack.getTag().remove("UNKNOWN");
                                    RandomEntry(toStack, toStack.getTag().getInt("modifier_refresh_rarity"), toStack.getTag().getInt("modifier_refresh_add"), toStack.getTag().getString("wash_item"), 0);
                                }
                            }
                        }
                    }
                }

                int addLevelSystemCount = Config.add_level_system_count;
                if (Config.add_level_system_count != 0) {
                    ItemLevelRefresh(toStack, 0, addLevelSystemCount, "none");
                }
            }
            return false;
        }

        @SubscribeEvent
        public static void armorChange(LivingEquipmentChangeEvent event) throws ScriptException {
            if (event.getEntity() instanceof Player player) {
                boolean b = handleArmorChange(player, event.getFrom(), event.getTo(), event.getEntity().level().isClientSide());
                MinecraftForge.EVENT_BUS.post(new ExAfterArmorChange(event, b));
            }
        }


        public static boolean SuitOperate(@NotNull Player player, ItemStack stack1, ItemStack stack2) {

            if (player.level().isClientSide) return false;
            boolean flag = false;
            flag = handleStack(player, stack1, WEAR);
            if (handleStack(player, stack2, TAKE)) flag = true;
            return flag;
        }

        private static boolean handleStack(Player player, ItemStack stack, EntityAttrUtil.WearOrTake effectType) {
            if (player == null) return false;
            boolean flag = false;
            if (stack.isEmpty() &&  effectType == WEAR) return false;
            //  if (!hasAttr(stack)) return false;

            CompoundTag tag = stack.getTag();
            ModifierEntryHelper modifierEntryHelper = ModifierEntryHelper.of(stack);
            if (tag == null || modifierEntryHelper.getModifierEntriesSize() <= 0) return false;

            //int effectMultiplier = effectType == WEAR ? 1 : -1;
            List<ModifierEntry> modifierEntries = modifierEntryHelper.getModifierEntriesB();
            if (modifierEntries.isEmpty()) return false;
            for (int i = 0; i < modifierEntries.size(); i++) {
                String modifier = modifierEntries.get(i).id;
                if (modifier.isEmpty()) continue;
                List<String> founds = new ArrayList<>();
                List<ExSuit> suits = ExSuitHandle.FindExSuitFromEntry(modifier);
                for (ExSuit suit : suits) {
                    if (founds.contains(suit.id)) continue;
                    founds.add(suit.id);
                    if (effectType == WEAR && suit.setting.getOrDefault("excludeArmorInHand", "false").equals("true") && stack.getItem() instanceof ArmorItem) {
                        continue;
                    }

                    if (effectType == TAKE && suit.setting.getOrDefault("excludeArmorInHand", "false").equals("true") && stack.getItem() instanceof ArmorItem) {
                        continue;
                    }

                    if (effectType == WEAR) {
                        ExSuitHandle.addSuitLevel(player, suit, 1);
                    } else {
                        ExSuitHandle.RemoveSuitLevel(player, suit, 1);
                    }
                    flag = true;


                    int suitLevel = ExSuitHandle.GetSuitLevel(player, suit);
                    List<AttriGetherNormal> attriGethers = suit.attriGether.get(effectType == WEAR ? suitLevel : suitLevel + 1);

                    if (attriGethers != null) {
                        for (AttriGetherNormal attrGether : attriGethers.stream().filter(attrGether -> attrGether.getOnlyItems().isEmpty()).toList()) {
                            Exmodifier.LOGGER.debug("items : " + attrGether.getOnlyItems().toString());
                            //    if (attrGether.getOnlySlots() ==null|| attrGether.getOnlySlots().isEmpty()) {

                            ExApplySuitAttrigetherEvent event1 = new ExApplySuitAttrigetherEvent(player, stack, effectType, attrGether);
                            try {
                                Exmodifier.LOGGER.debug("Apply Suit AttriGether: " + attrGether.attribute.getDescriptionId() + " " + attrGether.attributeModifier.getOperation().toString() + " " + attrGether.attributeModifier.getAmount());
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                            MinecraftForge.EVENT_BUS.post(event1);
                            if (!event1.isCanceled())
                                EntityAttrUtil.entityAddAttrTF(event1.attriGether.attribute, event1.attriGether.getModifier(), event1.player, event1.effectType);
                            //  }
                        }
                    }
                    ExSuitApplyOnChangeEvent event = new ExSuitApplyOnChangeEvent(player, suit, i, effectType);
                    MinecraftForge.EVENT_BUS.post(event);
//                    player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
//                        Map<String, Integer> suitsNum = capability.SuitsNum;
//                        if (suitsNum != null) {
//                            if (suitLevel > 0 && !suitsNum.containsKey(suit.id)) {
//
//                            } else if (suitLevel <= 0) {
//                                suitsList.remove(suit);
//                            }
//                            suitsList.removeIf(suit1 -> !ExSuitHandle.LoadExSuit.containsValue(suit1));
//                            capability.Suits = suitsList;
//                            capability.syncPlayerVariables(player);
//                        }
//                    });

                }
            }
            return flag;
        }

        public static Runnable init(Runnable runnable) throws IOException {
            clearOldData();
            BaseItemSelected.IDS = new HashMap<>();
            RefreshContainTagHandle.readConfig();
            RefreshContainItemHandle.readConfig();
            ModifierHandle.sendClearDataToAllClient();
            ExTypeHandle.readConfig();
            ItemQualityHandle.init();
            ExElementHandle.init();
            ZipHandle.ZipFunction init = ZipHandle.init();
            if (runnable != null) runnable.run();

            ItemQualityHandle.init2();

            ModifierHandle.readConfig();
            ExSuitHandle.readConfig();
            ModifierSlotHandle.reload();



            ExElementHandle.init2();
            ExElementHandle.init3();
            RefineHandle.init();
            init.elementDefault().forEach(Runnable::run);
            init.defaultEntry().forEach(Runnable::run);
            init.suit() .forEach(Runnable::run);

            ModifierHandle.EEMatchQueueHandle();
            LanguageLoader.load(LanguageLoader.LANGUAGES_FILE_PATH);
            clearReadTempData();
            MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
            if (currentServer != null) {
            if (runnable!=null)return () -> sendUpdatedModifiersToClients(currentServer);
            sendUpdatedModifiersToClients(currentServer);
            }
            return null;
        }

        @SubscribeEvent
        public static void atReload(AddReloadListenerEvent event) throws IOException {
//            Runnable init = init(() -> {
//            event.addListener(new ModifierPreparableReloadListener());
//            event.addListener(new ElementPreparableReloadListener());
//            event.addListener(new DefaultEntityElementPreparableReloadListener());
//            event.addListener(new DefaultItemElementPreparableReloadListener());
//            });
//            if (init != null) {
//                init.run();
//            }
            event.addListener(new ExSuitPreparableReloadListener());
            event.addListener(new WashingMaterialsPreparableReloadListener());

            event.addListener(new ModifierPreparableReloadListener());

            event.addListener(new ElementPreparableReloadListener());
            event.addListener(new DefaultEntityPreparableReloadListener());
            event.addListener(new RefineItemRecordPreparableReloadListener());
            event.addListener(new DefaultItemPreparableReloadListener());


        }

        @SubscribeEvent
        public static void playJoinServer(PlayerEvent.PlayerLoggedInEvent event) {
            Player entity = event.getEntity();
            if (!entity.level().isClientSide()) {
                DataCache dataCache = DataCache.create();
                ModifierHandle.sendClearDataToClient((ServerPlayer) entity);

                sendExmoServerDataToServerPlayer((ServerPlayer) entity, dataCache);
            }
        }
    }

    public static record DataCache(
            List<ExSuit> exSuits,
            List<ModifierEntry> modifierEntries,
            List<ExElement> exElements,
            List<DefaultItemElement> defaultItemElements,
            List<DefaultEntityElement> defaultEntityElements,
            LangMessage.LangMessageHandler langMessageHandler

    ) {
        public static DataCache create() {
            return new DataCache(
                    new ArrayList<>(ExSuitHandle.LoadExSuit.values()),
                    new ArrayList<>(ModifierHandle.modifierEntryMap.values()),
                    new ArrayList<>(ExElementHandle.exElements.values()),
                    new ArrayList<>(ExElementHandle.elementDefaultMap.values()),
                    new ArrayList<>(ExElementHandle.elementDefaultMap2.values()),
                    new LangMessage.LangMessageHandler(LanguageLoader.LANGUAGES)
            );
        }
    }
    public static void sendExmoServerDataToServerPlayer(ServerPlayer entity,DataCache dataCache) {
        // 使用副本遍历以避免 ConcurrentModificationException
        List<ExSuit> exSuits = dataCache.exSuits();
        for (ExSuit exSuit : exSuits) {
            ModifierHandle.sendExSuitToClient(exSuit, entity);
        }
        List<ModifierEntry> modifierEntries = dataCache.modifierEntries();
        for (ModifierEntry modifierEntry : modifierEntries) {
            ModifierHandle.sendModifierEntryToClient(modifierEntry, entity);
        }
        List<ExElement> exElements = dataCache.exElements();
        for (ExElement exElement : exElements) {
            ModifierHandle.sendElementToClient(exElement, entity);
        }
        List<DefaultItemElement> defaultItemElements = dataCache.defaultItemElements();
        for (var e : defaultItemElements) {
            ModifierHandle.sendDefaultItemElementToClient(e, entity);
        }
        List<DefaultEntityElement> defaultEntityElements = dataCache.defaultEntityElements();
        for (var e : defaultEntityElements) {
            ModifierHandle.sendDefaultEntityElementToClient(e, entity);
        }
        LangMessage.LangMessageHandler langMessageHandler = dataCache.langMessageHandler;
        ModifierHandle.sendLangMessageToClient(langMessageHandler, entity);


    }

    public static void clearOldData() {
        ExSuitHandle.LoadExSuit.clear();
        ExElementEntityData.defaultEntityAttributes.clear();
        LanguageLoader.LANGUAGES.clear();
        ModifierHandle.modifierEntryMap.clear();
        ExTypeHandle.itemTypes.values().removeIf(e -> !ExType.defaultTypes.contains(e.name()));
        ItemLevelHandle.ItemLevels.clear();
        ModifierHandle.onlyCanRefreshPointEntryItemIds.clear();
        ModifierHandle.cantWashItemIds.clear();
        ExElementHandle.elementDefaultMap.clear();
        ExElementHandle.elementDefaultMap2.clear();
        ExElementHandle.exElements.clear();
        itemsDefaultEntry.clear();
        ModifierHandle.materialsList.clear();
        ModifierSlotHandle.registerSlots.clear();
        ModifierSlotHandle.unLockSlotItems.clear();
        ItemQualityHandle.itemQualityMap.clear();
        ItemQualityHandle.itemDefaultQualityMap.clear();

        clearReadTempData();

    }

    public static void clearReadTempData() {
        ModifierHandle.Foundmoconfigs.clear();
        ItemLevelHandle.Foundlvconfigs.clear();
        ExSuitHandle.FoundSuitConfigs.clear();
        ItemQualityHandle.FoundQualityConfigs.clear();
        ItemQualityHandle.FoundDefaultQualityConfigs.clear();
        ExTypeHandle.FoundTypeConfigs.clear();
        ExElementHandle.FoundElementConfigs.clear();
        FoundEntityDefaultElementConfigs.clear();
        FoundDefaultElementConfigs.clear();
    }
}

