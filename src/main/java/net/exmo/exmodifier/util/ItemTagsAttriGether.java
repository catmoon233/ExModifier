package net.exmo.exmodifier.util;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.ArrayList;
import java.util.List;

public class ItemTagsAttriGether extends AttriGether{
    public List<String> tags = new ArrayList<>();
    public ItemTagsAttriGether(Attribute attribute, AttributeModifier modifier, EquipmentSlot slot, List<String> tags) {
        super(attribute, modifier, slot);
        this.tags = tags;
    }

    public ItemTagsAttriGether(Attribute attribute, AttributeModifier modifier, EquipmentSlot slot) {
        super(attribute, modifier, slot);
    }

    public ItemTagsAttriGether(Attribute attribute, AttributeModifier modifier) {
        super(attribute, modifier);
    }

    public ItemTagsAttriGether(Attribute attribute, AttributeModifier modifier, boolean isAutoEquipmentSlot) {
        super(attribute, modifier, isAutoEquipmentSlot);
    }
}
