package net.exmo.exmodifier.content.helper.register;

import net.exmo.exmodifier.content.specialEffects.SpecialEffect;
import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.type.ItemType;

import java.util.Arrays;
import java.util.List;

public class ModifierCreateHelper {
    public ModifierEntry modifierEntry;
    private int index=0;
  public static ModifierCreateHelper CreateNew(String id,ItemType... type){
    return new ModifierCreateHelper(id, type);
  }
  public  ModifierCreateHelper (String id, ItemType... type){
    this.modifierEntry = new ModifierEntry(Arrays.toString(type).substring(0,2)+ id);
    this.modifierEntry.types.addAll(List.of(type));

  }
  public ModifierCreateHelper IsAutoEquipment(boolean isAutoEquipment){
    modifierEntry.isRandom = isAutoEquipment;
    return this;
  }
  public ModifierCreateHelper addSpecialEffect(SpecialEffect effect){
    modifierEntry.specialTags.add(effect.id());
    return this;
  }
  public ModifierCreateHelper addSpecialEffect(String effect){
    modifierEntry.specialTags.add(effect);
    return this;
  }
  public ModifierCreateHelper type(List<ItemType> type){
    modifierEntry.types = type;
    this.modifierEntry.id = type.toString().substring(0,2)+ modifierEntry.id;
    return this;
  }
  public ModifierCreateHelper weight(float weight){
    modifierEntry.weight = weight;
    return this;
  }
  public ModifierCreateHelper setMaxLevel(int maxLevel){
    modifierEntry.maxLevel = maxLevel;
    return this;
  }
  public ModifierCreateHelper cantSelect(boolean cantSelect){
    modifierEntry.cantSelect = cantSelect;
    return this;
  }

public ModifierCreateHelper setTypes(List<ItemType> type){
    modifierEntry.types = type;
    return this;
}

  public void clear(){
    this.modifierEntry = null;
  }
  public void clear(String id){
    this.modifierEntry = new ModifierEntry(id);
  }
  public ModifierEntry getModifierEntry() {
    return modifierEntry;
  }
  public void setModifierEntry(ModifierEntry modifierEntry) {
    this.modifierEntry = modifierEntry;
  }
  public ModifierCreateHelper addModifierAttriGether(ModifierAttriGether modifierAttriGether){
    modifierEntry.attriGether.add(modifierAttriGether);
    return this;
  }
  public ModifierCreateHelper setLocalDescription(String localDescription){
    modifierEntry.localDescription = localDescription;
    return this;
  }
  public ModifierAttriGetherCreateHelper addModifierAttriGether(){
    index++;
    return new ModifierAttriGetherCreateHelper(this,index);

  }
  public ModifierCreateHelper addIntoList(List<ModifierEntry> list){
    list.add(modifierEntry);
    return this;
  }
  public ModifierEntry finish(){
    return modifierEntry;
  }
  public ModifierCreateHelper addIntoListAndReload(List<ModifierEntry> list){
    list.add(modifierEntry);
    clear();
    return this;
  }
  public ModifierCreateHelper addIntoListAndReload(List<ModifierEntry> list,String s){
    list.add(modifierEntry);
    clear(s);
    return this;
  }
}