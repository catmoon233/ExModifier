package net.exmo.exmodifier.network.sync;

import net.exmo.exmodifier.content.element.ExElementHandle;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public abstract class AbstractClearElementMessage<T> {
    // 通用编码方法（不需要传输数据）
    public static <T> void encode(AbstractClearElementMessage<T> msg, FriendlyByteBuf buffer) {}

    // 通用解码方法
    public static <T> AbstractClearElementMessage<T> decode(FriendlyByteBuf buffer) {
        return new AbstractClearElementMessage<T>() {
            @Override
            protected Map<?, ?> getMap() {
                return Map.of();
            }
        };
    }

    // 通用处理逻辑
    public static <T> void handle(AbstractClearElementMessage<T> msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 通过抽象方法获取需要操作的map
            Map<?, ?> targetMap = msg.getMap();
            if (targetMap != null) {
                targetMap.clear();
            }
        });
        ctx.get().setPacketHandled(true);
    }

    // 抽象方法由子类实现，提供具体要操作的map
    protected abstract Map<?, ?> getMap();
}