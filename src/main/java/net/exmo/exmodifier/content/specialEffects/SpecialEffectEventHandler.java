package net.exmo.exmodifier.content.specialEffects;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.helper.ModifierEntryHelper;
import net.exmo.exmodifier.content.modifier.ModifierInstant;
import net.exmo.exmodifier.events.LivingSwingEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber
public class SpecialEffectEventHandler {
    
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 触发攻击者的特殊效果
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            ItemStack mainHandItem = attacker.getMainHandItem();
            triggerSpecialEffects(attacker, mainHandItem, effect -> effect.attackEntity(event));
        }
        
        // 触发受击者的特殊效果
        LivingEntity entity = event.getEntity();
        ItemStack armor = getFirstArmorWithSpecialEffect(entity);
        if (armor != null) {
            triggerSpecialEffects(entity, armor, effect -> effect.hurt(event));
        }
    }
    
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            ItemStack mainHandItem = attacker.getMainHandItem();
            triggerSpecialEffects(attacker, mainHandItem, effect -> effect.attackStart(event));
        }
    }
    
    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        ItemStack armor = getFirstArmorWithSpecialEffect(entity);
        if (armor != null) {
            triggerSpecialEffects(entity, armor, effect -> effect.jump(event));
        }
    }
    
    @SubscribeEvent
    public static void onProjectileHit(ProjectileImpactEvent event) {
        if (event.getProjectile().getOwner() instanceof LivingEntity shooter) {
            ItemStack mainHandItem = shooter.getMainHandItem();
            triggerSpecialEffects(shooter, mainHandItem, effect -> effect.projectileHit(event));
        }
    }
    
    @SubscribeEvent
    public static void onSwing(LivingSwingEvent event) {
        LivingEntity entity = event.getEntity();
        ItemStack mainHandItem = entity.getMainHandItem();
        triggerSpecialEffects(entity, mainHandItem, effect -> effect.playerSwing(event));
    }
    
    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        LivingEntity entity = event.getEntity();
        ItemStack mainHandItem = entity.getMainHandItem();
        triggerSpecialEffects(entity, mainHandItem, effect -> effect.playerCrit(event));
    }
    
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
            ItemStack mainHandItem = player.getMainHandItem();
            triggerSpecialEffects(player, mainHandItem, effect -> effect.digger(event));

    }
    
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        ItemStack armor = getFirstArmorWithSpecialEffect(entity);
        if (armor != null) {
            triggerSpecialEffects(entity, armor, effect -> effect.onDeath(event));
        }
        
        // 检查击杀者
        if (event.getSource().getEntity() instanceof LivingEntity killer) {
            ItemStack mainHandItem = killer.getMainHandItem();
            triggerSpecialEffects(killer, mainHandItem, effect -> effect.onKill(event));
        }
    }
    
    @SubscribeEvent
    public static void onUseItem(LivingEntityUseItemEvent event) {
        LivingEntity entity = event.getEntity();
        ItemStack item = event.getItem();
        triggerSpecialEffects(entity, item, effect -> effect.onUseItem(event));
    }
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            LivingEntity player = event.player;
            
            // 检查主手物品
            ItemStack mainHandItem = player.getMainHandItem();
            triggerSpecialEffects(player, mainHandItem, effect -> effect.onTick(event));
            
            // 检查防具
            for (ItemStack armor : player.getArmorSlots()) {
                triggerSpecialEffects(player, armor, effect -> effect.onTick(event));
            }
        }
    }
    
    /**
     * 触发物品的特殊效果
     */
    private static void triggerSpecialEffects(LivingEntity entity, ItemStack item, SpecialEffectTrigger trigger) {
        if (item.isEmpty() || !item.hasTag()) return;
        
        try {
            ModifierEntryHelper helper = ModifierEntryHelper.of(item);
            List<ModifierInstant> modifiers = helper.getModifierEntries();
            
            for (ModifierInstant modifier : modifiers) {
                var entry = modifier.getModifierEntry();
                if (entry != null && entry.specialTags != null) {
                    for (String specialTag : entry.specialTags) {
                        SpecialEffect effect = SpecialEffectHandle.getSpecialEffect(specialTag);
                        if (effect != null) {
                            try {
                                trigger.trigger(effect);
                            } catch (Exception e) {
                                Exmodifier.LOGGER.Logger.error("Error triggering special effect: {}", specialTag, e);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Exmodifier.LOGGER.error("Error processing special effects for item", e);
        }
    }
    
    /**
     * 获取第一个带有特殊效果的防具
     */
    private static ItemStack getFirstArmorWithSpecialEffect(LivingEntity entity) {
        for (ItemStack armor : entity.getArmorSlots()) {
            if (!armor.isEmpty() && armor.hasTag()) {
                return armor;
            }
        }
        return null;
    }
    
    @FunctionalInterface
    private interface SpecialEffectTrigger {
        void trigger(SpecialEffect effect);
    }
}
