package net.exmo.exmodifier.network.sync.defaultEntityElement;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.element.DefaultItemElement;
import net.exmo.exmodifier.content.element.ExElement;
import net.exmo.exmodifier.content.element.ExElementHandle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncDefaultItemElementMessage(DefaultItemElement exElement) {

    public static void encode(SyncDefaultItemElementMessage msg, FriendlyByteBuf buffer) {
        buffer.writeNbt(DefaultItemElement.EX_SERIALIZE.toNbt(msg.exElement));
    }

    public static SyncDefaultItemElementMessage decode(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt(); // Adjust the length as needed
        if (tag != null) {
            return new SyncDefaultItemElementMessage(DefaultItemElement.EX_SERIALIZE.fromNbt(tag));
        }
        return null;
    }

    public static void handle(SyncDefaultItemElementMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Handle the received message
            DefaultItemElement element = msg.exElement();
            // Process the exElement as needed
            Exmodifier.LOGGER.debug("Received DefaultItemElement: " + element);
            ExElementHandle.elementDefaultMap.put(element.itemSelector(), element);
        });
        ctx.get().setPacketHandled(true);
    }
}