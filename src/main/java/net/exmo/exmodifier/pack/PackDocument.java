package net.exmo.exmodifier.pack;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;

public record PackDocument(PackKind kind, ResourceLocation id, JsonElement json) {}
