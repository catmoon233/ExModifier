package net.exmo.exmodifier.content.modifier;

import net.exmo.exmodifier.Exmodifier;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RefreshContainItemHandle {
    public static List<String> refreshContainItem;
    public static final Path RefreshContainItem = FMLPaths.CONFIGDIR.get().resolve("exmo/RefreshContainItem.txt");

    public static void readConfig() throws IOException {
        refreshContainItem = new ArrayList<>();
        if (Files.exists(RefreshContainItem)) {
            try {
                List<String> lines = Files.readAllLines(RefreshContainItem);
                refreshContainItem.addAll(lines);
            } catch (IOException e) {
                Exmodifier.LOGGER.error("Error reading RefreshContainTag config file", e);
            }
        }
    }
}

