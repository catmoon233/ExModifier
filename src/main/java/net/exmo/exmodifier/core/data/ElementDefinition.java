package net.exmo.exmodifier.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record ElementDefinition(
        float damageBonus,
        float damageResist,
        int color,
        Map<ResourceLocation, Float> restrain,
        List<ResourceLocation> damageTypes,
        Optional<ResourceLocation> school
) {
    public static final Codec<ElementDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("damage_bonus", 0.0F).forGetter(ElementDefinition::damageBonus),
            Codec.FLOAT.optionalFieldOf("damage_resist", 0.0F).forGetter(ElementDefinition::damageResist),
            Codec.INT.optionalFieldOf("color", 0xFFFFFF).forGetter(ElementDefinition::color),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.FLOAT).optionalFieldOf("restrain", Map.of()).forGetter(ElementDefinition::restrain),
            ResourceLocation.CODEC.listOf().optionalFieldOf("damage_types", List.of()).forGetter(ElementDefinition::damageTypes),
            ResourceLocation.CODEC.optionalFieldOf("school").forGetter(ElementDefinition::school)
    ).apply(instance, ElementDefinition::new));

    public ElementDefinition {
        restrain = restrain == null ? Map.of() : Map.copyOf(restrain);
        damageTypes = List.copyOf(damageTypes == null ? List.of() : damageTypes);
        school = school == null ? Optional.empty() : school;
    }

    public ElementDefinition(float damageBonus, float damageResist, int color) {
        this(damageBonus, damageResist, color, Map.of(), List.of(), Optional.empty());
    }

    public float restrainAgainst(ResourceLocation other) {
        return restrain.getOrDefault(other, 1.0F);
    }
}
