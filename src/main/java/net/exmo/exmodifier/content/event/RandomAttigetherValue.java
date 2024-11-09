package net.exmo.exmodifier.content.event;

import net.exmo.exmodifier.events.ExApplyEntryAttrigetherEvent;
import net.exmo.exmodifier.util.WeightedUtil;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.text.DecimalFormat;

@Mod.EventBusSubscriber
public class RandomAttigetherValue {
    @SubscribeEvent
    public static void apply(ExApplyEntryAttrigetherEvent event){
        if (event.attriGether.maxValue!=event.attriGether.minValue){
            double randomValue = event.attriGether.minValue + Math.random()*(event.attriGether.maxValue-event.attriGether.minValue);
            DecimalFormat df = new DecimalFormat("0." + "0".repeat(event.attriGether.reserveDouble));
            double formattedRandomValue = Double.parseDouble(df.format(randomValue));
            event.attriGether.modifier = new AttributeModifier(event.attriGether.modifier.getId(),event.attriGether.modifier.getName(),formattedRandomValue,event.attriGether.modifier.getOperation());
        }else {
            if (event.attriGether.simpleWeight.isEmpty())return;
            WeightedUtil<Double> weightedUtil = new WeightedUtil<Double>(event.attriGether.simpleWeight);
            Double v = weightedUtil.selectRandomKeyBasedOnWeights();
            if (v != null) {
                double randomValue = v;
                event.attriGether.modifier = new AttributeModifier(event.attriGether.modifier.getId(), event.attriGether.modifier.getName(), randomValue, event.attriGether.modifier.getOperation());
            }
        }
    }
}