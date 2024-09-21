package net.exmo.exmodifier.content.event;

import net.exmo.exmodifier.events.ExAddEntryAttrigetherEvent;
import net.exmo.exmodifier.util.WeightedUtil;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.text.DecimalFormat;

@Mod.EventBusSubscriber
public class RandomAttigetherValue {
    @SubscribeEvent
    public static void apply(ExAddEntryAttrigetherEvent event){
        if (event.selectedAttriGether.maxValue!=event.selectedAttriGether.minValue){
            double randomValue = event.selectedAttriGether.minValue + Math.random()*(event.selectedAttriGether.maxValue-event.selectedAttriGether.minValue);
            DecimalFormat df = new DecimalFormat("0." + "0".repeat(event.selectedAttriGether.reserveDouble));
            double formattedRandomValue = Double.parseDouble(df.format(randomValue));
            event.selectedAttriGether.modifier = new AttributeModifier(event.selectedAttriGether.modifier.getId(),event.selectedAttriGether.modifier.getName(),formattedRandomValue,event.selectedAttriGether.modifier.getOperation());
        }else {
            if (event.selectedAttriGether.simpleWeight.isEmpty())return;
            WeightedUtil<Double> weightedUtil = new WeightedUtil<Double>(event.selectedAttriGether.simpleWeight);
            Double v = weightedUtil.selectRandomKeyBasedOnWeights();
            if (v != null) {
                double randomValue = v;
                event.selectedAttriGether.modifier = new AttributeModifier(event.selectedAttriGether.modifier.getId(), event.selectedAttriGether.modifier.getName(), randomValue, event.selectedAttriGether.modifier.getOperation());
            }
        }
    }
}