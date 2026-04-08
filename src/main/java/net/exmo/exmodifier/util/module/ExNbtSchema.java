package net.exmo.exmodifier.util.module;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 声明式NBT字段定义，可同时处理NBT和JSON的读写。
 * 参考ExSerialize，但保持简洁，面向Helper的NBT操作场景。
 * 可向上移植到更高MC版本。
 *
 * @param <T> 目标对象类型
 */
public class ExNbtSchema<T> {
    private final List<NbtField<T, ?>> fields = new ArrayList<>();

    public static <T> ExNbtSchema<T> create() {
        return new ExNbtSchema<>();
    }

    // region 字段定义API

    public ExNbtSchema<T> defineString(String key, Function<T, String> getter, BiConsumer<T, String> setter) {
        fields.add(new NbtField<>(key, getter, setter,
                (tag, k) -> tag.getString(k),
                (tag, k, v) -> tag.putString(k, (String) v),
                json -> json.getAsString(),
                NbtField.FieldType.STRING));
        return this;
    }

    public ExNbtSchema<T> defineInt(String key, Function<T, Integer> getter, BiConsumer<T, Integer> setter) {
        fields.add(new NbtField<>(key, getter, setter,
                (tag, k) -> tag.getInt(k),
                (tag, k, v) -> tag.putInt(k, (Integer) v),
                json -> json.getAsInt(),
                NbtField.FieldType.INT));
        return this;
    }

    public ExNbtSchema<T> defineDouble(String key, Function<T, Double> getter, BiConsumer<T, Double> setter) {
        fields.add(new NbtField<>(key, getter, setter,
                (tag, k) -> tag.getDouble(k),
                (tag, k, v) -> tag.putDouble(k, (Double) v),
                json -> json.getAsDouble(),
                NbtField.FieldType.DOUBLE));
        return this;
    }

    public ExNbtSchema<T> defineFloat(String key, Function<T, Float> getter, BiConsumer<T, Float> setter) {
        fields.add(new NbtField<>(key, getter, setter,
                (tag, k) -> tag.getFloat(k),
                (tag, k, v) -> tag.putFloat(k, (Float) v),
                json -> json.getAsFloat(),
                NbtField.FieldType.FLOAT));
        return this;
    }

    public ExNbtSchema<T> defineBoolean(String key, Function<T, Boolean> getter, BiConsumer<T, Boolean> setter) {
        fields.add(new NbtField<>(key, getter, setter,
                (tag, k) -> tag.getBoolean(k),
                (tag, k, v) -> tag.putBoolean(k, (Boolean) v),
                json -> json.getAsBoolean(),
                NbtField.FieldType.BOOLEAN));
        return this;
    }

    public ExNbtSchema<T> defineResourceLocation(String key,
                                                  Function<T, ResourceLocation> getter,
                                                  BiConsumer<T, ResourceLocation> setter) {
        fields.add(new NbtField<>(key, getter, setter,
                (tag, k) -> ResourceLocation.tryParse(tag.getString(k)),
                (tag, k, v) -> tag.putString(k, v != null ? v.toString() : ""),
                json -> ResourceLocation.tryParse(json.getAsString()),
                NbtField.FieldType.RESOURCE_LOCATION));
        return this;
    }

    public ExNbtSchema<T> defineStringList(String key,
                                           Function<T, List<String>> getter,
                                           BiConsumer<T, List<String>> setter) {
        fields.add(new NbtField<>(key, getter, setter,
                (tag, k) -> {
                    ListTag listTag = tag.getList(k, Tag.TAG_STRING);
                    List<String> list = new ArrayList<>();
                    for (int i = 0; i < listTag.size(); i++) {
                        list.add(listTag.getString(i));
                    }
                    return list;
                },
                (tag, k, v) -> {
                    ListTag listTag = new ListTag();
                    if (v instanceof List<?> list) {
                        for (Object s : list) {
                            listTag.add(StringTag.valueOf(s.toString()));
                        }
                    }
                    tag.put(k, listTag);
                },
                json -> {
                    List<String> list = new ArrayList<>();
                    if (json.isJsonArray()) {
                        json.getAsJsonArray().forEach(e -> list.add(e.getAsString()));
                    }
                    return list;
                },
                NbtField.FieldType.STRING_LIST));
        return this;
    }

