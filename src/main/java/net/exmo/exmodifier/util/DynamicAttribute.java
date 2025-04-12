package net.exmo.exmodifier.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.apache.commons.lang3.tuple.Pair;

public record DynamicAttribute(String baseAttribute, String modifierName, AttributeModifier.Operation baseOperation,
                               net.exmo.exmodifier.util.DynamicAttribute.DynamicAttributeGenerator generator, DynamicAttributeGeneratorType type) {

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