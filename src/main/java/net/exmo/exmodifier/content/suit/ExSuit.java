package net.exmo.exmodifier.content.suit;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.type.ItemType;
import net.exmo.exmodifier.util.NBTCounterUtil;
import net.exmo.exmodifier.util.exSerialize.ExSerialize;
import net.exmo.exmodifier.util.gether.AttriGetherNormal;
import net.minecraft.nbt.*;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.*;
import java.util.stream.Collectors;

import static net.exmo.exmodifier.Exmodifier.GSON;

public class ExSuit {
public static ExSerialize<ExSuit> ExSer = ExSerialize.create(ExSuit::new)
    // 基础字段
    .addStringField("id", ExSuit::getId, (obj, id) -> obj.id = id)
    .addStringField("type",
        e -> e.type != null ? e.type.name() : "",
        (e, s) -> e.type = ModifierEntry.StringToType(s))
    .addStringField("LocalDescription", ExSuit::getLocalDescription, (obj, desc) -> obj.LocalDescription = desc)
    // 特殊类型处理
    .addBooleanField("visible", ExSuit::isVisible, (obj, visible) -> obj.visible = visible)
    // 只读字段
    .addIntField("MaxLevel", ExSuit::getMaxLevel, ExSuit::setMaxLevel)
        .addBooleanField("newTooltipPage", ExSuit::isNewTooltipPage, ExSuit::setNewTooltipPage)
        .addBooleanField("hasMobEffect",  ExSuit::isHasMobEffect, (obj, hasMobEffect) -> obj.hasMobEffect = hasMobEffect)
        .addIntStringMapField("effectLocalDescription", ExSuit::getEffectLocalDescription, ExSuit::setEffectLocalDescription)
        .addIntStringMapField("levelDescription", ExSuit::getLevelDescription, ExSuit::setLevelDescription)
         .addIntStringMapField("commands", 
                exSuit -> exSuit.getCommands().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> String.join("=-;-=", e.getValue()))),
                (exSuit, commands) -> exSuit.commands = commands.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> Arrays.asList(e.getValue().split("=-;-="))))
        )
        .addIntStringMapField("specialEffects",
            exSuit -> exSuit.getSpecialEffects().entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> String.join("=-;-=", e.getValue() == null ? List.of() : e.getValue()))),
            (exSuit, specialEffects) -> exSuit.specialEffects = specialEffects.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> {
                        if (e.getValue() == null || e.getValue().isEmpty()) {
                        return new ArrayList<>();
                        }
                        return Arrays.stream(e.getValue().split("=-;-="))
                            .filter(value -> !value.isEmpty())
                            .collect(Collectors.toCollection(ArrayList::new));
                    }))
        )
        .addIntStringMapField("triggers",
                exSuit -> exSuit.getTriggers().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().toString())),
                (exSuit, triggers) -> exSuit.triggers = triggers.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> ExSuit.StringToTrigger(e.getValue())))
        )
        .addIntStringMapField( "attriGether",
                exSuit -> exSuit.getAttriGether().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().stream()
                                        .map(ag -> ag.toNbt().toString())
                                        .collect(Collectors.joining(";"))  // 使用分号连接字符串
                        )),
                (exSuit, attriGethers) -> exSuit.attriGether = attriGethers.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> Arrays.stream(e.getValue().split(";"))  // 使用分号拆分字符串
                                        .map(a-> {
                                            try {
                                                return AttriGetherNormal.fromNbt(TagParser.parseTag(a));
                                            } catch (CommandSyntaxException ex) {
                                                throw new RuntimeException(ex);
                                            }
                                        })
                                        .collect(Collectors.toList())
                        ))
        )
        .addStringMapField("setting",ExSuit::getSetting,ExSuit::setSetting)
        .addIntStringMapField( "effect",
                exSuit -> exSuit.getEffect().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().stream()
                                        .map(ExSuit::getEffectString)
                                        .collect(Collectors.joining(";"))  // 修改为用分号连接字符串
                        )),
                (exSuit, attriGethers) -> exSuit.effect = attriGethers.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> Arrays.stream(e.getValue().split(";"))  // 修改为用分号拆分字符串
                                        .map(ExSuit::getEffectFromString)
                                        .collect(Collectors.toList())
                        ))
        )



    ;

    public Map<String,String> setting = new java.util.HashMap<>();
    public boolean hasMobEffect = false;
    public ItemType type;
    public String id;


    public boolean isNewTooltipPage() {
        return newTooltipPage;
    }

    public ExSuit setNewTooltipPage(boolean newTooltipPage) {
        this.newTooltipPage = newTooltipPage;
        return this;
    }

    public boolean newTooltipPage;

    public String getLocalDescription() {
        return LocalDescription;
    }

    public ExSuit setLocalDescription(String localDescription) {
        LocalDescription = localDescription;
        return this;
    }

    public static ExSerialize<ExSuit> getExSer() {
        return ExSer;
    }

    public static void setExSer(ExSerialize<ExSuit> exSer) {
        ExSer = exSer;
    }

    public Map<String, String> getSetting() {
        return setting;
    }

    public ExSuit setSetting(Map<String, String> setting) {
        this.setting = setting;
        return this;
    }

    public boolean isHasMobEffect() {
        return hasMobEffect;
    }

    public ExSuit setHasMobEffect(boolean hasMobEffect) {
        this.hasMobEffect = hasMobEffect;
        return this;
    }

    public ItemType getType() {
        return type;
    }

    public ExSuit setType(ItemType type) {
        this.type = type;
        return this;
    }

    public String getId() {
        return id;
    }

    public ExSuit setId(String id) {
        this.id = id;
        return this;
    }

    public int getMaxLevel() {
        return MaxLevel;
    }

    public ExSuit setMaxLevel(int maxLevel) {
        MaxLevel = maxLevel;
        return this;
    }

    public boolean isVisible() {
        return visible;
    }

    public ExSuit setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public Map<String, Float> getItemDamage() {
        return itemDamage;
    }

    public ExSuit setItemDamage(Map<String, Float> itemDamage) {
        this.itemDamage = itemDamage;
        return this;
    }

    public String LocalDescription ="";

    public Map<Integer, String> getEffectLocalDescription() {
        return effectLocalDescription;
    }

    public ExSuit setEffectLocalDescription(Map<Integer, String> effectLocalDescription) {
        this.effectLocalDescription = effectLocalDescription;
        return this;
    }

    public Map<Integer, String> getLevelDescription() {
        return levelDescription;
    }

    public ExSuit setLevelDescription(Map<Integer, String> levelDescription) {
        this.levelDescription = levelDescription;
        return this;
    }

    public Map<Integer,String> effectLocalDescription= new HashMap<>();
    public Map<Integer,String> levelDescription= new HashMap<>();
	public Map<Integer,List<String>> commands = new HashMap<>();
    public Map<Integer, List<String>> specialEffects = new HashMap<>();
    public int MaxLevel ;
    public boolean visible = true;

    public Map<Integer, Trigger> getTriggers() {
        return triggers;
    }

    public void setTriggers(Map<Integer, Trigger> triggers) {
        this.triggers = triggers;
    }
    public void setLevelTriggers(int level, Trigger trigger) {
        triggers.put(level,trigger);
    }

    public static final Trigger MainTrigger = Trigger.TICK;
    public Map<Integer,Trigger> triggers = new HashMap<>();
    public Map<Integer, List<AttriGetherNormal>> getAttriGether() {
        return attriGether;
    }    public Map<Integer, List<AttriGetherNormal>> getAttriGetherC() {
        return new HashMap<>(attriGether);

    }
    public static enum Trigger {
        TICK, ON_HURT, ATTACK,ATTACK_START, JUMP, SHOOT, EAT, DODGE, CRIT, KILL, DIE,MOVECHANGE,SWING,PROJECTILE_HIT,ON_USE, SWIM, IN_LAVA,DIG;
    }

    @Override
    public String toString() {
        return "ExSuit{" +
                "setting=" + setting +
                ", hasMobEffect=" + hasMobEffect +
                ", type=" + type +
                ", Id='" + id + '\'' +
                ", LocalDescription='" + LocalDescription + '\'' +
                ", levelDescription=" + levelDescription +
                ", commands=" + commands +
                ", specialEffects=" + specialEffects +
                ", MaxLevel=" + MaxLevel +
                ", visible=" + visible +
                ", MainTrigger=" + MainTrigger +
                ", triggers=" + triggers +
                ", attriGether=" + attriGether +
                ", effect=" + effect +
                ", itemDamage=" + itemDamage +
                '}';
    }

    public static Trigger StringToTrigger(String trigger){
        for (Trigger trigger1 : Trigger.values()){
            if (trigger1.toString().equalsIgnoreCase(trigger)){
                return trigger1;
            }
        }
        return Trigger.TICK;
//        return switch (trigger.toLowerCase()) {
//            case "on_hurt" -> Trigger.ON_HURT;
//            case "attack" -> Trigger.ATTACK;
//            case "jump" -> Trigger.JUMP;
//            case "shoot" -> Trigger.SHOOT;
//            case "eat" -> Trigger.EAT;
//            case "dodge" -> Trigger.DODGE;
//            case "crit" -> Trigger.CRIT;
//            case "projectile_hit"-> Trigger.PROJECTILE_HIT;
//            case "movechange" -> Trigger.MOVECHANGE;
//            case "kill" -> Trigger.KILL;
//            case "swing" -> Trigger.SWING;
//            case "die" -> Trigger.DIE;
//            default -> Trigger.TICK;
//        };
    }

    public String getSetting(String key){
        if (setting.containsKey(key))
            return setting.get(key);
        return null;
    }
    //public List<String> entry = new ArrayList<>();
    public Map<Integer,List<AttriGetherNormal>> attriGether = new java.util.HashMap<>();
    private   Map<Integer,List<MobEffectInstance> > effect = new java.util.HashMap<>();
    public Map<String,Float> itemDamage = new java.util.HashMap<>();
    public ExSuit(){

    }

    public static String getEffectString(MobEffectInstance mobEffectInstance){
        String asString = mobEffectInstance.save(new CompoundTag()).getAsString();
        return asString;
    }
    public static MobEffectInstance getEffectFromString(String effectString){
        try {
            return MobEffectInstance.load((TagParser.parseTag(effectString)));
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }

    }
    public int CountMaxLevelAndGet() {
        int maxLevel = 0;

        maxLevel = Math.max(maxLevel, getMaxLevel(attriGether));
        maxLevel = Math.max(maxLevel, getMaxLevel(effect));
        maxLevel = Math.max(maxLevel, getMaxLevel(specialEffects));

        this.MaxLevel = maxLevel;
        return maxLevel;
    }

    public void CountMaxLevel(int maxLevel) {
        this.MaxLevel = Math.max(maxLevel, CountMaxLevelAndGet());

    }

    private static int getMaxLevel(Map<Integer, ?> levelMap) {
        if (levelMap == null || levelMap.isEmpty()) {
            return 0;
        }
        return Collections.max(levelMap.keySet());
    }
    public ExSuit(String id, Map<Integer,List< AttriGetherNormal>> attriGether) {
        this.id = id;
        this.attriGether = attriGether;
//        this.MaxLevel = Collections.max(attriGether.keySet());
    }




    public Map<Integer, List<MobEffectInstance>> getEffect() {
        return effect;
    }
    public void setLevelAttriGether(int level,List< AttriGetherNormal> attriGether) {
        Exmodifier.LOGGER.debug("setLevelAttriGether " + level + " " + attriGether);
        if (attriGether != null) {
            this.attriGether.put(level, attriGether);
        }
    }
    public void setAttriGether(Map<Integer,List< AttriGetherNormal>> attriGether) {
        this.attriGether = attriGether;
    }

    public void setEffect(Map<Integer, List<MobEffectInstance>> effect) {
        Exmodifier.LOGGER.debug("setEffect " + effect);
        this.effect = effect;
        if (!effect.isEmpty()){
            hasMobEffect = true;
        }
    }
    public void setLevelEffects(int level,List<MobEffectInstance> mobEffectInstances){
        Exmodifier.LOGGER.debug("setLevelEffects " + level + " " + mobEffectInstances);
        effect.put(level,mobEffectInstances);
        hasMobEffect = true;
    }
    public void addEffect(int level,MobEffectInstance mobEffectInstance){
        if (!effect.containsKey(level)){
            effect.put(level,new java.util.ArrayList<>());
        }
        effect.get(level).add(mobEffectInstance);
        hasMobEffect = true;
    }

    public Map<Integer, List<String>> getCommands() {
        return commands;
    }

    public void setCommands(Map<Integer, List<String>> commands) {
        this.commands = commands;
    }

    public Map<Integer, List<String>> getSpecialEffects() {
        return specialEffects;
    }

    public void setSpecialEffects(Map<Integer, List<String>> specialEffects) {
        this.specialEffects = specialEffects;
    }

    public void setLevelSpecialEffects(int level, List<String> levelSpecialEffects) {
        Exmodifier.LOGGER.debug("setLevelSpecialEffects " + level + " " + levelSpecialEffects);
        if (levelSpecialEffects == null || levelSpecialEffects.isEmpty()) {
            return;
        }
        specialEffects.put(level, new ArrayList<>(levelSpecialEffects));
    }
}
