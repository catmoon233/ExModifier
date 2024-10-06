package net.exmo.exmodifier.content.event.parameter;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.level.ItemLevelHandle;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
@Mod.EventBusSubscriber
public class EventCI<T extends LivingEvent> {
    public EventC<T> eventC;
    public List<EventParameter<?>> lvs= new java.util.ArrayList<>();

//    public EventCI(EventC<T> eventC,T e) {
//        this.eventC = eventC;
//    }

    public EventCI(EventC<T> v) {
        this.eventC =  v;
    }


    public void AddXp(LivingEvent event1) {
        if (true)return;
        // Exmodifier.LOGGER.debug("Adding Xp for " + event1);
        if (!event1.getClass().equals(this.eventC.clazz)) return;
        if (!this.eventC.clazz.isAssignableFrom(event1.getClass())) return; // 允许子类

        this.eventC.arg0 = eventC.clazz.cast(event1);
        try {
            this.eventC.setParameters();
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        LivingEntity entity = event1.getEntity();
        if (eventC.eventParameters.size() > 1) {
            //  if ( eventC.eventParameters.get(1).getValue() instanceof DamageSource) {
            entity = (LivingEntity) ((DamageSource) eventC.eventParameters.get(1).getValue()).getEntity();
            if (entity != null) {
                Exmodifier.LOGGER.debug("Adding Xp for22 " + entity);
            } else Exmodifier.LOGGER.debug("Adding Xp for22 null");
            //  }
        } else {
            // this.eventC.eventParameters.get(0).name = "hurtamout";
        }
        //   Class<?> livingEntityClass = this.eventC.getLivingEntityClass();
//        if (livingEntityClass.isInstance(entity)) {
        //  if (entity != null && entity.getClass() == eventC.getLivingEntityClass()) {
        if (entity instanceof Player player) {
            String name = eventC.clazz.getName();
            ItemLevelHandle.ItemAddXpAuto(player, this.eventC.eventParameters, eventC.clazz.cast(event1), name);
            Exmodifier.LOGGER.debug(name + " " + "Adding Xp for11 " + this.eventC.eventParameters.get(0).getValue());
            // }
            //  }
        }
    }
    @SubscribeEvent
    public static void AddXp1(LivingHurtEvent event1) {
        if (event1.getEntity() instanceof  Player player){
            if (!event1.getSource().is(DamageTypes.GENERIC_KILL)) {
                List<EventParameter<?>> eventParameters = new java.util.ArrayList<>();
                eventParameters.add(new EventParameter<>("amount", event1.getAmount()));
                addx(player, eventParameters, event1, "net.minecraftforge.event.entity.living.LivingHurtEvent");
            }
        }
        if (event1.getSource().getEntity() instanceof  Player player){
            List<EventParameter<?>> eventParameters = new java.util.ArrayList<>();
            eventParameters.add(new EventParameter<>("amount", event1.getAmount()));
            addx(player,eventParameters,event1,"net.minecraftforge.event.entity.living.LivingHurtEvent");
        }
//        // Exmodifier.LOGGER.debug("Adding Xp for " + event1);
//        if (!event1.getClass().equals(this.eventC.clazz)) return;
//        if (!this.eventC.clazz.isAssignableFrom(event1.getClass())) return; // 允许子类
//
//        this.eventC.arg0 = eventC.clazz.cast(event1);
//        try {
//            this.eventC.setParameters();
//        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
//            throw new RuntimeException(e);
//        }
//        LivingEntity entity = event1.getEntity();
//        if (eventC.eventParameters.size() > 1) {
//            entity = (LivingEntity) ((DamageSource) eventC.eventParameters.get(1).getValue()).getEntity();
//            if (entity != null) {
//                Exmodifier.LOGGER.debug("Adding Xp for22 " + entity);
//            } else Exmodifier.LOGGER.debug("Adding Xp for22 null");
//            //  }
//        } else {
//        }
//        if (entity instanceof Player player) {
//            Exmodifier.LOGGER.debug(name + " " + "Adding Xp for11 " + this.eventC.eventParameters.get(0).getValue());
//            // }
//            //  }
//        }
    }
    public static void addx(Player player,List<EventParameter<?>> eventParameters,LivingEvent event1,String name){
        ItemLevelHandle.ItemAddXpAuto(player, eventParameters, event1, name);

    }
}