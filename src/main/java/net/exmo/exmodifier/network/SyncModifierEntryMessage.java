package net.exmo.exmodifier.network;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierEntryDataBuilder;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncModifierEntryMessage(ModifierEntry modifierEntry) {

    public static void encode(SyncModifierEntryMessage msg, FriendlyByteBuf buffer) {
        buffer.writeNbt(new ModifierEntryDataBuilder(msg.modifierEntry).toNBT());
    }

    public static SyncModifierEntryMessage decode(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt(); // Adjust the length as needed
        if (tag != null) {
            return new SyncModifierEntryMessage(ModifierEntryDataBuilder.fromNBT(tag).build());
        }
        return null;
    }

    public static void handle(SyncModifierEntryMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Handle the received message
            ModifierEntry modifierEntry = msg.modifierEntry();
            // Process the modifierEntry as needed
            System.out.println("Received modifierEntry: " + modifierEntry);
            ModifierHandle.RegisterModifierEntry(modifierEntry);
        });
        ctx.get().setPacketHandled(true);
    }
}