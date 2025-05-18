package net.exmo.exmodifier.content.modifier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.element.AbstractReloadListener;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;


public class ModifierPreparableReloadListener extends AbstractReloadListener<ModifierEntry> {
    public ModifierPreparableReloadListener() {
        super("modifier_entries",
            "loading modifier data...",
            json -> {
                ArrayList<ModifierEntry> list = new ArrayList<>();
                ModifierHandle.processModifierEntry(json.toString(), list);
                return list;
            },
            (key, entry) -> ModifierHandle.RegisterModifierEntry(entry)
        );
    }

    @Override
    public String getName() {
        return "ModifierPreparableReloadListener";
    }
}