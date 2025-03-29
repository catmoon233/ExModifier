package net.exmo.exmodifier.content.resources;

import net.exmo.exmodifier.Exmodifier;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.*;
public class ZipHandle {
    public static final Path ZIP_FILE_DIR = FMLPaths.CONFIGDIR.get().resolve("exmo/packs");
    public static void init() throws IOException {
        if (!Files.exists(ZIP_FILE_DIR))return;
        Files.newDirectoryStream(ZIP_FILE_DIR).forEach((path -> {
            if(path.endsWith(".zip"))
                try (ZipFile zipFile = new ZipFile(path.toFile())){
                    readConfigFromZipFile(zipFile);
                } catch (IOException e){
                    Exmodifier.LOGGER.error("IOException:",e);
                }
        }));
    }
}
