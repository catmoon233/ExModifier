package net.exmo.exmodifier.core.runtime;

import net.exmo.exmodifier.events.ExDodgeEvent;
import net.exmo.exmodifier.events.ModifierLivingHurtBaseDamage;
import net.exmo.exmodifier.init.ExAttributes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class AttributeCombat {
    private AttributeCombat() {}

    @SubscribeEvent
    public static void incoming(LivingIncomingDamageEvent event) {
        if (bypassesImmunity(event)) return;
        LivingEntity victim = event.getEntity();
        ModifierLivingHurtBaseDamage base = new ModifierLivingHurtBaseDamage(victim, event.getAmount());
        NeoForge.EVENT_BUS.post(base);
        event.setAmount(finite(base.getDamage(), event.getOriginalAmount()));
        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity living ? living : null;

        if (attacker != null
                && !event.getSource().is(DamageTypes.MAGIC) && !event.getSource().is(DamageTypes.INDIRECT_MAGIC)
                && !event.getSource().is(DamageTypes.LAVA)) {
            double dodge = capped(ExAttributes.value(victim, ExAttributes.DODGE, 1.0D), ExAttributes.value(victim, ExAttributes.MAX_DODGE, 1.85D), 1.0D);
            double hit = ExAttributes.value(attacker, ExAttributes.HIT_RATE, 1.0D);
            double chance = dodge - hit;
            if (chance > 0 && victim.getRandom().nextDouble() <= chance) {
                ExDodgeEvent dodgeEvent = new ExDodgeEvent(victim, attacker, chance, ExDodgeEvent.Result.MISS);
                NeoForge.EVENT_BUS.post(dodgeEvent);
                if (dodgeEvent.result() == ExDodgeEvent.Result.MISS) {
                    puff(victim);
                    event.setCanceled(true);
                    return;
                }
            } else if (chance < 0) {
                ExDodgeEvent dodgeEvent = new ExDodgeEvent(victim, attacker, chance, ExDodgeEvent.Result.HIT);
                NeoForge.EVENT_BUS.post(dodgeEvent);
                if (dodgeEvent.result() == ExDodgeEvent.Result.MISS) {
                    puff(victim);
                    event.setCanceled(true);
                    return;
                }
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, (int) (-chance * 2.5), false, false));
            }
        }

        float amount = event.getAmount();
        if (event.getSource().is(DamageTypes.FALL)) {
            amount *= (float) (2.0D - ExAttributes.value(victim, ExAttributes.FALL_PROTECT, 1.0D));
        }
        if (attacker != null && lookingBehind(victim, event.getSource().getSourcePosition())) {
            double multiplier = ExAttributes.value(attacker, ExAttributes.BEHIND_DAMAGE, 1.0D);
            amount *= (float) multiplier;
            if (multiplier > 1.0D && !attacker.level().isClientSide) {
                attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
        if (attacker != null && event.getSource().is(DamageTypes.FIREWORKS)) {
            amount *= (float) ExAttributes.value(attacker, ExAttributes.FIREWORK_DAMAGE, 1.0D);
        }
        amount *= defenseFactor(ExAttributes.value(victim, ExAttributes.DEFENSE, 0));
        double injury = capped(ExAttributes.value(victim, ExAttributes.INJURY_FREE, 1.0D), ExAttributes.value(victim, ExAttributes.MAX_INJURY_FREE, 1.85D), 1.0D);
        if (injury != 1.0D) {
            amount *= (float) (2.0D - injury);
        }
        amount = (float) Math.max(0, amount - ExAttributes.value(victim, ExAttributes.DIRECT_PROTECTION, 0));
        if (event.getSource().is(DamageTypes.MAGIC)) {
            amount = (float) Math.max(0, amount - ExAttributes.value(victim, ExAttributes.MAGIC_PROTECTION, 0));
        }
        if (attacker != null) {
            double heal = ExAttributes.value(attacker, ExAttributes.PERCENT_HEAL, 1.0D);
            if (heal != 1.0D && Double.isFinite(heal) && attacker.getAttributes().hasAttribute(Attributes.MAX_HEALTH)) {
                attacker.heal((float) (attacker.getAttributeValue(Attributes.MAX_HEALTH) * (heal - 1.0D)));
            }
        }
        event.setAmount(Math.max(0.0F, finite(amount, event.getOriginalAmount())));
    }

    static boolean bypassesImmunity(LivingIncomingDamageEvent event) {
        return event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY) || event.getSource().is(DamageTypes.GENERIC_KILL);
    }

    static double capped(double value, double max, double fallback) {
        if (!Double.isFinite(value)) return fallback;
        if (!Double.isFinite(max)) return value;
        return Math.min(value, max);
    }

    static float defenseFactor(double defense) {
        if (defense == 0.0D || !Double.isFinite(defense)) return 1.0F;
        double denom = defense + 99.0D;
        if (denom == 0.0D || !Double.isFinite(denom)) return 1.0F;
        double raw = 100.0D / denom;
        if (!Double.isFinite(raw) || raw <= 0.0D) return raw < 0.0D ? 0.0F : 1.0F;
        double rounded = Math.round(raw * 100.0D) / 100.0D;
        if (rounded <= 0.0D) rounded = Math.min(raw, 0.01D);
        return (float) rounded;
    }

    private static float finite(float value, float fallback) {
        return Float.isFinite(value) ? value : (Float.isFinite(fallback) ? fallback : 0.0F);
    }

    @SubscribeEvent
    public static void breakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        event.setNewSpeed((float) (event.getNewSpeed() * ExAttributes.value(player, ExAttributes.DIG_SPEED, 1.0D)));
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        Player oldP = event.getOriginal();
        Player newP = event.getEntity();
        ExAttributes.ATTRIBUTES.getEntries().forEach(holder -> {
            if (oldP.getAttributes().hasAttribute(holder) && newP.getAttributes().hasAttribute(holder)) {
                newP.getAttribute(holder).setBaseValue(oldP.getAttribute(holder).getBaseValue());
            }
        });
    }

    private static boolean lookingBehind(LivingEntity target, Vec3 attackerLocation) {
        if (attackerLocation == null) return false;
        Vec3 looking = target.getViewVector(1.0F);
        Vec3 angle = attackerLocation.subtract(target.position()).normalize();
        angle = new Vec3(angle.x, 0.0D, angle.z);
        return angle.dot(looking) < -0.5D;
    }

    private static void puff(LivingEntity entity) {
        if (entity.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.CLOUD, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(), 5, 0.2, 0.2, 0.2, 0.02);
        }
    }
}
