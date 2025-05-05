package net.exmo.exmodifier.content.attributeEffect.modern;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// CustomEffectsCapImpl.java - Capability 实现
public class CustomEffectsCapImpl implements ICustomEffectsCap {
    private final List<CustomEffectInstance> effects = new ArrayList<>();

    @Override
    public List<CustomEffectInstance> getEffects() { return new ArrayList<>(effects); }

    @Override
    public void addEffect(CustomEffectInstance effect) {
        Optional<CustomEffectInstance> existing = getEffect(effect.getEffectId());
        existing.ifPresent(this.effects::remove);
        this.effects.add(effect);
    }

    @Override
    public void removeEffect(ResourceLocation id) {
        effects.removeIf(e -> e.getEffectId().equals(id));
    }

    @Override
    public Optional<CustomEffectInstance> getEffect(ResourceLocation id) {
        return effects.stream().filter(e -> e.getEffectId().equals(id)).findFirst();
    }
}
