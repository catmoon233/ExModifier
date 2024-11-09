package net.exmo.exmodifier.content.helper.register;

import net.exmo.exmodifier.content.SpecialEffects.SpecialEffect;
import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.content.modifier.ModifierEntry;

import java.util.List;

public class ModifierCreateHelper {
    public ModifierEntry modifierEntry;
    private int index=0;
  public static ModifierCreateHelper CreateNew(String id,ModifierEntry.Type type){
    return new ModifierCreateHelper(id, type);
  }
  public  ModifierCreateHelper (String id,ModifierEntry.Type type){
    this.modifierEntry = new ModifierEntry(type.toString().substring(0,2)+ id);
    this.modifierEntry.type = type;

  }
  public ModifierCreateHelper addSpecialEffect(SpecialEffect effect){
    modifierEntry.specialTags.add(effect.id);
    return this;
  }
  public ModifierCreateHelper addSpecialEffect(String effect){
    modifierEntry.specialTags.add(effect);
    return this;
  }
  public ModifierCreateHelper type(ModifierEntry.Type type){
    modifierEntry.type = type;
    this.modifierEntry.id = type.toString().substring(0,2)+ modifierEntry.id;
    return this;
  }
  public ModifierCreateHelper weight(float weight){
    modifierEntry.weight = weight;
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
  public ModifierAttriGetherCreateHelper addModifierAttriGether(){
    index++;
    return new ModifierAttriGetherCreateHelper(this,index);

  }
  public ModifierCreateHelper addIntoList(List<ModifierEntry> list){
    list.add(modifierEntry);
    return this;
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