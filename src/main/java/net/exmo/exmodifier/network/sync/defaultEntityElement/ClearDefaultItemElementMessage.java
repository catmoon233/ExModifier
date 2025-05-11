
package net.exmo.exmodifier.network.sync.defaultEntityElement;
import net.exmo.exmodifier.content.element.ExElementHandle;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.function.Supplier;

public record ClearDefaultItemElementMessage() {

    public static void encode(ClearDefaultItemElementMessage msg, FriendlyByteBuf buffer) {

    }

    public static ClearDefaultItemElementMessage decode(FriendlyByteBuf buffer) {
            return new ClearDefaultItemElementMessage();
    }

    public static void handle(ClearDefaultItemElementMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(ExElementHandle.elementDefaultMap::clear);
        ctx.get().setPacketHandled(true);
    }
}