package net.exmo.exmodifier.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record WashingMaterialDefinition(int consumeCount, int xpCost, int keepEntries, int addEntries, float minWeight) {
    public static final Codec<WashingMaterialDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("consume_count", 1).forGetter(WashingMaterialDefinition::consumeCount),
            Codec.INT.optionalFieldOf("xp_cost", 0).forGetter(WashingMaterialDefinition::xpCost),
            Codec.INT.optionalFieldOf("keep_entries", 0).forGetter(WashingMaterialDefinition::keepEntries),
            Codec.INT.optionalFieldOf("add_entries", 1).forGetter(WashingMaterialDefinition::addEntries),
            Codec.FLOAT.optionalFieldOf("min_weight", 0.0F).forGetter(WashingMaterialDefinition::minWeight)
    ).apply(instance, WashingMaterialDefinition::new));
}
