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
    public int MaxLevel ;
    public Map<Integer, List<ModifierAttriGether>> getAttriGether() {
        return attriGether;
    }    public Map<Integer, List<ModifierAttriGether>> getAttriGetherC() {
        return new HashMap<>(attriGether);

    }

    @Override
    public String toString() {
        return "ExSuit{" +
                "setting=" + setting +
                ", hasMobEffect=" + hasMobEffect +
                ", type=" + type +
                ", id='" + id + '\'' +
                ", entry=" + entry +
                ", attriGether=" + attriGether +
                ", effect=" + effect +
                ", itemDamage=" + itemDamage +
                '}';
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
        this.MaxLevel =Math.max(Collections.max(attriGether.keySet()), Collections.max(effect.keySet()));
        return this.MaxLevel;
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
}
