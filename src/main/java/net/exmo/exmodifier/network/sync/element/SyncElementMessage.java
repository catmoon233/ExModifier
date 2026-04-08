package net.exmo.exmodifier.network.sync.element;
import net.exmo.exmodifier.Config;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.element.ExElement;
import net.exmo.exmodifier.content.element.ExElementHandle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncElementMessage(ExElement exElement) {

    public static void encode(SyncElementMessage msg, FriendlyByteBuf buffer) {
        buffer.writeNbt(ExElement.EX_SERIALIZE.toNbt(msg.exElement));
    }

    public static SyncElementMessage decode(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt(); // Adjust the length as needed
        if (tag != null) {
            return new SyncElementMessage(ExElement.EX_SERIALIZE.fromNbt(tag));
        }
        return null;
    }

    public static void handle(SyncElementMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (!Config.ELEMENT_SYSTEM.get())return;

            // Handle the received message
            ExElement element = msg.exElement();
            // Process the exElement as needed
            Exmodifier.LOGGER.debug("Received exElement: " + element);
            ExElementHandle.registerExElement(element);
        });
        ctx.get().setPacketHandled(true);
    }
}