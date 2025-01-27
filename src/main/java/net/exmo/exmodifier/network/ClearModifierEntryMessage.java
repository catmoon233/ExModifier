package net.exmo.exmodifier.network;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierEntryDataBuilder;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.function.Supplier;

public record ClearModifierEntryMessage() {

    public static void encode(ClearModifierEntryMessage msg, FriendlyByteBuf buffer) {

    }

    public static ClearModifierEntryMessage decode(FriendlyByteBuf buffer) {
            return new ClearModifierEntryMessage();
    }

    public static void handle(ClearModifierEntryMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ModifierHandle.modifierEntryMap = new HashMap<>();
        });
        ctx.get().setPacketHandled(true);
    }
}