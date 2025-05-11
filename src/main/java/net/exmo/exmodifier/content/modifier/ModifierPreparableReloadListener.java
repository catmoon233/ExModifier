package net.exmo.exmodifier.content.modifier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.exmo.exmodifier.Exmodifier;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;


public class ModifierPreparableReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, JsonObject>> {

    private static final Gson GSON = new Gson();

    Map<ResourceLocation, JsonObject> entries;

    @Override
    protected Map<ResourceLocation, JsonObject> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        pProfiler.startTick();
        pProfiler.push("loading modifier data...");
        var loader = listResources(pResourceManager, pProfiler);
        pProfiler.pop();
        return loader;
    }

    @SuppressWarnings("unchecked")
    public Map<ResourceLocation, JsonObject> listResources(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        Map<ResourceLocation, JsonObject> loader = new HashMap<>();
        for (Map.Entry<ResourceLocation, Resource> resource : pResourceManager.listResources("modifier_entries", p -> p.getPath().endsWith(".json")).entrySet()) {
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
    protected void apply(Map<ResourceLocation, JsonObject> jsonObjects, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        for (Map.Entry<ResourceLocation, JsonObject> entry : jsonObjects.entrySet()) {
            try {
                ArrayList<ModifierEntry> jsonList = new ArrayList<>();
                ModifierHandle.processModifierEntry(entry.getValue().toString(), jsonList);
                for (ModifierEntry modifierEntry : jsonList) {
                    ModifierHandle.RegisterModifierEntry(modifierEntry);
                }
            } catch (Exception e) {
                Exmodifier.LOGGER.Logger.error("Failed to process modifier entry: {}", entry.getKey(), e);
            }
        }
    }

    @Override
    public String getName() {
        return "ModifierPreparableReloadListener";
    }
}