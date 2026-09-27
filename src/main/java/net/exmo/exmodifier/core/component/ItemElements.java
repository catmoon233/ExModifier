package net.exmo.exmodifier.core.component;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public record ItemElements(Map<ResourceLocation, Integer> amounts) {
    public static final ItemElements EMPTY = new ItemElements(Map.of());
    public static final Codec<ItemElements> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT)
            .xmap(ItemElements::new, ItemElements::amounts);
    public static final StreamCodec<ByteBuf, ItemElements> STREAM_CODEC =
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.VAR_INT)
                    .map(ItemElements::new, value -> new HashMap<>(value.amounts()));

    public ItemElements {
        amounts = amounts == null ? Map.of() : Map.copyOf(amounts);
    }

    public ItemElements with(ResourceLocation id, int amount) {
        HashMap<ResourceLocation, Integer> next = new HashMap<>(amounts);
        if (amount <= 0) next.remove(id);
        else next.put(id, amount);
        return new ItemElements(next);
    }
}
