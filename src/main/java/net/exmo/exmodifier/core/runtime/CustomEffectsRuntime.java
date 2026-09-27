package net.exmo.exmodifier.core.runtime;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.core.attachment.CustomEffectInstance;
import net.exmo.exmodifier.core.attachment.CustomEffects;
import net.exmo.exmodifier.core.attachment.ExModifierAttachments;
import net.exmo.exmodifier.core.data.AttriEffectDefinition;
import net.exmo.exmodifier.core.data.AttributeSpec;
import net.exmo.exmodifier.core.registry.ExModifierDatapack;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashMap;
import java.util.Map;

public final class CustomEffectsRuntime {
    private static volatile Map<ResourceLocation, AttriEffectDefinition> DEFINITIONS = Map.of();

    private CustomEffectsRuntime() {}

    public static void bind(RegistryAccess access) {
        HashMap<ResourceLocation, AttriEffectDefinition> map = new HashMap<>();
        access.registry(ExModifierDatapack.ATTR_EFFECTS).ifPresent(registry ->
                registry.holders().forEach(holder -> map.put(holder.key().location(), holder.value())));
        DEFINITIONS = Map.copyOf(map);
    }

    public static Map<ResourceLocation, AttriEffectDefinition> definitions() {
        return DEFINITIONS;
    }

    public static boolean add(LivingEntity entity, ResourceLocation id, int amplifier, int duration, boolean replace) {
        AttriEffectDefinition definition = DEFINITIONS.get(id);
        if (entity == null || definition == null) return false;
        CustomEffects current = entity.getData(ExModifierAttachments.CUSTOM_EFFECTS);
        if (!replace && current.find(id).isPresent()) return false;
        CustomEffectInstance previous = current.find(id).orElse(null);
        if (previous != null) strip(entity, previous);
        CustomEffectInstance next = new CustomEffectInstance(id, duration, amplifier, definition.attributes());
        entity.setData(ExModifierAttachments.CUSTOM_EFFECTS, current.with(next));
        apply(entity, next);
        return true;
    }

    @SubscribeEvent
    public static void tick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity living) || living.level().isClientSide()) return;
        CustomEffects current = living.getData(ExModifierAttachments.CUSTOM_EFFECTS);
        if (current.values().isEmpty()) return;
        CustomEffects next = CustomEffects.EMPTY;
        for (CustomEffectInstance instance : current.values()) {
            CustomEffectInstance ticked = instance.tick();
            if (ticked.expired()) {
                strip(living, instance);
            } else {
                next = next.with(ticked);
            }
        }
        if (next.values().size() != current.values().size()) {
            living.setData(ExModifierAttachments.CUSTOM_EFFECTS, next);
        } else {
            living.setData(ExModifierAttachments.CUSTOM_EFFECTS, next);
        }
    }

    private static void apply(LivingEntity entity, CustomEffectInstance instance) {
        int level = instance.amplifier() + 1;
        for (int i = 0; i < instance.attributes().size(); i++) {
            AttributeSpec spec = instance.attributes().get(i);
            Holder<Attribute> holder = BuiltInRegistries.ATTRIBUTE.getHolder(spec.attribute()).orElse(null);
            if (holder == null) continue;
            AttributeInstance attr = entity.getAttribute(holder);
            if (attr == null) continue;
            ResourceLocation id = modifierId(instance.id(), i);
            attr.removeModifier(id);
            attr.addTransientModifier(new AttributeModifier(id, spec.amountAt(level), spec.operation()));
        }
    }

    private static void strip(LivingEntity entity, CustomEffectInstance instance) {
        for (int i = 0; i < instance.attributes().size(); i++) {
            AttributeSpec spec = instance.attributes().get(i);
            Holder<Attribute> holder = BuiltInRegistries.ATTRIBUTE.getHolder(spec.attribute()).orElse(null);
            if (holder == null) continue;
            AttributeInstance attr = entity.getAttribute(holder);
            if (attr != null) attr.removeModifier(modifierId(instance.id(), i));
        }
    }

    private static ResourceLocation modifierId(ResourceLocation effect, int index) {
        return Exmodifier.id("ceffect/" + effect.getNamespace() + "/" + effect.getPath() + "/" + index);
    }
}
