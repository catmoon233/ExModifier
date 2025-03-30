package net.exmo.exmodifier.content.resources;

import net.exmo.exmodifier.Exmodifier;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.*;
public class ZipHandle {
    public static final Path ZIP_FILE_DIR = FMLPaths.CONFIGDIR.get().resolve("exmo/packs");

    public static void copyPacks(Minecraft p_100000_, List<Path> p_100001_, Path p_100002_) {
        MutableBoolean mutableboolean = new MutableBoolean();
        p_100001_.forEach((p_170009_) -> {
            try (Stream<Path> stream = Files.walk(p_170009_)) {
                stream.forEach((p_170005_) -> {
                    try {
                        Util.copyBetweenDirs(p_170009_.getParent(), p_100002_, p_170005_);
                    } catch (IOException ioexception1) {
                        Exmodifier.LOGGER.Logger.warn("Failed to copy exmo zip config file  from {} to {}", new Object[]{p_170005_, p_100002_, ioexception1});
                        mutableboolean.setTrue();
                    }

                });
            } catch (IOException var8) {
                Exmodifier.LOGGER.Logger.warn("Failed to copy exmo zip config file from {} to {}", p_170009_, p_100002_);
                mutableboolean.setTrue();
            }

        });
        if (mutableboolean.isTrue()) {
            SystemToast.onPackCopyFailure(p_100000_, p_100002_.toString());
        }

    }
    public static void init() throws IOException {
        if (!Files.exists(ZIP_FILE_DIR)) {
            Files.createDirectories(ZIP_FILE_DIR);
            return;
        }

        Files.list(ZIP_FILE_DIR).forEach(path -> {


            if (path.toString().endsWith(".zip") || path.toString().endsWith(".jar") ||
                    path.toString().endsWith(".7z") || path.toString().endsWith(".rar") ||
                    path.toString().endsWith(".gz")) {
                try (ZipFile zipFile = new ZipFile(path.toFile())) {
                    readConfigFromZipFile(zipFile);
                } catch (IOException e) {
                    Exmodifier.LOGGER.error("IOException:", e);
                }
            }

        });

    }

}
