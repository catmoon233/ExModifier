package net.exmo.exmodifier.events;

import com.google.gson.JsonObject;
import net.exmo.exmodifier.content.modifier.MoConfig;
import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.content.suit.ExSuit;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

public class ExAddSuitAttrigethersEvent extends Event {


    public   List<ModifierAttriGether> modifierAttriGathers;
    public   MoConfig moconfig;
    public   ExSuit exSuit;
    public   JsonObject attrGethers;

    public ExAddSuitAttrigethersEvent(MoConfig moconfig, ExSuit exSuit, JsonObject attrGethers, List<ModifierAttriGether> modifierAttriGathers){
        this.moconfig = moconfig;
        this.exSuit = exSuit;
        this.attrGethers = attrGethers;
        this.modifierAttriGathers = modifierAttriGathers;



    }


    public List<ModifierAttriGether> getModifierAttriGathers() {
        return modifierAttriGathers;
    }

    public void setModifierAttriGathers(List<ModifierAttriGether> modifierAttriGathers) {
        this.modifierAttriGathers = modifierAttriGathers;
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

    public JsonObject getAttrGethers() {
        return attrGethers;
    }

    public void setAttrGethers(JsonObject attrGethers) {
        this.attrGethers = attrGethers;
    }
}
