package net.exmo.exmodifier.content.quality;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.exmo.exmodifier.Exmodifier;

import net.exmo.exmodifier.content.helper.ItemQualityHelper;
import net.exmo.exmodifier.content.modifier.MoConfig;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.content.suit.ExSuit;
import net.exmo.exmodifier.content.type.ExType;
import net.exmo.exmodifier.content.type.ExTypeHandle;
import net.exmo.exmodifier.content.type.ItemType;
import net.exmo.exmodifier.util.ExConfigHandle;
import net.exmo.exmodifier.util.ItemSelector;
import net.exmo.exmodifier.util.WeightedUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static net.exmo.exmodifier.Exmodifier.LOGGER;


public class ItemQualityHandle {

    public static Map<String, ItemQuality> itemQualityMap = new java.util.HashMap<>();
    public static Map<ItemSelector, ItemQuality> itemDefaultQualityMap = new java.util.HashMap<>();


    public static final Path ItemsQualityConfigPath = FMLPaths.CONFIGDIR.get().resolve("exmo/quality/");
    public static final Path ItemsDefaultQualityConfigPath = FMLPaths.CONFIGDIR.get().resolve("exmo/defaultQuality/");
    public static List<MoConfig> FoundQualityConfigs = new ArrayList<>();
    public static List<MoConfig> FoundDefaultQualityConfigs = new ArrayList<>();

