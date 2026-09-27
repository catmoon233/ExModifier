package net.exmo.exmodifier.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DynamicAttributeDefinition(double min, double max, double defaultValue) {
    public static final Codec<DynamicAttributeDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("min", 0.0D).forGetter(DynamicAttributeDefinition::min),
            Codec.DOUBLE.optionalFieldOf("max", 1.0E8).forGetter(DynamicAttributeDefinition::max),
            Codec.DOUBLE.optionalFieldOf("default", 0.0D).forGetter(DynamicAttributeDefinition::defaultValue)
    ).apply(instance, DynamicAttributeDefinition::new));
}
