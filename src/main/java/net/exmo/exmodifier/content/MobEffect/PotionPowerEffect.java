package net.exmo.exmodifier.content.MobEffect;

import net.exmo.exmodifier.init.ExAttribute;
import net.exmo.exmodifier.init.RegisterOther;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PotionPowerEffect extends MobEffect {


    public PotionPowerEffect() {
        super(MobEffectCategory.NEUTRAL, -26368);    }


    @Override
    public String getDescriptionId()
    {
        return "effect.exmodifier.potion_power";
    }
    @SubscribeEvent
    public static void applyEvent(PotionEvent.PotionAddedEvent event){
        LivingEntity entity = (LivingEntity) event.getEntity();
        if (entity !=null){
            MobEffectInstance effect1 = entity.getEffect(RegisterOther.EffectAbout.POTION_POWER_EFFECT.get());
            if (effect1 !=null) {
                MobEffectInstance effect = event.getPotionEffect();
                if (effect.getEffect() != effect1.getEffect()) {
                    event.getPotionEffect().update(new MobEffectInstance(effect.getEffect(), effect.getDuration(), effect.getAmplifier() + effect1.getAmplifier() + 1));
                }
            }
        }
    }
}