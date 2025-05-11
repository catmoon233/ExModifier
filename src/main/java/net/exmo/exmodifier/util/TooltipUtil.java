package net.exmo.exmodifier.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public class TooltipUtil {
    public static List<Component> sprit(MutableComponent component){
        return component.toFlatList();
    }
}
