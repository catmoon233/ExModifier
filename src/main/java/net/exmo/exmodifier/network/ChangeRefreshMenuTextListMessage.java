package net.exmo.exmodifier.network;
import net.exmo.exmodifier.content.slot.menu.EmbeddedMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.function.Supplier;

public record ChangeRefreshMenuTextListMessage(int a,int b  ) {

    public static void encode(ChangeRefreshMenuTextListMessage msg, FriendlyByteBuf buffer) {
        CompoundTag p130080 = new CompoundTag();
        p130080.putInt("aa", msg.a);
        p130080.putInt("bb", msg.b);
        buffer.writeNbt(p130080);
    }

    public static ChangeRefreshMenuTextListMessage decode(FriendlyByteBuf buffer) {
        CompoundTag compoundTag = buffer.readNbt();
        return new ChangeRefreshMenuTextListMessage(
                    compoundTag.getInt("aa"),
                    compoundTag.getInt("bb")
            );
    }

    public static void handle(ChangeRefreshMenuTextListMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(()->{
            MessageClientFunction.ApplyChangeRefreshMenuTextListMessage(msg.a,msg.b);
        });
        ctx.get().setPacketHandled(true);
    }
}