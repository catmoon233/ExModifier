package net.exmo.exmodifier.content.event;

import net.exmo.exmodifier.Config;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.modifier.RefreshContainTagHandle;
import net.exmo.exmodifier.content.client.LanguageLoader;
import net.exmo.exmodifier.content.event.parameter.EventParameter;
import net.exmo.exmodifier.content.helper.*;
import net.exmo.exmodifier.content.level.ItemLevelHandle;
import net.exmo.exmodifier.content.modifier.*;
import net.exmo.exmodifier.content.quality.ItemQualityHandle;
import net.exmo.exmodifier.content.selected.BaseItemSelected;
import net.exmo.exmodifier.content.slot.ModifierSlotHandle;
import net.exmo.exmodifier.content.suit.ExSuit;
import net.exmo.exmodifier.content.suit.ExSuitHandle;
import net.exmo.exmodifier.content.type.ExType;
import net.exmo.exmodifier.content.type.ExTypeHandle;
import net.exmo.exmodifier.content.resources.ZipHandle;
import net.exmo.exmodifier.events.*;
import net.exmo.exmodifier.network.ExModifiervaV;
import net.exmo.exmodifier.util.AttributeCuriosHandle;
import net.exmo.exmodifier.util.CuriosUtil;
import net.exmo.exmodifier.util.DynamicExpressionEvaluator;
import net.exmo.exmodifier.util.EntityAttrUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
//import net.minecraftforge.client.eventC.MovementInputUpdateEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.GrindstoneEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2ic;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static net.exmo.exmodifier.Config.refresh_time;
import static net.exmo.exmodifier.content.level.ItemLevelHandle.ItemLevelRefresh;
import static net.exmo.exmodifier.content.modifier.ModifierHandle.CommonEvent.*;
import static net.exmo.exmodifier.content.modifier.ModifierHandle.itemsDefaultEntry;
import static net.exmo.exmodifier.util.EntityAttrUtil.WearOrTake.TAKE;
import static net.exmo.exmodifier.util.EntityAttrUtil.WearOrTake.WEAR;
import static net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil.renderTooltipBackground;

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
        public static void TooltipChange(ItemTooltipEvent event) {
            ItemStack itemStack = event.getItemStack();
            if (itemStack.getTag()!= null){
           // if (!CuriosUtil.isCuriosItem(event.getItemStack())) {

                List<Component> toolTip1 = event.getToolTip();

                List<Component> toolTip = CommonEvent.ItemToolTipsChange(itemStack, toolTip1, event.getEntity());
                if (toolTip.isEmpty())return;
                List<Component> tooo = new ArrayList<>();
                tooo.add(toolTip.get(0));
                for (var a : ItemQualityHelper.of(itemStack).getQualityEntriesTooltip()){
                    if (a.isShowInHeadTooltip){
                     //   tooo.set(0,a.mutableComponent.append(Component.literal(" §r")).append(toolTip1.get(0)));
                    }else tooo.add(a.mutableComponent);
                }
                tooo.addAll(ItemLevelHandle.genItemLevelInfo(itemStack));
                for (int i = 1; i < toolTip.size(); i++){
                    tooo.add(toolTip.get(i));
                }
                toolTip1.clear();
                toolTip1.addAll(tooo);
         //   }

                }
        }

        @SubscribeEvent
        public static void CuriosChange(CurioChangeEvent event) {

            if (!(event.getEntity() instanceof Player player))return;

            ItemStack stack = event.getTo();
            for (String s : UnMatchingModIDs){
                if (ForgeRegistries.ITEMS.getKey(stack.getItem()).toString().startsWith(s))return;
            }
            ModifierEntryHelper modifierEntryHelper = ModifierEntryHelper.of(stack);
            if (stack.getTag() == null || modifierEntryHelper.getModifierEntriesSize()<=0) {
                RandomEntryCurios(stack, 0, refresh_time,"none");
            }
            if (stack.getTag() != null) {
                if (stack.getTag().contains("modifier_refresh")) {
                    if (stack.getTag().getBoolean("modifier_refresh")) {
                        stack.getTag().remove("modifier_refresh");
                        stack.getTag().remove("UNKNOWN");

                        //  stack.getTag().putInt("exmodifier_armor_modifier_applied", 0);

                        RandomEntryCurios(stack, stack.getOrCreateTag().getInt("modifier_refresh_rarity"), stack.getOrCreateTag().getInt("modifier_refresh_add"),stack.getTag().getString("wash_item"));
                        AttributeCuriosHandle.handleCurios(new CurioChangeEvent(player,event.getIdentifier(),event.getSlotIndex(),event.getFrom(),stack));
                    }
                }
            }
            SuitOperate((Player) event.getEntity(), event.getTo(), event.getFrom());

        }
