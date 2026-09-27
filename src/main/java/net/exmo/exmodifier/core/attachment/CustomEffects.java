package net.exmo.exmodifier.core.attachment;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record CustomEffects(List<CustomEffectInstance> values) {
    public static final CustomEffects EMPTY = new CustomEffects(List.of());
    public static final Codec<CustomEffects> CODEC = CustomEffectInstance.CODEC.listOf()
            .xmap(CustomEffects::new, CustomEffects::values);
    public static final StreamCodec<RegistryFriendlyByteBuf, CustomEffects> STREAM_CODEC =
            CustomEffectInstance.STREAM_CODEC.apply(ByteBufCodecs.list()).map(CustomEffects::new, CustomEffects::values);

    public CustomEffects {
        values = List.copyOf(values == null ? List.of() : values);
    }

    public Optional<CustomEffectInstance> find(ResourceLocation id) {
        return values.stream().filter(value -> value.id().equals(id)).findFirst();
    }

    public CustomEffects with(CustomEffectInstance instance) {
        List<CustomEffectInstance> next = new ArrayList<>();
        boolean replaced = false;
        for (CustomEffectInstance existing : values) {
            if (existing.id().equals(instance.id())) {
                next.add(instance);
                replaced = true;
            } else next.add(existing);
        }
        if (!replaced) next.add(instance);
        return new CustomEffects(next);
    }

    public CustomEffects without(ResourceLocation id) {
        return new CustomEffects(values.stream().filter(value -> !value.id().equals(id)).toList());
    }
}
