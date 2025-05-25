package net.exmo.exmodifier.content.element;

import net.exmo.exmodifier.util.exSerialize.ExSerClass;
import net.exmo.exmodifier.util.exSerialize.ExSerialize;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class ExElement implements ExSerClass {
    private ResourceLocation id;
    private String CustomLocation ="";

    public float getParticleScale() {
        return ParticleScale;
    }

    public ExElement setParticleScale(float particleScale) {
        ParticleScale = particleScale;
        return this;
    }

    private float ParticleScale = 0.2f;
    private ParticleType<?> simpleParticleType;
    public static final ExSerialize<ExElement> EX_SERIALIZE = ExSerialize.create(()-> new ExElement(new ResourceLocation("exmodifier:null")))
            .withAutoId((e)->e.getResId().toString(),(e, id) -> e.setId(new ResourceLocation(id)))
            .addResourceLocationField("id", ExElement::getResId,ExElement::setId)
            .addResourceLocationField("icon", ExElement::getIconTexture,ExElement::setIconTexture)
            .addIntField("color", ExElement::getColor,ExElement::setColor)
            .addStringField("custom_loc", ExElement::getCustomLocation,ExElement::setCustomLocation)
            .addFloatField("particle_scale",  ExElement::getParticleScale,ExElement::setParticleScale)
            .addStringField("particle_id",exElement -> exElement.getSimpleParticleType().toString(),(exElement, s) -> {
                ParticleType<?> value = ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation(s));
                if (value !=null) {
                    exElement.setSimpleParticleType(value);
                }
            })
            .addFloatMapField("restrain", ExElement::getRestrain,ExElement::setRestrain);



    public ExElement(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation getIconTexture() {
        return iconTexture;
    }

    public ExElement setIconTexture(ResourceLocation iconTexture) {
        this.iconTexture = iconTexture;
        return this;
    }


    public int getColor() {
        return color;
    }

    public ExElement setColor(int color) {
        this.color = color;
        return this;
    }

    private ResourceLocation iconTexture;
    private int color =0;
    private Map<String,Float> Restrain = new HashMap<>();

    public Map<String, Float> getRestrain() {
        return Restrain;
    }

    public ExElement setRestrain(Map<String, Float> restrain) {
        Restrain = restrain;
        return this;
    }

    public String getCustomLocation() {
        return CustomLocation;
    }

    public ExElement setCustomLocation(String customLocation) {
        CustomLocation = customLocation;
        return this;
    }

    public ResourceLocation getResId() {
        return id;
    }

    @Override
    public ExSerialize getExSerialize() {
        return EX_SERIALIZE;
    }

    public ExElement setId(ResourceLocation id) {
        this.id = id;
        return this;
    }


    public ParticleType<?> getSimpleParticleType() {
        return simpleParticleType;
    }

    public ExElement setSimpleParticleType(ParticleType<?> simpleParticleType) {
        this.simpleParticleType = simpleParticleType;
        return this;
    }
}
