package net.exmo.exmodifier.core.component;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record UnlockedSlots(List<ResourceLocation> slots) {
    public static final UnlockedSlots EMPTY = new UnlockedSlots(List.of());
    public static final Codec<UnlockedSlots> CODEC = ResourceLocation.CODEC.listOf().xmap(UnlockedSlots::new, UnlockedSlots::slots);
    public static final StreamCodec<ByteBuf, UnlockedSlots> STREAM_CODEC =
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()).map(UnlockedSlots::new, UnlockedSlots::slots);

    public UnlockedSlots {
        slots = List.copyOf(slots == null ? List.of() : slots);
    }

    public UnlockedSlots with(ResourceLocation slot) {
        if (slots.contains(slot)) return this;
        ArrayList<ResourceLocation> next = new ArrayList<>(slots);
        next.add(slot);
        return new UnlockedSlots(next);
    }

    public boolean contains(ResourceLocation slot) {
        return slots.contains(slot);
    }
}
