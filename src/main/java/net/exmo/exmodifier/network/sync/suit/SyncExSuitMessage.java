package net.exmo.exmodifier.network.sync.suit;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.suit.ExSuit;
import net.exmo.exmodifier.content.suit.ExSuitHandle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncExSuitMessage(ExSuit exSuit) {

    public static void encode(SyncExSuitMessage msg, FriendlyByteBuf buffer) {
        buffer.writeNbt(ExSuit.ExSer.toNbt(msg.exSuit));
    }

    public static SyncExSuitMessage decode(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt(); // Adjust the length as needed
        if (tag != null) {
            return new SyncExSuitMessage(ExSuit.ExSer.fromNbt(tag));
        }
        return null;
    }

    public static void handle(SyncExSuitMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Handle the received message
            ExSuit exSuit1 = msg.exSuit();
            Exmodifier.LOGGER.debug("Received ExSuit: " + exSuit1);
            ExSuitHandle.registerExSuit(exSuit1);
        });
        ctx.get().setPacketHandled(true);
    }
}