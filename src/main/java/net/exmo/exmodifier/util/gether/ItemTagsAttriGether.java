package net.exmo.exmodifier.util.gether;

import net.exmo.exmodifier.util.ExAttributeModifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.ArrayList;
import java.util.List;

public class ItemTagsAttriGether extends AttriGether {
    public List<String> tags = new ArrayList<>();
    public ItemTagsAttriGether(Attribute attribute, ExAttributeModifier modifier, EquipmentSlot slot, List<String> tags) {
        super(attribute, modifier, slot);
        this.tags = tags;
    }

    public ItemTagsAttriGether(Attribute attribute, ExAttributeModifier modifier, EquipmentSlot slot) {
        super(attribute, modifier, slot);
    }

    public ItemTagsAttriGether(Attribute attribute, ExAttributeModifier modifier) {
        super(attribute, modifier);
    }

    public ItemTagsAttriGether(Attribute attribute, ExAttributeModifier modifier, boolean isAutoEquipmentSlot) {
        super(attribute, modifier, isAutoEquipmentSlot);
    }
}
