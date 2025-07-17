package net.exmo.exmodifier.content.client;

import net.exmo.exmodifier.Exmodifier;
import com.google.common.collect.Maps;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import net.exmo.exmodifier.network.sync.lang.LangMessage;
import net.exmo.exmodifier.util.exSerialize.ExSerialize;
import net.minecraft.locale.Language;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class LanguageLoader {
    public static final Map<String, Map<String, String>> LANGUAGES = Maps.newHashMap();

    /**
     * 将格式为 "key1=value1,key2=value2" 的字符串转换为 Map<String, String>
     * @param toMap 要转换的字符串
     * @return 转换后的Map
     */
    public static Map<String, String> handle(String toMap) {
        Map<String, String> map = new HashMap<>();
        if (toMap == null || toMap.trim().isEmpty()) {
            return map;
        }

        String[] pairs = toMap.split(",,,");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                map.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        return map;
    }

    /**
     * 将 Map<String, String> 转换为格式为 "key1=value1,key2=value2" 的字符串
     * @param map 要转换的Map
     * @return 转换后的字符串
     */
    public static String mapToString(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }

        StringJoiner joiner = new StringJoiner(",,,");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            joiner.add(entry.getKey() + "=" + entry.getValue());
        }
        return joiner.toString();
    }
    public static void sendToAllClient() {
        LangMessage langMessage = new LangMessage(new LangMessage.LangMessageHandler(LanguageLoader.LANGUAGES));

    }

  //  private static final Marker MARKER = MarkerManager.getMarker("LanguageLoader");
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
