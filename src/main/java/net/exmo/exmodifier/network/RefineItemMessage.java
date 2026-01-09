package net.exmo.exmodifier.network;

import net.exmo.exmodifier.Config;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.helper.ModifierEntryHelper;
import net.exmo.exmodifier.content.refine.RefineHelper;
import net.exmo.exmodifier.content.refine.RefineItemRecord;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public record RefineItemMessage(int refreshItem, int toRefreshItem) {
    public static void encode(RefineItemMessage msg, FriendlyByteBuf buffer) {
        CompoundTag p130080 = new CompoundTag();
        p130080.putInt("refreshItem", msg.refreshItem);
        p130080.putInt("toRefreshItem", msg.toRefreshItem);
        buffer.writeNbt(p130080);
    }

    public static RefineItemMessage decode(FriendlyByteBuf buffer) {
        CompoundTag compoundTag = buffer.readNbt();
        return new RefineItemMessage(compoundTag.getInt("refreshItem"),compoundTag.getInt("toRefreshItem"));
    }

    public static void handle(RefineItemMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (!Config.refine_system){
                Exmodifier.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new PlayerRefreshScreenOverMessageMessage(ItemStack.EMPTY, Component.translatable("gui.exmodifier.close_refine_system")));
                return;
            }
            ItemStack refineItem = player.getInventory().getItem(msg.refreshItem); // 消耗品
            ItemStack toRefreshItem = player.getInventory().getItem(msg.toRefreshItem); // 目标物品
            RefineHelper refineHelper = RefineHelper.of(toRefreshItem);
            boolean b = refineHelper.canRefine(refineItem);
            
            PlayerRefreshScreenOverMessageMessage message1;
            if (b){
                // 获取消耗品的精炼记录
                ResourceLocation refineItemId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(refineItem.getItem());
                RefineItemRecord record = net.exmo.exmodifier.content.refine.RefineHandle.getRefineItem(refineItemId);
                
                boolean success = true;
                if (record != null) {
                    // 使用随机数判断是否成功
                    int chance = record.getChance();
                    success = net.minecraft.util.RandomSource.createNewThreadLocalInstance().nextInt(100) < chance;
                }
                
                if (success) {
                    // 检查目标物品当前星级是否已达到该消耗品定义的最大升星数
                    if (record != null && refineHelper.getRefineLevel() >= record.getMaxBoostStar()) {
                        // 已达到该消耗品允许的最大星级
                        message1 = new PlayerRefreshScreenOverMessageMessage(ItemStack.EMPTY, Component.translatable("gui.exmodifier.refine_fail"));
                    } else {
                        // 增加星级
                        refineHelper.addRefine(true,1);
                        message1 = new PlayerRefreshScreenOverMessageMessage(ItemStack.EMPTY, Component.translatable("gui.exmodifier.refine_success"));
                        refineItem.shrink(1);
                    }
                } else {
                    message1 = new PlayerRefreshScreenOverMessageMessage(ItemStack.EMPTY, Component.translatable("gui.exmodifier.refine_fail"));
                }
            }else {
                message1 = new PlayerRefreshScreenOverMessageMessage(ItemStack.EMPTY, Component.translatable("gui.exmodifier.refine_fail"));
            }
            Exmodifier.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), message1);
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
