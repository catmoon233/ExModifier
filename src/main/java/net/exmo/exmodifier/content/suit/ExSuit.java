package net.exmo.exmodifier.content.suit;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExSuit {
    public boolean hasMobEffect = false;
    public ModifierEntry.Type type;
    public String id;
    public List<ModifierEntry> entry = new ArrayList<>();
    public Map<Integer,List< ModifierAttriGether>> attriGether = new java.util.HashMap<>();
    private   Map<Integer,List<MobEffectInstance> > effect = new java.util.HashMap<>();
    public ExSuit(){

    }
    public ExSuit(String id, List<ModifierEntry> entry, Map<Integer,List< ModifierAttriGether>> attriGether) {
        this.id = id;
        this.entry = entry;
        this.attriGether = attriGether;
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
