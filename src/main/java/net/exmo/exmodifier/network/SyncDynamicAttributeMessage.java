package net.exmo.exmodifier.network;

import net.exmo.exmodifier.content.dynamicAttributes.DynamicAttribute;
import net.exmo.exmodifier.content.dynamicAttributes.DynamicAttributeRegister;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public record SyncDynamicAttributeMessage(Map<String, DynamicAttribute> map) {
    public static void encode(SyncDynamicAttributeMessage msg, FriendlyByteBuf buffer) {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<String, DynamicAttribute> entry : msg.map.entrySet()) {
            tag.put(entry.getKey(), DynamicAttribute.SERIALIZER.toNbt(entry.getValue()));
        }
        buffer.writeNbt(tag);
    }

    public static SyncDynamicAttributeMessage decode(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt();
        Map<String, DynamicAttribute> map = new HashMap<>();
        if (tag != null) {
            for (String key : tag.getAllKeys()) {
                DynamicAttribute dynamicAttribute = DynamicAttribute.SERIALIZER.fromNbt(tag.getCompound(key));
                map.put(key, dynamicAttribute);
            }
        }
        return new SyncDynamicAttributeMessage(map);
    }

    public static void handle(SyncDynamicAttributeMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DynamicAttributeRegister.dynamicAttributes  = msg.map;
        });
        ctx.get().setPacketHandled(true);
    }
}
