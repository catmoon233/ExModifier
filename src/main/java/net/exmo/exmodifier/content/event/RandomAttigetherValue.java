package net.exmo.exmodifier.content.event;

import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.events.ExApplyEntryAttrigetherEvent;
import net.exmo.exmodifier.util.WeightedUtil;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.text.DecimalFormat;
import java.util.Random;

@Mod.EventBusSubscriber
public class RandomAttigetherValue {
    @SubscribeEvent
    public static void apply(ExApplyEntryAttrigetherEvent event){
        ModifierAttriGether attriGether = event.attriGether.copy();
        if (attriGether.maxValue!= attriGether.minValue){
            double randomValue = new Random().nextDouble(attriGether.minValue, attriGether.maxValue);
            DecimalFormat df = new DecimalFormat("0." + "0".repeat(attriGether.reserveDouble));
            double formattedRandomValue = Double.parseDouble(df.format(randomValue));
            attriGether.modifier = new AttributeModifier(attriGether.modifier.getId(), attriGether.modifier.getName(),formattedRandomValue, attriGether.modifier.getOperation());
            event.attriGether = attriGether;
        }else {
            if (attriGether.simpleWeight.isEmpty())return;
            WeightedUtil<Double> weightedUtil = new WeightedUtil<Double>(attriGether.simpleWeight);
            Double v = weightedUtil.selectRandomKeyBasedOnWeights();
            if (v != null) {
                double randomValue = v;
                attriGether.modifier = new AttributeModifier(attriGether.modifier.getId(), attriGether.modifier.getName(), randomValue, attriGether.modifier.getOperation());
            }
        }
    }
}