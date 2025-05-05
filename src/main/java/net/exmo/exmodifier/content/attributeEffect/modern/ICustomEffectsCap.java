package net.exmo.exmodifier.content.attributeEffect.modern;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface ICustomEffectsCap {
    List<CustomEffectInstance> getEffects();
    void addEffect(CustomEffectInstance effect);
    void removeEffect(ResourceLocation id);
    Optional<CustomEffectInstance> getEffect(ResourceLocation id);
}

