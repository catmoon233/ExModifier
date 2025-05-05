package net.exmo.exmodifier.content.attributeEffect.modern;

import net.exmo.exmodifier.Exmodifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.PacketDistributor;



public class CEEffectUtils {
    public static void applyEffect(LivingEntity entity, CustomEffectInstance effect) {
        entity.getCapability(CapabilityRegistration.CUSTOM_EFFECTS_CAP).ifPresent(cap -> {
            if (cap.getEffects().stream().anyMatch(e -> e.getEffectId().equals(effect.getEffectId())))cap.removeEffect(effect.getEffectId());
            cap.addEffect(effect);
            effect.applyAttributes(entity);
            // 同步到客户端
            Exmodifier.PACKET_HANDLER.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                    new EffectSyncPacket(entity.getId(), EffectEventHandler.serializeNBT(effect), true));
        });
    }
}
