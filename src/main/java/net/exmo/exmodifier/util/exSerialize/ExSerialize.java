package net.exmo.exmodifier.util.exSerialize;

import com.google.gson.*;
import net.exmo.exmodifier.Exmodifier;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class ExSerialize<T> {
    private final Supplier<T> constructor;
    private autoID autoIdSetter;
    private Map<Pair<Function<T, Object>,BiConsumer<T, Object>>,ExSerialize<Object>> subclasses  = new HashMap<>();
    private List<String> dontSyncToClientField = new ArrayList<>();
    private List<String> onlyReadField = new ArrayList<>();
    private String lastField = "";
    public ExSerialize<T> dontSyncToClient(String fieldName) {
        dontSyncToClientField.add(fieldName);
        return this;
    }
    public ExSerialize<T> dontSyncToClient() {
        dontSyncToClientField.add(lastField);
        return this;
    }
    public ExSerialize<T> onlyRead(String fieldName) {
        onlyReadField.add(fieldName);
        return this;
    }
    public ExSerialize<T> onlyRead() {
        onlyReadField.add(lastField);
        return this;
    }


    public class autoID {
        public Function<T, String> getter;
        public BiConsumer<T, String> autoIdSetter;

        public autoID(Function<T, String> getter, BiConsumer<T, String> autoIdSetter) {
            this.getter = getter;
            this.autoIdSetter = autoIdSetter;
        }
    }

    public List<FieldHandler<T, ?>> getFields() {
        return fields;
    }

    private final List<FieldHandler<T, ?>> fields = new ArrayList<>();

    public ExSerialize<T> marge(ExSerialize<?> other){
        fields.addAll((Collection<? extends FieldHandler<T, ?>>) other.fields);
        return this;
    }
    private ExSerialize(Supplier<T> constructor) {
        this.constructor = constructor;
    }
    public ExSerialize<T> addSubclass(Function<T, Object> getter,BiConsumer<T, Object> setter,ExSerialize<Object> ser){
        subclasses.put(new Pair<>(getter,setter),ser);
        return this;
    }

    public static <T> ExSerialize<T> create(Supplier<T> constructor) {
        return new ExSerialize<>(constructor);
    }

    public ExSerialize<T> withAutoId(Function<T, String> getter,BiConsumer<T, String> autoIdSetter) {
        this.autoIdSetter = new autoID(getter, autoIdSetter);
        return this;
    }

    // region 字段注册方法
    // 新增Integer-String Map字段支持
    public ExSerialize<T> addIntStringMapField(String name, BiConsumer<T, Map<Integer, String>> setter) {
        return addIntStringMapField(name, t -> Collections.emptyMap(), setter);
    }

    public ExSerialize<T> addIntStringMapField(String name,
                                              Function<T, Map<Integer, String>> getter,
                                              BiConsumer<T, Map<Integer, String>> setter) {
        return addField(name,
                json -> parseIntStringMap(json.getAsJsonObject()),
                setter,
                tag -> parseNbtIntStringMap((CompoundTag) tag),
                (nbt, value) -> serializeNbtIntStringMap(nbt, name, value),
                getter
        );
    }
    // 基础类型字段
    public ExSerialize<T> addStringField(String name, BiConsumer<T, String> setter) {
        return addStringField(name, t -> null, setter);
    }

    public ExSerialize<T> addStringField(String name,
                                         Function<T, String> getter,
                                         BiConsumer<T, String> setter) {
        return addField(name,
                json -> getJsonPrimitive(json).getAsString(),
                setter,
                Tag::getAsString,
                (nbt, value) -> nbt.putString(name, value),
                getter
        );
    }

    public ExSerialize<T> addBooleanField(String name, BiConsumer<T, Boolean> setter) {
        return addBooleanField(name, t -> false, setter);
    }

    public ExSerialize<T> addBooleanField(String name,
                                      Function<T, Boolean> getter,
                                      BiConsumer<T, Boolean> setter) {
        return addField(name,
                json -> getJsonPrimitive(json).getAsBoolean(),
                setter,
                tag ->  ((ByteTag) tag).getAsByte() !=0,
                (nbt, value) -> nbt.putBoolean(name, value),
                getter
        );
    }
    public ExSerialize<T> addIntField(String name, BiConsumer<T, Integer> setter) {
        return addIntField(name, t -> 0, setter);
    }

    public ExSerialize<T> addIntField(String name,
                                      Function<T, Integer> getter,
                                      BiConsumer<T, Integer> setter) {
        return addField(name,
                json -> getJsonPrimitive(json).getAsInt(),
                setter,
                tag -> ((IntTag) tag).getAsInt(),
                (nbt, value) -> nbt.putInt(name, value),
                getter
        );
    }

    public ExSerialize<T> addFloatField(String name, BiConsumer<T, Float> setter) {
        return addFloatField(name, t -> 0f, setter);
    }
    public ExSerialize<T> addDoubleField(String name, BiConsumer<T, Double> setter) {
        return addDoubleField(name, t -> 0d, setter);
    }
    public ExSerialize<T> addDoubleField(String name,
                                      Function<T, Double> getter,
                                      BiConsumer<T, Double> setter) {
        return addField(name,
                json -> getJsonPrimitive(json).getAsDouble(),
                setter,
                tag -> ((DoubleTag) tag).getAsDouble(),
                (nbt, value) -> nbt.putDouble(name, value),
                getter
        );
    }


    public ExSerialize<T> addFloatField(String name,
                                        Function<T, Float> getter,
                                        BiConsumer<T, Float> setter) {
        return addField(name,
                json -> getJsonPrimitive(json).getAsFloat(),
                setter,
                tag -> ((FloatTag) tag).getAsFloat(),
                (nbt, value) -> nbt.putFloat(name, value),
                getter
        );
    }

    // 复杂类型字段
    public ExSerialize<T> addResourceLocationField(String name, BiConsumer<T, ResourceLocation> setter) {
        return addResourceLocationField(name, t -> null, setter);
    }

    public ExSerialize<T> addResourceLocationField(String name,
                                                   Function<T, ResourceLocation> getter,
                                                   BiConsumer<T, ResourceLocation> setter) {
        return addField(name,
                json -> new ResourceLocation(json.getAsString()),
                setter,
                tag -> new ResourceLocation(tag.getAsString()),
                (nbt, value) -> nbt.putString(name, value.toString()),
                getter
        );
    }

    public ExSerialize<T> addFloatMapField(String name, BiConsumer<T, Map<String, Float>> setter) {
        return addFloatMapField(name, t -> Collections.emptyMap(), setter);
    }

    public ExSerialize<T> addFloatMapField(String name,
                                           Function<T, Map<String, Float>> getter,
                                           BiConsumer<T, Map<String, Float>> setter) {
        return addField(name,
                json -> parseFloatMap(json.getAsJsonObject()),
                setter,
                tag -> parseNbtFloatMap((CompoundTag) tag),
                (nbt, value) -> serializeNbtFloatMap(nbt, name, value),
                getter
        );
    }

    public ExSerialize<T> addStringListField(String name, BiConsumer<T, List<String>> setter) {
        return addStringListField(name, t -> Collections.emptyList(), setter);
    }

    public ExSerialize<T> addStringListField(String name,
                                             Function<T, List<String>> getter,
                                             BiConsumer<T, List<String>> setter) {
        return addField(name,
                json -> parseStringList(json.getAsJsonArray()),
                setter,
                tag -> parseNbtStringList((ListTag) tag),
                (nbt, value) -> serializeNbtStringList(nbt, name, value),
                getter
        );
    }

    public ExSerialize<T> addJsonObjectField(String name, BiConsumer<T, JsonObject> setter) {
        return addJsonObjectField(name, t -> new JsonObject(), setter);
    }

    public ExSerialize<T> addJsonObjectField(String name,
                                             Function<T, JsonObject> getter,
                                             BiConsumer<T, JsonObject> setter) {
        return addField(name,
                JsonElement::getAsJsonObject,
                setter,
                tag -> parseNbtJsonObject((CompoundTag) tag),
                (nbt, value) -> nbt.put(name, serializeJsonObjectToNbt(value)),
                getter
        );
    }

    public ExSerialize<T> addJsonObjectList(String name, BiConsumer<T, List<JsonObject>> setter) {
        return addJsonObjectList(name, t -> Collections.emptyList(), setter);
    }

    public ExSerialize<T> addJsonObjectList(String name,
                                            Function<T, List<JsonObject>> getter,
                                            BiConsumer<T, List<JsonObject>> setter) {
        return addField(name,
                json -> parseJsonObjectList(json.getAsJsonArray()),
                setter,
                tag -> parseNbtJsonObjectList((ListTag) tag),
                (nbt, value) -> serializeNbtJsonObjectList(nbt, name, value),
                getter
        );
    }

    // 新增String Map字段支持
    public ExSerialize<T> addStringMapField(String name, BiConsumer<T, Map<String, String>> setter) {
        return addStringMapField(name, t -> Collections.emptyMap(), setter);
    }

    public ExSerialize<T> addStringMapField(String name,
                                           Function<T, Map<String, String>> getter,
                                           BiConsumer<T, Map<String, String>> setter) {
        return addField(name,
                json -> parseStringMap(json.getAsJsonObject()),
                setter,
                tag -> parseNbtStringMap((CompoundTag) tag),
                (nbt, value) -> serializeNbtStringMap(nbt, name, value),
                getter
        );
    }
    // endregion

    // region 核心序列化/反序列化方法
    public JsonObject toSingleJson(T object) {
        JsonObject json = new JsonObject();
        fields.forEach(field -> {
            try {
                JsonElement value = serializeFieldToJson(field, object);
                if (value != null && !value.isJsonNull()) {
                    json.add(field.name, value);
                }
            } catch (Exception e) {
                getError("Field '{}' serialization failed: {}", field.name, e);
            }
        });
        subclasses.forEach(
                (tFunction, exSerialize) -> {
                    JsonObject singleJson = exSerialize.toSingleJson(tFunction.getA().apply(object));
                    singleJson.asMap().forEach(json::add);
                }
        );
        return json;
    }


    public JsonArray toJson(List<T> objects) {
        JsonArray array = new JsonArray();
        objects.forEach(obj -> array.add(toSingleJson(obj)));
        return array;
    }

    public T fromJsonSingle(JsonObject json) {
        return createInstance("", json);
    }
    public List<T> fromJson(JsonObject json) {
        List<T> result = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            if (!entry.getValue().isJsonObject())continue;
            result.add(createInstance(
                    entry.getKey(),
                    entry.getValue().getAsJsonObject()
            ));
        }
        return result;
    }
    public List<T> fromJson(JsonArray array) {
        return fromJson(array, obj -> obj.get("id").getAsString());
    }

    public List<T> fromJson(JsonArray array, Function<JsonObject, String> idExtractor) {
        List<T> result = new ArrayList<>();
        array.forEach(element -> {
            JsonObject json =null;
            if (element.isJsonObject()) {
                try {
                    json = element.getAsJsonObject();
                } catch (Exception ignored) {
                }
            }
            if (json!=null) {
                result.add(createInstance(idExtractor.apply(json), json));
            }
        });

        return result;
    }

    public CompoundTag toNbt(T object) {
        CompoundTag tag = new CompoundTag();
        subclasses.forEach(
                (tFunction, exSerialize) -> {
                    tag.merge(exSerialize.toNbt(object));
                }
        );
        fields.forEach(field -> {
            if (field.nbtSerializer != null) {
                if ( !onlyReadField.contains(field.name)) {
                    try {
                        field.serializeToNbt(object, tag);
                    } catch (Exception e) {
                        getError("NBT serialization failed for field '{}': {}", field.name, e);
                    }
                }
            }
        });
        if (autoIdSetter!=null) tag.putString("id", autoIdSetter.getter.apply(object));
        return tag;
    }

    private static <T> void getError(String s, String field, Exception e) {
        Exmodifier.LOGGER.Logger.error(s,
                field, e.getMessage());
    }

    public CompoundTag toSyncNbt(T object) {
        CompoundTag tag = new CompoundTag();
        fields.forEach(field -> {
            if (field.nbtSerializer != null) {
                if (!dontSyncToClientField.contains(field.name) && !onlyReadField.contains(field.name)) {
                    try {
                        field.serializeToNbt(object, tag);
                    } catch (Exception e) {
                        getError("NBT serialization failed for field '{}': {}", field.name, e);
                    }
                }
            }
        });
        if (autoIdSetter!=null) tag.putString("id", autoIdSetter.getter.apply(object));
        return tag;
    }
    public T fromNbt(CompoundTag tag) {
        T instance = constructor.get();
        fields.forEach(field -> {
            if (field.nbtDeserializer != null && tag.contains(field.name)) {
                try {
                    field.deserializeFromNbt(tag, instance);
                } catch (Exception e) {
                    getError("NBT deserialization failed for field '{}': {}", field.name, e);
                }
            }
        });
        if (autoIdSetter!=null) autoIdSetter.autoIdSetter.accept(instance, tag.getString("id"));
        subclasses.forEach(
                (tFunction, exSerialize) -> {
                   tFunction.getB().accept(instance,exSerialize.fromNbt(tag));
                }
        );
        return instance;
    }
    // endregion

    // region 实现细节
    private <V> ExSerialize<T> addField(
            String name,
            Function<JsonElement, V> jsonDeserializer,
            BiConsumer<T, V> setter,
            Function<Tag, V> nbtDeserializer,
            BiConsumer<CompoundTag, V> nbtSerializer,
            Function<T, V> getter
    ) {
        lastField = name;
        fields.add(new FieldHandler<>(
                name,
                jsonDeserializer,
                setter,
                (obj, tag) -> nbtSerializer.accept(tag, getter.apply(obj)),
                nbtDeserializer
        ));
        return this;
    }
    @SuppressWarnings("unchecked")
    private T createInstance(String id, JsonObject json) {
        T instance = constructor.get();
        if (autoIdSetter != null) {
            autoIdSetter.autoIdSetter.accept(instance, id);
        }
        fields.forEach(field -> {
            if (json.has(field.name)) {
                try {
                    JsonElement element = json.get(field.name);

                    // 显式类型转换处理
                    FieldHandler<T, Object> typedHandler = (FieldHandler<T, Object>) field;
                    Object value = typedHandler.deserializer.apply(element);

                    // 使用安全类型转换
                    BiConsumer<T, Object> setter = typedHandler.setter;
                    setter.accept(instance, value);

                } catch (Exception e) {
                    getError("Field '{}' parsing failed: {}", field.name, e);
                }
            }
        });
        subclasses.forEach(
                (tFunction, exSerialize) -> {
                    tFunction.getB().accept(instance,exSerialize.fromJson(json));
                }
        );
        return instance;
    }

    private <V> JsonElement serializeFieldToJson(FieldHandler<T, V> field, T object) {
        try {
            if (field.nbtSerializer != null) {
                CompoundTag tempTag = new CompoundTag();
                field.nbtSerializer.accept(object, tempTag);
                return parseNbtValueToJson(tempTag.get(field.name));
            }
            return JsonNull.INSTANCE;
        } catch (Exception e) {
            getError("JSON serialization failed for field '{}': {}", field.name, e);
            return JsonNull.INSTANCE;
        }
    }
    // endregion

    // region NBT/JSON转换工具
    private CompoundTag serializeJsonObjectToNbt(JsonObject json) {
        CompoundTag tag = new CompoundTag();
        json.entrySet().forEach(entry ->
                tag.put(entry.getKey(), serializeJsonValueToNbt(entry.getValue()))
        );
        return tag;
    }

    private Tag serializeJsonValueToNbt(JsonElement json) {
        if (json.isJsonPrimitive()) {
            JsonPrimitive prim = json.getAsJsonPrimitive();
            if (prim.isString()) return StringTag.valueOf(prim.getAsString());
            if (prim.isNumber()) {
                float num = prim.getAsFloat();
                return num == (int) num ?
                        IntTag.valueOf(prim.getAsInt()) :
                        FloatTag.valueOf(num);
            }
        }
        if (json.isJsonObject()) return serializeJsonObjectToNbt(json.getAsJsonObject());
        if (json.isJsonArray()) {
            ListTag list = new ListTag();
            json.getAsJsonArray().forEach(e -> list.add(serializeJsonValueToNbt(e)));
            return list;
        }
        return new CompoundTag();
    }

    private JsonObject parseNbtJsonObject(CompoundTag tag) {
        JsonObject json = new JsonObject();
        tag.getAllKeys().forEach(key ->
                json.add(key, parseNbtValueToJson(tag.get(key)))
        );
        return json;
    }

    private JsonElement parseNbtValueToJson(Tag tag) {
        if (tag instanceof StringTag) return new JsonPrimitive(tag.getAsString());
        if (tag instanceof IntTag) return new JsonPrimitive(((IntTag) tag).getAsInt());
        if (tag instanceof FloatTag) return new JsonPrimitive(((FloatTag) tag).getAsFloat());
        if (tag instanceof CompoundTag) return parseNbtJsonObject((CompoundTag) tag);
        if (tag instanceof ListTag) {
            JsonArray array = new JsonArray();
            ((ListTag) tag).forEach(t -> array.add(parseNbtValueToJson(t)));
            return array;
        }
        return JsonNull.INSTANCE;
    }

    private Map<String, Float> parseNbtFloatMap(CompoundTag tag) {
        return tag.getAllKeys().stream()
                .collect(Collectors.toMap(
                        k -> k,
                        k -> tag.getFloat(k)
                ));
    }

    private void serializeNbtFloatMap(CompoundTag parent, String name, Map<String, Float> map) {
        CompoundTag mapTag = new CompoundTag();
        map.forEach(mapTag::putFloat);
        parent.put(name, mapTag);
    }

    private List<String> parseNbtStringList(ListTag listTag) {
        List<String> list = new ArrayList<>();
        listTag.forEach(t -> list.add(t.getAsString()));
        return list;
    }

    private void serializeNbtStringList(CompoundTag parent, String name, List<String> list) {
        ListTag listTag = new ListTag();
        list.forEach(s -> listTag.add(StringTag.valueOf(s)));
        parent.put(name, listTag);
    }

    private List<JsonObject> parseNbtJsonObjectList(ListTag listTag) {
        List<JsonObject> list = new ArrayList<>();
        listTag.forEach(t -> list.add(parseNbtJsonObject((CompoundTag) t)));
        return list;
    }

    private void serializeNbtJsonObjectList(CompoundTag parent, String name, List<JsonObject> list) {
        ListTag listTag = new ListTag();
        list.forEach(json -> listTag.add(serializeJsonObjectToNbt(json)));
        parent.put(name, listTag);
    }
    // endregion

    // region 辅助方法
    private JsonPrimitive getJsonPrimitive(JsonElement element) {
        if (!element.isJsonPrimitive()) {
            throw new JsonSyntaxException("Expected primitive value");
        }
        return element.getAsJsonPrimitive();
    }

    private JsonObject getJsonObject(JsonElement element) {
        if (!element.isJsonObject()) {
            throw new JsonSyntaxException("Expected JSON object");
        }
        return element.getAsJsonObject();
    }

    private JsonArray getJsonArray(JsonElement element) {
        if (!element.isJsonArray()) {
            throw new JsonSyntaxException("Expected JSON array");
        }
        return element.getAsJsonArray();
    }

    private Map<String, Float> parseFloatMap(JsonObject json) {
        return json.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().getAsFloat()
                ));
    }

    private List<String> parseStringList(JsonArray array) {
        List<String> list = new ArrayList<>();
        array.forEach(e -> list.add(e.getAsString()));
        return list;
    }

    private List<JsonObject> parseJsonObjectList(JsonArray array) {
        List<JsonObject> list = new ArrayList<>();
        array.forEach(e -> list.add(e.getAsJsonObject()));
        return list;
    }

    private Map<String, String> parseStringMap(JsonObject json) {
        return json.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().getAsString()
                ));
    }

    private Map<String, String> parseNbtStringMap(CompoundTag tag) {
        return tag.getAllKeys().stream()
                .collect(Collectors.toMap(
                        k -> k,
                        k -> tag.getString(k)
                ));
    }

    private void serializeNbtStringMap(CompoundTag parent, String name, Map<String, String> map) {
        CompoundTag mapTag = new CompoundTag();
        map.forEach(mapTag::putString);
        parent.put(name, mapTag);
    }

    private Map<Integer, String> parseIntStringMap(JsonObject json) {
        return json.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> Integer.parseInt(e.getKey()),
                        e -> e.getValue().getAsString()
                ));
    }

    private Map<Integer, String> parseNbtIntStringMap(CompoundTag tag) {
        return tag.getAllKeys().stream()
                .collect(Collectors.toMap(
                        k -> Integer.parseInt(k),
                        k -> tag.getString(k)
                ));
    }

    private void serializeNbtIntStringMap(CompoundTag parent, String name, Map<Integer, String> map) {
        CompoundTag mapTag = new CompoundTag();
        map.forEach((k, v) -> mapTag.putString(k.toString(), v));
        parent.put(name, mapTag);
    }
    // endregion

    public static class FieldHandler<T, V> {
        final String name;
        final Function<JsonElement, V> deserializer;
        final BiConsumer<T, V> setter;
        final BiConsumer<T, CompoundTag> nbtSerializer;
        final Function<Tag, V> nbtDeserializer;

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
                Tag nbtValue = tag.get(name);
                V value = nbtDeserializer.apply(nbtValue);
                setter.accept(object, value);
            }
        }
    }
}