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
    }

    public static void registerSpecialEffect(SpecialEffect specialEffect) {
        specialEffects.put(specialEffect.id(), specialEffect);
    }

    public static SpecialEffect getSpecialEffect(String id) {
        return specialEffects.get(id);
    }

}
