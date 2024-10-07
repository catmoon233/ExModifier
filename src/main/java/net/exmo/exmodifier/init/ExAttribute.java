package net.exmo.exmodifier.init;


import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.events.ExDodgeEvent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
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
    public static final RegistryObject<Attribute> ARROWBASEDAMAGE = ATTRIBUTES.register("arrow_base_damage", () -> (new RangedAttribute("attribute." + Exmodifier.MODID + ".arrow_base_damage", 0, 0, 100000000)).setSyncable(true));
    public static final RegistryObject<Attribute> DODGE = ATTRIBUTES.register("dodge", () -> (new RangedAttribute("attribute." + Exmodifier.MODID + ".dodge", 1, 0, 10000000)).setSyncable(true));
    public static final RegistryObject<Attribute> MAX_DODGE = ATTRIBUTES.register("max_dodge", () -> (new RangedAttribute("attribute." + Exmodifier.MODID + ".max_dodge", 1.85, 0, 10000000)).setSyncable(true));
    public static final RegistryObject<Attribute> HIT_RATE = ATTRIBUTES.register("hit_rate", () -> (new RangedAttribute("attribute." + Exmodifier.MODID + ".hit_rate", 1, 0, 10000000)).setSyncable(true));
    public static final RegistryObject<Attribute> PERCENT_HEAL = ATTRIBUTES.register("percent_heal", () -> (new RangedAttribute("attribute." + Exmodifier.MODID + ".percent_heal", 1, 0, 10000000)).setSyncable(true));
    public static final RegistryObject<Attribute> INJURY_FREE = ATTRIBUTES.register("injury_free", () -> (new RangedAttribute("attribute." + Exmodifier.MODID + ".injury_free", 1, -100000, 10000000)).setSyncable(true));
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
            event.add(e, DODGE.get());
            event.add(e, INJURY_FREE.get());
            event.add(e, HIT_RATE.get());
            event.add(e, PERCENT_HEAL.get());
            event.add(e, ARROWBASEDAMAGE.get());
            if (e.equals(EntityType.PLAYER)) event.add(e, MAX_DODGE.get());

        });


    }

    @Mod.EventBusSubscriber
    private class Utils {
        public static 	void particle(Entity entity){
            if (entity.level instanceof ServerLevel _level)
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
        public static void AtAttack(LivingAttackEvent event) {

            LivingEntity entity = (LivingEntity) event.getEntity();
            if (!(event.getSource().getEntity() instanceof LivingEntity souree)) return;
            if (event.getSource().isMagic() )return;
            if (entity.getAttributes().hasAttribute(ExAttribute.DODGE.get())) {
                double remove_value = 0;
                double max_Dodge = 99999999;
                if (entity.getAttributes().hasAttribute(ExAttribute.MAX_DODGE.get())) {
                    max_Dodge =  entity.getAttributeValue( ExAttribute.MAX_DODGE.get());
                }
                if (souree.getAttributes().hasAttribute(ExAttribute.HIT_RATE.get())) {
                    remove_value =  souree.getAttributeValue(ExAttribute.HIT_RATE.get());
                }
                double v = Math.min(entity.getAttributeValue( ExAttribute.DODGE.get()),max_Dodge) - remove_value;
                if (Math.random() <= v) {
                    particle(entity);
                    move(entity);
                    ExDodgeEvent e  = new ExDodgeEvent(entity,event,entity.getAttributeValue(ExAttribute.DODGE.get()),remove_value, ExDodgeEvent.resultType.MISS);
                    MinecraftForge.EVENT_BUS.post(e);
                    if (e.result== ExDodgeEvent.resultType.MISS) event.setCanceled(true);
                }else {
                    if (v < 0) {
                        ExDodgeEvent e  = new ExDodgeEvent(entity,event,entity.getAttributeValue(ExAttribute.DODGE.get()),remove_value, ExDodgeEvent.resultType.HIT);
                        MinecraftForge.EVENT_BUS.post(e);
                        if (e.result== ExDodgeEvent.resultType.MISS) event.setCanceled(true);
                        else entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, (int)(v*2.5*-1), false, false));
                    }
                }
            }
        }
        @SubscribeEvent
        public static void AtHurt(LivingHurtEvent event) {
            LivingEntity entity = ((LivingEntity) event.getEntity());
            if (entity.getAttributes().hasAttribute(ExAttribute.INJURY_FREE.get())){
                double v = entity.getAttributeValue(ExAttribute.INJURY_FREE.get());
                event.setAmount((float) ((2- v) * (event.getAmount())));
            }
            if ((event.getSource().getEntity() instanceof LivingEntity entity1)) {
                if (entity1.getAttributes().hasAttribute(ExAttribute.PERCENT_HEAL.get()) && entity1.getAttributes().hasAttribute(Attributes.MAX_HEALTH)) {
                    double v = entity1.getAttributeValue(ExAttribute.PERCENT_HEAL.get());
                    entity1.heal((float) (entity1.getAttributeValue(Attributes.MAX_HEALTH) * (v - 1)));
                }
            }
        }
        @SubscribeEvent
        public static void persistAttributes(PlayerEvent.Clone event) {
            Player oldP = event.getOriginal();
            Player newP = (Player) event.getEntity();
            newP.getAttribute(DODGE.get()).setBaseValue(oldP.getAttribute(DODGE.get()).getBaseValue());
            newP.getAttribute(PERCENT_HEAL.get()).setBaseValue(oldP.getAttribute(PERCENT_HEAL.get()).getBaseValue());
            newP.getAttribute(HIT_RATE.get()).setBaseValue(oldP.getAttribute(HIT_RATE.get()).getBaseValue());
        }
    }
}

