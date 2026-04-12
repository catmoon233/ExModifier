package net.exmo.exmodifier.content.suit;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.modifier.MoConfig;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.content.specialEffects.SpecialEffectHandle;
import net.exmo.exmodifier.content.type.ExType;
import net.exmo.exmodifier.content.type.ExTypeHandle;
import net.exmo.exmodifier.content.type.ItemType;
import net.exmo.exmodifier.events.ExAddSuitAttrigetherEvent;
import net.exmo.exmodifier.events.ExAddSuitAttrigethersEvent;
import net.exmo.exmodifier.network.ExModifiervaV;
import net.exmo.exmodifier.util.gether.AttriGetherNormal;
import net.exmo.exmodifier.util.ExConfigHandle;
import net.exmo.exmodifier.util.module.ExDataModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static net.exmo.exmodifier.content.suit.ExSuit.StringToTrigger;

public class ExSuitHandle extends ExDataModule<String, ExSuit> {

    public static final ExSuitHandle INSTANCE = new ExSuitHandle();

    /** @deprecated 使用 INSTANCE.getAll() */
    @Deprecated public static Map<String, ExSuit> LoadExSuit = INSTANCE.registry;
    /** @deprecated 使用 INSTANCE.foundConfigs */
    @Deprecated public static List<MoConfig> FoundSuitConfigs = INSTANCE.foundConfigs;
    /** @deprecated 使用 INSTANCE.getConfigPath() */
    @Deprecated public static final Path ConfigPath = FMLPaths.CONFIGDIR.get().resolve("exmo/suit");

    private ExSuitHandle() {
        super("Suit");
    }

