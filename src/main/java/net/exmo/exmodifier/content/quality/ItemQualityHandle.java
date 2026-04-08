package net.exmo.exmodifier.content.quality;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.exmo.exmodifier.Exmodifier;

import net.exmo.exmodifier.content.helper.ItemQualityHelper;
import net.exmo.exmodifier.content.modifier.MoConfig;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.content.type.ExType;
import net.exmo.exmodifier.content.type.ExTypeHandle;
import net.exmo.exmodifier.content.type.ItemType;
import net.exmo.exmodifier.util.ExConfigHandle;
import net.exmo.exmodifier.util.ExRegistryHelper;
import net.exmo.exmodifier.util.ItemSelector;
import net.exmo.exmodifier.util.WeightedUtil;
import net.exmo.exmodifier.util.module.ExDataModule;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static net.exmo.exmodifier.Exmodifier.LOGGER;


public class ItemQualityHandle extends ExDataModule<String, ItemQuality> {

    public static final ItemQualityHandle INSTANCE = new ItemQualityHandle();

    /** @deprecated 使用 INSTANCE.getAll() */
    @Deprecated public static final Map<String, ItemQuality> itemQualityMap = INSTANCE.registry;
    /** @deprecated 使用 INSTANCE.foundConfigs */
    @Deprecated public static List<MoConfig> FoundQualityConfigs = INSTANCE.foundConfigs;
    /** @deprecated 使用 DEFAULT_QUALITY_MODULE.foundConfigs */
    @Deprecated public static List<MoConfig> FoundDefaultQualityConfigs = new ArrayList<>();
    public static final Map<ItemSelector, ItemQuality> itemDefaultQualityMap = new java.util.HashMap<>();

    // 默认品质模块（第二配置路径）
    private static final ExDataModule<String, ItemQuality> DEFAULT_QUALITY_MODULE = new ExDataModule<>("DefaultQuality") {
        @Override
        protected Path getConfigPath() {
            return FMLPaths.CONFIGDIR.get().resolve("exmo/defaultQuality/");
        }

        @Override
        protected void processEntry(String entryKey, JsonObject json, MoConfig moConfig) {
            processItemsDefaultQualityConfigEntry(entryKey, json);
        }
    };

    private ItemQualityHandle() {
        super("Quality");
    }

    @Override
    protected Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get().resolve("exmo/quality/");
    }

    @Override
    protected void processEntry(String entryKey, JsonObject json, MoConfig moConfig) {
        processItemsQualityConfigEntry(entryKey, json);
    }

    @Override
    protected void onPreInit() {
        super.onPreInit();
        itemDefaultQualityMap.clear();
    }
    // region 兼容旧API的静态方法

    public static void registerStatic(String id, ItemQuality itemQuality){
        INSTANCE.register(id, itemQuality);
    }

    /** @deprecated 使用 INSTANCE.getConfigPath() */
    @Deprecated public static final Path ItemsQualityConfigPath = FMLPaths.CONFIGDIR.get().resolve("exmo/quality/");
    /** @deprecated 使用 DEFAULT_QUALITY_MODULE */
    @Deprecated public static final Path ItemsDefaultQualityConfigPath = FMLPaths.CONFIGDIR.get().resolve("exmo/defaultQuality/");

    public static void init() throws IOException {
        INSTANCE.load();
    }

    public static void init2() throws IOException {
        DEFAULT_QUALITY_MODULE.load();
    }

    public static void processMoConfigEntries(MoConfig moconfig) throws FileNotFoundException {
        INSTANCE.processMoConfig(moconfig);
    }

    public static void processMoConfigEntries2(MoConfig moconfig) throws FileNotFoundException {
        DEFAULT_QUALITY_MODULE.processMoConfig(moconfig);
    }

    public static List<ItemSelector> getItemSelector(ItemStack stack) {
        return itemDefaultQualityMap.keySet().stream().filter(entry -> entry != null && entry.compare(stack)).toList();
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
    private static void processItemsQualityConfigEntry(String id, JsonObject jsonObject) {
        try {
            int rarity = ExRegistryHelper.getInt(jsonObject, "rarity", 0);
            int maxRefine = ExRegistryHelper.getInt(jsonObject, "maxRefine", 0);
            float growValue = ExRegistryHelper.getFloat(jsonObject, "growValue", 0F);
            float addRefreshValue = ExRegistryHelper.getFloat(jsonObject, "addRefreshValue", 0F);
            String localDescription = jsonObject.has("LocalDescription") ? jsonObject.get("LocalDescription").getAsString() : "";
            List<String> items = ExRegistryHelper.getStringList(jsonObject, "items");
            List<String> materials = ExRegistryHelper.getStringList(jsonObject, "materials");
            List<ModifierEntry> modifierEntries = ExRegistryHelper.mapJsonArray(
                    jsonObject,
                    "ModifierEntries",
                    modifier -> ModifierHandle.modifierEntryMap.get(modifier.getAsString())
            ).stream().filter(Objects::nonNull).toList();

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
            itemQuality.LocalDescription = localDescription;
            itemQuality.autoRefresh = jsonObject.has("autoRefresh") && jsonObject.get("autoRefresh").getAsBoolean();
            itemQuality.isRandom = !jsonObject.has("isRandom") || jsonObject.get("isRandom").getAsBoolean();
            itemQuality.materials = materials;
            INSTANCE.register(id,itemQuality);
            LOGGER.debug("Add ItemsQuality: "+id );

        }catch (Exception e){
            LOGGER.error("Error reading ItemsDefaultEntry config file", e);
        }
    }
    private static void processItemsDefaultQualityConfigEntry(String key, JsonObject jsonObject) {
        try {
            ItemQuality itemQuality = itemQualityMap.get(key);
            if (jsonObject.has("id")){
                itemQuality = itemQualityMap.get(jsonObject.get("id").getAsString());
            }
            ItemSelector itemSelector =null;
            if (jsonObject.has("itemSelector")){
                itemSelector= ItemSelector.EX_SERIALIZE.fromJsonSingle(jsonObject.get("itemSelector").getAsJsonObject());
            }
            if (itemSelector!=null && itemQuality != null) itemDefaultQualityMap.put(itemSelector,itemQuality);
            LOGGER.debug("Add ItemsDefaultQuality: " + (itemQuality != null ? itemQuality.Id : "null"));

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
