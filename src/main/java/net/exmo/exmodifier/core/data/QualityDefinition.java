package net.exmo.exmodifier.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record QualityDefinition(
        int maxRefine,
        Optional<String> tooltipKey,
        float weight,
        int rarity,
        float growValue,
        boolean autoRefresh,
        boolean cantRemoveEntry,
        boolean showInHeadTooltip,
        boolean refineNeedSameStar,
        List<ResourceLocation> boundEntries,
        List<ResourceLocation> onlyItems,
        List<String> onlyTags,
        List<ResourceLocation> itemTypes
) {
    public static final Codec<QualityDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("max_refine", 0).forGetter(QualityDefinition::maxRefine),
            Codec.STRING.optionalFieldOf("tooltip_key").forGetter(QualityDefinition::tooltipKey),
            Codec.FLOAT.optionalFieldOf("weight", 1.0F).forGetter(QualityDefinition::weight),
            Codec.INT.optionalFieldOf("rarity", 0).forGetter(QualityDefinition::rarity),
            Codec.FLOAT.optionalFieldOf("grow_value", 0.0F).forGetter(QualityDefinition::growValue),
            Codec.BOOL.optionalFieldOf("auto_refresh", false).forGetter(QualityDefinition::autoRefresh),
            Codec.BOOL.optionalFieldOf("cant_remove_entry", false).forGetter(QualityDefinition::cantRemoveEntry),
            Codec.BOOL.optionalFieldOf("show_in_head_tooltip", false).forGetter(QualityDefinition::showInHeadTooltip),
            Codec.BOOL.optionalFieldOf("refine_need_same_star", false).forGetter(QualityDefinition::refineNeedSameStar),
            ResourceLocation.CODEC.listOf().optionalFieldOf("bound_entries", List.of()).forGetter(QualityDefinition::boundEntries),
            ResourceLocation.CODEC.listOf().optionalFieldOf("only_items", List.of()).forGetter(QualityDefinition::onlyItems),
            Codec.STRING.listOf().optionalFieldOf("only_tags", List.of()).forGetter(QualityDefinition::onlyTags),
            ResourceLocation.CODEC.listOf().optionalFieldOf("item_types", List.of()).forGetter(QualityDefinition::itemTypes)
    ).apply(instance, QualityDefinition::new));

    public QualityDefinition {
        boundEntries = List.copyOf(boundEntries == null ? List.of() : boundEntries);
        onlyItems = List.copyOf(onlyItems == null ? List.of() : onlyItems);
        onlyTags = List.copyOf(onlyTags == null ? List.of() : onlyTags);
        itemTypes = List.copyOf(itemTypes == null ? List.of() : itemTypes);
        weight = Math.max(0.0F, weight);
        maxRefine = Math.max(0, maxRefine);
        tooltipKey = tooltipKey == null ? Optional.empty() : tooltipKey;
    }
}
