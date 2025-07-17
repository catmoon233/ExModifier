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

public record SyncEntityElementMessage(UUID uuid, ExElementInstant exElementInstant) {
    public static void encode(SyncEntityElementMessage msg, FriendlyByteBuf buffer) {
        if (msg.exElementInstant.getElement() ==null || msg.exElementInstant.getElement().getResId() == null)return;
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putInt("Level", msg.exElementInstant.getLevel());
        compoundTag.putString("ID", msg.exElementInstant.getElement().getResId().toString());
        compoundTag.putString("UUID",msg.uuid.toString());

        buffer.writeNbt(compoundTag);
    }

    public static SyncEntityElementMessage decode(FriendlyByteBuf buffer) {
        CompoundTag compoundTag = buffer.readNbt();
        return new SyncEntityElementMessage(
                UUID.fromString(compoundTag.getString("UUID")),
                new ExElementInstant(
                        ExElementHandle.getExElement(compoundTag.getString("ID")),
                        compoundTag.getInt("Level")
                )
        );
    }

    public static void handle(SyncEntityElementMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(()->{
            if (!Config.ELEMENT_SYSTEM.get())return;

            ExElementEntityData.ELEMENT_ENTITY_DATA.put(msg.uuid,msg.exElementInstant);
        });
        ctx.get().setPacketHandled(true);
    }
}
