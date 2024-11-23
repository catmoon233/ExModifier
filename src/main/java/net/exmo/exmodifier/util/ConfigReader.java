package net.exmo.exmodifier.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ConfigReader {

    public static <T> T readConfig(JsonObject json, Class<T> clazz) throws IllegalAccessException, InstantiationException {
        T instance = clazz.newInstance();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(JsonField.class)) {
                JsonField annotation = field.getAnnotation(JsonField.class);
                String jsonKey = annotation.value();

                if (json.has(jsonKey)) {
                    JsonElement jsonElement = json.get(jsonKey);
                    field.setAccessible(true);
                    setFieldValue(instance, field, jsonElement);
                }
            }
        }

        return instance;
    }

    private static void setFieldValue(Object instance, Field field, JsonElement jsonElement) throws IllegalAccessException {
        if (field.getType().equals(String.class)) {
            field.set(instance, jsonElement.getAsString());
        } else if (field.getType().equals(int.class)) {
            field.set(instance, jsonElement.getAsInt());
        } else if (field.getType().equals(boolean.class)) {
            field.set(instance, jsonElement.getAsBoolean());
        }else if (field.getType().equals(float.class)){
            field.set(instance, jsonElement.getAsFloat());
        }else if (field.getType().equals(double.class)){
            field.set(instance, jsonElement.getAsDouble());
        }else if (field.getType().equals(long.class)){
            field.set(instance, jsonElement.getAsLong());
        }else if (field.getType().equals(short.class)){
            field.set(instance, jsonElement.getAsShort());
        }else if (field.getType().equals(byte.class)){
            field.set(instance, jsonElement.getAsByte());
        }else if (field.getType().equals(ArrayList.class)){
            for (JsonElement element : jsonElement.getAsJsonArray())
                ((ArrayList)field.get(instance)).add(element.getAsString());
        }

        // Add more types as needed...
    }
}