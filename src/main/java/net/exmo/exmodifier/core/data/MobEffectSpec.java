package net.exmo.exmodifier.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record MobEffectSpec(ResourceLocation effect, int duration, int amplifier, boolean ambient, boolean visible) {
    public static final Codec<MobEffectSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("effect").forGetter(MobEffectSpec::effect),
            Codec.INT.optionalFieldOf("duration", 40).forGetter(MobEffectSpec::duration),
            Codec.INT.optionalFieldOf("amplifier", 0).forGetter(MobEffectSpec::amplifier),
            Codec.BOOL.optionalFieldOf("ambient", false).forGetter(MobEffectSpec::ambient),
            Codec.BOOL.optionalFieldOf("visible", false).forGetter(MobEffectSpec::visible)
    ).apply(instance, MobEffectSpec::new));

    public MobEffectSpec {
        duration = Math.max(1, duration);
        amplifier = Math.max(0, amplifier);
    }
}
