package net.exmo.exmodifier;

import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import dev.shadowsoffire.attributeslib.api.client.GatherSkippedAttributeTooltipsEvent;
import net.exmo.exmodifier.compat.compat.apoth.ApothCompat;
import net.exmo.exmodifier.content.client.EntryItemRender;
import net.exmo.exmodifier.content.modifier.EntryItem;
import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.util.WeightedUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.exmo.exmodifier.content.event.MainEvent.CommonEvent.init;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("exmodifier")
public class Exmodifier {

    // Directly reference a slf4j logger
    public static final String MODID = "exmodifier";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, MODID), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    private static int messageID = 0;
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<Item> ENTRY_ITEM = ITEMS.register("entry_item", () -> new EntryItem(new Item.Properties()));
    public static  ItemStack TabIcon;

    public final static  RegistryObject<CreativeModeTab> ExModifierTab =  CREATIVE_MODE_TABS.register("exmodifier_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(Exmodifier::getTabIcon)
            .displayItems((parameters, output) -> {

        //    output.accept(ENTRY_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
    }).build());

    private static ItemStack getTabIcon() {
        TabIcon = ENTRY_ITEM.get().getDefaultInstance();
        TabIcon.setHoverName(Component.translatable("modifiler.entry.example"));
        TabIcon.getOrCreateTag().putString("modifier_id", "example");

        return TabIcon;
    }

    ;


    public Exmodifier() {

        long time_start = System.currentTimeMillis();
        // Register the setup method for modloading
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();


        ITEMS.register(modEventBus);
        try {
            init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        CREATIVE_MODE_TABS.register(modEventBus);
        modEventBus.addListener(this::setup);
        // Register the enqueueIMC method for modloading
        modEventBus.addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        modEventBus.addListener(this::processIMC);

        modEventBus.addListener(this::AddToTab);
        if (ModList.get().isLoaded("attributeslib")){
            MinecraftForge.EVENT_BUS.addListener( new ApothCompat()::SkinAttr);
        }

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        long time_end = System.currentTimeMillis();
        LOGGER.info("Mod loaded in " + (time_end - time_start) + "ms");
    }

    private void setup(final FMLCommonSetupEvent event) {
        // Some preinit code
//        LOGGER.info("HELLO FROM PREINIT");
//        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    public void AddToTab(BuildCreativeModeTabContentsEvent event){
        if (event.getTab() == ExModifierTab.get()) {
            WeightedUtil<String> weight = new WeightedUtil<>(ModifierHandle.modifierEntryMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().weight)));
            ModifierHandle.modifierEntryMap.forEach((entry, modifierEntry) -> {
                ItemStack stack = ENTRY_ITEM.get().getDefaultInstance();
                stack.getOrCreateTag().putString("modifier_id", entry);
                stack.getOrCreateTag().putString("modifier_type",modifierEntry.type.toString());
                stack.getOrCreateTag().putDouble("modifier_possibility",weight.getProbability(entry));
                // stack.setHoverName(Component.translatable("modifiler.entry." + entry));
                event.accept(stack);
            });


            }
        }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // Some example code to dispatch IMC to another mod
//        InterModComms.sendTo("exmodifier", "helloworld", () -> {
//            LOGGER.info("Hello world from the MDK");
//            return "Hello world";
//        });
    }

    private void processIMC(final InterModProcessEvent event) {
        // Some example code to receive and process InterModComms from other mods
//        LOGGER.info("Got IMC {}", event.getIMCStream().map(m -> m.messageSupplier().get()).collect(Collectors.toList()));
    }
    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientEvents {
        @SubscribeEvent
        public static void registerItemDecoration(RegisterItemDecorationsEvent event) {
            event.register(ENTRY_ITEM.get(), new EntryItemRender());
        }
    }
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
//        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {

    }
    public static <T> void addNetworkMessage(Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer) {
        PACKET_HANDLER.registerMessage(messageID, messageType, encoder, decoder, messageConsumer);
        messageID++;
    }
}
