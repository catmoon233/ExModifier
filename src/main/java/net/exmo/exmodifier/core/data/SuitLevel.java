package net.exmo.exmodifier.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record SuitLevel(
        int pieces,
        ResourceLocation trigger,
        List<AttributeSpec> attributes,
        List<MobEffectSpec> effects,
        List<String> commands,
        List<ResourceLocation> specialEffects
) {
    public static final Codec<SuitLevel> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("pieces").forGetter(SuitLevel::pieces),
            ResourceLocation.CODEC.optionalFieldOf("trigger", ResourceLocation.parse("exmodifier:tick")).forGetter(SuitLevel::trigger),
            AttributeSpec.CODEC.listOf().optionalFieldOf("attributes", List.of()).forGetter(SuitLevel::attributes),
            MobEffectSpec.CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(SuitLevel::effects),
            Codec.STRING.listOf().optionalFieldOf("commands", List.of()).forGetter(SuitLevel::commands),
            ResourceLocation.CODEC.listOf().optionalFieldOf("special_effects", List.of()).forGetter(SuitLevel::specialEffects)
    ).apply(instance, SuitLevel::new));

    public SuitLevel {
        attributes = List.copyOf(attributes == null ? List.of() : attributes);
        effects = List.copyOf(effects == null ? List.of() : effects);
        commands = List.copyOf(commands == null ? List.of() : commands);
        specialEffects = List.copyOf(specialEffects == null ? List.of() : specialEffects);
        pieces = Math.max(1, pieces);
    }

    public SuitLevel(int pieces, ResourceLocation trigger, List<AttributeSpec> attributes) {
        this(pieces, trigger, attributes, List.of(), List.of(), List.of());
    }
}
