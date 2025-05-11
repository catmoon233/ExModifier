package net.exmo.exmodifier.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.apache.commons.lang3.tuple.Pair;

public record DynamicAttributeInstant(String baseAttribute, String modifierName, AttributeModifier.Operation baseOperation,
                                      DynamicAttributeInstant.DynamicAttributeGenerator generator, DynamicAttributeGeneratorType type) {

    public Pair<SimpleAttrGather, Double> calculate(LivingEntity entity) {
        SimpleAttrGather gather = new SimpleAttrGather(
                baseAttribute,
                modifierName,
                baseOperation
        );
        double value = generator.generateValue(entity, gather);
        return Pair.of(gather, value);
    }
    public enum DynamicAttributeGeneratorType {
        TICK,
        CUSTOM
    }

    @FunctionalInterface
    public interface DynamicAttributeGenerator {
        double generateValue(LivingEntity entity, SimpleAttrGather context);
    }


}