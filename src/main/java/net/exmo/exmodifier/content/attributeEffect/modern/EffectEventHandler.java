package net.exmo.exmodifier.content.attributeEffect.modern;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.util.gether.AttrGether;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
@Mod.EventBusSubscriber
public class EffectEventHandler {
    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide) {
            entity.getCapability(CapabilityRegistration.CUSTOM_EFFECTS_CAP).ifPresent(cap -> {
                List<CustomEffectInstance> toRemove = new ArrayList<>();
                for (CustomEffectInstance effect : cap.getEffects()) {
                    effect.tick(entity);
                    if (effect.isExpired()) {
                        effect.removeAttributes(entity);
                        toRemove.add(effect);
                        Exmodifier.PACKET_HANDLER.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                                new EffectSyncPacket(entity.getId(), serializeNBT(effect), false));
                    } else if (effect.needsSync()) {
                        Exmodifier.PACKET_HANDLER.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                                new EffectSyncPacket(entity.getId(), serializeNBT(effect), true));
                        effect.setNeedsSync(false);
                    }
                }
                toRemove.forEach(e -> cap.removeEffect(e.getEffectId()));
            });
        }
    }

    public static CompoundTag serializeNBT(CustomEffectInstance effect) {
        CompoundTag tag = new CompoundTag();
        ListTag effectsList = new ListTag();
            CompoundTag effectTag = new CompoundTag();
            effectTag.putString("Id", effect.getEffectId().toString());
            effectTag.putInt("Duration", effect.getDuration());
            effectTag.putInt("Amplifier", effect.getAmplifier());
            effectTag.putString("Icon", effect.getIcon().toString());
            if (!effect.getAttributes().isEmpty()) {
                ListTag specialTagsArray = new ListTag();
                for (var ts : effect.getAttributes()) {
                    specialTagsArray.add((AttrGether.exSerialize.toNbt(ts)));
                }
                effectTag.put("Attributes", specialTagsArray);
            }
            effectsList.add(effectTag);
        tag.put("Effects", effectsList);
        return tag;

}
}