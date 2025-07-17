package net.exmo.exmodifier.util;

import com.google.gson.JsonObject;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.client.LanguageLoader;
import net.exmo.exmodifier.content.element.ExElementHandle;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.content.modifier.RefreshContainItemHandle;
import net.exmo.exmodifier.content.modifier.RefreshContainTagHandle;
import net.exmo.exmodifier.content.quality.ItemQualityHandle;
import net.exmo.exmodifier.content.resources.ZipHandle;
import net.exmo.exmodifier.content.selected.BaseItemSelected;
import net.exmo.exmodifier.content.slot.ModifierSlotHandle;
import net.exmo.exmodifier.content.suit.ExSuitHandle;
import net.exmo.exmodifier.content.type.ExTypeHandle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static net.exmo.exmodifier.Exmodifier.GSON;
import static net.exmo.exmodifier.commands.ExModifierReloadCommand.sendUpdatedModifiersToClients;
import static net.exmo.exmodifier.content.event.MainEvent.clearOldData;
import static net.exmo.exmodifier.content.event.MainEvent.clearReadTempData;

public abstract class AbstractReloadListener<T> extends SimplePreparableReloadListener<Map<ResourceLocation, JsonObject>> {
    // 统计计数器
    private static final AtomicInteger loadedCount = new AtomicInteger(0);
    private static final AtomicInteger totalCount = new AtomicInteger(0);
    
    private final String resourcePath;
    private final String logPrefix;
    private final Function<JsonObject, Iterable<T>> deserializer;
    private final BiConsumer<ResourceLocation, T> consumer;

    protected AbstractReloadListener(String resourcePath,
                                     String logPrefix,
                                     Function<JsonObject, Iterable<T>> deserializer,
                                     BiConsumer<ResourceLocation, T> consumer) {
        this.resourcePath = resourcePath;
        this.logPrefix = logPrefix;
        this.deserializer = deserializer;
        this.consumer = consumer;
        totalCount.incrementAndGet();
    }

    @Override
    protected Map<ResourceLocation, JsonObject> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        pProfiler.startTick();
        pProfiler.push(logPrefix);
        var loader = listResources(pResourceManager, pProfiler);
        pProfiler.pop();
        return loader;
    }

    protected Map<ResourceLocation, JsonObject> listResources(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        Map<ResourceLocation, JsonObject> loader = new HashMap<>();
        for (Map.Entry<ResourceLocation, Resource> resource : pResourceManager.listResources(resourcePath, p -> p.getPath().endsWith(".json")).entrySet()) {
            ResourceLocation key = resource.getKey();
            try (Reader reader = resource.getValue().openAsReader()) {
                Resource packResources = pResourceManager.getResource(key).orElseThrow(() -> new IOException("Resource not found: " + key));
                String jsonString = IOUtils.toString(packResources.open(), StandardCharsets.UTF_8);
                JsonObject jsonObject = GSON.fromJson(jsonString, JsonObject.class);
                loader.put(key, jsonObject);
            } catch (Exception e) {
                Exmodifier.LOGGER.Logger.error("Failed to load custom data pack: {}", key, e);
            }
        }
        return loader;
    }

    private static  ZipHandle.ZipFunction zipFunction = null;
    @Override
    protected void apply(Map<ResourceLocation, JsonObject> jsonObjects, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        if (loadedCount.get() == 0){
            clearOldData();
            BaseItemSelected.IDS = new HashMap<>();
            try {
            RefreshContainTagHandle.readConfig();
            RefreshContainItemHandle.readConfig();
            ModifierHandle.sendClearDataToAllClient();
            ExTypeHandle.readConfig();
            ItemQualityHandle.init();
            ExElementHandle.init();
                zipFunction = ZipHandle.init();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Exmodifier.LOGGER.Logger.info("Start loading reload listeners");
        }
        for (Map.Entry<ResourceLocation, JsonObject> entry : jsonObjects.entrySet()) {
            for (T element : deserializer.apply(entry.getValue())) {
                consumer.accept(entry.getKey(), element);
            }
        }
        loadedCount.incrementAndGet();
        if (loadedCount.get() == totalCount.get()) {
            try {
                ItemQualityHandle.init2();


            ModifierHandle.readConfig();
            ExSuitHandle.readConfig();
            ModifierSlotHandle.reload();



            ExElementHandle.init2();
            ExElementHandle.init3();
            if (zipFunction!=null) {
                zipFunction.elementDefault().forEach(Runnable::run);
                zipFunction.defaultEntry().forEach(Runnable::run);
                zipFunction.suit().forEach(Runnable::run);
            }
            ModifierHandle.EEMatchQueueHandle();
            LanguageLoader.load(LanguageLoader.LANGUAGES_FILE_PATH);
            clearReadTempData();

            loadedCount .set(0);
             totalCount.set(0);
            MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
            if (currentServer != null) {
                sendUpdatedModifiersToClients(currentServer);
            }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Exmodifier.LOGGER.Logger.info("All reload listeners loaded");
        }
    }

    // 新增统计方法
    public static boolean isAllLoaded() {
        return loadedCount.get() == totalCount.get();
    }

    public static String getLoadingStatus() {
        return String.format("Loaded %d/%d reload listeners", loadedCount.get(), totalCount.get());
    }
}