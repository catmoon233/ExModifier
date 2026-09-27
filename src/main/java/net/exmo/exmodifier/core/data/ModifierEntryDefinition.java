package net.exmo.exmodifier.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record ModifierEntryDefinition(
        List<ResourceLocation> itemTypes,
        List<ResourceLocation> slots,
        float weight,
        int maxLevel,
        boolean random,
        boolean cantSelect,
        List<AttributeSpec> attributes,
        List<ResourceLocation> specialEffects,
        List<ResourceLocation> suits,
        Optional<String> descriptionKey
) {
    public static final Codec<ModifierEntryDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().optionalFieldOf("item_types", List.of()).forGetter(ModifierEntryDefinition::itemTypes),
            ResourceLocation.CODEC.listOf().optionalFieldOf("slots", List.of()).forGetter(ModifierEntryDefinition::slots),
            Codec.FLOAT.optionalFieldOf("weight", 1.0F).forGetter(ModifierEntryDefinition::weight),
            Codec.INT.optionalFieldOf("max_level", 1).forGetter(ModifierEntryDefinition::maxLevel),
            Codec.BOOL.optionalFieldOf("random", true).forGetter(ModifierEntryDefinition::random),
            Codec.BOOL.optionalFieldOf("cant_select", false).forGetter(ModifierEntryDefinition::cantSelect),
            AttributeSpec.CODEC.listOf().optionalFieldOf("attributes", List.of()).forGetter(ModifierEntryDefinition::attributes),
            ResourceLocation.CODEC.listOf().optionalFieldOf("special_effects", List.of()).forGetter(ModifierEntryDefinition::specialEffects),
            ResourceLocation.CODEC.listOf().optionalFieldOf("suits", List.of()).forGetter(ModifierEntryDefinition::suits),
            Codec.STRING.optionalFieldOf("description_key").forGetter(ModifierEntryDefinition::descriptionKey)
    ).apply(instance, ModifierEntryDefinition::new));

    public ModifierEntryDefinition {
        itemTypes = List.copyOf(itemTypes == null ? List.of() : itemTypes);
        slots = List.copyOf(slots == null ? List.of() : slots);
        attributes = List.copyOf(attributes == null ? List.of() : attributes);
        specialEffects = List.copyOf(specialEffects == null ? List.of() : specialEffects);
        suits = List.copyOf(suits == null ? List.of() : suits);
        maxLevel = Math.max(1, maxLevel);
        weight = Math.max(0.0F, weight);
        descriptionKey = descriptionKey == null ? Optional.empty() : descriptionKey;
    }
}
