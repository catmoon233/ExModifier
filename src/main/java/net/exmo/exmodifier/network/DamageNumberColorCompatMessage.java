package net.exmo.exmodifier.network;


import net.exmo.exmodifier_compat.compat.DamageNumberCompatData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Date;
import java.util.function.Supplier;

public record DamageNumberColorCompatMessage(int color) {
    public static void encode(DamageNumberColorCompatMessage msg, FriendlyByteBuf buffer) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putInt("color", msg.color);
        buffer.writeNbt(compoundTag);
    }

    public static DamageNumberColorCompatMessage decode(FriendlyByteBuf buffer) {
         return new DamageNumberColorCompatMessage(
                buffer.readNbt().getInt("color")
        );
    }

    public static void handle(DamageNumberColorCompatMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(()->{
            DamageNumberCompatData.lastColor = msg.color;
        });
        ctx.get().setPacketHandled(true);
    }
}