    public static void register(String id, ItemQuality itemQuality){
        itemQualityMap.put(id, itemQuality);
        Exmodifier.LOGGER.debug("Register ItemQuality: " + id);
    }
    public static List<ItemSelector> getItemSelector(ItemStack stack) {
        return itemDefaultQualityMap.keySet().stream().filter(entry -> entry.compare(stack)).toList();
    }
    public static void init() throws IOException {
        if (Files.exists(ItemsQualityConfigPath)) {
            long startTime = System.nanoTime(); // 记录开始时间

            FoundQualityConfigs = ExConfigHandle.listFiles(ItemsQualityConfigPath);
            for (MoConfig moconfig : FoundQualityConfigs)
            {
                processMoConfigEntries(moconfig);
            }


            long endTime = System.nanoTime(); // 记录结束时间
            long duration = endTime - startTime; // 计算持续时间
            Exmodifier.LOGGER.debug("ReadConfig Quality Over time: " + duration / 1000000 + " ms");

//            MoConfig washingMaterialsConfig = new MoConfig(ItemsQualityConfigPath);
//
//            for (Map.Entry<String, JsonElement> entry : washingMaterialsConfig.readEntrys()) {
//                processItemsQualityConfigEntry(entry);
//            }
        }else {
            //创建文件夹
            Files.createDirectories(ItemsQualityConfigPath);

        }

    }
    public static void init2() throws IOException {
        if (Files.exists(ItemsDefaultQualityConfigPath)) {
            long startTime = System.nanoTime();
            FoundDefaultQualityConfigs = ExConfigHandle.listFiles(ItemsDefaultQualityConfigPath);
            for (MoConfig moconfig : FoundDefaultQualityConfigs) {
                processMoConfigEntries2(moconfig);
            }
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            Exmodifier.LOGGER.debug("ReadConfig DefaultQuality Over time: " + duration / 1000000 + " ms");
        }

    }
    public static void processMoConfigEntries(MoConfig moconfig) throws FileNotFoundException {
        if(moconfig.readEntrys().isEmpty()){
            Exmodifier.LOGGER.info("No Quality Config Found :"+moconfig.configFile);
            return;
        }
        for (Map.Entry<String, JsonElement> entry : moconfig.readEntrys()) {
            processItemsQualityConfigEntry(entry);
        }


    }
    public static void processMoConfigEntries2(MoConfig moconfig) throws FileNotFoundException {
        if(moconfig.readEntrys().isEmpty()){
            Exmodifier.LOGGER.info("No Default Quality Config Found :"+moconfig.configFile);
            return;
        }
        for (Map.Entry<String, JsonElement> entry : moconfig.readEntrys()) {
            processItemsDefaultQualityConfigEntry(entry);
        }


    }
    public static class CommonEvent {

    }
    public static void contaiff(ItemStack stack, int rarity , int refreshnumber, ItemType type)  {
        Exmodifier.LOGGER.debug("itemQualityRefresh: " + stack.getDescriptionId() + " " + type);
        WeightedUtil<String> weightedUtil = new WeightedUtil<>(
                itemQualityMap.entrySet().stream()
                        .filter(e -> e.getValue().type == type )
                        .filter(e -> (e.getValue().getOnlyItemIds().isEmpty() ||e.getValue().getOnlyItemIds().contains(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString())))
//                                .filter(e -> !e.getValue().cantSelect)
//                                .filter(e -> e.getValue().needFreshValue ==0 || e.getValue().needFreshValue <= refreshnumber)
                        .filter(e -> e.getValue().getOnlyItemTags().isEmpty() ||e.getValue().containTag(stack))
                        .filter(e -> e.getValue().getOnlyWashItems().isEmpty() ||e.getValue().getOnlyWashItems().contains(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString()))
//                                .filter(e -> e.getValue().OnlyWashItems.isEmpty() ||e.getValue().OnlyWashItems.contains(washItem))
//                                .filter(e -> {
//                                    boolean hasWashItem = materialsList.stream()
//                                            .filter(m -> m.ItemId.equals(washItem))
//                                            .findAny()
//                                            .map(m -> !m.OnlyHasWashEntry)
//                                            .orElse(true);
//
//                                    return hasWashItem || e.getValue().OnlyWashItems.contains(washItem);
//                                })
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getWeight()))
        );
        //RandomEntryCurios(stack, weightedUtil, curiosType, refreshnumber);
        if (!weightedUtil.weights.isEmpty()) {
            Exmodifier.LOGGER.debug("RandomLevelEntry: " + type);
            weightedUtil.increaseWeightsByRarity(rarity);
            itemQualityRefresh2(stack, weightedUtil, refreshnumber);

            Exmodifier.LOGGER.debug("RandomLevel: " + type + " " + stack.getDescriptionId());
//            stack.getTag().putInt("exmodifier_level_modifier_applied",
//                    stack.getTag().getInt("exmodifier_level_modifier_applied") + 1);
        }

    }
    public static void itemQualityRefresh2(ItemStack stack, WeightedUtil<String> weightedUtil, int refreshnumber)  {
        int numAddedModifiers = 0;
        List<ItemQuality> foundItemLevels = new ArrayList<>();

        if (weightedUtil.weights.size()<refreshnumber)refreshnumber = weightedUtil.weights.size();
        while (numAddedModifiers < refreshnumber) {
            ItemQuality itemQuality = itemQualityMap.get(weightedUtil.selectRandomKeyBasedOnWeights());
            if (foundItemLevels.contains(itemQuality))continue;
            Exmodifier.LOGGER.debug("add QualityEntry: " + itemQuality.Id);
            foundItemLevels.add(itemQuality);
            ItemQualityHelper.of(stack).addQualityEntry(itemQuality,true,true);

            numAddedModifiers++;

        }
    }
    public static void ItemQualityRefresh(ItemStack stack, int rarity , int refreshnumber, String washItem)  {
        if (stack.getTag() == null)return;
        if (  ItemQualityHelper.of(stack).of(stack).getQualityEntriesSize()>0) return;
        // List<String> curiosType = CuriosUtil.getSlotsFromItemstack(stack);
        boolean find = false;
        for (ItemType a : ExTypeHandle.itemTypes.values().stream().filter(e -> e != ExType.UNKNOWN.get()).filter(e -> e != ExType.ALL.get()).toList()) {
            if (ModifierEntry.containItemType(stack, a)) {
                contaiff(stack,rarity,refreshnumber,a);
                find = true;
                break;
            }

        }
        if (!find) {
            contaiff(stack,rarity,refreshnumber, ExType.ALL.get());
            Exmodifier.LOGGER.debug("ItemQualityRefresh: No Type And refresh ALL TYPE");
        }
    }
    private static void processItemsQualityConfigEntry(Map.Entry<String, JsonElement> entry) {
        if (!entry.getValue().isJsonObject()) {
            return;
        }
        try {
            JsonObject jsonObject = entry.getValue().getAsJsonObject();
            int rarity = jsonObject.has("rarity") ? jsonObject.get("rarity").getAsInt() : 0;
            int maxRefine = jsonObject.has("maxRefine") ? jsonObject.get("maxRefine").getAsInt() : 0;
            float growValue = jsonObject.has("growValue") ? jsonObject.get("growValue").getAsFloat() : 0F;
            float addRefreshValue = jsonObject.has("addRefreshValue") ? jsonObject.get("addRefreshValue").getAsFloat() : 0F;
            String id = entry.getKey();
            String LocalDescription = jsonObject.has("LocalDescription") ? jsonObject.get("LocalDescription").getAsString() : "";
            List<String> items = new ArrayList<>();
            List<ModifierEntry> modifierEntries = new ArrayList<>();
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
            if (jsonObject.has("ModifierEntries")) {
                for (JsonElement modifier : jsonObject.get("ModifierEntries").getAsJsonArray()){
                    modifierEntries.add(ModifierHandle.modifierEntryMap.get(modifier.getAsString()));
                }
            }
            ItemQuality itemQuality = new ItemQuality(rarity,id);
            itemQuality.items = items;
            itemQuality.entries = modifierEntries;
            itemQuality.growValue = growValue;
            itemQuality.addRefreshValue = addRefreshValue;
            itemQuality.Max_Refine = maxRefine;
            itemQuality.setShowInHeadTooltip(jsonObject.has("showInHeadTooltip") && jsonObject.get("showInHeadTooltip").getAsBoolean());
            itemQuality.ShowModifierComponent =(!jsonObject.has("ShowModifierComponent") || jsonObject.get("ShowModifierComponent").getAsBoolean());
            itemQuality.refineNeedSameStar =(!jsonObject.has("refineNeedSameStar") || jsonObject.get("refineNeedSameStar").getAsBoolean());
            itemQuality.cantRemoveEntry = jsonObject.has("cantRemoveEntry") && jsonObject.get("cantRemoveEntry").getAsBoolean();
            itemQuality.LocalDescription = LocalDescription;
            itemQuality.autoRefresh = jsonObject.has("autoRefresh") && jsonObject.get("autoRefresh").getAsBoolean();
            itemQuality.isRandom = !jsonObject.has("isRandom") || jsonObject.get("isRandom").getAsBoolean();
            itemQuality.materials = materials;
            register(id,itemQuality);
            LOGGER.debug("Add ItemsQuality: "+id );

        }catch (Exception e){
            LOGGER.error("Error reading ItemsDefaultEntry config file", e);
        }
    }
    private static void processItemsDefaultQualityConfigEntry(Map.Entry<String, JsonElement> entry) {
        if (!entry.getValue().isJsonObject()) {
            return;
        }
        try {
            JsonObject jsonObject = entry.getValue().getAsJsonObject();
            ItemQuality itemQuality = itemQualityMap.get(entry.getKey());
            if (jsonObject.has("id")){
                itemQuality = itemQualityMap.get(jsonObject.get("id").getAsString());
            }
            ItemSelector itemSelector =null;
            if (jsonObject.has("itemSelector")){
                itemSelector= ItemSelector.EX_SERIALIZE.fromJsonSingle(jsonObject.get("itemSelector").getAsJsonObject());
            }
            if (itemSelector!=null) itemDefaultQualityMap.put(itemSelector,itemQuality);
            LOGGER.debug("Add ItemsDefaultQuality: " +  itemQuality.Id);

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
