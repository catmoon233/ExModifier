package net.exmo.exmodifier.init;


import net.exmo.exmodifier.Exmodifier;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ExAttribute {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, Exmodifier.MODID);
    public static final RegistryObject<Attribute> DODGE = ATTRIBUTES.register("dodge", () -> (new RangedAttribute("attribute." + Exmodifier.MODID + ".dodge", 0, 0, 10000000)).setSyncable(true));
    public static final RegistryObject<Attribute> HIT_RATE = ATTRIBUTES.register("hit_rate", () -> (new RangedAttribute("attribute." + Exmodifier.MODID + ".hit_rate", 0, 0, 10000000)).setSyncable(true));

    @SubscribeEvent
    public static void register(FMLConstructModEvent event) {
        event.enqueueWork(() -> {
            ATTRIBUTES.register(FMLJavaModLoadingContext.get().getModEventBus());
        });
    }

    @SubscribeEvent
    public static void addAttributes(EntityAttributeModificationEvent event) {
        List<EntityType<? extends LivingEntity>> entityTypes = event.getTypes();

        entityTypes.forEach((e) -> {
            Class<? extends Entity> baseClass = e.getBaseClass();
            if (baseClass.isAssignableFrom(LivingEntity.class)) {
                event.add(e, DODGE.get());
                event.add(e, HIT_RATE.get());
            }
        });


    }

    @Mod.EventBusSubscriber
    private class Utils {
        public static 	void particle(Entity entity){
            if (entity.level() instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.CLOUD,entity.getX(), entity.getY()+entity.getBbHeight()*0.5, entity.getZ(), 5, 0.2, 0.2, 0.2, 0.02 );
        }
        public static void move(Entity entity){
            Random random = new Random();
            double a =-1;
            if (Math.random() <0.5)
                a=1;
            entity.setDeltaMovement(new Vec3((Math.cos(Math.toRadians(entity.getYRot())) * 2) *a, 0, (Math.sin(Math.toRadians(entity.getYRot())))*a));

        }
        @SubscribeEvent
        public static void AtAttack(LivingHurtEvent event) {
            LivingEntity entity = event.getEntity();
            if (!(event.getSource().getEntity() instanceof LivingEntity souree)) return;
            if (entity.getAttributes().hasAttribute(ExAttribute.DODGE.get())) {
                int remove_value = 0;
                if (souree.getAttributes().hasAttribute(ExAttribute.HIT_RATE.get())) {
                    remove_value = (int) souree.getAttributeValue(ExAttribute.HIT_RATE.get());
                }
                double v = entity.getAttributeValue(ExAttribute.DODGE.get()) - remove_value;
                if (Math.random() <= v) {
                    particle(entity);
                    move(entity);
                    event.setCanceled(true);
                    return;
                }else {
                    if (v < 0) {
                        entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20, (int)(v*5), false, false));
                    }
                }
            }
        }
        @SubscribeEvent
        public static void persistAttributes(PlayerEvent.Clone event) {
            Player oldP = event.getOriginal();
            Player newP = (Player) event.getEntity();
            newP.getAttribute(DODGE.get()).setBaseValue(oldP.getAttribute(DODGE.get()).getBaseValue());
        }
    }
}

