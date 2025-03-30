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
import net.minecraft.world.entity.EquipmentSlot;
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
    //public static final Codec<ExTypeHandle> CODEC = Codec.unit(ExTypeHandle::new);
    public  static Map<String, ItemType> itemTypes = new HashMap<>();
    public static Path ConfigPath = FMLPaths.GAMEDIR.get().resolve("config/exmo/type");
    public static List<MoConfig> FoundTypeConfigs = new ArrayList<>();

    public static void registerItemType(ItemType itemType) {
        itemTypes.put(itemType.name(), itemType);
        Exmodifier.LOGGER.debug("Registered Item Type: " + itemType);
    }
    public static void readConfig() throws IOException {
        long startTime = System.nanoTime(); // 记录开始时间

        FoundTypeConfigs = ExConfigHandle.listFiles(ConfigPath);
        for (MoConfig moconfig : FoundTypeConfigs)
        {
            processItemTypes(moconfig);
        }

        long endTime = System.nanoTime(); // 记录结束时间
        long duration = endTime - startTime; // 计算持续时间
        Exmodifier.LOGGER.debug("ReadConfig Types Over time: " + duration / 1000000 + " ms");
    }

    public static void processItemTypes(MoConfig moconfig) throws FileNotFoundException {
        if(moconfig.readEntrys().isEmpty()){
            Exmodifier.LOGGER.info("No Types Config Found");
            return;
        }
        List<ItemType> entries = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : moconfig.readEntrys()) {
            try {
                Exmodifier.LOGGER.debug("Reading Type Config: " + entry.getKey());
                processItemType(moconfig, entry, entries);
                Exmodifier.LOGGER.debug("Reading Type Config Over: " + entry.getKey());
            } catch (Exception e) {
                Exmodifier.LOGGER.Logger.error("Error processing Type: " + entry.getKey(), e);
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

        List<ItemSelector> selectors = new ArrayList<>();
        List<EquipmentSlot> equipmentSlots = new ArrayList<>();

        // 解析 selectors 数组
        if (itemObject.has("selectors")) {
            JsonArray selectorsArray = itemObject.getAsJsonArray("selectors");
            for (JsonElement selectorElement : selectorsArray) {
                JsonObject selectorObj = selectorElement.getAsJsonObject();
                List<CompoundTag> containNBT = new ArrayList<>();
                List<TagKey<Item>> containTag = new ArrayList<>();
                List<String> items = new ArrayList<>();

                // 解析 containNBT
                if (selectorObj.has("containNBT")) {
                    JsonArray nbtArray = selectorObj.getAsJsonArray("containNBT");
                    for (JsonElement nbtEntry : nbtArray) {
                        if (nbtEntry.isJsonObject()) {
                            CompoundTag nbtTag = new CompoundTag();
                            JsonObject nbtObject = nbtEntry.getAsJsonObject();
                            for (Map.Entry<String, JsonElement> nbtPair : nbtObject.entrySet()) {
                                nbtTag.putString(nbtPair.getKey(), nbtPair.getValue().getAsString());
                            }
                            containNBT.add(nbtTag);
                        }
                    }
                }

                // 解析 containTag
                if (selectorObj.has("containTag")) {
                    JsonArray tagArray = selectorObj.getAsJsonArray("containTag");
                    for (JsonElement tagElement : tagArray) {
                        if (tagElement.isJsonPrimitive()) {
                            containTag.add(TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(),
                                    new ResourceLocation(tagElement.getAsString())));
                        }
                    }
                }
                if (selectorObj.has("itemIds")) {
                    JsonArray tagArray = selectorObj.getAsJsonArray("itemIds");
                    for (JsonElement tagElement : tagArray) {
                        if (tagElement.isJsonPrimitive()) {
                            items.add(tagElement.getAsString());
                        }
                    }
                }

                // 解析其他参数
              //  String itemStr = selectorObj.get("item").getAsString();
            //    ResourceLocation itemRL = ResourceLocation.tryParse(itemStr);
              //  Item item = ForgeRegistries.ITEMS.getValue(itemRL);
            //    String itemId = selectorObj.get("itemId").getAsString();
                String typeStr = selectorObj.get("type").getAsString();
                ItemSelector.CompareType type = ItemSelector.CompareType.valueOf(typeStr);

                // 创建 ItemSelector 并添加到列表
                ItemSelector selector = new ItemSelector(
                        null,
                        items,
                        containNBT,
                        type,
                        containTag,
                        null
                );
                selectors.add(selector);
            }
        }

        // 解析 equipmentSlot 数组
        if (itemObject.has("equipmentSlot")) {
            JsonArray slotArray = itemObject.getAsJsonArray("equipmentSlot");
            for (JsonElement slotElement : slotArray) {
                String slotName = slotElement.getAsString();
                EquipmentSlot slot = EquipmentSlot.byName(slotName.toLowerCase());
                if (slot != null) {
                    equipmentSlots.add(slot);
                } else {
                    Exmodifier.LOGGER.Logger.error("Invalid equipment slot: " + slotName);
                }
            }
        }

        // 创建 ItemType 实例
        String name;
        if (itemObject.has("name")){
            name= itemObject.get("name").getAsString();
        }else name = entry.getKey();
        var itemType = new ItemType(
                name,
                new ArrayList<>(selectors),  // 转换为 ArrayList
                equipmentSlots.toArray(new EquipmentSlot[0])  // 转换为数组
        );
        entries.add(itemType);
    }

    // 你看一下 ItemSelector.java 筛选类
}
