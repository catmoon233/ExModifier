package net.exmo.exmodifier.content.suit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.modifier.MoConfig;
import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.network.ExModifiervaV;
import net.exmo.exmodifier.util.ExConfigHandle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.getUUID;

public class ExSuitHandle {
    public static Map<String, ExSuit> LoadExSuit = new java.util.HashMap<>();
    public static ExSuit FoundAllTypeSuitById(String id){

        for (ExSuit exSuit : LoadExSuit.values()){
            if (exSuit.entry.stream().anyMatch(entry -> entry.id.equals(id))){
                return exSuit;
            }
        }
      return null;

    }
    public static List<ExSuit> FindExSuit(String id){
        List<ExSuit> exSuits = new ArrayList<>();
        for (ExSuit exSuit : LoadExSuit.values()){
            if (exSuit.type == ModifierEntry.Type.ALL){
                if (exSuit.entry.stream().anyMatch(entry -> entry.id.substring(2).equals(id.substring(2))))
                {
                    Exmodifier.LOGGER.debug("Found About ExSuit: "+exSuit.id);
                    exSuits.add(exSuit);
                }else
                {
                    if (exSuit.id.equals(id)) {
                        Exmodifier.LOGGER.debug("Found About ExSuit: " + exSuit.id);
                        exSuits.add(exSuit);
                    }
                }
            }
        }
        return exSuits;
    }
    public static void addSuitLevel(Player player,String s,int amount){
        player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            if (capability.SuitsNum.containsKey(s)){
                capability.SuitsNum.put(s,capability.SuitsNum.get(s)+amount);
            }else {
                capability.SuitsNum.put(s,amount);
            }
            capability.syncPlayerVariables(player);
        });
    }
    public static void RemoveSuitLevel(Player player,String s,int amount){
        player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            if (capability.SuitsNum.containsKey(s)){
                if (capability.SuitsNum.get(s)==amount){
                    capability.SuitsNum.remove(s);
                }else {
                    capability.SuitsNum.put(s,capability.SuitsNum.get(s)-amount);
                }
            }
            capability.syncPlayerVariables(player);
        });
    }
    public static void SetSuitLevel(Player player,String s,int level){
        player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            if (capability.SuitsNum.containsKey(s)){
                capability.SuitsNum.put(s,level);
            }else {
                capability.SuitsNum.put(s,level);
            }
            capability.syncPlayerVariables(player);
        });
    }
    public static Integer GetSuitLevel(Player player,String s){
        return player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).map(capability -> capability.SuitsNum.getOrDefault(s, 0)).orElse(0);
    }
    public static void RegisterExSuit(ExSuit exSuit){
        LoadExSuit.put(exSuit.id,exSuit);
        Exmodifier.LOGGER.info("Registered ExSuit: "+ exSuit);
    }
    public static Path ConfigPath = FMLPaths.GAMEDIR.get().resolve("config/exmo/suit");
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
    public static void processModifierEntry(MoConfig moconfig, Map.Entry<String, JsonElement> entry, List<ExSuit> entries) throws FileNotFoundException {
        JsonElement itemElement = entry.getValue();
        if (!itemElement.isJsonObject()) {
            return;
        }
        JsonObject itemObject = itemElement.getAsJsonObject();
        ExSuit exSuit = new ExSuit();
        if (moconfig.type== ModifierEntry.Type.ALL){
            List<ModifierEntry.Type> types = List.of(ModifierEntry.Type.values());
            for (ModifierEntry.Type type : types){
                String key = type.toString().substring(0, 2) + entry.getKey();
               // Exmodifier.LOGGER.debug("匹配中: "+key);
                ModifierEntry entry1 = ModifierHandle.modifierEntryMap.get(key);
                if (entry1 != null) {
                    exSuit.addEntry(entry1);
                    Exmodifier.LOGGER.debug("Add About ModifierEntry: "+entry1.id +" in "+entry.getKey());
                }
            }
        }
        else {
            ModifierEntry entry1 = ModifierHandle.modifierEntryMap.get(moconfig.type.toString().substring(0, 2) + entry.getKey());
            if (entry1 != null) {
                exSuit.addEntry(entry1);
                Exmodifier.LOGGER.debug("Found About ModifierEntry: "+entry1.id);
            }else Exmodifier.LOGGER.error("No ModifierEntry Found: " + moconfig.type.toString().substring(0,2) + entry.getKey());

        }
        if (exSuit.entry.isEmpty()) {
            if (moconfig.type!= ModifierEntry.Type.ALL) {
                Exmodifier.LOGGER.error("No ModifierEntry Found: " + moconfig.type.toString().substring(0, 2) + entry.getKey());
            }else Exmodifier.LOGGER.error("No ModifierEntry Found any one about: " + entry.getKey());
            return;
        }
        exSuit.type = moconfig.type;
            exSuit.id = moconfig.type.toString().substring(0,2) + entry.getKey();
        if (itemObject.has("excludeArmorInHand"))exSuit.setting.put("excludeArmorInHand", String.valueOf(itemObject.get("excludeArmorInHand").getAsBoolean()));
        for (int i = 1; i <= 10; i++) {
            if (itemObject.has(i + "")) {
                JsonObject suitObj = itemObject.getAsJsonObject(i + "");
                if (suitObj.has("effect")) {
                    if (suitObj.getAsJsonObject("effect") != null) {
                      exSuit.setLevelEffects(i, processEffects(moconfig, exSuit, suitObj.getAsJsonObject("effect")));
                    }
                }else {
                    Exmodifier.LOGGER.debug("No effect Found: " + moconfig.type.toString().substring(0,2) + entry.getKey());
                }
                if (suitObj.has("attrGethers")) {
                    if (suitObj.getAsJsonObject("attrGethers") != null) {
                       exSuit.setLevelAttriGether(i,processAttrGethers(moconfig, exSuit, suitObj.getAsJsonObject("attrGethers")));
                    }
                }

            }
        }
        entries.add(exSuit);
    }

    private static List<MobEffectInstance> processEffects(MoConfig moconfig, ExSuit exSuit, JsonObject attrGethers) {
        List<MobEffectInstance> effects = new ArrayList<>();
        for (Map.Entry<String, JsonElement> EffectEntry : attrGethers.entrySet()) {
            try {
               effects.add(processEffect(moconfig, exSuit, EffectEntry));
            } catch (Exception e) {
                Exmodifier.LOGGER.error("Error processing attrGether: " + EffectEntry.getKey(), e);
            }
        }
        return effects;
    }

    private static MobEffectInstance processEffect(MoConfig moconfig, ExSuit exSuit, Map.Entry<String, JsonElement> effectEntry) {

        JsonObject effectObj = effectEntry.getValue().getAsJsonObject();
        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectEntry.getKey()));
        int level = 0;
        if (effectObj !=null) {
            if (effectObj.has("level")) {
                level = effectObj.get("level").getAsInt();
            }
        }
        if (effect != null) {
            Exmodifier.LOGGER.debug("Registered ExSuit: " + exSuit.id + " with effect: " + effectEntry.getKey() + " level: " + level);
            return new MobEffectInstance(effect, 20, level,false,true,true);
        }
        Exmodifier.LOGGER.error("No MobEffect Found: " + effectEntry.getKey());
        return null;
    }

    private static List<ModifierAttriGether> processAttrGethers(MoConfig moconfig, ExSuit exSuit, JsonObject attrGethers) {
        List<ModifierAttriGether> attrGethersToReturn = new ArrayList<>();
        int index = 0;
            for (Map.Entry<String, JsonElement> attrGetherEntry : attrGethers.entrySet()) {
                try {
                    attrGethersToReturn.add(processAttrGether(moconfig, exSuit, attrGetherEntry,index));
                    index++;
                } catch (Exception e) {
                    Exmodifier.LOGGER.error("Error processing attrGether: " + attrGetherEntry.getKey(), e);
                }
            }
            return attrGethersToReturn;
    }
    private static ModifierAttriGether processAttrGether(MoConfig moconfig, ExSuit exSuit, Map.Entry<String, JsonElement> attrGetherEntry,int index) {
        JsonObject attrGetherObj = attrGetherEntry.getValue().getAsJsonObject();
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attrGetherEntry.getKey()));
        double attrValue = attrGetherObj.get("value").getAsDouble();
        UUID uuid = getUUID(attrGetherObj,exSuit);

        AttributeModifier.Operation operation = ExConfigHandle.getOperation(attrGetherObj.get("operation").getAsString());
        String modifierName = attrGetherObj.get("modifierName").getAsString();
        if (attrGetherObj.has("autoName")){
            if (attrGetherObj.get("autoName").getAsBoolean())
            {
                modifierName = exSuit.id + index;

            }
        }
        if (attrGetherObj.has("autoUUID")){
            if (attrGetherObj.get("autoUUID").getAsBoolean()) {
                uuid = UUID.nameUUIDFromBytes(modifierName.getBytes());

            }
        }
        AttributeModifier modifier = new AttributeModifier(uuid, modifierName, attrValue, operation);

        ModifierAttriGether attrGether = new ModifierAttriGether(attribute, modifier);
        attrGether.hasUUID = attrGetherObj.has("uuid");
        if (attrGetherObj.has("OnlyItems")){
            for (JsonElement item : attrGetherObj.getAsJsonArray("OnlyItems")){
                attrGether.OnlyItems.add(item.getAsString());
            }
        }
        if (attrGetherObj.has("OnlySlots")){
            for (JsonElement item : attrGetherObj.getAsJsonArray("OnlySlots")){
                attrGether.OnlySlots.add(item.getAsString());
                Exmodifier.LOGGER.debug("Adding Slot: " + item.getAsString());
            }
        }
        Exmodifier.LOGGER.debug("Attribute: " + attribute + " key: " + attrGetherEntry.getKey());
        ExConfigHandle.autoUUID++;
        return attrGether;
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
                processModifierEntry(moconfig, entry, entries);
                Exmodifier.LOGGER.debug("Reading Suit Config Over: " + entry.getKey());
            } catch (Exception e) {
                Exmodifier.LOGGER.error("Error processing modifier entry: " + entry.getKey(), e);
            }
        }
        for (ExSuit exSuit : entries){
            RegisterExSuit(exSuit);
        }
    }
    public static void init() throws Exception {

    }
}
