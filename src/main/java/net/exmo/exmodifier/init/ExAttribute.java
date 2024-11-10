package net.exmo.exmodifier.init;


import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.events.ExDodgeEvent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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

    // 属性注册对象
    public static final RegistryObject<Attribute> ARROW_BASE_DAMAGE;
    public static final RegistryObject<Attribute> DURABILITY;
    public static final RegistryObject<Attribute> DIG_SPEED;
    public static final RegistryObject<Attribute> DODGE;
    public static final RegistryObject<Attribute> MAX_INJURY_FREE;
    public static final RegistryObject<Attribute> MAX_DODGE;
    public static final RegistryObject<Attribute> HIT_RATE;
    public static final RegistryObject<Attribute> PERCENT_HEAL;
    public static final RegistryObject<Attribute> INJURY_FREE;
    public static final RegistryObject<Attribute> BEHIND_DAMAGE ;


    static {
        //背后伤害
        BEHIND_DAMAGE = registerAttribute("behind_damage", 1, -100000000, 100000000);

        // 弓箭基础伤害
        ARROW_BASE_DAMAGE = registerAttribute("arrow_base_damage", 0, 0, 100000000);

        // 耐久度
        DURABILITY = registerAttribute("durability", 1, 0, 100000000);

        // 挖掘速度
        DIG_SPEED = registerAttribute("dig_speed", 1, 0, 100000000);

        // 闪避
        DODGE = registerAttribute("dodge", 1, 0, 10000000);

        // 最大免伤
        MAX_INJURY_FREE = registerAttribute("max_injury_free", 1.85, 0, 10000000);

        // 最大闪避
        MAX_DODGE = registerAttribute("max_dodge", 1.85, 0, 10000000);

        // 命中率
        HIT_RATE = registerAttribute("hit_rate", 1, 0, 10000000);

        // 击中时自己百分比生命恢复
        PERCENT_HEAL = registerAttribute("percent_heal", 1, 0, 10000000);

        // 免伤
        INJURY_FREE = registerAttribute("injury_free", 1, -100000, 10000000);
    }


    private static RegistryObject<Attribute> registerAttribute(String name, double defaultValue, double minValue, double maxValue) {
        return ATTRIBUTES.register(name, () -> new RangedAttribute("attribute." + Exmodifier.MODID + "." + name, defaultValue, minValue, maxValue).setSyncable(true));
    }
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
            event.add(e, DIG_SPEED.get());
            event.add(e, INJURY_FREE.get());
            event.add(e, HIT_RATE.get());
            event.add(e, PERCENT_HEAL.get());
            event.add(e, BEHIND_DAMAGE.get());
            event.add(e, ARROW_BASE_DAMAGE.get());
            event.add(e, DURABILITY.get());
            if (e.equals(EntityType.PLAYER)) {
                event.add(e, MAX_DODGE.get());
                event.add(e, MAX_INJURY_FREE.get());
            }
        });


    }

    @Mod.EventBusSubscriber
    public static class Utils {
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
        public static boolean isLookingBehindTarget(LivingEntity target, Vec3 attackerLocation) {
            if (attackerLocation != null) {
                Vec3 lookingVector = target.getViewVector(1.0F);
                Vec3 attackAngleVector = attackerLocation.subtract(target.position()).normalize();
                attackAngleVector = new Vec3(attackAngleVector.x, 0.0D, attackAngleVector.z);
                return attackAngleVector.dot(lookingVector) < -0.5D;
            }
            return false;
        }

        public static float getBackstabbingDamagePerLevel(float amount, int level) {
            float multiplier = ((level * 0.2F) + 1.2F);
            return amount * multiplier;
        }
        @SubscribeEvent
        public static void AtAttack(LivingAttackEvent event) {

            LivingEntity entity = event.getEntity();
            if (!(event.getSource().getEntity() instanceof LivingEntity souree)) return;
            if (event.getSource().is(DamageTypes.MAGIC) || event.getSource().is(DamageTypes.INDIRECT_MAGIC) || event.getSource().is(DamageTypes.LAVA) )return;
            if (entity.getAttributes().hasAttribute(ExAttribute.DODGE.get())) {
                double remove_value = 0;
                if (souree.getAttributes().hasAttribute(ExAttribute.HIT_RATE.get())) {
                    remove_value =  souree.getAttributeValue(ExAttribute.HIT_RATE.get());
                }
                double v = entity.getAttributeValue(ExAttribute.DODGE.get()) - remove_value;
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
            LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity ? (LivingEntity) event.getSource().getEntity() : null;
            if (event.getSource().getEntity() instanceof LivingEntity) {
                if( isLookingBehindTarget(event.getEntity(), event.getSource().getSourcePosition())) {
                    if (attacker!=null){
                    if (attacker.getAttributes().hasAttribute(ExAttribute.BEHIND_DAMAGE.get())) {
                        double multiplier = attacker.getAttributeValue(ExAttribute.BEHIND_DAMAGE.get());
                        event.setAmount((float) (multiplier * event.getAmount()));
                        Level level = attacker.level();
                        if (!level.isClientSide)
                            level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                    }
                }
            }
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
            newP.getAttribute(BEHIND_DAMAGE.get()).setBaseValue(oldP.getAttribute(BEHIND_DAMAGE.get()).getBaseValue());
            newP.getAttribute(PERCENT_HEAL.get()).setBaseValue(oldP.getAttribute(PERCENT_HEAL.get()).getBaseValue());
            newP.getAttribute(HIT_RATE.get()).setBaseValue(oldP.getAttribute(HIT_RATE.get()).getBaseValue());
        }
    }
}

