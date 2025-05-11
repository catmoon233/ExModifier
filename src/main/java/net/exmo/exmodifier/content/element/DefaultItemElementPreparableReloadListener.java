package net.exmo.exmodifier.content.element;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.util.ItemSelector;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static net.exmo.exmodifier.Exmodifier.GSON;
import static net.exmo.exmodifier.util.ExUtil.classToString;

public class DefaultItemElementPreparableReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, JsonObject>> {

    @Override
    protected Map<ResourceLocation, JsonObject> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        pProfiler.startTick();
        pProfiler.push("loading default item elements data...");
        var loader = listResources(pResourceManager, pProfiler);
        pProfiler.pop();
        return loader;
    }

    @SuppressWarnings("unchecked")
    public Map<ResourceLocation, JsonObject> listResources(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        Map<ResourceLocation, JsonObject> loader = new HashMap<>();
        for (Map.Entry<ResourceLocation, Resource> resource : pResourceManager.listResources("default_elements", p -> p.getPath().endsWith(".json")).entrySet()) {
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

    @Override
    protected void apply(Map<ResourceLocation, JsonObject> jsonObjects, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<ItemSelector, DefaultItemElement> loader = new HashMap<>();
        for (Map.Entry<ResourceLocation, JsonObject> entry : jsonObjects.entrySet()) {
            for (DefaultItemElement read : DefaultItemElement.EX_SERIALIZE.fromJson(entry.getValue())) {
                if (read.getItemSelector() != null) {
                    loader.put(read.itemSelector(), read);
                }
            }
        }
        ExElementHandle.elementDefaultMap.putAll(loader);
    }

    @Override
    public String getName() {
        return classToString(this.getClass());
    }
}