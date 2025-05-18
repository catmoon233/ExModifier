package net.exmo.exmodifier.content.element;

import com.google.common.base.CaseFormat;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static net.exmo.exmodifier.Exmodifier.GSON;
import static net.exmo.exmodifier.util.ExUtil.classToString;


public class ElementPreparableReloadListener extends AbstractReloadListener<ExElement> {

    public ElementPreparableReloadListener() {
        super("elements",
            "loading exElement data...",
            ExElement.EX_SERIALIZE::fromJson,
            (key, element) -> ExElementHandle.registryExElement(element)
        );
    }

    @Override
    public String getName() {
        return classToString(this.getClass());
    }
}