    public <V> ExNbtSchema<T> defineCompound(String key,
                                             Function<T, V> getter,
                                             BiConsumer<T, V> setter,
                                             ExNbtSchema<V> subSchema,
                                             java.util.function.Supplier<V> subConstructor) {
        fields.add(new NbtField<>(key, getter, setter,
                (tag, k) -> {
                    CompoundTag sub = tag.getCompound(k);
                    V instance = subConstructor.get();
                    subSchema.readFromNbt(sub, instance);
                    return instance;
                },
                (tag, k, v) -> {
                    if (v != null) {
                        @SuppressWarnings("unchecked")
                        V typed = (V) v;
                        tag.put(k, subSchema.writeToNbt(typed));
                    }
                },
                json -> {
                    V instance = subConstructor.get();
                    if (json.isJsonObject()) {
                        subSchema.readFromJson(json.getAsJsonObject(), instance);
                    }
                    return instance;
                },
                NbtField.FieldType.COMPOUND));
        return this;
    }

    public <V> ExNbtSchema<T> defineList(String key,
                                         Function<T, List<V>> getter,
                                         BiConsumer<T, List<V>> setter,
                                         ExNbtSchema<V> itemSchema,
                                         java.util.function.Supplier<V> itemConstructor) {
        fields.add(new NbtField<>(key, getter, setter,
                (tag, k) -> {
                    ListTag listTag = tag.getList(k, Tag.TAG_COMPOUND);
                    List<V> list = new ArrayList<>();
                    for (int i = 0; i < listTag.size(); i++) {
                        V item = itemConstructor.get();
                        itemSchema.readFromNbt(listTag.getCompound(i), item);
                        list.add(item);
                    }
                    return list;
                },
                (tag, k, v) -> {
                    ListTag listTag = new ListTag();
                    if (v instanceof List<?> list) {
                        for (Object item : list) {
                            @SuppressWarnings("unchecked")
                            V typed = (V) item;
                            listTag.add(itemSchema.writeToNbt(typed));
                        }
                    }
                    tag.put(k, listTag);
                },
                json -> {
                    List<V> list = new ArrayList<>();
                    if (json.isJsonArray()) {
                        json.getAsJsonArray().forEach(e -> {
                            V item = itemConstructor.get();
                            if (e.isJsonObject()) {
                                itemSchema.readFromJson(e.getAsJsonObject(), item);
                            }
                            list.add(item);
                        });
                    }
                    return list;
                },
                NbtField.FieldType.LIST));
        return this;
    }

    // endregion

    // region NBT读写

    @SuppressWarnings("unchecked")
    public void readFromNbt(CompoundTag tag, T target) {
        for (NbtField<T, ?> field : fields) {
            if (tag.contains(field.key)) {
                Object value = field.nbtReader.read(tag, field.key);
                ((BiConsumer<T, Object>) field.setter).accept(target, value);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public CompoundTag writeToNbt(T source) {
        CompoundTag tag = new CompoundTag();
        for (NbtField<T, ?> field : fields) {
            Object value = ((Function<T, Object>) field.getter).apply(source);
            if (value != null) {
                field.nbtWriter.write(tag, field.key, value);
            }
        }
        return tag;
    }

    public void writeToNbt(T source, CompoundTag tag) {
        for (NbtField<T, ?> field : fields) {
            @SuppressWarnings("unchecked")
            Object value = ((Function<T, Object>) field.getter).apply(source);
            if (value != null) {
                field.nbtWriter.write(tag, field.key, value);
            }
        }
    }

    // endregion

    // region JSON读写

    @SuppressWarnings("unchecked")
    public void readFromJson(JsonObject json, T target) {
        for (NbtField<T, ?> field : fields) {
            if (json.has(field.key)) {
                Object value = field.jsonReader.apply(json.get(field.key));
                ((BiConsumer<T, Object>) field.setter).accept(target, value);
            }
        }
    }

    // endregion

    // region 字段查找

    public List<String> getFieldNames() {
        return fields.stream().map(f -> f.key).toList();
    }

    // endregion

    /**
     * 单个字段定义
     */
    public static class NbtField<T, V> {
        public final String key;
        final Function<T, V> getter;
        final BiConsumer<T, V> setter;
        final NbtReader nbtReader;
        final NbtWriter nbtWriter;
        final Function<JsonElement, Object> jsonReader;
        final FieldType fieldType;

        NbtField(String key, Function<T, V> getter, BiConsumer<T, V> setter,
                 NbtReader nbtReader, NbtWriter nbtWriter,
                 Function<JsonElement, Object> jsonReader, FieldType fieldType) {
            this.key = key;
            this.getter = getter;
            this.setter = setter;
            this.nbtReader = nbtReader;
            this.nbtWriter = nbtWriter;
            this.jsonReader = jsonReader;
            this.fieldType = fieldType;
        }

        enum FieldType {
            STRING, INT, DOUBLE, FLOAT, BOOLEAN, RESOURCE_LOCATION,
            STRING_LIST, COMPOUND, LIST
        }

        @FunctionalInterface
        interface NbtReader {
            Object read(CompoundTag tag, String key);
        }

        @FunctionalInterface
        interface NbtWriter {
            void write(CompoundTag tag, String key, Object value);
        }
    }
}
