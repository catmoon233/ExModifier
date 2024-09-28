package net.exmo.exmodifier.content.quality;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.exmo.exmodifier.content.modifier.MoConfig;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.exmo.exmodifier.Exmodifier.LOGGER;

public class ItemQualityHandle {
    public static Map<String,ItemQuality> itemQualityMap = new HashMap<>();
    public static final Path ItemsQualityConfigPath = FMLPaths.CONFIGDIR.get().resolve("exmo/ItemsQualityConfigPath.json");
    public static void init() throws FileNotFoundException {
        if (Files.exists(ItemsQualityConfigPath)) {
            itemQualityMap = new HashMap<>();
            MoConfig washingMaterialsConfig = new MoConfig(ItemsQualityConfigPath);

            for (Map.Entry<String, JsonElement> entry : washingMaterialsConfig.readEntrys()) {
                processItemsQualityConfigEntry(entry);
            }
        }
    }

    private static void processItemsQualityConfigEntry(Map.Entry<String, JsonElement> entry) {
        if (!entry.getValue().isJsonObject()) {
            return;
        }
        try {
            JsonObject jsonObject = entry.getValue().getAsJsonObject();
            int rarity = jsonObject.has("rarity") ? jsonObject.get("rarity").getAsInt() : 0;
            String id = entry.getKey();
            String LocalDescription = jsonObject.has("LocalDescription") ? jsonObject.get("LocalDescription").getAsString() : "";
            List<String> items = new ArrayList<>();
            List<String> materials = new ArrayList<>();
            if (jsonObject.has("items")) {
                for (JsonElement item : jsonObject.get("items").getAsJsonArray()) {
                    items.add(item.getAsString());
                }
            }
            if (jsonObject.has("materials")) {
                for (JsonElement material : jsonObject.get("materials").getAsJsonArray()){
                    materials.add(material.getAsString());
                }
            }
            ItemQuality itemQuality = new ItemQuality(rarity,id);
            itemQuality.items = items;
            itemQuality.LocalDescription = LocalDescription;
            itemQuality.autoRefresh = jsonObject.has("autoRefresh") ? jsonObject.get("autoRefresh").getAsBoolean() : false;
            itemQuality.isRandom = jsonObject.has("isRandom") ? jsonObject.get("isRandom").getAsBoolean() : true;
            itemQuality.materials = materials;
            itemQualityMap.put(id,itemQuality);
            LOGGER.debug("Add ItemsQuality: "+id );

        }catch (Exception e){
            LOGGER.error("Error reading ItemsDefaultEntry config file", e);
        }
    }
    /*
    即将更新 装备强化模块
	武器装备饰品将拥有稀有度，可内置每把武器的，
    也可通过洗练（或者开启自动刷新，拿到手上即可刷新）
    可以根据材料刷新固定或随机的 稀有度可自定义
    （比如优良，精良，稀有，神化）
	每个装备会拥有等级，可以配置等级提升时
    一个类型提升的属性，未来将添加等级提升词条属性的选项，
    可以根据品质设定最高等级
	然后每把同样的武器可以精炼提升被精炼武器的属性，
    会被赋予一个精炼次数,每一个阶段等级可以精炼一次，
    也可以不用阶段等级就可以一直精炼（配置开关）
	武器进阶可以设置消耗同等等级的武器来进阶
     */
}
//我写个默认词条 等等哈
