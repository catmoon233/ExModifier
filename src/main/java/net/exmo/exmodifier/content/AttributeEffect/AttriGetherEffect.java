package net.exmo.exmodifier.content.AttributeEffect;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.util.AttriGether;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class AttriGetherEffect
{
    private BossEvent.BossBarColor bossBarColor;
    private BossEvent.BossBarOverlay bossBarOverlay;
    private SimpleParticleType particle;
    public static SimpleParticleType StringToParticle(String particle)
    {
        for (ResourceLocation particleId : ForgeRegistries.PARTICLE_TYPES.getKeys()){
            if (particleId.toString().equals(particle))
                return (SimpleParticleType) ForgeRegistries.PARTICLE_TYPES.getValue(particleId);
        }
        return null;
    }
    public void setLocalDescription(String localDescription) {
        this.localDescription = localDescription;
    }

    private String localDescription;
    private boolean visible;
    private List<AttriGether> attriGethers;
    public AttriGetherEffect(BossEvent.BossBarColor bossBarColor,BossEvent.BossBarOverlay bossBarOverlay,boolean visible,String localDescription ,List<AttriGether> attriGethers) {
        this.bossBarColor = bossBarColor;
        this.bossBarOverlay = bossBarOverlay;
        this.localDescription = localDescription;
        this.visible = visible;
        this.attriGethers = attriGethers;
        if (attriGethers.isEmpty())
            Exmodifier.LOGGER.Logger.error(("AttriGetherEffect must have at least one AttriGether"));
    }

    public BossEvent.BossBarColor getBossBarColor() {
        return bossBarColor;
    }

    public void setBossBarColor(BossEvent.BossBarColor bossBarColor) {
        this.bossBarColor = bossBarColor;
    }

    public BossEvent.BossBarOverlay getBossBarOverlay() {
        return bossBarOverlay;
    }

    public void setBossBarOverlay(BossEvent.BossBarOverlay bossBarOverlay) {
        this.bossBarOverlay = bossBarOverlay;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public List<AttriGether> getAttriGethers() {
        return attriGethers;
    }

    public void setAttriGethers(List<AttriGether> attriGethers) {
        this.attriGethers = attriGethers;
    }

    public String getLocalDescription() {
        return localDescription;
    }


}