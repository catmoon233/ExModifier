package net.exmo.exmodifier.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record DefaultItemEntryDefinition(List<ResourceLocation> items, List<ResourceLocation> entries, int level) {
    public static final Codec<DefaultItemEntryDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(DefaultItemEntryDefinition::items),
            ResourceLocation.CODEC.listOf().optionalFieldOf("entries", List.of()).forGetter(DefaultItemEntryDefinition::entries),
            Codec.INT.optionalFieldOf("level", 1).forGetter(DefaultItemEntryDefinition::level)
    ).apply(instance, DefaultItemEntryDefinition::new));

    public DefaultItemEntryDefinition {
        items = List.copyOf(items == null ? List.of() : items);
        entries = List.copyOf(entries == null ? List.of() : entries);
        level = Math.max(1, level);
    }
}
