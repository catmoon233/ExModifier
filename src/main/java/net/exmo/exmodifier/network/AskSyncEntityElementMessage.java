package net.exmo.exmodifier.network;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.element.ExElementInstant;
import net.exmo.exmodifier.content.helper.entity.ExElementEntityHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public record AskSyncEntityElementMessage(UUID uuid) {
    public static void encode(AskSyncEntityElementMessage msg, FriendlyByteBuf buffer) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("UUID",msg.uuid.toString());
        buffer.writeNbt(compoundTag);
    }

    public static AskSyncEntityElementMessage decode(FriendlyByteBuf buffer) {
        return new AskSyncEntityElementMessage(
                UUID.fromString(buffer.readNbt().getString("UUID"))
        );
    }

    public static void handle(AskSyncEntityElementMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(
                () -> {
                    ServerPlayer sender = ctx.get().getSender();
                    AtomicReference<ExElementInstant> exElementInstant = new AtomicReference<>();
                    sender.level().
                            getEntities(sender, new AABB(sender.getX()-50,sender.getY()-50,sender.getZ()-50,sender.getX()+50,sender.getY()+50,sender.getZ()+50)).stream().filter(
                                    entity ->entity.getUUID().equals(msg.uuid)
                            ).findFirst().ifPresent(entity -> {
                                exElementInstant.set(ExElementEntityHelper.of(((LivingEntity) entity)).getExElementInstants().get(0));

                            });
                    if (exElementInstant.get() == null) return;
                    Exmodifier.PACKET_HANDLER.send(
                            PacketDistributor.PLAYER.with(() -> sender),new SyncEntityElementMessage(msg.uuid,exElementInstant.get()
                                    )
                    );
                }
        );
    }
}
