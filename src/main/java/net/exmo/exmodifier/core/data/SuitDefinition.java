package net.exmo.exmodifier.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record SuitDefinition(List<ResourceLocation> entries, List<SuitLevel> levels) {
    public static final Codec<SuitDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().optionalFieldOf("entries", List.of()).forGetter(SuitDefinition::entries),
            SuitLevel.CODEC.listOf().optionalFieldOf("levels", List.of()).forGetter(SuitDefinition::levels)
    ).apply(instance, SuitDefinition::new));

    public SuitDefinition {
        entries = List.copyOf(entries == null ? List.of() : entries);
        levels = List.copyOf(levels == null ? List.of() : levels);
    }
}
