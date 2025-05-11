
package net.exmo.exmodifier.network.sync.element;
import net.exmo.exmodifier.content.element.ExElementHandle;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.function.Supplier;

public record ClearElementMessage() {

    public static void encode(ClearElementMessage msg, FriendlyByteBuf buffer) {

    }

    public static ClearElementMessage decode(FriendlyByteBuf buffer) {
            return new ClearElementMessage();
    }

    public static void handle(ClearElementMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ExElementHandle.exElements = new HashMap<>();
        });
        ctx.get().setPacketHandled(true);
    }
}