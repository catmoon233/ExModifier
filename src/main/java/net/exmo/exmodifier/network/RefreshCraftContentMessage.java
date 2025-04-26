package net.exmo.exmodifier.network;

import net.exmo.exmodifier.content.client.TableCraftData;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.function.Supplier;

public record RefreshCraftContentMessage(int index) {
    public static void encode(RefreshCraftContentMessage msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.index);
    }

    public static RefreshCraftContentMessage decode(FriendlyByteBuf buffer) {
        return new RefreshCraftContentMessage(
                buffer.readInt()
        );
    }

    public static void handle(RefreshCraftContentMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender().containerMenu instanceof CraftingMenu craftingMenu){
                TableCraftData.changeIndex = true;
                ctx.get().getSender().getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY).ifPresent(
                        e ->{
                            e.craftIndex = msg.index;
                            e.syncPlayerVariables(ctx.get().getSender());
                        }
                );
                craftingMenu.slotsChanged(null);

            }
        });
        ctx.get().setPacketHandled(true);
    }
}
