package net.exmo.exmodifier.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public class ExClientUtils {
    public static <T> @Nullable T getCurrentGuiAs(Class<T> clazz) {
        return (T)(Minecraft.getInstance().screen == null ? null : getGuiAs(Minecraft.getInstance().screen, clazz));

    }
    public static <T> @Nullable T getGuiAs(Screen gui, Class<T> clazz) {
        if (gui instanceof Screen) {
            if (clazz.isAssignableFrom(gui.getClass())) {
                return (T)gui;
            }
        }

        return (T)(clazz.isAssignableFrom(gui.getClass()) ? Minecraft.getInstance().screen : null);
    }
}
