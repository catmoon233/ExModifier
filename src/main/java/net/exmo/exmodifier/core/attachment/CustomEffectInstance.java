package net.exmo.exmodifier.core.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.exmo.exmodifier.core.data.AttributeSpec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record CustomEffectInstance(ResourceLocation id, int duration, int amplifier, List<AttributeSpec> attributes) {
    public static final Codec<CustomEffectInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(CustomEffectInstance::id),
            Codec.INT.optionalFieldOf("duration", 0).forGetter(CustomEffectInstance::duration),
            Codec.INT.optionalFieldOf("amplifier", 0).forGetter(CustomEffectInstance::amplifier),
            AttributeSpec.CODEC.listOf().optionalFieldOf("attributes", List.of()).forGetter(CustomEffectInstance::attributes)
    ).apply(instance, CustomEffectInstance::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CustomEffectInstance> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, CustomEffectInstance::id,
            ByteBufCodecs.VAR_INT, CustomEffectInstance::duration,
            ByteBufCodecs.VAR_INT, CustomEffectInstance::amplifier,
            AttributeSpec.STREAM_CODEC.apply(ByteBufCodecs.list()), CustomEffectInstance::attributes,
            CustomEffectInstance::new
    );

    public CustomEffectInstance {
        attributes = List.copyOf(attributes == null ? List.of() : attributes);
        duration = Math.max(0, duration);
        amplifier = Math.max(0, amplifier);
    }

    public CustomEffectInstance tick() {
        return new CustomEffectInstance(id, Math.max(0, duration - 1), amplifier, attributes);
    }

    public boolean expired() {
        return duration <= 0;
    }
}
