package net.exmo.exmodifier.content.event.parameter;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.level.ItemLevelHandle;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

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
        // Exmodifier.LOGGER.debug("Adding Xp for " + event1);
        if (!event1.getClass().equals(this.eventC.clazz)) return;
        if (!this.eventC.clazz.isAssignableFrom(event1.getClass())) return; // 允许子类

        this.eventC.arg0 = eventC.clazz.cast(event1);
        try {
            this.eventC.setParameters();
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        var  entity = event1.getEntity();
        if (eventC.eventParameters.size()>1) {
          //  if ( eventC.eventParameters.get(1).getValue() instanceof DamageSource) {
                entity = (LivingEntity) ((DamageSource) eventC.eventParameters.get(1).getValue()).getEntity();
                if (entity != null) {
                    Exmodifier.LOGGER.debug("Adding Xp for22 " + entity.serializeNBT());
                } else Exmodifier.LOGGER.debug("Adding Xp for22 null");
          //  }
        } else {
            // this.eventC.eventParameters.get(0).name = "hurtamout";
        }
          Class<?> livingEntityClass = this.eventC.getLivingEntityClass();
//        if (livingEntityClass.isInstance(entity)) {


        String name = eventC.clazz.getName();
        ItemLevelHandle.ItemAddXpAuto(entity, this.eventC.eventParameters, eventC.clazz.cast(event1), name);
            Exmodifier.LOGGER.debug(name +" " +"Adding Xp for11 " + this.eventC.eventParameters.get(0).getValue());
       // }
    }
}