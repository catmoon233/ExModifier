package net.exmo.exmodifier.pack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.List;

/** JSON edits shared by the command chain and the visual editor. */
public final class PackMutations {
    private PackMutations() {}

    public static JsonObject object(JsonElement element) {
        if (element != null && element.isJsonObject()) return element.getAsJsonObject();
        return new JsonObject();
    }

    public static void set(JsonObject root, String path, JsonElement value) {
        ensureSuitLevel(root, path);
        Slot slot = slot(root, path, true);
        if (slot == null) return;
        slot.put(value);
    }

    public static void add(JsonObject root, String path, JsonElement value) {
        ensureSuitLevel(root, path);
        JsonArray array = array(root, path, true);
        if (array == null || value == null) return;
        String text = value.isJsonPrimitive() ? value.getAsString() : value.toString();
        for (JsonElement existing : array) {
            if (existing.isJsonPrimitive() && existing.getAsString().equals(text)) return;
        }
        array.add(value);
    }

    public static boolean remove(JsonObject root, String path, String valueOrIndex) {
        JsonArray array = array(root, path, false);
        if (array == null || valueOrIndex == null) return false;
        Integer index = tryIndex(valueOrIndex);
        if (index != null && index >= 0 && index < array.size() && valueOrIndex.matches("\\d+")) {
            array.remove(index);
            return true;
        }
        for (int i = 0; i < array.size(); i++) {
            JsonElement element = array.get(i);
            if (element.isJsonPrimitive() && element.getAsString().equals(valueOrIndex)) {
                array.remove(i);
                return true;
            }
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                if (valueOrIndex.equals(text(object, "attribute")) || valueOrIndex.equals(text(object, "effect"))) {
                    array.remove(i);
                    return true;
                }
            }
        }
        return false;
    }

    public static void putMap(JsonObject root, String path, String key, JsonElement value) {
        ensureSuitLevel(root, path);
        JsonObject map = map(root, path, true);
        if (map == null || key == null || key.isBlank()) return;
        map.add(key, value);
    }

    public static boolean removeMap(JsonObject root, String path, String key) {
        JsonObject map = map(root, path, false);
        if (map == null || key == null || !map.has(key)) return false;
        map.remove(key);
        return true;
    }

    public static void addAttribute(JsonObject root, String path, String attribute, double amount, double perLevel, String operation, String expression) {
        ensureSuitLevel(root, path);
        JsonArray array = array(root, path, true);
        if (array == null) return;
        JsonObject spec = new JsonObject();
        spec.addProperty("attribute", attribute);
        spec.addProperty("amount", amount);
        if (perLevel != 0.0D) spec.addProperty("amount_per_level", perLevel);
        if (operation != null && !operation.isBlank()) spec.addProperty("operation", operation);
        if (expression != null && !expression.isBlank()) spec.addProperty("expression", expression);
        array.add(spec);
    }

    public static void addEffect(JsonObject root, String path, String effect, int duration, int amplifier, boolean ambient, boolean visible) {
        ensureSuitLevel(root, path);
        JsonArray array = array(root, path, true);
        if (array == null) return;
        JsonObject spec = new JsonObject();
        spec.addProperty("effect", effect);
        spec.addProperty("duration", Math.max(1, duration));
        spec.addProperty("amplifier", Math.max(0, amplifier));
        spec.addProperty("ambient", ambient);
        spec.addProperty("visible", visible);
        array.add(spec);
    }

    public static void addSuitLevel(JsonObject root, int pieces, String trigger) {
        JsonArray levels = array(root, "levels", true);
        JsonObject level = new JsonObject();
        level.addProperty("pieces", Math.max(1, pieces));
        level.addProperty("trigger", trigger == null || trigger.isBlank() ? "exmodifier:tick" : trigger);
        levels.add(level);
    }

    public static boolean setScalar(JsonObject root, PackField field, String path, String raw) {
        if (field == null || raw == null) return false;
        JsonElement value = scalar(field, raw);
        if (value == null) return false;
        set(root, path, value);
        return true;
    }

    public static JsonElement scalar(PackField field, String raw) {
        String text = raw.trim();
        try {
            return switch (field.type()) {
                case INT -> new JsonPrimitive(Integer.parseInt(text));
                case FLOAT -> new JsonPrimitive(Float.parseFloat(text));
                case DOUBLE -> new JsonPrimitive(Double.parseDouble(text));
                case BOOL -> new JsonPrimitive(Boolean.parseBoolean(text));
                case COLOR -> {
                    Integer color = PackIds.color(text);
                    yield color == null ? null : new JsonPrimitive(color);
                }
                case STRING, ID -> text.isEmpty() ? null : new JsonPrimitive(text);
                default -> null;
            };
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static List<String> strings(JsonObject root, String path) {
        JsonArray array = array(root, path, false);
        if (array == null) return List.of();
        List<String> values = new ArrayList<>();
        for (JsonElement element : array) {
            if (element.isJsonPrimitive()) values.add(element.getAsString());
        }
        return values;
    }

    private static void ensureSuitLevel(JsonObject root, String path) {
        if (path == null || !path.startsWith("levels.")) return;
        String[] parts = path.split("\\.");
        if (parts.length < 2 || !parts[1].matches("\\d+")) return;
        int index = Integer.parseInt(parts[1]);
        JsonArray levels = array(root, "levels", true);
        while (levels.size() <= index) {
            JsonObject level = new JsonObject();
            level.addProperty("pieces", levels.size() + 1);
            level.addProperty("trigger", "exmodifier:tick");
            levels.add(level);
        }
        if (!levels.get(index).isJsonObject()) {
            JsonObject level = new JsonObject();
            level.addProperty("pieces", index + 1);
            level.addProperty("trigger", "exmodifier:tick");
            levels.set(index, level);
        } else {
            JsonObject level = levels.get(index).getAsJsonObject();
            if (!level.has("pieces")) level.addProperty("pieces", index + 1);
            if (!level.has("trigger")) level.addProperty("trigger", "exmodifier:tick");
        }
    }

    private static JsonArray array(JsonObject root, String path, boolean create) {
        Slot slot = slot(root, path, create);
        if (slot == null) return null;
        JsonElement current = slot.get();
        if (current == null || current.isJsonNull() || !current.isJsonArray()) {
            if (!create) return null;
            current = new JsonArray();
            slot.put(current);
        }
        return current.getAsJsonArray();
    }

    private static JsonObject map(JsonObject root, String path, boolean create) {
        Slot slot = slot(root, path, create);
        if (slot == null) return null;
        JsonElement current = slot.get();
        if (current == null || current.isJsonNull() || !current.isJsonObject()) {
            if (!create) return null;
            current = new JsonObject();
            slot.put(current);
        }
        return current.getAsJsonObject();
    }

    private static Slot slot(JsonObject root, String path, boolean create) {
        if (root == null || path == null || path.isBlank()) return null;
        String[] parts = path.split("\\.");
        JsonElement current = root;
        for (int i = 0; i < parts.length - 1; i++) {
            current = descend(current, parts[i], parts[i + 1], create);
            if (current == null) return null;
        }
        String last = parts[parts.length - 1];
        if (current.isJsonArray() && last.matches("\\d+")) {
            JsonArray array = current.getAsJsonArray();
            int index = Integer.parseInt(last);
            return new Slot(null, array, index);
        }
        if (!current.isJsonObject()) return null;
        return new Slot(current.getAsJsonObject(), null, last);
    }

    private static JsonElement descend(JsonElement current, String part, String next, boolean create) {
        boolean nextIndex = next.matches("\\d+");
        if (part.matches("\\d+")) {
            if (current == null || !current.isJsonArray()) return null;
            JsonArray array = current.getAsJsonArray();
            int index = Integer.parseInt(part);
            if (index < 0) return null;
            if (index >= array.size()) {
                if (!create) return null;
                while (array.size() <= index) array.add(nextIndex ? new JsonArray() : new JsonObject());
            }
            return array.get(index);
        }
        if (current == null || !current.isJsonObject()) return null;
        JsonObject object = current.getAsJsonObject();
        if (!object.has(part) || object.get(part).isJsonNull()) {
            if (!create) return null;
            object.add(part, nextIndex ? new JsonArray() : new JsonObject());
        }
        return object.get(part);
    }

    private static Integer tryIndex(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String text(JsonObject object, String key) {
        return object.has(key) && object.get(key).isJsonPrimitive() ? object.get(key).getAsString() : "";
    }

    private static final class Slot {
        private final JsonObject object;
        private final JsonArray array;
        private final int index;
        private final String key;

        private Slot(JsonObject object, JsonArray array, int index) {
            this.object = object;
            this.array = array;
            this.index = index;
            this.key = null;
        }

        private Slot(JsonObject object, JsonArray array, String key) {
            this.object = object;
            this.array = array;
            this.index = -1;
            this.key = key;
        }

        private JsonElement get() {
            if (array != null) {
                return index >= 0 && index < array.size() ? array.get(index) : null;
            }
            return object.has(key) ? object.get(key) : null;
        }

        private void put(JsonElement value) {
            if (array != null) {
                while (array.size() <= index) array.add(new JsonObject());
                array.set(index, value);
                return;
            }
            object.add(key, value);
        }
    }
}
