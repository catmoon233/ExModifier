package net.exmo.exmodifier.util;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.ArrayList;
import java.util.List;

public class ItemAttriGether  extends AttriGether{
    public List<String> items  = new ArrayList<>();
    public ItemAttriGether(Attribute attribute, AttributeModifier modifier, EquipmentSlot slot, List<String> items) {
        super(attribute, modifier, slot);
        this.items = items;
    }

    public ItemAttriGether(Attribute attribute, AttributeModifier modifier, EquipmentSlot slot) {
        super(attribute, modifier, slot);
    }

    public ItemAttriGether(Attribute attribute, AttributeModifier modifier) {
        super(attribute, modifier);
    }

    public ItemAttriGether(Attribute attribute, AttributeModifier modifier, boolean isAutoEquipmentSlot) {
        super(attribute, modifier, isAutoEquipmentSlot);
    }
}
