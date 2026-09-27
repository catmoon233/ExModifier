package net.exmo.exmodifier.core.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record RefineState(int count, Optional<ResourceLocation> lastMaterial) {
    public static final RefineState EMPTY = new RefineState(0, Optional.empty());
    public static final Codec<RefineState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("count", 0).forGetter(RefineState::count),
            ResourceLocation.CODEC.optionalFieldOf("last_material").forGetter(RefineState::lastMaterial)
    ).apply(instance, RefineState::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, RefineState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RefineState::count,
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs::optional), RefineState::lastMaterial,
            RefineState::new
    );

    public RefineState {
        count = Math.max(0, count);
        lastMaterial = lastMaterial == null ? Optional.empty() : lastMaterial;
    }

    public RefineState incremented(ResourceLocation material) {
        return new RefineState(count + 1, Optional.ofNullable(material));
    }
}

