package net.exmo.exmodifier;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;


import mod.arcomit.emberthral.client.filter.Filter;
import mod.arcomit.emberthral.client.filter.FilterManager;
import net.exmo.exmodifier.content.attributeEffect.modern.EffectSyncPacket;
import net.exmo.exmodifier.content.modifier.*;
import net.exmo.exmodifier.content.type.ExTypeHandle;
import net.exmo.exmodifier.content.type.ItemType;
import net.exmo.exmodifier.events.ExCustomTabEvent;
import net.exmo.exmodifier.init.RegisterOther;
import net.exmo.exmodifier.network.*;
import net.exmo.exmodifier.network.sync.defaultEntityElement.ClearDefaultItemElementMessage;
import net.exmo.exmodifier.network.sync.defaultEntityElement.SyncDefaultItemElementMessage;
import net.exmo.exmodifier.network.sync.defaultItemElement.ClearDefaultEntityElementMessage;
import net.exmo.exmodifier.network.sync.defaultItemElement.SyncDefaultEntityElementMessage;

import net.exmo.exmodifier.network.sync.element.ClearElementMessage;
import net.exmo.exmodifier.network.sync.element.SyncElementMessage;
import net.exmo.exmodifier.network.sync.lang.LangMessage;
import net.exmo.exmodifier.network.sync.modifier.ClearModifierEntryMessage;
import net.exmo.exmodifier.network.sync.modifier.SyncModifierEntryMessage;
import net.exmo.exmodifier.network.sync.suit.ClearExSuitMessage;
import net.exmo.exmodifier.network.sync.suit.SyncExSuitMessage;
import net.exmo.exmodifier.util.WeightedUtil;
import net.exmo.exmodifier_compat.compat.ApothCompat;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;

import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.exmo.exmodifier.content.event.MainEvent.CommonEvent.init;
import static net.exmo.exmodifier.content.modifier.ModifierHandle.modifierEntryMap;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("exmodifier")
public class Exmodifier {
    public static final Gson GSON = new Gson();
    // Directly reference a slf4j logger
    public static final String MODID = "exmodifier";
    public static Random random = new Random();

    // public static final Logger LOGGER = LogUtils.getLogger();
    public static class LOGGER {
        public static Logger Logger = LogUtils.getLogger();

        public static void info(String msg) {
            Logger.info(msg);

        }

        public static void debug(String msg) {
            if (Config.DebugInInfo) Logger.info(msg);
            if (Config.Debug) Logger.debug(msg);
        }

        public static void error(String s, Exception e) {
            Logger.error(s, e);
        }
    }

