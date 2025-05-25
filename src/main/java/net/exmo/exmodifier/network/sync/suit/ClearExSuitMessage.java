package net.exmo.exmodifier.network.sync.suit;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.content.suit.ExSuit;
import net.exmo.exmodifier.content.suit.ExSuitHandle;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.function.Supplier;

public record ClearExSuitMessage() {

    public static void encode(ClearExSuitMessage msg, FriendlyByteBuf buffer) {

    }

    public static ClearExSuitMessage decode(FriendlyByteBuf buffer) {
            return new ClearExSuitMessage();
    }

    public static void handle(ClearExSuitMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ExSuitHandle.LoadExSuit = new HashMap<>();
        });
        ctx.get().setPacketHandled(true);
    }
}