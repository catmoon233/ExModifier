package net.exmo.exmodifier.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record ItemTypeDefinition(List<String> itemIds, List<String> itemTags) {
    public static final Codec<ItemTypeDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().optionalFieldOf("items", List.of()).forGetter(ItemTypeDefinition::itemIds),
            Codec.STRING.listOf().optionalFieldOf("tags", List.of()).forGetter(ItemTypeDefinition::itemTags)
    ).apply(instance, ItemTypeDefinition::new));

    public ItemTypeDefinition {
        itemIds = List.copyOf(itemIds == null ? List.of() : itemIds);
        itemTags = List.copyOf(itemTags == null ? List.of() : itemTags);
    }
}
