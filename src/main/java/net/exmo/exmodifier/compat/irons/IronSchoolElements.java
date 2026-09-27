package net.exmo.exmodifier.compat.irons;

import net.exmo.exmodifier.core.data.ElementDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * When Iron's Spellbooks is loaded, every registered school becomes an ExModifier element.
 * Addon schools in {@code irons_spellbooks:schools} are included automatically.
 * Does not class-load ISS types unless the mod is present.
 */
public final class IronSchoolElements {
    public static final String ISS_MODID = "irons_spellbooks";

    private IronSchoolElements() {}

    public static boolean available() {
        return ModList.get().isLoaded(ISS_MODID);
    }

    public static Map<ResourceLocation, ElementDefinition> discover() {
        if (!available()) return Map.of();
        try {
            return readRegistry();
        } catch (Throwable ignored) {
            return Map.of();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<ResourceLocation, ElementDefinition> readRegistry() throws Exception {
        Class<?> registryClass = Class.forName("io.redspace.ironsspellbooks.api.registry.SchoolRegistry");
        Field field = registryClass.getField("REGISTRY");
        Object registry = field.get(null);
        if (!(registry instanceof Iterable<?> schools)) return Map.of();
        Method getId = null;
        Method getDamageType = null;
        Method getDisplayName = null;
        HashMap<ResourceLocation, ElementDefinition> overlay = new HashMap<>();
        for (Object school : schools) {
            if (school == null) continue;
            if (getId == null) {
                getId = school.getClass().getMethod("getId");
                getDamageType = school.getClass().getMethod("getDamageType");
                getDisplayName = school.getClass().getMethod("getDisplayName");
            }
            Object idObj = getId.invoke(school);
            if (!(idObj instanceof ResourceLocation id)) continue;
            int color = 0xFFFFFF;
            Object name = getDisplayName.invoke(school);
            if (name instanceof Component component) {
                TextColor textColor = component.getStyle().getColor();
                if (textColor != null) color = textColor.getValue();
            }
            List<ResourceLocation> damageTypes = List.of();
            Object damage = getDamageType.invoke(school);
            if (damage instanceof ResourceKey<?> key) {
                damageTypes = List.of(key.location());
            }
            overlay.put(id, new ElementDefinition(0.10F, 0.05F, color, Map.of(), damageTypes, Optional.of(id)));
        }
        return overlay;
    }
}
