package net.exmo.exmodifier.content.level;

import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ItemLevelHandle {
    public static final Path LEVEL_CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("exmo/ItemLevel/");
}