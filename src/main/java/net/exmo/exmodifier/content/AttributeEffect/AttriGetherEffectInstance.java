package net.exmo.exmodifier.content.AttributeEffect;

import net.minecraft.world.entity.LivingEntity;

public class AttriGetherEffectInstance {
    public int duration;
    public AttriGetherEffect attriGetherEffect;
    public int amplifier;
    public boolean showParticle;
    public boolean showBossBar;

    public AttriGetherEffectInstance(int duration, AttriGetherEffect attriGetherEffect, int amplifier, boolean showParticle, boolean showBossBar) {
        this.duration = duration;
        this.attriGetherEffect = attriGetherEffect;
        this.amplifier = amplifier;
        this.showParticle = showParticle;
        this.showBossBar = showBossBar;

    }
    public AttriGetherEffectInstance(int duration, boolean showParticle, int amplifier, AttriGetherEffect attriGetherEffect) {
        this.duration = duration;
        this.showParticle = showParticle;
        this.amplifier = amplifier;
        this.attriGetherEffect = attriGetherEffect;
        this.showBossBar = this.attriGetherEffect.isVisible();
    }
    public String getDescriptionId() {
        return this.attriGetherEffect.getLocalDescription();
    }
    public String toString() {
        String s;
        if (this.amplifier > 0) {
            s = this.getDescriptionId() + " x " + (this.amplifier + 1) + ", Duration: " + this.describeDuration();
        } else {
            s = this.getDescriptionId() + ", Duration: " + this.describeDuration();
        }

        if (!this.showBossBar) {
            s = s + ", Particles: false";
        }

        if (!this.showParticle) {
            s = s + ", Show Particle: false";
        }

        return s;
    }

    private boolean hasRemainingDuration() {
        return this.isInfiniteDuration() || this.duration > 0;
    }

    private String describeDuration() {
        return this.isInfiniteDuration() ? "infinite" : Integer.toString(this.duration);
    }
    public boolean isInfiniteDuration() {
        return this.duration == -1;
    }

}