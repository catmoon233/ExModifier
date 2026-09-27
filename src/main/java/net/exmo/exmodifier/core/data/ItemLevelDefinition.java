package net.exmo.exmodifier.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record ItemLevelDefinition(
        int maxLevel,
        int defaultLevel,
        int xpPerLevel,
        List<AttributeSpec> attributes,
        Optional<String> expression,
        Optional<String> xpAddExpression,
        String upEvent,
        float weight,
        List<ResourceLocation> itemTypes,
        List<ResourceLocation> onlyItems,
        List<String> onlyTags
) {
    public static final Codec<ItemLevelDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("max_level", 100).forGetter(ItemLevelDefinition::maxLevel),
            Codec.INT.optionalFieldOf("default_level", 1).forGetter(ItemLevelDefinition::defaultLevel),
            Codec.INT.optionalFieldOf("xp_per_level", 50).forGetter(ItemLevelDefinition::xpPerLevel),
            AttributeSpec.CODEC.listOf().optionalFieldOf("attributes", List.of()).forGetter(ItemLevelDefinition::attributes),
            Codec.STRING.optionalFieldOf("expression").forGetter(ItemLevelDefinition::expression),
            Codec.STRING.optionalFieldOf("xp_add_expression").forGetter(ItemLevelDefinition::xpAddExpression),
            Codec.STRING.optionalFieldOf("up_event", "KILL").forGetter(ItemLevelDefinition::upEvent),
            Codec.FLOAT.optionalFieldOf("weight", 1.0F).forGetter(ItemLevelDefinition::weight),
            ResourceLocation.CODEC.listOf().optionalFieldOf("item_types", List.of()).forGetter(ItemLevelDefinition::itemTypes),
            ResourceLocation.CODEC.listOf().optionalFieldOf("only_items", List.of()).forGetter(ItemLevelDefinition::onlyItems),
            Codec.STRING.listOf().optionalFieldOf("only_tags", List.of()).forGetter(ItemLevelDefinition::onlyTags)
    ).apply(instance, ItemLevelDefinition::new));

    public ItemLevelDefinition {
        maxLevel = Math.max(1, maxLevel);
        defaultLevel = Math.max(1, Math.min(defaultLevel, maxLevel));
        xpPerLevel = Math.max(1, xpPerLevel);
        attributes = List.copyOf(attributes == null ? List.of() : attributes);
        expression = expression == null ? Optional.empty() : expression;
        xpAddExpression = xpAddExpression == null ? Optional.empty() : xpAddExpression;
        upEvent = upEvent == null || upEvent.isBlank() ? "KILL" : upEvent;
        weight = Math.max(0.0F, weight);
        itemTypes = List.copyOf(itemTypes == null ? List.of() : itemTypes);
        onlyItems = List.copyOf(onlyItems == null ? List.of() : onlyItems);
        onlyTags = List.copyOf(onlyTags == null ? List.of() : onlyTags);
    }

    public int xpNeeded(int level) {
        if (expression.isPresent() && !expression.get().isBlank()) {
            return Math.max(1, (int) Math.round(LevelExpression.evaluate(expression.get(), level)));
        }
        return xpPerLevel * Math.max(1, level);
    }

    public int xpGain(int level, double amount) {
        if (xpAddExpression.isPresent() && !xpAddExpression.get().isBlank()) {
            return Math.max(0, (int) Math.round(LevelExpression.evaluate(xpAddExpression.get(), level)));
        }
        return Math.max(1, (int) Math.round(Math.max(1.0D, amount)));
    }
}
