package net.exmo.exmodifier.util;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public record MobEffectInstantBuilder(MobEffect mobEffect, int duration, int amplifier, boolean ambient, boolean visible, boolean showIcon) {



    public MobEffectInstantBuilder(MobEffect mobEffect, int duration, int amplifier) {
        this(mobEffect, duration, amplifier, false, true, true);
    }

    public MobEffectInstantBuilder(MobEffect mobEffect, int duration, int amplifier, boolean ambient) {
        this(mobEffect, duration, amplifier, ambient, true, true);
    }

    public MobEffectInstantBuilder(MobEffect mobEffect, int duration, int amplifier, boolean ambient, boolean visible) {
        this(mobEffect, duration, amplifier, ambient, visible, true);
    }
}
