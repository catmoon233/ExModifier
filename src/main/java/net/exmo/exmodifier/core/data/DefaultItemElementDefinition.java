package net.exmo.exmodifier.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public record DefaultItemElementDefinition(List<ResourceLocation> items, Map<ResourceLocation, Integer> elements) {
    public static final Codec<DefaultItemElementDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(DefaultItemElementDefinition::items),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).optionalFieldOf("elements", Map.of()).forGetter(DefaultItemElementDefinition::elements)
    ).apply(instance, DefaultItemElementDefinition::new));

    public DefaultItemElementDefinition {
        items = List.copyOf(items == null ? List.of() : items);
        elements = elements == null ? Map.of() : Map.copyOf(elements);
    }
}
