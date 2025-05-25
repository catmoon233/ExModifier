package net.exmo.exmodifier.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.exmo.exmodifier.Exmodifier;
import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ExConfig {
   public Path configFile;
    public JsonObject jsonObject;
    //public Set<Map.Entry<String, JsonElement>> entrys = new HashSet<>();

    public Path getConfigFile() {
        return configFile;
    }
    public JsonObject read() throws FileNotFoundException {
        try {


            if (Files.exists(configFile)) {
                try (FileReader reader = new FileReader(configFile.toFile())) {
                    Gson gson = new Gson();

                    return gson.fromJson(reader, JsonObject.class);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }catch (Exception e){

            return null;
        }
        return null;
    }
    public JsonObject readFromZipFile(InputStream stream){
        try {
            Gson gson = new Gson();
            String json = IOUtils.toString(stream, StandardCharsets.UTF_8);
            return gson.fromJson(json, JsonObject.class);
        } catch (IOException e) {
            getError("Error while reading config file : not exists");
        }
        return null;
    }
    public Set<Map.Entry<String, JsonElement>> readEntrys() throws FileNotFoundException {
        try {
                return jsonObject.entrySet();
        }catch (Exception e){
            getError("Error while reading config file : not exists");
        }
        return null;
    }

    private static void getError(String s) {
        Exmodifier.LOGGER.Logger.error(s);
    }

    public JsonElement readSetting(String key) {
        try {


            return jsonObject.get(key);
        }catch (Exception e){
            getError("Error while reading config file : not exists");
        }
        return null;
    }
    public ExConfig(Path configFile) throws FileNotFoundException {
        this.configFile = configFile;
        this.jsonObject = read();
    }
    public ExConfig(Path configFile,boolean skin) throws FileNotFoundException {
        this.configFile = configFile;

    }
    public ExConfig(Path configFile, InputStream stream) throws FileNotFoundException {
        this.configFile = configFile;
        this.jsonObject = readFromZipFile(stream);
    }
    public com.google.gson.JsonObject getAllJsonObject() {
        return jsonObject;
    }
    public  JsonObject getJsonObjectInJson(String key,JsonObject obj) {
        try {
            return obj.get(key).getAsJsonObject();
        }catch (Exception e){
            getError("Error while getting JsonObject from config file");
            return null;
        }
    }

    public String getSource() {
        return configFile.toString();
    }
}
