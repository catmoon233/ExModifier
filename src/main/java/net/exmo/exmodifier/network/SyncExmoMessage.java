package net.exmo.exmodifier.network;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncExmoMessage {
//    private List<ModifierEntry> list;
//
//    public SyncExmoMessage(List<ModifierEntry> list) {
//        this.list = list;
//    }
//
//    public List<ModifierEntry> getList() {
//        return list;
//    }
//
//    public static void encode(SyncExmoMessage msg, FriendlyByteBuf buf) {
//        buf.writeInt(msg.list.size());
//        for (ModifierEntry s : msg.list) {
//            buf.writeUtf(s);
//        }
//    }
//
//    public static SyncExmoMessage decode(FriendlyByteBuf buf) {
//        int size = buf.readInt();
//        List<String> list = new ArrayList<>(size);
//        for (int i = 0; i < size; i++) {
//            list.add(buf.readUtf(32767)); // Adjust the length as needed
//        }
//        return new SyncExmoMessage(list);
//    }
//
//    public static void handle(SyncExmoMessage msg, Supplier<NetworkEvent.Context> ctx) {
//        ctx.get().enqueueWork(() -> {
//            // Handle the received list on the main thread
//            // For example, update the client-side list
//            // ClientListHandler.updateList(msg.getList());
//        });
//        ctx.get().setPacketHandled(true);
//    }
}