package net.exmo.exmodifier.core.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record EntryBook(ResourceLocation entryId, int level, Optional<Double> probability) {
    public static final Codec<EntryBook> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("entry").forGetter(EntryBook::entryId),
            Codec.INT.optionalFieldOf("level", 1).forGetter(EntryBook::level),
            Codec.DOUBLE.optionalFieldOf("probability").forGetter(EntryBook::probability)
    ).apply(instance, EntryBook::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, EntryBook> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, EntryBook::entryId,
            ByteBufCodecs.VAR_INT, EntryBook::level,
            ByteBufCodecs.DOUBLE.apply(ByteBufCodecs::optional), EntryBook::probability,
            EntryBook::new
    );

    public EntryBook(ResourceLocation entryId, int level) {
        this(entryId, level, Optional.empty());
    }

    public EntryBook {
        level = Math.max(1, level);
        probability = probability == null ? Optional.empty() : probability;
    }
}
