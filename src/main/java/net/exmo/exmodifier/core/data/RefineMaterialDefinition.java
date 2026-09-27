package net.exmo.exmodifier.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record RefineMaterialDefinition(double bonus, int consumeCount) {
    public static final Codec<RefineMaterialDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("bonus", 0.0D).forGetter(RefineMaterialDefinition::bonus),
            Codec.INT.optionalFieldOf("consume_count", 1).forGetter(RefineMaterialDefinition::consumeCount)
    ).apply(instance, RefineMaterialDefinition::new));
}