    @Override
    protected Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get().resolve("exmo/suit");
    }

    @Override
    protected void processEntry(String entryKey, JsonObject json, MoConfig moConfig) {
        // ExSuitHandle的entry处理逻辑在processSuitEntry中
        // 通过 processMoConfig 覆写来处理
    }

    @Override
    public void processMoConfig(MoConfig moConfig) {
        try {
            processMoConfigEntries(moConfig);
        } catch (Exception e) {
            Exmodifier.LOGGER.Logger.error("Error processing suit config: {}", moConfig.configFile, e);
        }
    }

    // region 兼容旧API

    public static void registerExSuit(ExSuit exSuit){
        INSTANCE.register(exSuit.id, exSuit);
        Exmodifier.LOGGER.info("Registered ExSuit: "+ exSuit);
    }

    public static void readConfig() throws IOException {
        INSTANCE.load();
    }

    private static ModifierEntry resolveModifierEntry(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }

        ModifierEntry direct = ModifierHandle.findModifierEntry(id);
        if (direct != null) {
            return direct;
        }

        if (id.length() > 2) {
            ModifierEntry stripped = ModifierHandle.findModifierEntry(id.substring(2));
            if (stripped != null) {
                return stripped;
            }
        }

        for (ModifierEntry entry : ModifierHandle.modifierEntryMap.values()) {
            if (entry == null || entry.id == null || entry.id.isEmpty()) {
                continue;
            }
            if (entry.id.endsWith(id) || id.endsWith(entry.id)) {
                return entry;
            }
        }
        return null;
    }

    private static boolean hasKnownTypePrefix(String value) {
        if (value == null || value.length() <= 2) {
            return false;
        }
        String prefix = value.substring(0, 2);
        for (ItemType itemType : ExTypeHandle.itemTypes.values()) {
            if (itemType == null || itemType.name() == null) {
                continue;
            }
            String typeName = itemType.name();
            if (typeName.length() >= 2 && prefix.equals(typeName.substring(0, 2))) {
                return true;
            }
        }
        return false;
    }

    private static Set<String> buildSuitIdAliases(String value) {
        Set<String> aliases = new LinkedHashSet<>();
        if (value == null || value.isEmpty()) {
            return aliases;
        }

        aliases.add(value);
        if (hasKnownTypePrefix(value) && value.length() > 2) {
            aliases.add(value.substring(2));
        }
        return aliases;
    }

    private static boolean isSuitIdMatched(String configuredSuitId, ExSuit exSuit) {
        if (configuredSuitId == null || configuredSuitId.isEmpty() || exSuit == null || exSuit.id == null || exSuit.id.isEmpty()) {
            return false;
        }

        // 两边都带已知类型前缀时，仅允许完全一致，避免不同类型同后缀误匹配。
        boolean configuredHasPrefix = hasKnownTypePrefix(configuredSuitId);
        boolean suitHasPrefix = hasKnownTypePrefix(exSuit.id);
        if (configuredHasPrefix && suitHasPrefix && !configuredSuitId.equals(exSuit.id)) {
            return false;
        }

        Set<String> configuredAliases = buildSuitIdAliases(configuredSuitId);
        Set<String> suitAliases = buildSuitIdAliases(exSuit.id);
        for (String alias : configuredAliases) {
            if (suitAliases.contains(alias)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsSuitId(List<String> configuredSuitIds, ExSuit exSuit) {
        if (configuredSuitIds == null || configuredSuitIds.isEmpty()) {
            return false;
        }

        boolean hasTypedConfiguredId = false;
        for (String configuredSuitId : configuredSuitIds) {
            if (hasKnownTypePrefix(configuredSuitId)) {
                hasTypedConfiguredId = true;
                break;
            }
        }

        // 若配置中已存在带类型前缀ID，则对带前缀套装优先做精确匹配，避免被raw id误命中。
        if (hasTypedConfiguredId && hasKnownTypePrefix(exSuit.id)) {
            for (String configuredSuitId : configuredSuitIds) {
                if (exSuit.id.equals(configuredSuitId)) {
                    return true;
                }
            }
            return false;
        }

        for (String configuredSuitId : configuredSuitIds) {
            if (isSuitIdMatched(configuredSuitId, exSuit)) {
                return true;
            }
        }
        return false;
    }

    private static void bindSuitToModifierEntry(ModifierEntry modifierEntry, String rawSuitId, String typedSuitId) {
        if (modifierEntry == null) {
            return;
        }

        if (typedSuitId != null && !typedSuitId.isEmpty() && !modifierEntry.exsuits.contains(typedSuitId)) {
            modifierEntry.exsuits.add(typedSuitId);
        } else if (rawSuitId != null && !rawSuitId.isEmpty() && !modifierEntry.exsuits.contains(rawSuitId)) {
            modifierEntry.exsuits.add(rawSuitId);
        }
    }

    // endregion

    public static ExSuit FoundAllTypeSuitById(String id){
        ModifierEntry modifierEntry = resolveModifierEntry(id);
        if (modifierEntry != null && modifierEntry.exsuits != null && !modifierEntry.exsuits.isEmpty()) {
            for (ExSuit exSuit : LoadExSuit.values()){
                if (containsSuitId(modifierEntry.exsuits, exSuit)){
                    return exSuit;
                }
            }
        }
        List<ExSuit> fallback = findSuitsByEntryIdFallback(id, modifierEntry);
        if (!fallback.isEmpty()) {
            return fallback.get(0);
        }
        return null;

    }
    public static List<ExSuit> FindExSuitFromEntry(String id){
        ModifierEntry modifierEntry = resolveModifierEntry(id);
        if (modifierEntry != null && modifierEntry.exsuits != null && !modifierEntry.exsuits.isEmpty()) {
            List<ExSuit> matched = INSTANCE.getAll().values().stream().filter(exSuit -> containsSuitId(modifierEntry.exsuits, exSuit)).toList();
            if (!matched.isEmpty()) {
                return matched;
            }
        }

        return findSuitsByEntryIdFallback(id, modifierEntry);
    }

    private static List<ExSuit> findSuitsByEntryIdFallback(String entryId, ModifierEntry modifierEntry) {
        if (entryId == null || entryId.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        candidates.add(entryId);

        String rawEntryId = entryId;
        if (hasKnownTypePrefix(entryId) && entryId.length() > 2) {
            rawEntryId = entryId.substring(2);
            candidates.add(rawEntryId);
        }

        if (modifierEntry != null && modifierEntry.types != null) {
            for (ItemType type : modifierEntry.types) {
                if (type == null || type.name() == null || type.name().isEmpty()) {
                    continue;
                }
                String typeName = type.name();
                String prefix = typeName.length() >= 2 ? typeName.substring(0, 2) : typeName;
                candidates.add(prefix + rawEntryId);
            }
        }

        List<ExSuit> fallback = new ArrayList<>();
        for (ExSuit exSuit : LoadExSuit.values()) {
            for (String candidate : candidates) {
                if (isSuitIdMatched(candidate, exSuit)) {
                    fallback.add(exSuit);
                    break;
                }
            }
        }
        return fallback;
    }
    public static List<ExSuit> FindExSuit(String id){
        if (id == null || id.isEmpty()) {
            return List.of();
        }
        List<ExSuit> exSuits = new ArrayList<>();
        for (ExSuit exSuit : LoadExSuit.values()){
//            if (exSuit.type == ExType.ALL.get()) {
//                if (exSuit.entry.stream().anyMatch(entry -> entry.substring(2).equals(id.substring(2)))) {
//                    Exmodifier.LOGGER.debug("Found About ExSuit: " + exSuit.id);
//                    exSuits.add(exSuit);
//                }
//            }else
                {
                    if (isSuitIdMatched(id, exSuit)) {
                        Exmodifier.LOGGER.debug("Found About ExSuit: " + exSuit.id);
                        exSuits.add(exSuit);
                    }
                }

        }
        return exSuits;
    }
    public static void addSuitLevel(Player player,ExSuit s,int amount){
        player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            if (capability.SuitsNum.containsKey(s.id)){
                capability.SuitsNum.put(s.id,capability.SuitsNum.get(s.id)+amount);
            }else {
                capability.SuitsNum.put(s.id,amount);
            }
            capability.syncPlayerVariables(player);
        });
    }
//    public static boolean hasSuitByID(Player player,String id){
//        AtomicBoolean flag = new AtomicBoolean(false);
//        player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
//            for (ExSuit exSuit : capability.Suits){
//                if (exSuit.id.equals(id)){
//                    flag.set(true);
//                    break;
//                }
//            }
//        });
//        return flag.get();
//
//    }
    public int getPlayerLevel(Player player){
        return player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).map(capability -> capability.SuitsNum.values().stream().mapToInt(Integer::intValue).sum()).orElse(0);
    }
    public static int getPlayerLevelFromExSuitId(Player player,String id){
        return player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).map(capability -> capability.SuitsNum.getOrDefault(id, 0)).orElse(0);
    }
    public static void RemoveSuitLevel(Player player,ExSuit s,int amount){
        player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            if (capability.SuitsNum.containsKey(s.id)){
                if (capability.SuitsNum.get(s.id)==amount){
                    capability.SuitsNum.remove(s.id);
                }else {
                    capability.SuitsNum.put(s.id,capability.SuitsNum.get(s.id)-amount);
                }
            }
            capability.syncPlayerVariables(player);
        });
    }
    public static void SetSuitLevel(Player player,ExSuit s,int level){
        player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            if (capability.SuitsNum.containsKey(s.id)){
                capability.SuitsNum.put(s.id,level);
            }else {
                capability.SuitsNum.put(s.id,level);
            }
            capability.syncPlayerVariables(player);
        });
    }
    public static Integer GetSuitLevel(Player player,ExSuit s){
        return player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).map(capability -> capability.SuitsNum.getOrDefault(s.id, 0)).orElse(0);
    }

    private static ItemType resolveSuitEntryType(MoConfig moconfig, JsonObject itemObject, String entryKey) {
        ItemType fallback = moconfig == null ? null : moconfig.type;
        if (itemObject != null && itemObject.has("type") && itemObject.get("type").isJsonPrimitive()) {
            String rawType = itemObject.get("type").getAsString();
            ItemType parsed = ModifierEntry.StringToType(rawType);
            if (parsed != null && parsed != ExType.UNKNOWN.get()) {
                return parsed;
            }
            Exmodifier.LOGGER.debug("Suit entry type is unknown, use file type fallback. entry=" + entryKey + " type=" + rawType);
        }
        return fallback != null ? fallback : ExType.UNKNOWN.get();
    }

    public static void processSuitEntry(MoConfig moconfig, Map.Entry<String, JsonElement> entry, List<ExSuit> entries) throws FileNotFoundException {
        JsonElement itemElement = entry.getValue();
        if (!itemElement.isJsonObject()) {
            return;
        }
        JsonObject itemObject = itemElement.getAsJsonObject();
        ensureSuitMoConfigType(moconfig);
        if (moconfig.type == null) {
            Exmodifier.LOGGER.Logger.error("Skipping suit entry '" + entry.getKey() + "': type is null and could not be resolved. Path: " + moconfig.configFile, new RuntimeException("null type"));
            return;
        }
        ExSuit exSuit = new ExSuit();
        ItemType effectiveType = resolveSuitEntryType(moconfig, itemObject, entry.getKey());
        String string = effectiveType.name();
        String key1 = entry.getKey();
        String key2 = (string.length() >= 2 ? string.substring(0, 2) : string) + key1;
        if (effectiveType == ExType.ALL.get()) {
            boolean linked = false;
            for (ItemType type : ExTypeHandle.itemTypes.values()){
                String typeName = type.name();
                String key = (typeName.length() >= 2 ? typeName.substring(0, 2) : typeName) + key1;
                // Exmodifier.LOGGER.debug("匹配中: "+key);
                ModifierEntry entry1 = ModifierHandle.modifierEntryMap.get(key);
                if (entry1 != null) {
                    bindSuitToModifierEntry(entry1, key1, key2);
                    linked = true;
                    Exmodifier.LOGGER.debug("Add About ModifierEntry: "+entry1.id +" in "+ key1);
                }
            }
            ModifierEntry entry1 = ModifierHandle.modifierEntryMap.get(key1);
            if (entry1 != null) {
                bindSuitToModifierEntry(entry1, key1, key2);
                linked = true;
                Exmodifier.LOGGER.debug("Add About ModifierEntry: "+entry1.id +" in "+ key1);
            }
            if (!linked) {
                Exmodifier.LOGGER.Logger.error("No ModifierEntry Found any one about: " + key1);
            }
        }
        else {
            boolean linked = false;
            ModifierEntry entry1 = ModifierHandle.modifierEntryMap.get(key2);
            if (entry1 != null) {
                bindSuitToModifierEntry(entry1, key1, key2);
                linked = true;
                Exmodifier.LOGGER.debug("Found About ModifierEntry: "+entry1.id);
            }
            ModifierEntry entry2 = ModifierHandle.modifierEntryMap.get( key1);
            if (entry2 != null) {
                bindSuitToModifierEntry(entry2, key1, key2);
                linked = true;
                Exmodifier.LOGGER.debug("Found About ModifierEntry: "+entry2.id);
            }
            if (!linked) {
                Exmodifier.LOGGER.Logger.error("No ModifierEntry Found: " + key2 + " or " + key1);
            }
        }
//        if (exSuit.entry.isEmpty()) {
//            if (moconfig.type!= ExType.ALL.get()) {
//                Exmodifier.LOGGER.Logger.error("No ModifierEntry Found: " + string.substring(0, 2) + key1);
//            }else Exmodifier.LOGGER.Logger.error("No ModifierEntry Found any one about: " + key1);
//            return;
//        }
    exSuit.type = effectiveType;
        exSuit.id = key2;
        if (itemObject.has("visible"))exSuit.visible= itemObject.get("visible").getAsBoolean();
        if (itemObject.has("newTooltipPage"))exSuit.newTooltipPage= itemObject.get("newTooltipPage").getAsBoolean();
        if (itemObject.has("LocalDescription"))exSuit.LocalDescription= itemObject.get("LocalDescription").getAsString();
        if (itemObject.has("levelDescription") && itemObject.get("levelDescription").isJsonObject()) {
            JsonObject levelDesc = itemObject.getAsJsonObject("levelDescription");
            for (Map.Entry<String, JsonElement> descEntry : levelDesc.entrySet()) {
                try {
                    int lvl = Integer.parseInt(descEntry.getKey());
                    exSuit.levelDescription.put(lvl, descEntry.getValue().getAsString());
                } catch (Exception ignored) {}
            }
        }
        if (itemObject.has("effectLocalDescription") && itemObject.get("effectLocalDescription").isJsonObject()) {
            JsonObject levelDesc = itemObject.getAsJsonObject("effectLocalDescription");
            for (Map.Entry<String, JsonElement> descEntry : levelDesc.entrySet()) {
                try {
                    int lvl = Integer.parseInt(descEntry.getKey());
                    exSuit.effectLocalDescription.put(lvl, descEntry.getValue().getAsString());
                } catch (Exception ignored) {}
            }
        }
        // if (itemObject.has("trigger")) exSuit.MainTrigger = StringToTrigger(itemObject.get("trigger").getAsString());
        if (itemObject.has("excludeArmorInHand"))exSuit.setting.put("excludeArmorInHand", String.valueOf(itemObject.get("excludeArmorInHand").getAsBoolean()));
        for (int i = 0; i <= 16; i++) {
            if (itemObject.has(i + "")) {
                JsonObject suitObj = itemObject.getAsJsonObject(i + "");
                if (suitObj.has("description")) {
                    exSuit.levelDescription.put(i, suitObj.get("description").getAsString());
                }
                if (suitObj.has("effectLocalDescription")) {
                    exSuit.effectLocalDescription.put(i, suitObj.get("effectLocalDescription").getAsString());
                }
                ExSuit.Trigger trigger = suitObj.has("trigger") ?  StringToTrigger(suitObj.get("trigger").getAsString()) : ExSuit.MainTrigger;
                exSuit.setLevelTriggers(i, trigger);
                if (suitObj.has("effect")) {
                    if (suitObj.getAsJsonObject("effect") != null) {
                        exSuit.setLevelEffects(i, processEffects(moconfig, exSuit, suitObj.getAsJsonObject("effect")));
                    }
                }else {
                    Exmodifier.LOGGER.debug("No effect Found: " + string.substring(0,2) + key1);
                }
                if (suitObj.has("commands")) {
                    JsonArray commands = suitObj.getAsJsonArray("commands");
                    if (commands != null) {
                        List<String> commands1 = new ArrayList<>();
                        for (JsonElement command : commands){
                            commands1.add(command.getAsString());
                        }
                        exSuit.commands.put(i, commands1);

                    }
                }else {
                    Exmodifier.LOGGER.debug("No command Found: " + string.substring(0,2) + key1);
                }
                if (suitObj.has("specialEffects")) {
                    List<String> levelSpecialEffects = processSpecialEffects(moconfig, exSuit, suitObj.get("specialEffects"), i);
                    if (!levelSpecialEffects.isEmpty()) {
                        exSuit.setLevelSpecialEffects(i, levelSpecialEffects);
                    }
                }
                if (suitObj.has("attrGethers")) {
                    if (suitObj.getAsJsonObject("attrGethers") != null) {
                        exSuit.setLevelAttriGether(i,processAttrGethers(moconfig, exSuit, suitObj.getAsJsonObject("attrGethers"),i));
                    }
                }

            }
        }
        exSuit.CountMaxLevelAndGet();
        entries.add(exSuit);
    }

    private static List<String> processSpecialEffects(MoConfig moconfig, ExSuit exSuit, JsonElement specialEffectsElement, int level) {
        List<String> specialEffectIds = new ArrayList<>();
        if (specialEffectsElement == null || !specialEffectsElement.isJsonArray()) {
            Exmodifier.LOGGER.Logger.error("specialEffects should be a JSON array. suit=" + exSuit.id + " level=" + level);
            return specialEffectIds;
        }

        JsonArray specialEffects = specialEffectsElement.getAsJsonArray();
        for (JsonElement specialEffectElement : specialEffects) {
            String specialEffectId = parseSpecialEffectId(specialEffectElement);
            if (specialEffectId == null || specialEffectId.isEmpty()) {
                continue;
            }

            if (SpecialEffectHandle.getSpecialEffect(specialEffectId) == null) {
                Exmodifier.LOGGER.debug("Suit special effect id not found now: " + specialEffectId + " in suit=" + exSuit.id + " level=" + level);
            }
            specialEffectIds.add(specialEffectId);
        }
        return specialEffectIds;
    }

    private static String parseSpecialEffectId(JsonElement specialEffectElement) {
        if (specialEffectElement == null || specialEffectElement.isJsonNull()) {
            return null;
        }

        if (specialEffectElement.isJsonPrimitive()) {
            return specialEffectElement.getAsString();
        }

        if (!specialEffectElement.isJsonObject()) {
            return null;
        }

        JsonObject specialEffectObject = specialEffectElement.getAsJsonObject();
        if (specialEffectObject.has("id") && specialEffectObject.get("id").isJsonPrimitive()) {
            return specialEffectObject.get("id").getAsString();
        }

        if (specialEffectObject.entrySet().size() == 1) {
            return specialEffectObject.entrySet().iterator().next().getKey();
        }

        return null;
    }

    private static List<MobEffectInstance> processEffects(MoConfig moconfig, ExSuit exSuit, JsonObject attrGethers) {
        List<MobEffectInstance> effects = new ArrayList<>();
        for (Map.Entry<String, JsonElement> EffectEntry : attrGethers.entrySet()) {
            try {
                effects.add(processEffect(moconfig, exSuit, EffectEntry));
            } catch (Exception e) {
                Exmodifier.LOGGER.Logger.error("Error processing attrGether: " + EffectEntry.getKey(), e);
            }
        }
        return effects;
    }

    private static MobEffectInstance processEffect(MoConfig moconfig, ExSuit exSuit, Map.Entry<String, JsonElement> effectEntry) {

        JsonObject effectObj = effectEntry.getValue().getAsJsonObject();
        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectEntry.getKey()));
        int level = 0;
        int time = 20;
        if (effectObj !=null) {
            if (effectObj.has("level")) {
                level = effectObj.get("level").getAsInt();
            }
            if (effectObj.has("time")) {
                time = effectObj.get("time").getAsInt();
            }
        }
        if (effect != null) {
            Exmodifier.LOGGER.debug("Registered ExSuit: " + exSuit.id + " with effect: " + effectEntry.getKey() + " level: " + level);
            return new MobEffectInstance(effect, time, level,false,true,true);
        }
        Exmodifier.LOGGER.Logger.error("No MobEffect Found: " + effectEntry.getKey());
        return null;
    }

    private static List<AttriGetherNormal> processAttrGethers(MoConfig moconfig, ExSuit exSuit, JsonObject attrGethers, int level) {
        List<AttriGetherNormal> attrGethersToReturn = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, JsonElement> attrGetherEntry : attrGethers.entrySet()) {
            try {
                attrGethersToReturn.add(processAttrGether(moconfig, exSuit, attrGetherEntry,index,level));
                index++;
            } catch (Exception e) {
                Exmodifier.LOGGER.Logger.error("Error processing attrGether: " + attrGetherEntry.getKey(), e);
            }
        }
        ExAddSuitAttrigethersEvent event = new ExAddSuitAttrigethersEvent(moconfig,exSuit,attrGethers,attrGethersToReturn);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getModifierAttriGathers();
    }
    private static AttriGetherNormal processAttrGether(MoConfig moconfig, ExSuit exSuit, Map.Entry<String, JsonElement> attrGetherEntry,int index,int level) {
        JsonObject attrGetherObj = attrGetherEntry.getValue().getAsJsonObject();
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attrGetherEntry.getKey()));
        double attrValue = attrGetherObj.get("value").getAsDouble();
        // UUID uuid = getUUID(attrGetherObj,exSuit);

        AttributeModifier.Operation operation = ExConfigHandle.getOperation(attrGetherObj.get("operation").getAsString());
        String modifierName = (attrGetherObj.has("modifierName")) ? attrGetherObj.get("modifierName").getAsString() :exSuit.id + + level +"l" + index;;

        if (attrGetherObj.has("autoName")){
            if (attrGetherObj.has("autoName")) {
                if (attrGetherObj.get("autoName").getAsBoolean()) {
                    modifierName = exSuit.id + index;

                }
            }
        }
        UUID uuid =null ;
        if (attrGetherObj.has("uuid") && !attrGetherObj.get("uuid").getAsString().isEmpty()) {
            uuid = UUID.fromString(attrGetherObj.get("uuid").getAsString());
        }
        else{
            uuid = UUID.nameUUIDFromBytes(modifierName.getBytes());


        }
        if(attrGetherObj.has("autoUUID") && attrGetherObj.get("autoUUID").getAsBoolean()) uuid = UUID.nameUUIDFromBytes(modifierName.getBytes());
        //UUID uuid = ExConfigHandle.generateUUIDFromString(modifierName);
        Exmodifier.LOGGER.debug("uuid "+uuid);
        AttributeModifier modifier = new AttributeModifier(uuid, modifierName, attrValue, operation);

        AttriGetherNormal attrGether = new AttriGetherNormal(attribute, modifier);
        attrGether.hasUUID = attrGetherObj.has("uuid");
        if (attrGetherObj.has("OnlyItems")){
            for (JsonElement item : attrGetherObj.getAsJsonArray("OnlyItems")){
                String asString = item.getAsString();
                Exmodifier.LOGGER.debug("Adding Item: " + asString);
                attrGether.getOnlyItems().add(asString);

            }
        }
        if (attrGetherObj.has("OnlySlots")){
            for (JsonElement item : attrGetherObj.getAsJsonArray("OnlySlots")){
                attrGether.getOnlySlots().add(item.getAsString());
                Exmodifier.LOGGER.debug("Adding Slot: " + item.getAsString());
            }
        }
        Exmodifier.LOGGER.debug("Attribute: " + attribute + " key: " + attrGetherEntry.getKey());
        ExConfigHandle.autoUUID++;
        ExAddSuitAttrigetherEvent event = new ExAddSuitAttrigetherEvent(moconfig,exSuit,attrGetherEntry,index,attrGether);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getAttrGether();
    }
    private static final java.util.Set<String> SUIT_META_KEYS = java.util.Set.of("type", "group", "autoTypeId", "isList");

    private static void ensureSuitMoConfigType(MoConfig moconfig) {
        if (moconfig == null || moconfig.jsonObject == null) return;
        if (moconfig.type != null && moconfig.type != ExType.UNKNOWN.get()) return;
        com.google.gson.JsonElement typeSetting = moconfig.readSetting("type");
        if (typeSetting != null && typeSetting.isJsonPrimitive()) {
            ItemType parsedType = ModifierEntry.StringToType(typeSetting.getAsString());
            if (parsedType != null && parsedType != ExType.UNKNOWN.get()) {
                moconfig.type = parsedType;
            }
        }
        if (moconfig.type == null) {
            moconfig.type = ExType.UNKNOWN.get();
            Exmodifier.LOGGER.debug("Suit config type unresolved, fallback to UNKNOWN. Path: " + moconfig.configFile);
        }
    }

    public static void processMoConfigEntries(MoConfig moconfig) throws FileNotFoundException {
        ensureSuitMoConfigType(moconfig);
        if(moconfig.readEntrys().isEmpty()){
            Exmodifier.LOGGER.info("No Suit Config Found");
            return;
        }
        List<ExSuit> entries = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : moconfig.readEntrys()) {
            if (SUIT_META_KEYS.contains(entry.getKey())) continue;
            try {
                Exmodifier.LOGGER.debug("Reading Suit Config: " + entry.getKey());
                processSuitEntry(moconfig, entry, entries);
                Exmodifier.LOGGER.debug("Reading Suit Config Over: " + entry.getKey());
            } catch (Exception e) {
                Exmodifier.LOGGER.Logger.error("Error processing modifier entry: " + entry.getKey(), e);
            }
        }
        for (ExSuit exSuit : entries){
            registerExSuit(exSuit);
        }
    }
    public static List<ExSuit> processMoConfigEntries(JsonObject jsonObject) throws FileNotFoundException {

        ItemType fileType = ExType.UNKNOWN.get();
        if (jsonObject.has("type") && jsonObject.get("type").isJsonPrimitive()) {
            fileType = ModifierEntry.StringToType(jsonObject.get("type").getAsString());
        }

        List<ExSuit> entries = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            if (SUIT_META_KEYS.contains(entry.getKey())) {
                continue;
            }
            try {
                Exmodifier.LOGGER.debug("Reading Suit Config: " + entry.getKey());
                MoConfig moconfig = new MoConfig(null, true);
                moconfig.type = fileType;
                processSuitEntry(moconfig, entry, entries);
                Exmodifier.LOGGER.debug("Reading Suit Config Over: " + entry.getKey());
            } catch (Exception e) {
                Exmodifier.LOGGER.Logger.error("Error processing modifier entry: " + entry.getKey(), e);
            }
        }
        return entries;
    }
    public static void init() throws Exception {

    }
}
