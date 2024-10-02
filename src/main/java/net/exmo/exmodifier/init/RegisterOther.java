package net.exmo.exmodifier.init;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.event.parameter.EventC;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.IModBusEvent;
import net.minecraftforge.registries.*;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterOther {
   public static class EventAbout{
       public static final Map<Class<? extends LivingEvent>,EventC<? extends LivingEvent> > EVENT_C_LIST = new HashMap<>();
       public static void register(EventC<? extends LivingEvent> eventC){
            EVENT_C_LIST.put(eventC.clazz,eventC);
       }
       static {
           register(new EventC<>(LivingHurtEvent.class).addParameterField("amount").setPriority(EventPriority.LOWEST).addParameterField("source").setLivingEntityClass(Player.class));
          // register(new EventC<>(LivingHurtEvent.class).addParameterField("amount").setPriority(EventPriority.LOWEST).setLivingEntityClass(Player.class));

       }
       public static void init(){

       }
//        public static final class Keys {
//            private static void init() {}
//
//            private static <T> ResourceKey<Registry<T>> key(String name)
//            {
//                return ResourceKey.createRegistryKey(new ResourceLocation(name));
//            }
//            public static final ResourceKey<Registry<EventC<? extends Event>>> EVENT_C  = key("event_c");
//        }
//            public static final IForgeRegistry<EventC<? extends Event>> EVENT_C = RegistryManager.ACTIVE.getRegistry(Keys.EVENT_C);
//
//        public static final DeferredRegister<EventC<? extends Event>> EVENT_C_DEFERRED_REGISTER = DeferredRegister.create(EVENT_C, Exmodifier.MODID);
//        public final static RegistryObject<EventC<net.minecraftforge.eventC.entity.living.LivingHurtEvent>> LivingHurtEvent = EVENT_C_DEFERRED_REGISTER.register("living_hurt_event", () -> {
//            try {
//                return new EventC<LivingHurtEvent>().addParameterMethod("amount");
//            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
//                throw new RuntimeException(e);
//            }
//        });

    }
//    public static void init(){
//        EventAbout.Keys.init();
//    }

}