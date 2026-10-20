package net.exmo.exmodifier.pack;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Merges registered ids with ids that exist only inside the pack being edited. */
public final class PackSuggestions {
    public enum Origin {
        REGISTERED, PACK, BOTH, OTHER
    }

    public record Hit(String id, Origin origin) {}

    private PackSuggestions() {}

    public static List<String> operations() {
        return List.of("add_value", "add_multiplied_base", "add_multiplied_total");
    }

    public static List<String> upEvents() {
        return List.of("KILL", "ATTACK", "ON_HURT");
    }

    public static List<String> triggers() {
        return List.of(
                "exmodifier:tick", "exmodifier:attack", "exmodifier:on_hurt", "exmodifier:kill",
                "exmodifier:die", "exmodifier:swing", "exmodifier:crit", "exmodifier:dig",
                "exmodifier:dodge", "exmodifier:swim", "exmodifier:in_lava", "exmodifier:shoot");
    }

    public static List<String> booleans() {
        return List.of("true", "false");
    }

    public static List<Hit> merge(Collection<String> registered, Collection<String> pack, Collection<String> other, String typed, int limit) {
        Map<String, Origin> map = new LinkedHashMap<>();
        if (registered != null) {
            for (String id : registered) put(map, id, Origin.REGISTERED);
        }
        if (other != null) {
            for (String id : other) put(map, id, Origin.OTHER);
        }
        if (pack != null) {
            for (String id : pack) put(map, id, Origin.PACK);
        }
        String needle = typed == null ? "" : typed.trim().toLowerCase(Locale.ROOT);
        int cap = limit <= 0 ? Integer.MAX_VALUE : limit;
        return map.entrySet().stream()
                .filter(entry -> matches(entry.getKey(), needle))
                .sorted(Comparator
                        .comparingInt((Map.Entry<String, Origin> entry) -> rank(entry.getKey(), entry.getValue(), needle))
                        .thenComparing(Map.Entry::getKey))
                .limit(cap)
                .map(entry -> new Hit(entry.getKey(), entry.getValue()))
                .toList();
    }

    public static List<String> ids(Collection<String> registered, Collection<String> pack, Collection<String> other) {
        return merge(registered, pack, other, "", Integer.MAX_VALUE).stream().map(Hit::id).toList();
    }

    public static List<String> filter(Collection<String> values, String typed, int limit) {
        if (values == null) return List.of();
        String needle = typed == null ? "" : typed.trim().toLowerCase(Locale.ROOT);
        int cap = limit <= 0 ? Integer.MAX_VALUE : limit;
        return values.stream()
                .filter(value -> value != null && matches(value, needle))
                .sorted(Comparator.comparingInt((String value) -> rank(value, Origin.REGISTERED, needle)).thenComparing(value -> value))
                .limit(cap)
                .toList();
    }

    public static List<String> fieldPaths(PackKind kind, JsonElement json) {
        List<String> paths = new ArrayList<>();
        for (PackField field : kind.fields()) paths.add(field.key());
        if (kind != PackKind.SUIT) return paths;
        int count = 0;
        if (json != null && json.isJsonObject() && json.getAsJsonObject().has("levels")
                && json.getAsJsonObject().get("levels").isJsonArray()) {
            count = json.getAsJsonObject().getAsJsonArray("levels").size();
        }
        int shown = Math.max(count, 1);
        for (int i = 0; i < shown; i++) {
            paths.add("levels." + i + ".pieces");
            paths.add("levels." + i + ".trigger");
            paths.add("levels." + i + ".attributes");
            paths.add("levels." + i + ".effects");
            paths.add("levels." + i + ".commands");
            paths.add("levels." + i + ".special_effects");
        }
        return paths;
    }

    private static void put(Map<String, Origin> map, String id, Origin incoming) {
        if (id == null || id.isBlank()) return;
        map.merge(id, incoming, PackSuggestions::combine);
    }

    private static Origin combine(Origin current, Origin incoming) {
        if (current == incoming) return current;
        if (incoming == Origin.PACK && current != Origin.PACK) return Origin.BOTH;
        if (incoming == Origin.REGISTERED && current != Origin.REGISTERED) return Origin.BOTH;
        if (incoming == Origin.OTHER && current == Origin.REGISTERED) return Origin.BOTH;
        if (current == Origin.BOTH) return Origin.BOTH;
        return incoming;
    }

    private static boolean matches(String id, String needle) {
        return needle.isEmpty() || id.toLowerCase(Locale.ROOT).contains(needle);
    }

    private static int rank(String id, Origin origin, String needle) {
        String lower = id.toLowerCase(Locale.ROOT);
        int score = needle.isEmpty() ? 20 : 40;
        if (!needle.isEmpty() && lower.startsWith(needle)) score = 0;
        else if (!needle.isEmpty() && lower.contains(":" + needle)) score = 10;
        else if (!needle.isEmpty()) score = 20;
        score += switch (origin) {
            case PACK -> 0;
            case BOTH -> 1;
            case OTHER -> 2;
            case REGISTERED -> 3;
        };
        return score;
    }
}
