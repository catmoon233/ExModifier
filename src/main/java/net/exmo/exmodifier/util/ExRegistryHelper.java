package net.exmo.exmodifier.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.exmo.exmodifier.content.modifier.MoConfig;
import net.exmo.exmodifier.util.exSerialize.ExSerialize;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class ExRegistryHelper {
    private ExRegistryHelper() {
    }

    public static <V> void register(Map<ResourceLocation, V> registry, ResourceLocation id, V value) {
        if (id != null && value != null) {
            registry.put(id, value);
        }
    }

    @Nullable
    public static <V> V get(Map<ResourceLocation, V> registry, String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        ResourceLocation resourceLocation = ResourceLocation.tryParse(id);
        if (resourceLocation == null) {
            return null;
        }
        return registry.get(resourceLocation);
    }

    public static List<MoConfig> listConfigs(Path configPath) throws IOException {
        if (!Files.exists(configPath)) {
            return Collections.emptyList();
        }
        return ExConfigHandle.listFiles(configPath);
    }

    public static int getInt(JsonObject jsonObject, String key, int defaultValue) {
        return jsonObject.has(key) ? jsonObject.get(key).getAsInt() : defaultValue;
    }

    public static float getFloat(JsonObject jsonObject, String key, float defaultValue) {
        return jsonObject.has(key) ? jsonObject.get(key).getAsFloat() : defaultValue;
    }

    public static double getDouble(JsonObject jsonObject, String key, double defaultValue) {
        return jsonObject.has(key) ? jsonObject.get(key).getAsDouble() : defaultValue;
    }

    public static boolean getBoolean(JsonObject jsonObject, String key, boolean defaultValue) {
        return jsonObject.has(key) ? jsonObject.get(key).getAsBoolean() : defaultValue;
    }

    public static List<String> getStringList(JsonObject jsonObject, String key) {
        return mapJsonArray(jsonObject, key, JsonElement::getAsString);
    }

    public static <T> List<T> mapJsonArray(JsonObject jsonObject, String key, Function<JsonElement, T> mapper) {
        if (!jsonObject.has(key) || !jsonObject.get(key).isJsonArray()) {
            return Collections.emptyList();
        }
        List<T> values = new ArrayList<>();
        for (JsonElement element : jsonObject.getAsJsonArray(key)) {
            values.add(mapper.apply(element));
        }
        return values;
    }

    public static <T> List<JsonObject> toJsonObjects(Collection<T> values, ExSerialize<T> serializer) {
        return values.stream().map(serializer::toSingleJson).toList();
    }

    public static <T> List<T> fromJsonObjects(Collection<JsonObject> values, ExSerialize<T> serializer) {
        List<T> parsed = new ArrayList<>();
        for (JsonObject value : values) {
            parsed.add(serializer.fromJsonSingle(value));
        }
        return parsed;
    }
}
