package net.exmo.exmodifier.content.MobEffect;

import net.exmo.exmodifier.init.ExAttribute;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class HitRateEffect extends net.minecraft.world.effect.MobEffect{
    protected final double multiplier;
    protected final double MaxLevel;
    private static final String HIT_RATE_UUID =("e7b6b8b2-c9a3-4f02-b6b9-e7b6b8b2c9a4");
    public HitRateEffect() {
        super(MobEffectCategory.BENEFICIAL, -26368);
        addAttributeModifier(ExAttribute.HIT_RATE.get(), HIT_RATE_UUID,  0, AttributeModifier.Operation.MULTIPLY_BASE);
        this.multiplier = 0.05;
        this.MaxLevel = 3;
    }
    @Override
    public double getAttributeModifierValue(int p_19430_, AttributeModifier p_19431_) {
        return Math.min(p_19430_, MaxLevel) * multiplier;
    }
    @Override
    public String getDescriptionId() {
        return "effect.exmodifier.hit_rate";
    }
}