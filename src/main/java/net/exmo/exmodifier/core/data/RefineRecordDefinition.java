package net.exmo.exmodifier.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record RefineRecordDefinition(ResourceLocation quality, int maxCount) {
    public static final Codec<RefineRecordDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("quality", ResourceLocation.parse("exmodifier:common")).forGetter(RefineRecordDefinition::quality),
            Codec.INT.optionalFieldOf("max_count", 0).forGetter(RefineRecordDefinition::maxCount)
    ).apply(instance, RefineRecordDefinition::new));
}
