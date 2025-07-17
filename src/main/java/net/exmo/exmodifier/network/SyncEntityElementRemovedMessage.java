package net.exmo.exmodifier.network;

import net.exmo.exmodifier.Config;
import net.exmo.exmodifier.content.element.ExElementEntityData;
import net.exmo.exmodifier.content.element.ExElementHandle;
import net.exmo.exmodifier.content.element.ExElementInstant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record SyncEntityElementRemovedMessage(UUID uuid) {
    public static void encode(SyncEntityElementRemovedMessage msg, FriendlyByteBuf buffer) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("UUID",msg.uuid.toString());

        buffer.writeNbt(compoundTag);
    }

    public static SyncEntityElementRemovedMessage decode(FriendlyByteBuf buffer) {
        CompoundTag compoundTag = buffer.readNbt();
        return new SyncEntityElementRemovedMessage(
                UUID.fromString(compoundTag.getString("UUID"))

        );
    }

    public static void handle(SyncEntityElementRemovedMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(()->{
            if (!Config.ELEMENT_SYSTEM.get())return;

            ExElementEntityData.ELEMENT_ENTITY_DATA.remove(msg.uuid);
        });
        ctx.get().setPacketHandled(true);
    }
}
