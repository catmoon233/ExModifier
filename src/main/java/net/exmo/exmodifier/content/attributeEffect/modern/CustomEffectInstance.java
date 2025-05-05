package net.exmo.exmodifier.content.attributeEffect.modern;

import net.exmo.exmodifier.util.gether.AttrGether;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

import java.util.List;

public class CustomEffectInstance {
    private final ResourceLocation effectId;
    private int duration;

    public CustomEffectInstance setAmplifier(int amplifier) {
        this.amplifier = amplifier;
        return this;
    }

    private  int amplifier;
    private final ResourceLocation icon;

    public List<AttrGether> getAttributes() {
        return attributes;
    }

    public CustomEffectInstance setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public boolean isNeedsSync() {
        return needsSync;
    }

    private final List<AttrGether> attributes;
    private boolean needsSync = true;

    public CustomEffectInstance(ResourceLocation id, int duration, int amplifier, ResourceLocation icon, List<AttrGether> attributes) {
        this.effectId = id;
        this.duration = duration;
        this.amplifier = amplifier;
        this.icon = icon;
        this.attributes = attributes;
    }

    public void tick(LivingEntity entity) {
        if (duration > 0) duration--;
    }

    public boolean isExpired() {
        return duration <= 0;
    }

    // 应用属性到实体
    public void applyAttributes(LivingEntity entity) {
        attributes.forEach(ag -> {
            AttributeInstance attr = entity.getAttribute(ag.attribute);
            if (attr != null) {
                attr.addTransientModifier(ag.attributeModifier);
            }
        });
    }

    // 移除属性
    public void removeAttributes(LivingEntity entity) {
        attributes.forEach(ag -> {
            AttributeInstance attr = entity.getAttribute(ag.attribute);
            if (attr != null) {
                attr.removeModifier(ag.attributeModifier.getId());
            }
        });
    }

    // Getters
    public ResourceLocation getEffectId() { return effectId; }
    public int getDuration() { return duration; }
    public int getAmplifier() { return amplifier; }
    public ResourceLocation getIcon() { return icon; }
    public boolean needsSync() { return needsSync; }
    public void setNeedsSync(boolean needsSync) { this.needsSync = needsSync; }
}