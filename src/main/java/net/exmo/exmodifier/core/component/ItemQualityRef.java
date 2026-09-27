package net.exmo.exmodifier.core.component;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record ItemQualityRef(ResourceLocation qualityId) {
    public static final Codec<ItemQualityRef> CODEC = ResourceLocation.CODEC.xmap(ItemQualityRef::new, ItemQualityRef::qualityId);
    public static final StreamCodec<ByteBuf, ItemQualityRef> STREAM_CODEC =
            ResourceLocation.STREAM_CODEC.map(ItemQualityRef::new, ItemQualityRef::qualityId);
}
