package net.exmo.exmodifier.content.special;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.api.AppliedModifierView;
import net.exmo.exmodifier.api.ExModifierApi;
import net.exmo.exmodifier.core.registry.ExModifierCatalog;
import net.exmo.exmodifier.core.runtime.SuitRuntime;
import net.exmo.exmodifier.events.ExDodgeEvent;
import net.exmo.exmodifier.events.LivingPlayerSwimEvent;
import net.exmo.exmodifier.events.LivingSwingEvent;
import net.exmo.exmodifier.util.TickCooldown;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

/** Built-in special-effect ids referenced from datapack `special_effects`. */
public final class SpecialEffects {
    private SpecialEffects() {}

    public static boolean has(LivingEntity entity, String path) {
        return has(entity, Exmodifier.id(path));
    }

    public static boolean has(LivingEntity entity, ResourceLocation id) {
        if (entity == null || id == null) return false;
        for (AppliedModifierView view : ExModifierApi.modifiersOnEquipped(entity)) {
            var definition = ExModifierCatalog.current().entry(view.entryId());
            if (definition.isPresent() && definition.get().specialEffects().contains(id)) return true;
        }
        var pieces = ExModifierApi.countSuitPieces(entity);
        for (var suit : ExModifierCatalog.current().suits().entrySet()) {
            int count = pieces.getOrDefault(suit.getKey(), 0);
            for (var level : suit.getValue().levels()) {
                if (count >= level.pieces() && level.specialEffects().contains(id)) return true;
            }
        }
        return false;
    }

    public static void runIds(LivingEntity entity, List<ResourceLocation> ids, String trigger) {
        if (entity == null || ids == null) return;
        for (ResourceLocation id : ids) {
            if (id == null) continue;
            String path = id.getPath();
            if ("bloodthirsty".equals(path) && "ATTACK".equals(trigger)) { /* applied in incoming */ }
        }
    }

    @SubscribeEvent
    public static void incoming(LivingIncomingDamageEvent event) {
        if (event.isCanceled()) return;
        LivingEntity victim = event.getEntity();
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            if (has(attacker, "bloodthirsty") && attacker instanceof Player && TickCooldown.tryUse(attacker, "bloodthirsty", 40)) {
                attacker.heal(1.0F);
                puff(attacker, ParticleTypes.HEART, 2);
            }
            if (has(attacker, "soul_eater") && victim.getMaxHealth() <= attacker.getMaxHealth() * 0.05F) {
                event.setAmount(Math.max(event.getAmount(), victim.getHealth() + 1.0F));
            }
            if (has(attacker, "corpse_poison") && attacker instanceof Player && !isUndeadName(victim)
                    && TickCooldown.tryUse(attacker, "corpse_poison", 20)) {
                victim.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
            }
            if (has(attacker, "heavy_strike") && attacker instanceof Player && TickCooldown.tryUse(attacker, "heavy_strike", 100)) {
                event.setAmount(event.getAmount() + 3.0F);
            }
            SuitRuntime.onTrigger(attacker, "ATTACK");
        }
        if (has(victim, "thorns") && victim instanceof Player && TickCooldown.tryUse(victim, "thorns", 200)) {
            AABB box = victim.getBoundingBox().inflate(3.0D);
            for (LivingEntity nearby : victim.level().getEntitiesOfClass(LivingEntity.class, box, e -> e != victim && e.isAlive())) {
                nearby.hurt(victim.damageSources().generic(), 1.0F);
            }
            puff(victim, ParticleTypes.ELECTRIC_SPARK, 12);
        }
        if (has(victim, "concussion") && victim instanceof Player) {
            TickCooldown.tryUse(victim, "concussion", 400);
        }
        SuitRuntime.onTrigger(victim, "ON_HURT");
    }

    @SubscribeEvent
    public static void death(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity killer) {
            if (has(killer, "bandit") && event.getEntity() instanceof Villager && killer.level() instanceof ServerLevel serverLevel) {
                serverLevel.addFreshEntity(new ItemEntity(serverLevel, event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), new ItemStack(Items.EMERALD)));
            }
            SuitRuntime.onTrigger(killer, "KILL");
        }
        SuitRuntime.onTrigger(event.getEntity(), "DIE");
    }

    @SubscribeEvent
    public static void swing(LivingSwingEvent event) {
        SuitRuntime.onTrigger(event.getEntity(), "SWING");
    }

    @SubscribeEvent
    public static void crit(CriticalHitEvent event) {
        SuitRuntime.onTrigger(event.getEntity(), "CRIT");
    }

    @SubscribeEvent
    public static void dig(BlockEvent.BreakEvent event) {
        SuitRuntime.onTrigger(event.getPlayer(), "DIG");
    }

    @SubscribeEvent
    public static void dodge(ExDodgeEvent event) {
        if (event.result() == ExDodgeEvent.Result.MISS) SuitRuntime.onTrigger(event.victim(), "DODGE");
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        SuitRuntime.onTrigger(player, "TICK");
        if (player.isSwimming() || player.isInWater()) {
            SuitRuntime.onTrigger(player, "SWIM");
            NeoForge.EVENT_BUS.post(new LivingPlayerSwimEvent(player));
        }
        if (player.isInLava()) SuitRuntime.onTrigger(player, "IN_LAVA");
        if (has(player, "concussion") && player.tickCount % 20 == 0 && !TickCooldown.ready(player, "concussion")) {
            player.heal(1.0F);
        }
    }

    @SubscribeEvent
    public static void shoot(ArrowLooseEvent event) {
        Player player = event.getEntity();
        SuitRuntime.onTrigger(player, "SHOOT");
        if (!has(player, "reserve_arrow")) return;
        ItemStack bow = event.getBow();
        if (hasInfinity(player, bow)) return;
        if (!TickCooldown.tryUse(player, "reserve_arrow", 5)) return;
        player.addItem(new ItemStack(Items.ARROW));
        bow.hurtAndBreak(2, player, player.getEquipmentSlotForItem(bow));
    }

    private static boolean hasInfinity(Player player, ItemStack bow) {
        return player.level().registryAccess().lookup(Registries.ENCHANTMENT).flatMap(lookup -> lookup.get(Enchantments.INFINITY))
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, bow) > 0)
                .orElse(false);
    }

    private static boolean isUndeadName(LivingEntity entity) {
        String id = entity.getType().toString();
        return id.contains("zombie") || id.contains("skeleton") || id.contains("wither");
    }

    private static void puff(LivingEntity entity, net.minecraft.core.particles.ParticleOptions particle, int count) {
        if (entity.level() instanceof ServerLevel level) {
            level.sendParticles(particle, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(), count, 0.3, 0.3, 0.3, 0.1);
        }
    }
}
