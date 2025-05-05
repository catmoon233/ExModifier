package net.exmo.exmodifier.util.gether;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.List;

public class AttriGetherNormal extends AttrGether {


    public boolean hasUUID;
    private List<String> OnlyItems;
    private List<String> OnlySlots;


    public AttriGetherNormal(Attribute attribute, AttributeModifier attributeModifier) {
        super(attribute, attributeModifier);
    }

    public boolean isHasUUID() {
        return hasUUID;
    }

    public AttriGetherNormal setHasUUID(boolean hasUUID) {
        this.hasUUID = hasUUID;
        return this;
    }

    public List<String> getOnlyItems() {
        return OnlyItems;
    }

    public AttriGetherNormal setOnlyItems(List<String> onlyItems) {
        OnlyItems = onlyItems;
        return this;
    }

    public List<String> getOnlySlots() {
        return OnlySlots;
    }

    public AttriGetherNormal setOnlySlots(List<String> onlySlots) {
        OnlySlots = onlySlots;
        return this;
    }
}