//@SubscribeEvent
//        public static void CuriosTooltipChange(RenderTooltipEvent.GatherComponents event) {
//            ItemStack stack = event.getItemStack();
//            if (stack.getTag()==null)return;
//            if (CuriosUtil.isCuriosItem(stack)) {
//                boolean addf = false;
//                for (ModifierEntry modifierEntry : new ModifierEntryHelper(stack).getModifierEntriesB()) {
//                    if (stack.getTag().getBoolean("UNKNOWN")) {
//                        event.getTooltipElements().add(Either.left(Component.translatable("null")));
//                        event.getTooltipElements().add(Either.left(Component.translatable("modifier.entry.UNKNOWN")));
//                    } else {
//                        if (!addf) {
//                            addf = true;
//                            event.getTooltipElements().add(Either.left(Component.translatable("null")));
//                            event.getTooltipElements().add(Either.left(Component.translatable("modifier.entry")));
//                            for (ExSuit suit : ExSuitHandle.LoadExSuit.values().stream().filter(exSuit -> exSuit.entry.contains(modifierEntry))
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
//                        event.getTooltipElements().add(Either.left((Component.translatable("modifier.entry." + modifierEntry.Id.substring(2)))));
//
//                    }
//                }
//            }
//        }
@SubscribeEvent
public static void iLevelAttriGetherModifier(ExApplyEntryAttrigetherEvent event){
    if(event.attriGether.Expression!=null&&!event.attriGether.Expression.isEmpty()){
        ItemStack stack = event.stack;
        AttributeModifier modifier =  event.attriGether.getModifier();
        int level = event.modifierInstant.getLevel();
        Exmodifier.LOGGER.debug("iLevelAttriGetherModifier: "+event.attriGether.Expression + " level: "+level);
        DynamicExpressionEvaluator dynamicExpressionEvaluator = new DynamicExpressionEvaluator();
        dynamicExpressionEvaluator.setVariable("level", level);
        dynamicExpressionEvaluator.setVariable("l", level);
        double amout = dynamicExpressionEvaluator.evaluate(event.attriGether.Expression);
        event.attriGether.modifier = new AttributeModifier(modifier.getId(), modifier.getName(), amout, modifier.getOperation());
    }
}
        public static List<Component> ItemToolTipsChange(ItemStack stack, List<Component> tooltip, Player player) {
            if (stack.getTag()!=null){
                ModifierEntryHelper modifierEntryHelper = ModifierEntryHelper.of(stack);
                if (stack.getTag().getBoolean("UNKNOWN")) {
                    tooltip.add(Component.translatable("null"));
                    tooltip.add(Component.translatable("modifier.entry.UNKNOWN"));
                } else {
                    if (modifierEntryHelper.getModifierEntriesSize()>0) {



                        for (ModifierInstant modifierEntry : new ItemInfo(stack).getModifierEntryHelper().getModifierEntries()) {
                            if (modifierEntry.getSlot().isPresent())continue;
                            // Exmodifier.LOGGER.debug("modifier Id:" + modifierEntry.Id);
                            if (!Config.compact_tooltip) tooltip.add(Component.translatable("null"));
                            tooltip.addAll(generateEntryTooltip(modifierEntry, player, stack));

                        }
                    }
                    if (stack.getTag().getBoolean("can_add_max"))tooltip.add(Component.translatable("modifier.entry.can_add_max"));

                }
            }

            return tooltip;
        }
        @OnlyIn(Dist.CLIENT)
        @SubscribeEvent
        public static void RenderExtraTooltip(RenderTooltipEvent.Pre event) {
            var g = event.getGraphics();
            var mc = Minecraft.getInstance();
            if (mc.screen != null) {
                List<Component> extraComponents = ModifierSlotHandle.getTooltip(event.getItemStack(), mc.player);
                if (extraComponents.isEmpty())return;
                @NotNull List<ClientTooltipComponent> originalComponents = event.getComponents();

                Font font = mc.font;

                // Calculate the width and height of the original tooltip
                int originalWidth = 30;
                for (ClientTooltipComponent component : originalComponents) {
                    originalWidth = Math.max(originalWidth, (component.getWidth(font)));
                }
               // int originalHeight = originalComponents.size() * font.lineHeight;

                // Calculate the width and height of the extra tooltip
                int extraWidth = 0;
                for (Component component : extraComponents) {
                    extraWidth = Math.max(extraWidth, font.width(component));
                }
                int extraHeight = extraComponents.size() * font.lineHeight;
                Vector2ic vector2ic = event.getTooltipPositioner().positionTooltip(g.guiWidth(), g.guiHeight(), event.getX(), event.getY(), extraWidth, extraHeight);
                int screenW = mc.screen.width;
                int screenH = mc.screen.height;
                int tooltipX = vector2ic.x();
                int tooltipY = vector2ic.y() ; // Start at the same Y as the original tooltip

                // Determine the position for the extra tooltip
//                if (tooltipX + originalWidth + extraWidth > screenW) {
//                    // Not enough space on the right, try the left
//                    if (tooltipX - extraWidth < 0) {
//                        // Not enough space on the left, render on the right
//                        tooltipX = event.getX() + originalWidth+5;
//                    } else {
//                        // Render on the left
                        tooltipX = event.getX() - extraWidth  ;
//                   }
//                }

                // Adjust X position based on longest line in respective tooltips
           //     if (tooltipX == event.getX()) {
                    // Rendering on the right, adjust by longest line in original tooltip
            //        tooltipX += originalWidth -10;
            //    } else {
                    // Rendering on the left, adjust by longest line in extra tooltip
            //        tooltipX -= extraWidth -50;
            //    }

                // Render the extra tooltip text first with a darker color
                AtomicInteger line = new AtomicInteger();
                int finalTooltipX = tooltipX;
                g.pose().pushPose();
                TooltipRenderUtil.renderTooltipBackground(g, tooltipX, tooltipY, extraWidth, extraHeight, 400);
                g.pose().translate(0.0F, 0.0F, 400.0F);


                extraComponents.forEach(component -> {
                    ClientTooltipComponent clientTooltipComponent = ClientTooltipComponent.create(component.getVisualOrderText());
                    clientTooltipComponent.renderText(font, finalTooltipX, tooltipY + line.get() * font.lineHeight, g.pose().last().pose(), g.bufferSource());
                    line.getAndIncrement();
                });
                g.pose().popPose();
                // Render the extra tooltip background above the text

                // Update the event's Y position to account for the extra tooltip
            //    event.setY(tooltipY + extraHeight);
            }
        }









        @SubscribeEvent
        public static void AtJoinGame(PlayerEvent.PlayerLoggedInEvent event) {
            Player player = (Player) event.getEntity();
            player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {

                Map<ExSuit,Integer> map = capability.SuitsNum ;
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
            if(player==null)return;
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

                for (ExSuit suit : capability.Suits){
                    if (suit == null)continue;
                    int suitLevel = ExSuitHandle.GetSuitLevel(player, suit);
                    for (int level = 1; level <= suitLevel; level++) {
                        //事件触发器在此 !!!!!!!!!!!!!!!!!!!
                        if (suit.getTriggers().get(level) != trigger)continue;
                        // Run commands if present for the current suit level
                        List<String> commands = suit.getCommands().get(level);
                        if (commands != null && !player.level().isClientSide() && player.getServer() != null &&commandSourceStack!=null) {
                            int finalLevel = level;
                            commands.forEach(command ->{
                                if (trigger == ExSuit.Trigger.ATTACK || trigger == ExSuit.Trigger.PROJECTILE_HIT) {
                                    String string = player.getPersistentData().getString("hurtentity-uuid");
                                    if (!string.equals("null")) {
                                        command = command.replace("$(hurtentity)", string);
                                    }
                                }
                                command = command.replace("$(level)", finalLevel +"");
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

        public static void addx(Player player, List<EventParameter<?>> eventParameters, String name){
        ItemLevelHandle.ItemAddXpAuto(player, eventParameters, name);

    }
    @Mod.EventBusSubscriber
    public static class cheekEvent{
        @SubscribeEvent
        public static void PlayerHurtAndAttack(LivingHurtEvent event){
            if ((event.getEntity() instanceof Player player)){
                    List<EventParameter<?>> eventParameters = new java.util.ArrayList<>();
                    eventParameters.add(new EventParameter<>("amount", event.getAmount()));
                    eventParameters.add(new EventParameter<>("max_health", player.getAttributeValue(Attributes.MAX_HEALTH)));
                    addx(player,eventParameters,"ON_HURT");
                ApplySuitEffect(player, ExSuit.Trigger.ON_HURT);
            }
            if ((event.getSource().getEntity() instanceof Player player)){
                    List<EventParameter<?>> eventParameters = new java.util.ArrayList<>();
                    eventParameters.add(new EventParameter<>("amount", event.getAmount()));
                eventParameters.add(new EventParameter<>("max_health", player.getAttributeValue(Attributes.MAX_HEALTH)));
                    addx(player,eventParameters,"ATTACK");
                if (event.getEntity()!=null) player.getPersistentData().putString("hurtentity-uuid",event.getEntity().getUUID().toString());
                ApplySuitEffect(player, ExSuit.Trigger.ATTACK);
                player.getPersistentData().putString("hurtentity-uuid","null");
            }
        }
        @SubscribeEvent
        public static void PlayerJump(LivingEvent.LivingJumpEvent event){
            if ((event.getEntity() instanceof Player player)){
                    List<EventParameter<?>> eventParameters = new java.util.ArrayList<>();
                    addx(player,eventParameters,"JUMP");
                ApplySuitEffect(player, ExSuit.Trigger.JUMP);
            }
        }
        @SubscribeEvent
        public static void Digger(BlockEvent.BreakEvent event){
                 Player player = event.getPlayer();
                List<EventParameter<?>> eventParameters = new java.util.ArrayList<>();
                addx(player,eventParameters,"DIG");
                ApplySuitEffect(player, ExSuit.Trigger.DIG);

        }
        @SubscribeEvent
        public static void PlayerDeathAndKill(LivingDeathEvent event){
            if ((event.getEntity() instanceof Player player)){
                List<EventParameter<?>> eventParameters = new java.util.ArrayList<>();
                addx(player,eventParameters,"DIE");
                ApplySuitEffect(player, ExSuit.Trigger.DIE);
            }
            if ((event.getSource().getEntity() instanceof Player player)){
                List<EventParameter<?>> eventParameters = new java.util.ArrayList<>();
                addx(player,eventParameters,"KILL");
                ApplySuitEffect(player, ExSuit.Trigger.KILL);
            }
        }
        @SubscribeEvent
        public static void PlayerProjectile(ProjectileImpactEvent event){
            if ((event.getProjectile().getOwner() instanceof Player player)){
                List<EventParameter<?>> eventParameters = new java.util.ArrayList<>();
                if (event.getEntity()!=null) player.getPersistentData().putString("hurtentity-uuid",event.getEntity().getUUID().toString());
                addx(player,eventParameters,"PROJECTILE_HIT");
                ApplySuitEffect(player, ExSuit.Trigger.PROJECTILE_HIT);
            }
        }
        @SubscribeEvent
        public static void PlayerShoot(ArrowLooseEvent event){
            List<EventParameter<?>> eventParameters = new java.util.ArrayList<>();
            addx(event.getEntity(),eventParameters,"SHOOT");
           ApplySuitEffect(event.getEntity(), ExSuit.Trigger.SHOOT);
        }
//        @SubscribeEvent
//        public static void PlayerMove(MovementInputUpdateEvent eventC){
//             ApplySuitEffect(eventC.getEntity(), ExSuit.Trigger.MOVECHANGE);
//
//        }
        @SubscribeEvent
        public static void PlayerSwing(LivingSwingEvent event){
            if ((event.getEntity() instanceof Player player))   {
                List<EventParameter<?>> eventParameters = new java.util.ArrayList<>();
                addx(player,eventParameters,"SWING");
                ApplySuitEffect(player, ExSuit.Trigger.SWING);
            }
        }
        @SubscribeEvent
        public static void PlayerCrit(CriticalHitEvent event){
            List<EventParameter<?>> eventParameters = new java.util.ArrayList<>();
            eventParameters.add(new EventParameter<>("amount", event.getDamageModifier()));
            Player player = event.getEntity();
            addx(player,eventParameters,"CRIT");
       ApplySuitEffect(player, ExSuit.Trigger.CRIT);
        }
        @SubscribeEvent
        public static void PlayerDodge(ExDodgeEvent event){
            if ((event.getEntity() instanceof Player player))
                if (event.result == ExDodgeEvent.resultType.MISS) {
                    List<EventParameter<?>> eventParameters = new java.util.ArrayList<>();
                    addx(player,eventParameters,"DODGE");
                    ApplySuitEffect(player, ExSuit.Trigger.DODGE);
                }

        }
        @SubscribeEvent
        public static void PlayerUseItem(LivingEntityUseItemEvent event){
            if(event.getEntity() instanceof Player player){
                List<EventParameter<?>> eventParameters = new java.util.ArrayList<>();
                addx(player,eventParameters,"ON_USE");
                ApplySuitEffect(player,ExSuit.Trigger.ON_USE);
            }
        }
        @SubscribeEvent
        public static void PlayerSwim(LivingPlayerSwimEvent event){
            List<EventParameter<?>> eventParameters = new java.util.ArrayList<>();
            Player player = event.player;
            addx(player,eventParameters,"SWIM");
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
            handleArmorChangeExpectSuit(event.getOutput(),false);
        }

        public static boolean hasAttrOrBow(ItemStack stack) {
            if (stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem)return true;
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
            boolean isExSuitOperate = false;
            if (!isClientSide) {
                ItemStack stack = toStack;
                ItemInfo itemInfo = ItemInfo.of(stack);
                ModifierEntryHelper modifierEntryHelper = itemInfo.getModifierEntryHelper();
                ModifierEntryHelper.moveOldEntry(stack);
                ItemLevelHelper.moveOldLevel(stack);
                String string = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
                for (String s : UnMatchingModIDs) {
                    if (string.startsWith(s)) return true;
                }
                if (itemsDefaultEntry.containsKey(string)) {
                    for (ModifierEntry modifierEntry : itemsDefaultEntry.get(string)) {
                        new ModifierEntryHelper(stack).addModifierEntry(ModifierInstant.of(ModifierEntryHelper.getEntry(modifierEntry.id)), true, true);
                    }
                }

                if (CuriosUtil.isCuriosItem2(stack)) {
                    if (stack.getTag() == null || modifierEntryHelper.getModifierEntriesSize() <= 0) {
                        RandomEntryCurios(stack, 0, refresh_time, "none");
                    }
                    if (stack.getTag() != null) {
                        if (stack.getTag().contains("modifier_refresh")) {
                            if (stack.getTag().getBoolean("modifier_refresh")) {
                                stack.getTag().remove("modifier_refresh");
                                stack.getTag().remove("UNKNOWN");
                                RandomEntryCurios(stack, stack.getTag().getInt("modifier_refresh_rarity"), stack.getTag().getInt("modifier_refresh_add"), stack.getTag().getString("wash_item"));
                            }
                        }
                    }
                } else {
                    if (!stack.getTags().filter(e -> RefreshContainTagHandle.refreshContainTag.contains(e.toString())).toList().isEmpty() ||
                            RefreshContainItemHandle.refreshContainItem.contains(string) ||
                            hasAttrOrBow(stack) && !ModifierEntry.getType(stack).stream().filter(e -> e != ExType.UNKNOWN.get()).toList().isEmpty() && stack.getItem().getMaxStackSize(stack) == 1) {
                        if (stack.getTag() == null || modifierEntryHelper.getModifierEntriesSize() <= 0) {
                            ModifierSlotHelper modifierSlotHelper = ModifierSlotHelper.of(stack);
                            if (Config.FirstAddSlots && !modifierSlotHelper.validList()) {
                                modifierSlotHelper.addSlot(ModifierSlotHandle.getSlot(ResourceLocation.tryParse("exmodifier:front")));
                                modifierSlotHelper.addSlot(ModifierSlotHandle.getSlot(ResourceLocation.tryParse("exmodifier:centre")));
                            }
                            RandomEntry(stack, 0, refresh_time, "none",0);
                        }
                        if (stack.getTag() != null) {
                            if (stack.getTag().contains("modifier_refresh")) {
                                if (stack.getTag().getBoolean("modifier_refresh")) {
                                    stack.getTag().remove("modifier_refresh");
                                    stack.getTag().remove("UNKNOWN");
                                    RandomEntry(stack, stack.getTag().getInt("modifier_refresh_rarity"), stack.getTag().getInt("modifier_refresh_add"), stack.getTag().getString("wash_item"),0);
                                }
                            }
                        }
                    }
                }

                int addLevelSystemCount = Config.add_level_system_count;
                if (Config.add_level_system_count != 0) {
                    ItemLevelRefresh(stack, 0, addLevelSystemCount, "none");
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
            flag =  handleStack(player, stack1, EntityAttrUtil.WearOrTake.WEAR);
            if (handleStack(player, stack2, EntityAttrUtil.WearOrTake.TAKE))flag = true;
            return flag;
        }

        private static boolean handleStack(Player player, ItemStack stack, EntityAttrUtil.WearOrTake effectType) {
            if (player==null)return false;
            boolean flag = false;
          //  if (!hasAttr(stack)) return false;

            CompoundTag tag = stack.getTag();
            ModifierEntryHelper modifierEntryHelper = ModifierEntryHelper.of(stack);
            if (tag == null || modifierEntryHelper.getModifierEntriesSize()<=0) return false;

            int effectMultiplier = effectType == WEAR ? 1 : -1;
            List<ModifierEntry> modifierEntries = modifierEntryHelper.getModifierEntriesB();
            if (modifierEntries.isEmpty()) return false;
            for (int i = 0; i< modifierEntries.size(); i++) {
                String modifier = modifierEntries.get(i).id;
                if (modifier.isEmpty()) continue;
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
                        ExSuitHandle.addSuitLevel(player, suit, 1);
                    } else {
                        ExSuitHandle.RemoveSuitLevel(player, suit, 1);
                    }
                    flag =true;


                    int suitLevel = ExSuitHandle.GetSuitLevel(player, suit);
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
                        List<ExSuit> suitsList = capability.Suits;
                        if (suitLevel > 0 && !suitsList.contains(suit)) {
                            suitsList.add(suit);
                        } else if (suitLevel <= 0 && suitsList.contains(suit)) {
                            suitsList.remove(suit);
                        }
                        for (ExSuit suit1 : suitsList){
                            if (!ExSuitHandle.LoadExSuit.containsValue(suit1))suitsList.remove(suit1);
                        }
                        capability.Suits = suitsList;
                        capability.syncPlayerVariables(player);
                    });
                }
            }
            return flag;
        }
        public static void init(Runnable runnable) throws IOException {
            ZipHandle.init()
            BaseItemSelected.IDS = new HashMap<>();
            RefreshContainTagHandle.readConfig();
            RefreshContainItemHandle.readConfig();
            ModifierHandle.sendClearModifierEntryToAllClient();
            ModifierHandle.readConfig();
            ExTypeHandle.readConfig();
            ExSuitHandle.readConfig();
            ModifierSlotHandle.reload();
            ItemQualityHandle.init();
            if (runnable!=null) runnable.run();
            for (ModifierEntry modifierEntry : ModifierHandle.modifierEntryMap.values()) {
                ModifierHandle.sendModifierEntryToAllClient(modifierEntry);
            }
            ModifierHandle.EEMatchQueueHandle();
            LanguageLoader.load(FMLPaths.GAMEDIR.get().resolve(LanguageLoader.LANGUAGES_FILE_PATH));
        }

        @SubscribeEvent
        public static void atReload(AddReloadListenerEvent event) throws IOException {
            init(new Runnable() {
                @Override
                public void run() {
                    event.addListener(new ModifierPreparableReloadListener());

                }
            });


        }
        @SubscribeEvent
        public static void playJoinServer(PlayerEvent.PlayerLoggedInEvent event) {
            Player entity = event.getEntity();
            if (!entity.level().isClientSide()) {
                ModifierHandle.sendClearModifierEntryToClient( (ServerPlayer) entity);
                for (ModifierEntry modifierEntry : ModifierHandle.modifierEntryMap.values())
                    ModifierHandle.sendModifierEntryToClient(modifierEntry, (ServerPlayer) entity);

            }
        }
    }
}

