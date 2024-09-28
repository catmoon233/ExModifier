package net.exmo.exmodifier.content.level;

import net.exmo.exmodifier.util.AttriGether;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class LevelAttriGether extends AttriGether {
    public LevelAttriGether(Attribute attribute, AttributeModifier modifier, EquipmentSlot slot) {
        super(attribute, modifier, slot);
    }

    public LevelAttriGether(Attribute attribute, AttributeModifier modifier) {
        super(attribute, modifier);
    }

    public LevelAttriGether(Attribute attribute, AttributeModifier modifier, boolean isAutoEquipmentSlot) {
        super(attribute, modifier, isAutoEquipmentSlot);
    }
}