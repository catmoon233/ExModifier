package net.exmo.exmodifier.network;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.content.slot.menu.EmbeddedMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.function.Supplier;

public record SendServerSlotIDMessage(String slot,int id) {

    public static void encode(SendServerSlotIDMessage msg, FriendlyByteBuf buffer) {
        CompoundTag p130080 = new CompoundTag();
        p130080.putString("slot", msg.slot);
        p130080.putInt("id", msg.id);
        buffer.writeNbt(p130080);
    }

    public static SendServerSlotIDMessage decode(FriendlyByteBuf buffer) {
        CompoundTag compoundTag = buffer.readNbt();
        return new SendServerSlotIDMessage(compoundTag.getString("slot"),compoundTag.getInt("id"));
    }

    public static void handle(SendServerSlotIDMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            EmbeddedMenu.SlotMap.put(msg.slot,msg.id);
        });
        ctx.get().setPacketHandled(true);
    }
}