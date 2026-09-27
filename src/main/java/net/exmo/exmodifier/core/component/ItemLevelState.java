package net.exmo.exmodifier.core.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record ItemLevelState(ResourceLocation tableId, int level, int xp) {
    public static final ItemLevelState EMPTY = new ItemLevelState(ResourceLocation.parse("exmodifier:generic"), 1, 0);
    public static final Codec<ItemLevelState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("table", ResourceLocation.parse("exmodifier:generic")).forGetter(ItemLevelState::tableId),
            Codec.INT.optionalFieldOf("level", 1).forGetter(ItemLevelState::level),
            Codec.INT.optionalFieldOf("xp", 0).forGetter(ItemLevelState::xp)
    ).apply(instance, ItemLevelState::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemLevelState> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ItemLevelState::tableId,
            ByteBufCodecs.VAR_INT, ItemLevelState::level,
            ByteBufCodecs.VAR_INT, ItemLevelState::xp,
            ItemLevelState::new
    );

    public ItemLevelState {
        level = Math.max(1, level);
        xp = Math.max(0, xp);
    }

    public ItemLevelState withXp(int nextXp, int nextLevel) {
        return new ItemLevelState(tableId, nextLevel, nextXp);
    }
}
