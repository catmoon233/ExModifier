package net.exmo.exmodifier.network;

import net.exmo.exmodifier_compat.compat.DamageNumberCompatData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Date;
import java.util.function.Supplier;

public record DamageNumberCompatMessage(int color) {
    public static void encode(DamageNumberCompatMessage msg, FriendlyByteBuf buffer) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putInt("color", msg.color);
        buffer.writeNbt(compoundTag);
    }

    public static DamageNumberCompatMessage decode(FriendlyByteBuf buffer) {
         return new DamageNumberCompatMessage(
                buffer.readNbt().getInt("color")
        );
    }

    public static void handle(DamageNumberCompatMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(()->{
            long time = new Date().getTime();
            while (DamageNumberCompatData.cache.asMap().containsKey(time)) {
                time++;
            }
            DamageNumberCompatData.cache.put(time,  msg.color);
            DamageNumberCompatData.lastColor = msg.color;
        });
        ctx.get().setPacketHandled(true);
    }
}
