package net.exmo.exmodifier.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record AttriEffectDefinition(
        List<AttributeSpec> attributes,
        Optional<ResourceLocation> particle,
        Optional<String> descriptionKey,
        boolean visible
) {
    public static final Codec<AttriEffectDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            AttributeSpec.CODEC.listOf().optionalFieldOf("attributes", List.of()).forGetter(AttriEffectDefinition::attributes),
            ResourceLocation.CODEC.optionalFieldOf("particle").forGetter(AttriEffectDefinition::particle),
            Codec.STRING.optionalFieldOf("description_key").forGetter(AttriEffectDefinition::descriptionKey),
            Codec.BOOL.optionalFieldOf("visible", true).forGetter(AttriEffectDefinition::visible)
    ).apply(instance, AttriEffectDefinition::new));

    public AttriEffectDefinition {
        attributes = List.copyOf(attributes == null ? List.of() : attributes);
        particle = particle == null ? Optional.empty() : particle;
        descriptionKey = descriptionKey == null ? Optional.empty() : descriptionKey;
    }
}
