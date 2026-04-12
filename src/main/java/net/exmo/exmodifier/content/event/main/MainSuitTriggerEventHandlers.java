package net.exmo.exmodifier.content.event.main;

import net.exmo.exmodifier.content.event.MainEvent;
import net.exmo.exmodifier.content.event.parameter.EventParameter;
import net.exmo.exmodifier.content.suit.ExSuit;
import net.exmo.exmodifier.events.ExDodgeEvent;
import net.exmo.exmodifier.events.LivingPlayerSwimEvent;
import net.exmo.exmodifier.events.LivingSwingEvent;
import net.exmo.exmodifier.util.ExUtil;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public final class MainSuitTriggerEventHandlers {
    private MainSuitTriggerEventHandlers() {
    }

    @SubscribeEvent
    public static void playerHurtAndAttack(LivingHurtEvent event) {
        if (event.getSource().is(DamageTypes.GENERIC_KILL)) {
            return;
        }

        LivingEntity entity = event.getEntity();
        if (entity instanceof Player player) {
            if (MainEvent.CommonEvent.cache_invulnerableTime_time !=0){
                player.invulnerableTime = MainEvent.CommonEvent.cache_invulnerableTime_time;
            }
            ExUtil.getSpecialModifierEntries(player).forEach(specialEffect -> specialEffect.hurt(event));
            List<EventParameter<?>> eventParameters = new ArrayList<>(2);
            eventParameters.add(new EventParameter<>("amount", event.getAmount()));
            eventParameters.add(new EventParameter<>("max_health", player.getAttributeValue(Attributes.MAX_HEALTH)));
            MainEvent.CommonEvent.addx(player, eventParameters, "ON_HURT");
            MainSuitRuntimeContext.withLivingHurtEvent(event,
                    () -> MainEvent.CommonEvent.ApplySuitEffect(player, ExSuit.Trigger.ON_HURT));
        }

        if (event.getSource().getEntity() instanceof Player player) {
            if (MainEvent.CommonEvent.cache_invulnerableTime_time !=0){
                entity.invulnerableTime = MainEvent.CommonEvent.cache_invulnerableTime_time;
            }
            ExUtil.getSpecialModifierEntries(player).forEach(specialEffect -> specialEffect.attackEntity(event));
            List<EventParameter<?>> eventParameters = new ArrayList<>(2);
            eventParameters.add(new EventParameter<>("amount", event.getAmount()));
            eventParameters.add(new EventParameter<>("max_health", player.getAttributeValue(Attributes.MAX_HEALTH)));
            MainEvent.CommonEvent.addx(player, eventParameters, "ATTACK");
            if (entity != null) {
                player.getPersistentData().putString("hurtentity-uuid", entity.getUUID().toString());
            }
            MainSuitRuntimeContext.withLivingHurtEvent(event,
                    () -> MainEvent.CommonEvent.ApplySuitEffect(player, ExSuit.Trigger.ATTACK));
            player.getPersistentData().putString("hurtentity-uuid", "null");
        }
    }
    @SubscribeEvent
    public static void playerAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            ExUtil.getSpecialModifierEntries(player).forEach(specialEffect -> specialEffect.attackStart(event));
            List<EventParameter<?>> eventParameters = new ArrayList<>();
            MainEvent.CommonEvent.addx(player, eventParameters, "ATTACK_START");
            MainEvent.CommonEvent.ApplySuitEffect(player, ExSuit.Trigger.ATTACK_START);
            if (MainEvent.CommonEvent.skipInvulnerableTime){
                MainEvent.CommonEvent.skipInvulnerableTime = false;;
                MainEvent.CommonEvent.cache_invulnerableTime_time = event.getEntity().invulnerableTime ;
                event.getEntity().invulnerableTime = 0;

            }
        }
    }

    @SubscribeEvent
    public static void playerJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player) {
            ExUtil.getSpecialModifierEntries(player).forEach(specialEffect -> specialEffect.jump(event));
            List<EventParameter<?>> eventParameters = new ArrayList<>();
            MainEvent.CommonEvent.addx(player, eventParameters, "JUMP");
            MainEvent.CommonEvent.ApplySuitEffect(player, ExSuit.Trigger.JUMP);
        }
    }

    @SubscribeEvent
    public static void digger(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        ExUtil.getSpecialModifierEntries(player).forEach(specialEffect -> specialEffect.digger( event));
        List<EventParameter<?>> eventParameters = new ArrayList<>();
        MainEvent.CommonEvent.addx(player, eventParameters, "DIG");
        MainEvent.CommonEvent.ApplySuitEffect(player, ExSuit.Trigger.DIG);
    }

    @SubscribeEvent
    public static void playerDeathAndKill(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            ExUtil.getSpecialModifierEntries(player).forEach(specialEffect -> specialEffect.onDeath(( event)));
            List<EventParameter<?>> eventParameters = new ArrayList<>();
            MainEvent.CommonEvent.addx(player, eventParameters, "DIE");
            MainEvent.CommonEvent.ApplySuitEffect(player, ExSuit.Trigger.DIE);
        }
        if (event.getSource().getEntity() instanceof Player player) {
            ExUtil.getSpecialModifierEntries(player).forEach(specialEffect -> specialEffect.onKill(( event)));
            List<EventParameter<?>> eventParameters = new ArrayList<>();
            MainEvent.CommonEvent.addx(player, eventParameters, "KILL");
            MainEvent.CommonEvent.ApplySuitEffect(player, ExSuit.Trigger.KILL);
        }
    }

    @SubscribeEvent
    public static void playerProjectile(ProjectileImpactEvent event) {
        if (event.getProjectile().getOwner() instanceof Player player) {
            ExUtil.getSpecialModifierEntries(player).forEach(specialEffect -> specialEffect.projectileHit(( event)));
            List<EventParameter<?>> eventParameters = new ArrayList<>();
            if (event.getEntity() != null) {

                player.getPersistentData().putString("hurtentity-uuid", event.getEntity().getUUID().toString());
            }
            MainEvent.CommonEvent.addx(player, eventParameters, "PROJECTILE_HIT");
            MainEvent.CommonEvent.ApplySuitEffect(player, ExSuit.Trigger.PROJECTILE_HIT);
            player.getPersistentData().putString("hurtentity-uuid", "null");
        }
    }

    @SubscribeEvent
    public static void playerShoot(ArrowLooseEvent event) {
        List<EventParameter<?>> eventParameters = new ArrayList<>();
        MainEvent.CommonEvent.addx(event.getEntity(), eventParameters, "SHOOT");
        MainEvent.CommonEvent.ApplySuitEffect(event.getEntity(), ExSuit.Trigger.SHOOT);
    }

    @SubscribeEvent
    public static void playerSwing(LivingSwingEvent event) {
        if (event.getEntity() instanceof Player player) {
            ExUtil.getSpecialModifierEntries(player).forEach(specialEffect -> specialEffect.playerSwing(( event)));
            List<EventParameter<?>> eventParameters = new ArrayList<>();
            MainEvent.CommonEvent.addx(player, eventParameters, "SWING");
            MainEvent.CommonEvent.ApplySuitEffect(player, ExSuit.Trigger.SWING);
        }
    }

    @SubscribeEvent
    public static void playerCrit(CriticalHitEvent event) {

        List<EventParameter<?>> eventParameters = new ArrayList<>();
        eventParameters.add(new EventParameter<>("amount", event.getDamageModifier()));
        Player player = event.getEntity();
        ExUtil.getSpecialModifierEntries(player).forEach(specialEffect -> specialEffect.playerCrit(( event)));
        MainEvent.CommonEvent.addx(player, eventParameters, "CRIT");
        MainEvent.CommonEvent.ApplySuitEffect(player, ExSuit.Trigger.CRIT);
    }

    @SubscribeEvent
    public static void playerDodge(ExDodgeEvent event) {
        if (event.getEntity() instanceof Player player && event.result == ExDodgeEvent.resultType.MISS) {
            List<EventParameter<?>> eventParameters = new ArrayList<>();
            MainEvent.CommonEvent.addx(player, eventParameters, "DODGE");
            MainEvent.CommonEvent.ApplySuitEffect(player, ExSuit.Trigger.DODGE);
        }
    }

    @SubscribeEvent
    public static void playerUseItem(LivingEntityUseItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            ExUtil.getSpecialModifierEntries(player).forEach(specialEffect -> specialEffect.onUseItem(( event)));
            List<EventParameter<?>> eventParameters = new ArrayList<>();
            MainEvent.CommonEvent.addx(player, eventParameters, "ON_USE");
            MainEvent.CommonEvent.ApplySuitEffect(player, ExSuit.Trigger.ON_USE);
        }
    }

    @SubscribeEvent
    public static void playerSwim(LivingPlayerSwimEvent event) {
        List<EventParameter<?>> eventParameters = new ArrayList<>();
        Player player = event.player;
        MainEvent.CommonEvent.addx(player, eventParameters, "SWIM");
        MainEvent.CommonEvent.ApplySuitEffect(player, ExSuit.Trigger.SWIM);
    }

    @SubscribeEvent
    public static void playerLiving(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;
        ExUtil.getSpecialModifierEntries(player).forEach(specialEffect -> specialEffect.onTick(( event)));
        if (player.level().isClientSide) {
            return;
        }
        MainEvent.CommonEvent.ApplySuitEffect(player, ExSuit.Trigger.TICK);
    }
}
