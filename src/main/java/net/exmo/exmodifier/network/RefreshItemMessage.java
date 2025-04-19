package net.exmo.exmodifier.network;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.helper.ModifierEntryHelper;
import net.exmo.exmodifier.content.slot.menu.EmbeddedMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public record RefreshItemMessage(int refreshItem,int toRefreshItem) {
    public static void encode(RefreshItemMessage msg, FriendlyByteBuf buffer) {
        CompoundTag p130080 = new CompoundTag();
        p130080.putInt("refreshItem", msg.refreshItem);
        p130080.putInt("toRefreshItem", msg.toRefreshItem);
        buffer.writeNbt(p130080);
    }

    public static RefreshItemMessage decode(FriendlyByteBuf buffer) {
        CompoundTag compoundTag = buffer.readNbt();
        return new RefreshItemMessage(compoundTag.getInt("refreshItem"),compoundTag.getInt("toRefreshItem"));
    }

    public static void handle(RefreshItemMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            ItemStack refreshItem = player.getInventory().getItem(msg.refreshItem);
            ItemStack toRefreshItem = player.getInventory().getItem(msg.toRefreshItem);
            ModifierEntryHelper.refreshContent.applyRefreshEffect(player,toRefreshItem,refreshItem);
            player.containerMenu.slotsChanged(null);
            player.containerMenu.transferState(player.containerMenu);
            player.inventoryMenu.sendAllDataToRemote();
            player.containerMenu.broadcastChanges();
            player.inventoryMenu.broadcastChanges();
            ChangeRefreshMenuTextListMessage message = new ChangeRefreshMenuTextListMessage(msg.refreshItem, msg.toRefreshItem);
            Exmodifier.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), message);

        });
        ctx.get().setPacketHandled(true);
    }
}
