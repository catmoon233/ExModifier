package net.exmo.exmodifier.core.component;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record AppliedModifiers(List<AppliedModifier> values) {
    public static final AppliedModifiers EMPTY = new AppliedModifiers(List.of());
    public static final Codec<AppliedModifiers> CODEC = AppliedModifier.CODEC.listOf()
            .xmap(AppliedModifiers::new, AppliedModifiers::values);
    public static final StreamCodec<RegistryFriendlyByteBuf, AppliedModifiers> STREAM_CODEC =
            AppliedModifier.STREAM_CODEC.apply(ByteBufCodecs.list()).map(AppliedModifiers::new, AppliedModifiers::values);

    public AppliedModifiers {
        values = List.copyOf(values == null ? List.of() : values);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public Optional<AppliedModifier> find(ResourceLocation entryId) {
        return values.stream().filter(value -> value.entryId().equals(entryId)).findFirst();
    }

    public AppliedModifiers with(AppliedModifier modifier) {
        List<AppliedModifier> next = new ArrayList<>(values.size() + 1);
        boolean replaced = false;
        for (AppliedModifier existing : values) {
            if (existing.entryId().equals(modifier.entryId())) {
                next.add(modifier);
                replaced = true;
            } else {
                next.add(existing);
            }
        }
        if (!replaced) next.add(modifier);
        return new AppliedModifiers(next);
    }

    public AppliedModifiers without(ResourceLocation entryId) {
        return new AppliedModifiers(values.stream().filter(value -> !value.entryId().equals(entryId)).toList());
    }
}
