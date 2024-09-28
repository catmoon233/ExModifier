package net.exmo.exmodifier.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.exmo.exmodifier.Exmodifier;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class exconfig {
   public Path configFile;
    public JsonObject AlljsonObject;
    public Set<Map.Entry<String, JsonElement>> entrys = new HashSet<>();

    public Path getConfigFile() {
        return configFile;
    }
    public JsonObject read() throws FileNotFoundException {
        try {


            if (Files.exists(configFile)) {
                try (FileReader reader = new FileReader(configFile.toFile())) {
                    Gson gson = new Gson();

                    JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

                    return jsonObject;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }catch (Exception e){
            Exmodifier.LOGGER.Logger.error("Error while reading config file : not exists");
            return null;
        }
        return null;
    }
    public Set<Map.Entry<String, JsonElement>> readEntrys() throws FileNotFoundException {
        try {
                return AlljsonObject.entrySet();
        }catch (Exception e){
            Exmodifier.LOGGER.Logger.error("Error while reading config file : not exists");
        }
        return null;
    }
    public JsonElement readSetting(String key) {
        try {


            return AlljsonObject.get(key);
        }catch (Exception e){
            Exmodifier.LOGGER.Logger.error("Error while reading config file : not exists");
        }
        return null;
    }
    public exconfig(Path configFile) throws FileNotFoundException {
        this.configFile = configFile;
        this.AlljsonObject = read();
    }
    public com.google.gson.JsonObject getAllJsonObject() {
        return AlljsonObject;
    }
    public  JsonObject getJsonObjectInJson(String key,JsonObject obj) {
        try {
            return obj.get(key).getAsJsonObject();
        }catch (Exception e){
            Exmodifier.LOGGER.Logger.error("Error while getting JsonObject from config file");
            return null;
        }
    }

}
