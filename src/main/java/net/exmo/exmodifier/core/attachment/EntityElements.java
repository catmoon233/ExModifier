package net.exmo.exmodifier.core.attachment;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public record EntityElements(Map<ResourceLocation, Float> amounts) {
    public static final EntityElements EMPTY = new EntityElements(Map.of());
    public static final Codec<EntityElements> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, Codec.FLOAT)
            .xmap(EntityElements::new, EntityElements::amounts);
    public static final StreamCodec<ByteBuf, EntityElements> STREAM_CODEC =
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.FLOAT)
                    .map(EntityElements::new, value -> new HashMap<>(value.amounts()));

    public EntityElements {
        amounts = amounts == null ? Map.of() : Map.copyOf(amounts);
    }

    public EntityElements with(ResourceLocation id, float amount) {
        HashMap<ResourceLocation, Float> next = new HashMap<>(amounts);
        if (amount == 0.0F) next.remove(id);
        else next.put(id, amount);
        return new EntityElements(next);
    }
}
