package net.exmo.exmodifier.content.MobEffect;

import net.exmo.exmodifier.init.ExAttribute;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class DodgeEffect extends net.minecraft.world.effect.MobEffect{
    protected final double multiplier;
    protected final double MaxLevel;
    private static final String DODGE_UUID =("e7b6b8b2-c9a3-4f02-b6b9-e7b6b8b2c9a3");
    public DodgeEffect() {
        super(MobEffectCategory.BENEFICIAL, -26368);
        addAttributeModifier(ExAttribute.DODGE.get(), DODGE_UUID,  0, AttributeModifier.Operation.MULTIPLY_BASE);
        this.multiplier = 0.02;
        this.MaxLevel = 3;
    }

    @Override
    public double getAttributeModifierValue(int p_19430_, AttributeModifier p_19431_) {
        return Math.min(p_19430_, MaxLevel) * multiplier;
    }

    @Override
    public String getDescriptionId() {
        return "effect.exmodifier.dodge";
    }

}