package net.exmo.exmodifier.content.element;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.exmo.exmodifier.Exmodifier;
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

public class DefaultEntityElementPreparableReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, JsonObject>> {

    @Override
    protected Map<ResourceLocation, JsonObject> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        pProfiler.startTick();
        pProfiler.push("loading default entity elements data...");
        var loader = listResources(pResourceManager, pProfiler);
        pProfiler.pop();
        return loader;
    }

    public Map<ResourceLocation, JsonObject> listResources(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        Map<ResourceLocation, JsonObject> loader = new HashMap<>();
        for (Map.Entry<ResourceLocation, Resource> resource : pResourceManager.listResources("default_entity_elements", p -> p.getPath().endsWith(".json")).entrySet()) {
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
    protected void apply(Map<ResourceLocation, JsonObject> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<EntityType<?>, DefaultEntityElement> loader = new HashMap<>();
        for (Map.Entry<ResourceLocation, JsonObject> entry : map.entrySet()) {
            JsonObject jsonObject = entry.getValue();
            for (var read : DefaultEntityElement.SERIALIZER.fromJson(jsonObject)) {
                if (!read.getEntityTypeString().isEmpty()) {
                    loader.put(read.getEntityType(), read);
                }
            }
        }
        ExElementHandle.elementDefaultMap2.putAll(loader);
    }

    @Override
    public String getName() {
        return classToString(this.getClass());
    }
}