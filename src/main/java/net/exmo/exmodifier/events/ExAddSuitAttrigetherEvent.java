package net.exmo.exmodifier.events;

import com.google.gson.JsonElement;

import net.exmo.exmodifier.content.modifier.MoConfig;
import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.content.suit.ExSuit;
import net.minecraftforge.eventbus.api.Event;

import java.util.Map;

public class ExAddSuitAttrigetherEvent extends Event {
    public MoConfig moconfig;
    public ExSuit exSuit;
    public Map.Entry<String, JsonElement> attrGetherEntry;
    public int index;
    public ModifierAttriGether attrGether;
    public ExAddSuitAttrigetherEvent(MoConfig moconfig, ExSuit exSuit, Map.Entry<String, JsonElement> attrGetherEntry, int index, ModifierAttriGether attrGether){
        this.moconfig = moconfig;
        this.exSuit = exSuit;
        this.attrGetherEntry = attrGetherEntry;
        this.index = index;
        this.attrGether = attrGether;
    }

    public MoConfig getMoconfig() {
        return moconfig;
    }

    public void setMoconfig(MoConfig moconfig) {
        this.moconfig = moconfig;
    }

    public ExSuit getExSuit() {
        return exSuit;
    }

    public void setExSuit(ExSuit exSuit) {
        this.exSuit = exSuit;
    }

    public Map.Entry<String, JsonElement> getAttrGetherEntry() {
        return attrGetherEntry;
    }

    public void setAttrGetherEntry(Map.Entry<String, JsonElement> attrGetherEntry) {
        this.attrGetherEntry = attrGetherEntry;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public ModifierAttriGether getAttrGether() {
        return attrGether;
    }

    public void setAttrGether(ModifierAttriGether attrGether) {
        this.attrGether = attrGether;
    }
}
