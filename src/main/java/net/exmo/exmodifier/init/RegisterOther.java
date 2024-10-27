package net.exmo.exmodifier.init;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.MobEffect.DodgeEffect;
import net.exmo.exmodifier.content.MobEffect.HitRateEffect;
import net.exmo.exmodifier.content.MobEffect.ReduceInjuriesEffect;
import net.exmo.exmodifier.content.MobEffect.VulnerabilityEffect;
import net.exmo.exmodifier.content.client.RefreshTableEntityRenderer;
import net.exmo.exmodifier.content.event.parameter.EventC;
import net.exmo.exmodifier.content.modifier.block.RefreshTable;
import net.exmo.exmodifier.content.modifier.block.enitty.RefreshTableEntity;
import net.exmo.exmodifier.content.modifier.menu.RefreshMenu;
import net.exmo.exmodifier.content.modifier.menu.RefreshMenuScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.IModBusEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterOther {
    public static class ItemAbout{
        public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, Exmodifier.MODID);
        public static final RegistryObject<Item> Refresh_Table = block(BlockAbout.REFRESH_TABLE);
        private static RegistryObject<Item> block(RegistryObject<Block> block) {
            return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
        }
    }
    public static class BlockAbout{

        public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, Exmodifier.MODID);
        public static final RegistryObject<Block> REFRESH_TABLE = REGISTRY.register("refresh_table", () -> new RefreshTable());
    }
    public static class MenuAbout{
        public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Exmodifier.MODID);
        public static final RegistryObject<MenuType<RefreshMenu>> REFRESH_MENU = REGISTRY.register("refresh_menu",() -> IForgeMenuType.create(RefreshMenu::new));

    }
    @Mod.EventBusSubscriber(modid = Exmodifier.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class BlockEntityRenderAbout{
        @SubscribeEvent
        public static void RegisterModle(EntityRenderersEvent.RegisterRenderers event){
            event.registerBlockEntityRenderer(BlockEntityAbout.RefreshTableEntity.get(), RefreshTableEntityRenderer::new);
            Exmodifier.LOGGER.debug("Registering Block Entity Renderer");
        }
    }
    public static class BlockEntityAbout{
        public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Exmodifier.MODID);
        public static final RegistryObject<BlockEntityType<RefreshTableEntity>> RefreshTableEntity = REGISTRY.register("refresh_table",() -> BlockEntityType.Builder.of(RefreshTableEntity::new, BlockAbout.REFRESH_TABLE.get()).build(null));

        private static RegistryObject<BlockEntityType<?>> register(String registryname, RegistryObject<Block> block, BlockEntityType.BlockEntitySupplier<?> supplier) {
            return REGISTRY.register(registryname, () -> BlockEntityType.Builder.of(supplier, block.get()).build(null));
        }
    }
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ScreenAbout{
        @SubscribeEvent
        public static void clientLoad(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(MenuAbout.REFRESH_MENU.get(), RefreshMenuScreen::new);
            });
    }
    }
    public static class EffectAbout{
        public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Exmodifier.MODID);
        public static final RegistryObject<DodgeEffect> DODGE_EFFECT = REGISTRY.register("dodge_effect", DodgeEffect::new);
        public static final RegistryObject<VulnerabilityEffect> VULNERABILITY_EFFECT = REGISTRY.register("vulnerability_effect",VulnerabilityEffect::new);
        public static final RegistryObject<HitRateEffect> HIT_RATE_EFFECT = REGISTRY.register("hit_rate_effect", HitRateEffect::new);
        public static final RegistryObject<ReduceInjuriesEffect> REDUCE_INJURIES_EFFECT = REGISTRY.register("reduce_injuries_effect", ReduceInjuriesEffect::new);
    }
   public static class EventAbout{
       public static final Map<Class<? extends LivingEvent>,EventC<? extends LivingEvent> > EVENT_C_LIST = new HashMap<>();
       public static void register(EventC<? extends LivingEvent> eventC){
            EVENT_C_LIST.put(eventC.clazz,eventC);
       }
       static {
           register(new EventC<>(LivingHurtEvent.class).addParameterField("amount").setPriority(EventPriority.LOWEST).addParameterField("source").setLivingEntityClass(Player.class));
           register(new EventC<>(LivingHurtEvent.class).addParameterField("amount").setPriority(EventPriority.LOWEST).setLivingEntityClass(Player.class));

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