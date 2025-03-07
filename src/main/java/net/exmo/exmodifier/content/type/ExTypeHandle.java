package net.exmo.exmodifier.content.type;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.modifier.MoConfig;
import net.exmo.exmodifier.util.ExConfigHandle;
import net.exmo.exmodifier.util.ItemSelector;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExTypeHandle {
    public static final Codec<ExTypeHandle> CODEC = Codec.unit(ExTypeHandle::new);
    public final static Map<String, ItemType> values = new HashMap<>();
    public static Path ConfigPath = FMLPaths.GAMEDIR.get().resolve("config/exmo/type");
    public static List<MoConfig> FoundSuitConfigs = new ArrayList<>();

    public static void registerItemType(ItemType itemType) {
        values.put(itemType.name(), itemType);
    }
    public static void readConfig() throws IOException {
        long startTime = System.nanoTime(); // 记录开始时间

        FoundSuitConfigs = ExConfigHandle.listFiles(ConfigPath);
        for (MoConfig moconfig : FoundSuitConfigs)
        {
            processItemTypes(moconfig);
        }

        long endTime = System.nanoTime(); // 记录结束时间
        long duration = endTime - startTime; // 计算持续时间
        Exmodifier.LOGGER.debug("ReadConfig Suit Over time: " + duration / 1000000 + " ms");
    }

    public static void processItemTypes(MoConfig moconfig) throws FileNotFoundException {
        if(moconfig.readEntrys().isEmpty()){
            Exmodifier.LOGGER.info("No Suit Config Found");
            return;
        }
        List<ItemType> entries = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : moconfig.readEntrys()) {
            try {
                Exmodifier.LOGGER.debug("Reading Suit Config: " + entry.getKey());
                processItemType(moconfig, entry, entries);
                Exmodifier.LOGGER.debug("Reading Suit Config Over: " + entry.getKey());
            } catch (Exception e) {
                Exmodifier.LOGGER.Logger.error("Error processing modifier entry: " + entry.getKey(), e);
            }
        }
        for (ItemType itemType : entries){
            registerItemType(itemType);
        }
    }

    public static void processItemType(MoConfig moconfig, Map.Entry<String, JsonElement> entry, List<ItemType> entries) {
        JsonElement itemElement = entry.getValue();
        if (!itemElement.isJsonObject()) {
            return;
        }
        JsonObject itemObject = itemElement.getAsJsonObject();

        List<CompoundTag> containNBT = new ArrayList<>();
        if (itemObject.has("containNBT")) {
            JsonArray containNBTArray = itemObject.getAsJsonArray("containNBT");
            for (JsonElement nbtElement : containNBTArray) {
                if (nbtElement.isJsonObject()) {
                    CompoundTag nbtTag = new CompoundTag();
                    JsonObject nbtObject = nbtElement.getAsJsonObject();
                    for (Map.Entry<String, JsonElement> nbtEntry : nbtObject.entrySet()) {
                        nbtTag.putString(nbtEntry.getKey(), nbtEntry.getValue().getAsString());
                    }
                    containNBT.add(nbtTag);
                }
            }
        }

        List< TagKey < Item >> containTag = new ArrayList<>();
        if (itemObject.has("containTag")) {
            JsonArray containTagArray = itemObject.getAsJsonArray("containTag");
            for (JsonElement tagElement : containTagArray) {
                if (tagElement.isJsonPrimitive()) {
                    containTag.add(TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), new ResourceLocation(tagElement.getAsString())));
                }
            }
        }


        var itemType = new ItemType(
                itemObject.get("name").getAsString(),
              new ItemSelector(
                      ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(itemObject.get("item").getAsString())),
                      itemObject.get("itemId").getAsString(),
                      containNBT,
                      ItemSelector.CompareType.valueOf(itemObject.get("type").getAsString()),
                        containTag
              )
        );
        entries.add(itemType);


    }
    // 你看一下 ItemSelector.java 筛选类
}
