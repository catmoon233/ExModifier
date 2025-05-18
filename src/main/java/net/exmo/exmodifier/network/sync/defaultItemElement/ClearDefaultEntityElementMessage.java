
package net.exmo.exmodifier.network.sync.defaultItemElement;

import net.exmo.exmodifier.content.element.ExElementHandle;
import net.exmo.exmodifier.network.sync.AbstractClearElementMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public class ClearDefaultEntityElementMessage extends AbstractClearElementMessage<Object> {

    public static void encode(ClearDefaultEntityElementMessage msg, FriendlyByteBuf buffer) {

    }

    public static ClearDefaultEntityElementMessage decode(FriendlyByteBuf buffer) {
            return new ClearDefaultEntityElementMessage();
    }

    public static void handle(ClearDefaultEntityElementMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(ExElementHandle.elementDefaultMap2::clear);
        ctx.get().setPacketHandled(true);
    }

    protected Map<?, ?> getMap() {
        return ExElementHandle.elementDefaultMap2;
    }
}