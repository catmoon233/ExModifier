package net.exmo.exmodifier.util.gether;

import net.exmo.exmodifier.util.ExAttributeModifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.ArrayList;
import java.util.List;

public class ItemAttriGether  extends AttriGether {
    public List<String> items  = new ArrayList<>();
    public ItemAttriGether(Attribute attribute, ExAttributeModifier modifier, EquipmentSlot slot, List<String> items) {
        super(attribute, modifier, slot);
        this.items = items;
    }

    public ItemAttriGether(Attribute attribute, ExAttributeModifier modifier, EquipmentSlot slot) {
        super(attribute, modifier, slot);
    }

    public ItemAttriGether(Attribute attribute, ExAttributeModifier modifier) {
        super(attribute, modifier);
    }

    public ItemAttriGether(Attribute attribute, ExAttributeModifier modifier, boolean isAutoEquipmentSlot) {
        super(attribute, modifier, isAutoEquipmentSlot);
    }
}