    public static List<Filter> itemGroups = new ArrayList<>();


    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, MODID), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    private static int messageID = 0;
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<Item> ENTRY_ITEM = ITEMS.register("entry_item", () -> new EntryItem(new Item.Properties()));
    public static final RegistryObject<Item> ENTRY_ITEM_EMPTY = ITEMS.register("entry_item_empty", () -> new EntryItemEmpty(new Item.Properties()));
    public static ItemStack TabIcon;

    private static Map<String, Item> itemIconMap = new HashMap<>();
    public final static RegistryObject<CreativeModeTab> ExModifierTab = CREATIVE_MODE_TABS.register("exmodifier_tab", () -> CreativeModeTab.builder()
            .icon(Exmodifier::getTabIcon)
            .withSearchBar()
            .title(Component.translatable("itemGroup.exmodifier_tab"))
            .displayItems((parameters, output) -> {
            }).build());

    public static ItemStack getTabIcon() {
        TabIcon = ENTRY_ITEM.get().getDefaultInstance();
        TabIcon.setHoverName(Component.translatable("modifier.entry.example"));
        TabIcon.getOrCreateTag().putString("modifier_id", "example");

        return TabIcon;
    }

    ;

    public static <MSG> void registerMessage(Class<MSG> messageClass) {
        try {
            // 获取 encode 方法
            java.lang.reflect.Method encodeMethod = messageClass.getMethod("encode", messageClass, FriendlyByteBuf.class);
            // 获取 decode 方法
            java.lang.reflect.Method decodeMethod = messageClass.getMethod("decode", FriendlyByteBuf.class);
            // 获取 handle 方法
            java.lang.reflect.Method handleMethod = messageClass.getMethod("handle", messageClass, Supplier.class);

            // 将方法转换为 BiConsumer 和 Function
            BiConsumer<MSG, FriendlyByteBuf> encoder = (msg, buffer) -> {
                try {
                    encodeMethod.invoke(null, msg, buffer);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to encode message", e);
                }
            };

            Function<FriendlyByteBuf, MSG> decoder = buffer -> {
                try {
                    return (MSG) decodeMethod.invoke(null, buffer);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to decode message", e);
                }
            };

            BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer = (msg, ctx) -> {
                try {
                    handleMethod.invoke(null, msg, ctx);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to handle message", e);
                }
            };

            // 注册消息
            PACKET_HANDLER.registerMessage(messageID++, messageClass, encoder, decoder, messageConsumer);
            LOGGER.Logger.debug("Registered message: " + messageClass.getSimpleName() + " with ID: " + messageID);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to find required methods in message class", e);
        }
    }

    public Exmodifier() throws Exception {

        //    RealTimeWebServer.main(new String[]{""});
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC, String.valueOf(FMLPaths.CONFIGDIR.get().resolve("exmo/exmodifier.toml")));
        long time_start = System.currentTimeMillis();
        // Register the setup method for modloading
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        registerMessage(SyncModifierEntryMessage.class);
        registerMessage(ClearModifierEntryMessage.class);
        registerMessage(RefreshItemMessage.class);
        registerMessage(ChangeRefreshMenuTextListMessage.class);
        registerMessage(PlayerRefreshScreenOverMessageMessage.class);
        registerMessage(RefreshCraftContentMessage.class);
        registerMessage(DamageNumberCompatMessage.class);
        registerMessage(DamageNumberColorCompatMessage.class);
        registerMessage(SyncEntityElementMessage.class);
        registerMessage(SyncEntityElementRemovedMessage.class);
        registerMessage(AskSyncEntityElementMessage.class);
        registerMessage(RefineItemMessage.class);
        PACKET_HANDLER.registerMessage(messageID++, EffectSyncPacket.class,
                EffectSyncPacket::encode, EffectSyncPacket::new,
                EffectSyncPacket::handle);
        registerMessage(SyncElementMessage.class);
        registerMessage(ClearElementMessage.class);
        registerMessage(SyncDefaultEntityElementMessage.class);
        registerMessage(ClearDefaultEntityElementMessage.class);
        registerMessage(SyncDefaultItemElementMessage.class);
        registerMessage(ClearDefaultItemElementMessage.class);
        registerMessage(ClearExSuitMessage.class);
        registerMessage(SyncExSuitMessage.class);
        registerMessage(ClearDefaultEntityElementMessage.class);
        registerMessage(LangMessage.class);
        ITEMS.register(modEventBus);
        try {
            init(null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        CREATIVE_MODE_TABS.register(modEventBus);
        modEventBus.addListener(this::setup);
        // Register the enqueueIMC method for modloading
        modEventBus.addListener(this::enqueueIMC);
        modEventBus.addListener(this::gatherData);
        // Register the processIMC method for modloading
        modEventBus.addListener(this::processIMC);
        RegisterOther.EffectAbout.REGISTRY.register(modEventBus);
        RegisterOther.BlockAbout.REGISTRY.register(modEventBus);
        RegisterOther.ItemAbout.REGISTRY.register(modEventBus);
        modEventBus.addListener(EventPriority.HIGH, this::AddToTab);

        RegisterOther.MenuAbout.REGISTRY.register(modEventBus);
        RegisterOther.BlockEntityAbout.REGISTRY.register(modEventBus);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        long time_end = System.currentTimeMillis();

        RegisterOther.EventAbout.init();

//    for (EventC<? extends LivingEvent> v : RegisterOther.EventAbout.EVENT_C_LIST.itemTypes()){
//
//            EventCI<? extends LivingEvent> eventCI = new EventCI<>(v);
//
//            MinecraftForge.EVENT_BUS.addListener(v.priority,true,v.clazz,eventCI::AddXp);
//        }
//        for (EventC<? extends Event  > ec : ) {
//
//        }
        LOGGER.info("Mod loaded in " + (time_end - time_start) + "ms");
    }


    public void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        LOGGER.debug("ExGatherData");
//        APO provider = new APO(generator, existingFileHelper);
//        generator.addProvider(event.includeServer(), provider);
    }

    private void setup(final FMLCommonSetupEvent event) {
        ExCustomTabEvent event1 = new ExCustomTabEvent();
        MinecraftForge.EVENT_BUS.post(event1);
        event1.addTab("exmodifier_tab", getTabIcon());
        //  FilterManager.registerTabFilters(ExModifierTab.get(),itemGroups.toArray(new Filter[]{}));
    }

    private void AddToTab(BuildCreativeModeTabContentsEvent event) {


        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(RegisterOther.ItemAbout.Refresh_Table);
            event.accept(RegisterOther.ItemAbout.Embedded_Table);
        }

        if (event.getTab().equals(ExModifierTab.get())) {

            var list = generateModifierItemStacks();
            list.forEach(e -> {
                ModifierEntry entry = ModifierHandle.findModifierEntry(EntryItem.getModifierID(e));
                if (entry != null) {

                }
            });
        }
        if (event.getTab() == ExModifierTab.get()) {

            // 1. 获取FilterManager的Class对象

            event.accept(RegisterOther.ItemAbout.Refresh_Table);
            event.accept(RegisterOther.ItemAbout.Embedded_Table);
            LinkedHashSet<Filter> filters = FilterManager.filterTabMap.get(ExModifierTab.get());
            if (filters != null) filters.clear();
            FilterManager.filterTabMap.put(ExModifierTab.get(), filters);
            AtomicReference<List<ItemStack>> modifierItemStacks = new AtomicReference<>(generateModifierItemStacks());
            {
                modifierItemStacks.get().forEach(e -> {
                    ModifierEntry modifierEntry = ModifierHandle.findModifierEntry(EntryItem.getModifierID(e));
                    if (modifierEntry != null) {
                        String group = modifierEntry.group;
                        event.accept(e);
                        //if (group.equals("exmodifier_tab"))
                        //else
                        {
                            if (itemGroups.stream().noneMatch(itemGroup -> itemGroup.getName().equals(group))) {
                                Item item = itemIconMap.get(group);
                                if (item == null) {
                                    item = ENTRY_ITEM.get();
                                }
                                ItemStack defaultInstance = item.getDefaultInstance();
                                Filter e1 = new Filter(group, defaultInstance, null, ExModifierTab.getId());
                                e1.getFilteredItems().add(e);
                                itemGroups.add(e1);

                            } else {
                                var itemGroupList = itemGroups.stream().filter(itemGroup -> itemGroup.getName().equals(group)).toList();
                                itemGroupList.forEach(a -> {
                                    a.getFilteredItems().add(e);
                                });
                            }
                        }
                    }
                });


                FilterManager.filterTabMap.put(ExModifierTab.get(), new LinkedHashSet<>(itemGroups));
//                FilterManager.filterTabMap.forEach((k, v) -> {
//                    if (k == ExModifierTab.get()) v.forEach(Filter::loadItems);
//                });
            }
            ;

//            if (modifierItemStacks.get().isEmpty()){
//                queueServerWork(50,()->{
//                     modifierItemStacks.set(generateModifierItemStacks());
//                    runnable.run();
//                });
//            }else

        }
