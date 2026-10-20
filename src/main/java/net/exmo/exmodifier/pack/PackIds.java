package net.exmo.exmodifier.pack;

import net.minecraft.resources.ResourceLocation;

import java.util.regex.Pattern;

public final class PackIds {
    public static final Pattern NAME = Pattern.compile("[a-z0-9_]{1,32}");
    public static final String PREFIX = "exmod_";

    private PackIds() {}

    public static boolean validName(String name) {
        return name != null && NAME.matcher(name).matches();
    }

    public static String folder(String name) {
        return PREFIX + name;
    }

    public static String nameOfFolder(String folder) {
        if (folder == null || !folder.startsWith(PREFIX)) return null;
        String name = folder.substring(PREFIX.length());
        return validName(name) ? name : null;
    }

    public static ResourceLocation qualify(String raw, String namespace) {
        if (raw == null) return null;
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return null;
        if (!trimmed.contains(":")) {
            if (namespace == null || namespace.isBlank()) return null;
            trimmed = namespace.trim() + ":" + trimmed;
        }
        ResourceLocation id = ResourceLocation.tryParse(trimmed);
        if (id == null || !safe(id)) return null;
        return id;
    }

    public static boolean safe(ResourceLocation id) {
        if (id == null) return false;
        if (id.getNamespace().contains("..") || id.getPath().contains("..")) return false;
        for (String part : id.getPath().split("/")) {
            if (part.isBlank() || ".".equals(part) || "..".equals(part)) return false;
        }
        return true;
    }

    public static Integer color(String raw) {
        if (raw == null) return null;
        String text = raw.trim();
        if (text.startsWith("#")) text = text.substring(1);
        if (text.startsWith("0x") || text.startsWith("0X")) text = text.substring(2);
        try {
            if (text.matches("[0-9a-fA-F]{6}") || text.matches("[0-9a-fA-F]{8}")) {
                return (int) Long.parseLong(text, 16);
            }
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static String colorText(int color) {
        return String.format("#%06X", color & 0xFFFFFF);
    }
}
