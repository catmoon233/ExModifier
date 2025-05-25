package net.exmo.exmodifier.content.attributeEffect.modern;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import net.exmo.exmodifier.util.gether.AttrGether;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber
public class CapabilityAttachHandler {
//    @SubscribeEvent
//    public static void onSleep(PlayerSleepInBedEvent event){
//        // 创建属性修改器列表
//        List<AttrGether> attributes = List.of(
//                new AttrGether(
//                        Attributes.ATTACK_DAMAGE,
//                        new AttributeModifier(UUID.randomUUID(), "attack_boost", 5.0, AttributeModifier.Operation.MULTIPLY_BASE)
//                ));
//
//// 创建效果实例
//        CustomEffectInstance effect = new CustomEffectInstance(
//                new ResourceLocation("exmodifier", "strength_effect"),
//                200, // 10秒（假设每秒20tick）
//                1,
//                new ResourceLocation("exmodifier", "textures/gui/strength_icon.png"),
//                attributes
//        );
//
//// 应用到实体
//        CEEffectUtils.applyEffect(event.getEntity(), effect);
//    }
    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            event.addCapability(
                    new ResourceLocation("exmodifier", "custom_effects"),
                    new ICapabilitySerializable<CompoundTag>() {
                        final ICustomEffectsCap cap = new CustomEffectsCapImpl();

                        @Override
                        public CompoundTag serializeNBT() {
                            CompoundTag tag = new CompoundTag();
                            ListTag effectsList = new ListTag();
                            cap.getEffects().forEach(effect -> {
                                CompoundTag effectTag = new CompoundTag();
                                effectTag.putString("Id", effect.getEffectId().toString());
                                effectTag.putInt("Duration", effect.getDuration());
                                effectTag.putInt("Amplifier", effect.getAmplifier());
                                effectTag.putString("Icon", effect.getIcon().toString());
                                effectTag.putString("UUID", effect.getUuid().toString());
                                // 序列化属性...
                                if (!effect.getAttributes().isEmpty()) {
                                    ListTag specialTagsArray = new ListTag();
                                    for (var ts : effect.getAttributes()) {
                                        specialTagsArray.add((AttrGether.exSerialize.toNbt(ts)));
                                    }
                                    effectTag.put("Attributes", specialTagsArray);
                                }
                                effectsList.add(effectTag);
                            });
                            tag.put("Effects", effectsList);
                            return tag;
                        }

                        @Override
                        public void deserializeNBT(CompoundTag nbt) {
                            ListTag effectsList = nbt.getList("Effects", Tag.TAG_COMPOUND);
                            for (Tag tag : effectsList) {
                                CompoundTag effectTag = (CompoundTag) tag;
                                ResourceLocation id = new ResourceLocation(effectTag.getString("Id"));
                                UUID uuid = UUID.fromString(effectTag.getString("UUID"));
                                int duration = effectTag.getInt("Duration");
                                int amplifier = effectTag.getInt("Amplifier");
                                ResourceLocation icon = new ResourceLocation(effectTag.getString("Icon"));
                                ListTag specialTagsArray = effectTag.getList("Attributes", Tag.TAG_COMPOUND);
                                List<AttrGether> attrGet = specialTagsArray.stream().map(e -> AttrGether.exSerialize.fromNbt((CompoundTag) e)).toList();

                                cap.addEffect(new CustomEffectInstance(id, duration, amplifier, icon, attrGet,uuid));
                            }
                        }

                        @NotNull
                        @Override
                        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                            return CapabilityRegistration.CUSTOM_EFFECTS_CAP.orEmpty(cap, LazyOptional.of(() -> this.cap));
                        }
                    });
        }
    }
}
