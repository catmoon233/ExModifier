package net.exmo.exmodifier.network;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.content.slot.menu.EmbeddedMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.function.Supplier;

public record ClearSlotMapMessage() {

    public static void encode(ClearSlotMapMessage msg, FriendlyByteBuf buffer) {

    }

    public static ClearSlotMapMessage decode(FriendlyByteBuf buffer) {
            return new ClearSlotMapMessage();
    }

    public static void handle(ClearSlotMapMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            EmbeddedMenu.SlotMap = new HashMap<>();
        });
        ctx.get().setPacketHandled(true);
    }
}