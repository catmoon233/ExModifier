package net.exmo.exmodifier.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public record DefaultEntityElementDefinition(List<ResourceLocation> entities, Map<ResourceLocation, Float> elements) {
    public static final Codec<DefaultEntityElementDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().optionalFieldOf("entities", List.of()).forGetter(DefaultEntityElementDefinition::entities),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.FLOAT).optionalFieldOf("elements", Map.of()).forGetter(DefaultEntityElementDefinition::elements)
    ).apply(instance, DefaultEntityElementDefinition::new));

    public DefaultEntityElementDefinition {
        entities = List.copyOf(entities == null ? List.of() : entities);
        elements = elements == null ? Map.of() : Map.copyOf(elements);
    }
}
