package net.exmo.exmodifier.content.suit;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.util.AttriGetherPlus;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.*;

public class ExSuit {
    public Map<String,String> setting = new java.util.HashMap<>();
    public boolean hasMobEffect = false;
    public ModifierEntry.Type type;
    public String id;
    public String LocalDescription ="";
	public Map<Integer,List<String>> commands = new HashMap<>();
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
    public Map<Integer, List<ModifierAttriGether>> getAttriGether() {
        return attriGether;
    }    public Map<Integer, List<ModifierAttriGether>> getAttriGetherC() {
        return new HashMap<>(attriGether);

    }
    public static enum Trigger {
        TICK, ON_HURT, ATTACK, JUMP, SHOOT, EAT, DODGE, CRIT, KILL, DIE,MOVECHANGE,SWING,PROJECTILE_HIT,ON_USE, SWIM, IN_LAVA;
    }

    @Override
    public String toString() {
        return "ExSuit{" +
                "setting=" + setting +
                ", hasMobEffect=" + hasMobEffect +
                ", type=" + type +
                ", id='" + id + '\'' +
                ", LocalDescription='" + LocalDescription + '\'' +
                ", commands=" + commands +
                ", MaxLevel=" + MaxLevel +
                ", visible=" + visible +
                ", MainTrigger=" + MainTrigger +
                ", triggers=" + triggers +
                ", entry=" + entry +
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
    public List<ModifierEntry> entry = new ArrayList<>();
    public Map<Integer,List< ModifierAttriGether>> attriGether = new java.util.HashMap<>();
    private   Map<Integer,List<MobEffectInstance> > effect = new java.util.HashMap<>();
    public Map<String,Float> itemDamage = new java.util.HashMap<>();
    public ExSuit(){

    }

    public int CountMaxLevelAndGet() {
        int maxLevel = 0;

        // Check if attriGether is not null
        if (attriGether != null) {
            // Check if effect is not null
            if (effect != null && !effect.isEmpty()) {
                // Get the maximum key from effect
                maxLevel = Collections.max(effect.keySet());
            }

            // Get the maximum key from attriGether
            if (!attriGether.isEmpty()) {
                maxLevel = Math.max(maxLevel, Collections.max(attriGether.keySet()));
            }
        }

        this.MaxLevel = maxLevel;
        return maxLevel;
    }

    public void CountMaxLevel(int maxLevel) {
        this.MaxLevel =Math.max(Collections.max(attriGether.keySet()), Collections.max(effect.keySet()));

    }
    public ExSuit(String id, List<ModifierEntry> entry, Map<Integer,List< ModifierAttriGether>> attriGether) {
        this.id = id;
        this.entry = entry;
        this.attriGether = attriGether;
//        this.MaxLevel = Collections.max(attriGether.keySet());
    }

    public List<ModifierEntry> getEntry() {
        return entry;
    }

    public void setEntry(List<ModifierEntry> entry) {
        this.entry = entry;
    }
    public void addEntry(ModifierEntry modifierEntry){
        this.entry.add(modifierEntry);
    }

    public Map<Integer, List<MobEffectInstance>> getEffect() {
        return effect;
    }
    public void setLevelAttriGether(int level,List< ModifierAttriGether> attriGether) {
        Exmodifier.LOGGER.debug("setLevelAttriGether " + level + " " + attriGether);
        if (attriGether != null) {
            this.attriGether.put(level, attriGether);
        }
    }
    public void setAttriGether(Map<Integer,List< ModifierAttriGether>> attriGether) {
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
}
