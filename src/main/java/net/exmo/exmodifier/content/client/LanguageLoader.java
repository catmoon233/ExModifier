package net.exmo.exmodifier.content.client;

import net.exmo.exmodifier.Exmodifier;
import com.google.common.collect.Maps;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import net.minecraft.locale.Language;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class LanguageLoader {
    public static final Map<String, Map<String, String>> LANGUAGES = Maps.newHashMap();
    private static final Marker MARKER = MarkerManager.getMarker("LanguageLoader");
    public static final Path LANGUAGES_FILE_PATH = FMLPaths.CONFIGDIR.get().resolve("exmo/lang");
    public static void load(Path filePath) {
        if (!Files.isDirectory(filePath)) {
            return;
        }
        File[] subFiles = filePath.toFile().listFiles((dir, name) -> true);
        if (subFiles == null) {
            return;
        }
        for (File file : subFiles) {
            String name = file.getName();
            if (!name.endsWith(".json")) {
                continue;
            }
            String languageCode = name.substring(0, name.length() - 5);
            try (InputStream inputStream = Files.newInputStream(file.toPath())) {
                Map<String, String> languages = Maps.newHashMap();
                Language.loadFromJson(inputStream, languages::put);
                putLanguage(languageCode, languages);
            } catch (IOException | JsonSyntaxException | JsonIOException exception) {
                Exmodifier.LOGGER.Logger.error( "Failed to read language file: {}", file);
              //  exception.printStackTrace();
            }
        }
    }
    public static void putLanguage(String region, Map<String, String> lang){
        Map<String, String> language = LANGUAGES.getOrDefault(region, Maps.newHashMap());
        language.putAll(lang);
        LANGUAGES.put(region, language);
    }
    public static Map<String, String> getLanguages(String region){
        return LANGUAGES.get(region);
    }
}
