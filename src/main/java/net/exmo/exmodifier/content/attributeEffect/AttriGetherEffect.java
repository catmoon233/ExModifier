package net.exmo.exmodifier.content.attributeEffect;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.util.AttriGether;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class AttriGetherEffect
{
    private BossEvent.BossBarColor bossBarColor;
    private boolean randomAttrUUID;
    private BossEvent.BossBarOverlay bossBarOverlay;
    private String bossBarName;
    private ResourceLocation id;
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
    public AttriGetherEffect(BossEvent.BossBarColor bossBarColor,BossEvent.BossBarOverlay bossBarOverlay,String bossBarName,boolean visible,ResourceLocation id ,List<AttriGether> attriGethers) {
        this.bossBarColor = bossBarColor;
        this.bossBarOverlay = bossBarOverlay;
        this.id = id;
        this.bossBarName = bossBarName;
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


    public String getBossBarName() {
        return bossBarName;
    }

    public void setBossBarName(String bossBarName) {
        this.bossBarName = bossBarName;
    }

    public ResourceLocation getId() {
        return id;
    }

    public void setId(ResourceLocation id) {
        this.id = id;
    }

    public SimpleParticleType getParticle() {
        return particle;
    }

    public void setParticle(SimpleParticleType particle) {
        this.particle = particle;
    }

    public boolean isRandomAttrUUID() {
        return randomAttrUUID;
    }

    public AttriGetherEffect setRandomAttrUUID(boolean randomAttrUUID) {
        this.randomAttrUUID = randomAttrUUID;
    return this;
    }
}