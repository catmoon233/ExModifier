package net.exmo.exmodifier.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record SlotDefinition(Optional<ResourceLocation> unlockItem, int xpCost, int consumeCount) {
    public static final Codec<SlotDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("unlock_item").forGetter(SlotDefinition::unlockItem),
            Codec.INT.optionalFieldOf("xp_cost", 0).forGetter(SlotDefinition::xpCost),
            Codec.INT.optionalFieldOf("consume_count", 1).forGetter(SlotDefinition::consumeCount)
    ).apply(instance, SlotDefinition::new));
}
