package net.exmo.exmodifier.content.type;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.modifier.MoConfig;
import net.exmo.exmodifier.events.ExRegisterExType;
import net.exmo.exmodifier.util.ExConfigHandle;
import net.exmo.exmodifier.util.ItemSelector;
import net.exmo.exmodifier.util.module.ExDataModule;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExTypeHandle extends ExDataModule<String, ItemType> {

    public static final ExTypeHandle INSTANCE = new ExTypeHandle();

    /** @deprecated 使用 INSTANCE.getAll() */
    @Deprecated public static Map<String, ItemType> itemTypes = INSTANCE.registry;
    /** @deprecated 使用 INSTANCE.foundConfigs */
    @Deprecated public static List<MoConfig> FoundTypeConfigs = INSTANCE.foundConfigs;
    /** @deprecated 使用 INSTANCE.getConfigPath() */
    @Deprecated public static final Path ConfigPath = FMLPaths.CONFIGDIR.get().resolve("exmo/type");

    private ExTypeHandle() {
        super("Type");
    }

    @Override
    protected void onPreInit() {
        // Java 静态定义的默认类型（ExType.SWORD 等）在 registry 里只注册一次，
        // 必须在 clear 前把它们保存下来并在 clear 后复原，否则 reload 后这些类型会消失。
        java.util.Map<String, ItemType> defaults = new java.util.HashMap<>();
        for (String name : ExType.defaultTypes) {
            ItemType t = registry.get(name);
            if (t != null) defaults.put(name, t);
        }
        registry.clear();
        registry.putAll(defaults);
    }

    @Override
    protected Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get().resolve("exmo/type");
    }

    @Override
    protected void processEntry(String entryKey, JsonObject json, MoConfig moConfig) {
        List<ItemType> entries = new ArrayList<>();
        processItemType(moConfig, Map.entry(entryKey, (JsonElement) json), entries);
        entries.forEach(t -> register(t.name(), t));
    }

    @Override
    protected void onPostInit() {
        MinecraftForge.EVENT_BUS.post(new ExRegisterExType());
    }

    // region 兼容旧API

    public static void registerItemType(ItemType itemType) {
        INSTANCE.register(itemType.name(), itemType);
    }

    public static void readConfig() throws IOException {
        INSTANCE.load();
    }

    /** @deprecated 使用 INSTANCE.processMoConfig() */
    @Deprecated
    public static void processItemTypes(MoConfig moconfig) throws FileNotFoundException {
        INSTANCE.processMoConfig(moconfig);
    }

    // endregion

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
