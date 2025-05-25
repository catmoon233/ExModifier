package net.exmo.exmodifier.util.exSerialize;

import net.minecraft.resources.ResourceLocation;

public interface ExSerClass<T> {
    ResourceLocation getResId();
    ExSerialize<T> getExSerialize();
}
