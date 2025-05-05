package net.exmo.exmodifier.content.attributeEffect.modern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EffectSyncPacket {
    private int entityId;
    private CompoundTag effectData;
    private boolean add;

    public EffectSyncPacket(FriendlyByteBuf buf) {
        entityId = buf.readInt();
        effectData = buf.readNbt();
        add = buf.readBoolean();
    }

    public EffectSyncPacket(int entityId, CompoundTag effectData, boolean add) {
        this.entityId = entityId;
        this.effectData = effectData;
        this.add = add;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeNbt(effectData);
        buf.writeBoolean(add);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null) {
                Entity entity = level.getEntity(entityId);
                if (entity instanceof LivingEntity living) {
                    living.getCapability(CapabilityRegistration.CUSTOM_EFFECTS_CAP).ifPresent(cap -> {
                        if (add) {
                            // 反序列化effectData并添加
                        } else {
                            cap.removeEffect(new ResourceLocation(effectData.getString("Id")));
                        }
                    });
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
