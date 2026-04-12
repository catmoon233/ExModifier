package net.exmo.exmodifier.content.specialEffects;

import net.exmo.exmodifier.util.MobEffectInstantBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public  class SpecialEffectHandle {
    public static Map<String , SpecialEffect> specialEffects = new HashMap<>();

    public static void init() {
        // 注册嗜血特效
        registerSpecialEffect(new BloodthirstyEffect());
        // 注册尖刺特效
        registerSpecialEffect(new ThornsEffect());
        // 注册噬魂特效
        registerSpecialEffect(new SoulEaterEffect());
        // 注册尸毒特效
        registerSpecialEffect(new CorpsePoisonEffect());
        // 注册强盗特效
        registerSpecialEffect(new BanditEffect());
        // 注册凝神特效
        registerSpecialEffect(new ConcussionEffect());
        // 注册重击特效
        registerSpecialEffect(new HeavyStrikeEffect());
        // 注册备用箭特效
        registerSpecialEffect(new ReserveArrowEffect());
    }

    public static void registerSpecialEffect(SpecialEffect specialEffect) {
        specialEffects.put(specialEffect.id(), specialEffect);
    }

    public static SpecialEffect getSpecialEffect(String id) {
        return specialEffects.get(id);
    }

}