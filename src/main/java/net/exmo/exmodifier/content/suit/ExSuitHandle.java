package net.exmo.exmodifier.content.suit;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.modifier.MoConfig;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.content.type.ExType;
import net.exmo.exmodifier.content.type.ExTypeHandle;
import net.exmo.exmodifier.content.type.ItemType;
import net.exmo.exmodifier.events.ExAddSuitAttrigetherEvent;
import net.exmo.exmodifier.events.ExAddSuitAttrigethersEvent;
import net.exmo.exmodifier.network.ExModifiervaV;
import net.exmo.exmodifier.util.gether.AttriGetherNormal;
import net.exmo.exmodifier.util.ExConfigHandle;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.getUUID;
import static net.exmo.exmodifier.content.suit.ExSuit.StringToTrigger;

public class ExSuitHandle {
    public static Map<String, ExSuit> LoadExSuit = new java.util.HashMap<>();
    public static ExSuit FoundAllTypeSuitById(String id){

        ModifierEntry  modifierEntry = ModifierHandle.findModifierEntry(id);
        for (ExSuit exSuit : LoadExSuit.values()){
            if (modifierEntry.exsuits .contains(exSuit.id)){
                return exSuit;
            }
        }
        return null;

    }
    public static List<ExSuit> FindExSuitFromEntry(String id){
       return LoadExSuit.values().stream().filter(exSuit -> ModifierHandle.findModifierEntry(id).exsuits.contains(exSuit.id)).toList();
    }
    public static List<ExSuit> FindExSuit(String id){
        List<ExSuit> exSuits = new ArrayList<>();
        for (ExSuit exSuit : LoadExSuit.values()){
//            if (exSuit.type == ExType.ALL.get()) {
//                if (exSuit.entry.stream().anyMatch(entry -> entry.substring(2).equals(id.substring(2)))) {
//                    Exmodifier.LOGGER.debug("Found About ExSuit: " + exSuit.id);
//                    exSuits.add(exSuit);
//                }
//            }else
                {
                    if (exSuit.id.equals(id)) {
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
    public static void registerExSuit(ExSuit exSuit){
        LoadExSuit.put(exSuit.id,exSuit);
        Exmodifier.LOGGER.info("Registered ExSuit: "+ exSuit);
    }
    public static Path ConfigPath = FMLPaths.CONFIGDIR.get().resolve("exmo/suit");
    public static List<MoConfig> FoundSuitConfigs = new ArrayList<>();
    public static void readConfig() throws IOException {

        long startTime = System.nanoTime(); // 记录开始时间

        FoundSuitConfigs = ExConfigHandle.listFiles(ConfigPath);
        for (MoConfig moconfig : FoundSuitConfigs)
        {
            processMoConfigEntries(moconfig);
        }

        long endTime = System.nanoTime(); // 记录结束时间
        long duration = endTime - startTime; // 计算持续时间
        Exmodifier.LOGGER.debug("ReadConfig Suit Over time: " + duration / 1000000 + " ms");
    }
    public static void processSuitEntry(MoConfig moconfig, Map.Entry<String, JsonElement> entry, List<ExSuit> entries) throws FileNotFoundException {
        JsonElement itemElement = entry.getValue();
        if (!itemElement.isJsonObject()) {
            return;
        }
        JsonObject itemObject = itemElement.getAsJsonObject();
        ExSuit exSuit = new ExSuit();
        String string = moconfig.type.name();
        String key1 = entry.getKey();
        String key2 = string.substring(0, 2) + key1;
        if (moconfig.type== ExType.ALL.get()){
            for (ItemType type : ExTypeHandle.itemTypes.values()){
                String key = type.name().substring(0, 2) + key1;
                // Exmodifier.LOGGER.debug("匹配中: "+key);
                ModifierEntry entry1 = ModifierHandle.modifierEntryMap.get(key);
                if (entry1 != null && entry1.autoId) {
                    entry1.exsuits.add(key2);
                    Exmodifier.LOGGER.debug("Add About ModifierEntry: "+entry1.id +" in "+ key1);
                }
            }
            ModifierEntry entry1 = ModifierHandle.modifierEntryMap.get(key1);
            if (entry1 != null && entry1.autoId) {
                entry1.exsuits.add(key2);
                Exmodifier.LOGGER.debug("Add About ModifierEntry: "+entry1.id +" in "+ key1);
            }
        }
        else {
            ModifierEntry entry1 = ModifierHandle.modifierEntryMap.get(key2);
            if (entry1 != null &&  entry1.autoId) {
                entry1.exsuits.add( key2);
                Exmodifier.LOGGER.debug("Found About ModifierEntry: "+entry1.id);
            }else Exmodifier.LOGGER.Logger.error("No ModifierEntry Found: " + string.substring(0,2) + key1);
            ModifierEntry entry2 = ModifierHandle.modifierEntryMap.get( key1);
            if (entry2 != null) {
                entry2.exsuits.add( key2);
                Exmodifier.LOGGER.debug("Found About ModifierEntry: "+entry2.id);
            }else Exmodifier.LOGGER.Logger.error("No ModifierEntry Found: " + key1);
        }
//        if (exSuit.entry.isEmpty()) {
//            if (moconfig.type!= ExType.ALL.get()) {
//                Exmodifier.LOGGER.Logger.error("No ModifierEntry Found: " + string.substring(0, 2) + key1);
//            }else Exmodifier.LOGGER.Logger.error("No ModifierEntry Found any one about: " + key1);
//            return;
//        }
        exSuit.type = moconfig.type;
        exSuit.id = key2;
        if (itemObject.has("visible"))exSuit.visible= itemObject.get("visible").getAsBoolean();
        if (itemObject.has("newTooltipPage"))exSuit.newTooltipPage= itemObject.get("newTooltipPage").getAsBoolean();
        if (itemObject.has("LocalDescription"))exSuit.LocalDescription= itemObject.get("LocalDescription").getAsString();
        // if (itemObject.has("trigger")) exSuit.MainTrigger = StringToTrigger(itemObject.get("trigger").getAsString());
        if (itemObject.has("excludeArmorInHand"))exSuit.setting.put("excludeArmorInHand", String.valueOf(itemObject.get("excludeArmorInHand").getAsBoolean()));
        for (int i = 0; i <= 16; i++) {
            if (itemObject.has(i + "")) {
                JsonObject suitObj = itemObject.getAsJsonObject(i + "");
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
            UUID.fromString(attrGetherObj.get("uuid").getAsString());
        }
        else{
            UUID.nameUUIDFromBytes(modifierName.getBytes());


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
    public static void processMoConfigEntries(MoConfig moconfig) throws FileNotFoundException {
        if(moconfig.readEntrys().isEmpty()){
            Exmodifier.LOGGER.info("No Suit Config Found");
            return;
        }
        List<ExSuit> entries = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : moconfig.readEntrys()) {
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

        List<ExSuit> entries = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            try {
                Exmodifier.LOGGER.debug("Reading Suit Config: " + entry.getKey());
                MoConfig moconfig = new MoConfig(null, true);
                moconfig.type = ModifierEntry.StringToType(jsonObject.get("type").getAsString());
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
