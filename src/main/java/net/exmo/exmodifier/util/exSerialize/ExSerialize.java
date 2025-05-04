package net.exmo.exmodifier.util.exSerialize;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.*;


import com.google.gson.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.*;

import com.google.gson.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import java.util.*;
import java.util.function.*;

import com.google.gson.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import java.util.*;
import java.util.function.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import java.util.*;
import java.util.function.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import java.util.*;
import com.google.gson.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class ExSerialize<T> {
    private final Supplier<T> constructor;
    private BiConsumer<T, String> autoIdSetter;
    private final List<FieldHandler<T, ?>> fields = new ArrayList<>();

    private ExSerialize(Supplier<T> constructor) {
        this.constructor = constructor;
    }

    public static <T> ExSerialize<T> create(Supplier<T> constructor) {
        return new ExSerialize<>(constructor);
    }

    public ExSerialize<T> withAutoId(BiConsumer<T, String> autoIdSetter) {
        this.autoIdSetter = autoIdSetter;
        return this;
    }

    // JSON字段处理相关方法
    public ExSerialize<T> addStringField(String name, BiConsumer<T, String> setter) {
        return addField(name, JsonElement::getAsString, setter);
    }

    public ExSerialize<T> addIntField(String name, BiConsumer<T, Integer> setter) {
        return addField(name, JsonElement::getAsInt, setter);
    }
    public ExSerialize<T> addFloatField(String name, BiConsumer<T, Float> setter) {
        return addField(name, JsonElement::getAsFloat, setter);
    }

    public ExSerialize<T> addResourceLocationField(String name, BiConsumer<T, ResourceLocation> setter) {
        return addField(name, json -> new ResourceLocation(json.getAsString()), setter);
    }

    public ExSerialize<T> addFloatMapField(String name, BiConsumer<T, Map<String, Float>> setter) {
        return addField(name, json -> parseFloatMap(json.getAsJsonObject()), setter);
    }

    // 新增字符串列表字段处理方法
    public ExSerialize<T> addStringListField(String name, BiConsumer<T, List<String>> setter) {
        return addField(name, json -> parseStringList(json.getAsJsonArray()), setter);
    }

    // 新增字符串列表解析方法
    private List<String> parseStringList(JsonArray jsonArray) {
        List<String> list = new ArrayList<>();
        for (JsonElement element : jsonArray) {
            list.add(element.getAsString());
        }
        return list;
    }

    // NBT字段处理相关方法
    public ExSerialize<T> addNbtStringField(String name, Function<T, String> getter, BiConsumer<T, String> setter) {
        return addNbtField(name,
                Tag::getAsString,
                (nbt, value) -> nbt.putString(name, value),
                getter,
                setter);
    }

    public ExSerialize<T> addNbtIntField(String name, Function<T, Integer> getter, BiConsumer<T, Integer> setter) {
        return addNbtField(name,
                tag -> ((IntTag) tag).getAsInt(),  // 修复点：强制类型转换为 IntTag
                (nbt, value) -> nbt.putInt(name, value),
                getter,
                setter);
    }


    // 核心转换方法
    public List<T> fromJson(JsonObject json) {
        List<T> result = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            T instance = createInstance(entry.getKey(), entry.getValue().getAsJsonObject());
            result.add(instance);
        }
        return result;
    }

    public T fromJsonSingleObject(JsonObject json, String fieldName) {
            return createInstance(fieldName, json);
    }

    public CompoundTag toNbt(T object) {
        CompoundTag tag = new CompoundTag();
        for (FieldHandler<T, ?> field : fields) {
            if (field.nbtSerializer != null) {
                field.serializeToNbt(object, tag);
            }
        }
        return tag;
    }

    public T fromNbt(CompoundTag tag) {
        T instance = constructor.get();
        for (FieldHandler<T, ?> field : fields) {
            if (field.nbtDeserializer != null) {
                field.deserializeFromNbt(tag, instance);
            }
        }
        return instance;
    }

    // 实现细节
    private <V> ExSerialize<T> addField(String name, Function<JsonElement, V> deserializer, BiConsumer<T, V> setter) {
        fields.add(new FieldHandler<>(name, deserializer, setter));
        return this;
    }

    private <V> ExSerialize<T> addNbtField(String name,
                                           Function<Tag, V> nbtDeserializer,
                                           BiConsumer<CompoundTag, V> nbtSerializer,
                                           Function<T, V> getter,
                                           BiConsumer<T, V> setter) {
        FieldHandler<T, V> handler = new FieldHandler<>(
                name,
                null,
                setter,
                (obj, tag) -> nbtSerializer.accept(tag, getter.apply(obj)),
                nbtDeserializer
        );
        fields.add(handler);
        return this;
    }

    private T createInstance(String id, JsonObject json) {
        T instance = constructor.get();
        if (autoIdSetter != null) {
            autoIdSetter.accept(instance, id);
        }
        for (FieldHandler<T, ?> field : fields) {
            if (json.has(field.name)) {
                JsonElement element = json.get(field.name);
                Object value = field.deserializer.apply(element);
                ((BiConsumer<T, Object>) field.setter).accept(instance, value);
            }
        }
        return instance;
    }

    private Map<String, Float> parseFloatMap(JsonObject json) {
        return json.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().getAsFloat()
                ));
    }

    private static class FieldHandler<T, V> {
        final String name;
        final Function<JsonElement, V> deserializer;
        final BiConsumer<T, V> setter;
        final BiConsumer<T, CompoundTag> nbtSerializer;
        final Function<Tag, V> nbtDeserializer;

        FieldHandler(String name,
                     Function<JsonElement, V> deserializer,
                     BiConsumer<T, V> setter) {
            this(name, deserializer, setter, null, null);
        }

        FieldHandler(String name,
                     Function<JsonElement, V> deserializer,
                     BiConsumer<T, V> setter,
                     BiConsumer<T, CompoundTag> nbtSerializer,
                     Function<Tag, V> nbtDeserializer) {
            this.name = name;
            this.deserializer = deserializer;
            this.setter = setter;
            this.nbtSerializer = nbtSerializer;
            this.nbtDeserializer = nbtDeserializer;
        }

        void serializeToNbt(T object, CompoundTag tag) {
            if (nbtSerializer != null) {
                nbtSerializer.accept(object, tag);
            }
        }

        void deserializeFromNbt(CompoundTag tag, T object) {
            if (nbtDeserializer != null && tag.contains(name)) {
                V value = nbtDeserializer.apply(tag.get(name));
                setter.accept(object, value);
            }
        }
    }
}