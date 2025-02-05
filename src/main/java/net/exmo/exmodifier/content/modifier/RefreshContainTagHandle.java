package net.exmo.exmodifier.content.modifier;

import net.exmo.exmodifier.Exmodifier;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


import java.io.IOException;

public class RefreshContainTagHandle {
    public static List<String> refreshContainTag;
    public static final Path RefreshContainTag = FMLPaths.CONFIGDIR.get().resolve("exmo/RefreshContainTag.txt");

    public static void readConfig() throws IOException {
        refreshContainTag = new ArrayList<>();
        if (Files.exists(RefreshContainTag)) {
            try {
                List<String> lines = Files.readAllLines(RefreshContainTag);
                refreshContainTag.addAll(lines);
            } catch (IOException e) {
                Exmodifier.LOGGER.error("Error reading RefreshContainTag config file", e);
            }
        }
    }
}

