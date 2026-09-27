package net.exmo.exmodifier.core.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

/** Authoritative applied-entry payload stored on items as a data component. */
public record AppliedModifier(ResourceLocation entryId, int level, Optional<ResourceLocation> slotId, boolean locked) {
    public static final Codec<AppliedModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("entry").forGetter(AppliedModifier::entryId),
            Codec.INT.optionalFieldOf("level", 1).forGetter(AppliedModifier::level),
            ResourceLocation.CODEC.optionalFieldOf("slot").forGetter(AppliedModifier::slotId),
            Codec.BOOL.optionalFieldOf("locked", false).forGetter(AppliedModifier::locked)
    ).apply(instance, AppliedModifier::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AppliedModifier> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, AppliedModifier::entryId,
            ByteBufCodecs.VAR_INT, AppliedModifier::level,
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs::optional), AppliedModifier::slotId,
            ByteBufCodecs.BOOL, AppliedModifier::locked,
            AppliedModifier::new
    );

    public AppliedModifier {
        level = Math.max(1, level);
        slotId = slotId == null ? Optional.empty() : slotId;
    }

    public AppliedModifier withLevel(int next) {
        return new AppliedModifier(entryId, next, slotId, locked);
    }
}
