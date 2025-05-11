package net.exmo.exmodifier.network.sync.defaultItemElement;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.element.DefaultEntityElement;
import net.exmo.exmodifier.content.element.ExElement;
import net.exmo.exmodifier.content.element.ExElementHandle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncDefaultEntityElementMessage(DefaultEntityElement exElement) {

    public static void encode(SyncDefaultEntityElementMessage msg, FriendlyByteBuf buffer) {
        buffer.writeNbt(DefaultEntityElement.SERIALIZER.toNbt(msg.exElement));
    }

    public static SyncDefaultEntityElementMessage decode(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt(); // Adjust the length as needed
        if (tag != null) {
            return new SyncDefaultEntityElementMessage(DefaultEntityElement.SERIALIZER.fromNbt(tag));
        }
        return null;
    }

    public static void handle(SyncDefaultEntityElementMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Handle the received message
            DefaultEntityElement element = msg.exElement();
            // Process the exElement as needed
            Exmodifier.LOGGER.debug("Received DefaultEntityElement: " + element);
            ExElementHandle.elementDefaultMap2.put(element.getEntityType(),element);
        });
        ctx.get().setPacketHandled(true);
    }
}