//            modifierItemStacks.forEach(e->{
//                ModifierEntry modifierEntry = ModifierHandle.findModifierEntry(EntryItem.getModifierID(e));
//                if (modifierEntry != null){
//                   if (modifierEntry.group.equals(event.getTabKey().location().getPath())) event.accept(e);
//                }
//            });
    }

    private static final Collection<AbstractMap.SimpleEntry<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();

    public static void queueServerWork(int tick, Runnable action) {
        if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
            workQueue.add(new AbstractMap.SimpleEntry<>(action, tick));
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            List<AbstractMap.SimpleEntry<Runnable, Integer>> actions = new ArrayList<>();
            workQueue.forEach(work -> {
                work.setValue(work.getValue() - 1);
                if (work.getValue() == 0)
                    actions.add(work);
            });
            actions.forEach(e -> e.getKey().run());
            workQueue.removeAll(actions);
        }
    }

    // 新增方法：生成 Modifier 的 ItemStack 列表
    public static List<ItemStack> generateModifierItemStacks() {
        List<ItemStack> itemStacks = new ArrayList<>();
        Map<String, WeightedUtil<String>> weights = new HashMap<>();

        for (ItemType type : ExTypeHandle.itemTypes.values()) {
            weights.put(type.name(), new WeightedUtil<>(modifierEntryMap.entrySet().stream().filter(e -> {
                return e.getValue().types.contains(type) && !e.getValue().cantSelect;
            }).collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().weight))));
        }

        modifierEntryMap.forEach((entry, modifierEntry) -> {
            ItemStack stack = ENTRY_ITEM.get().getDefaultInstance();
            stack.getOrCreateTag().putString("modifier_id", entry);
            ListTag listTag = new ListTag();
            for (ItemType type : modifierEntry.types) {
                listTag.add(StringTag.valueOf(type.name()));
            }
            stack.getOrCreateTag().put("modifier_types", listTag);

            double probability = modifierEntry.types.stream().mapToDouble(type -> weights.get(type.name()).getProbability(entry)).sum();
            //  double probability = exElement.types.stream().mapToDouble(type -> weights.get(type.name()).getProbability(entry) / totalWeight).sum();
            stack.getOrCreateTag().putDouble("modifier_possibility", probability);
            if (modifierEntry.maxLevel <= 1) {
                stack.getOrCreateTag().putInt("modifier_level", 1);
                itemStacks.add(stack);
            } else {
                for (int i = 1; i <= modifierEntry.maxLevel; i++) {
                    ItemStack stack1 = stack.copy();
                    stack1.getOrCreateTag().putInt("modifier_level", i);
                    itemStacks.add(stack1);
                }
            }
        });

        return itemStacks;
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {

    }

    private void processIMC(final InterModProcessEvent event) {

    }


    public static <T> void addNetworkMessage(Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer) {
        PACKET_HANDLER.registerMessage(messageID, messageType, encoder, decoder, messageConsumer);
        messageID++;
    }
}
