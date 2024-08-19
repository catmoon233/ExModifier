package net.exmo.exmodifier.content.modifier;

import net.exmo.exmodifier.util.AttriGether;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.inventory.Slot;

import java.util.UUID;

public class ModifierAttriGether extends AttriGether {
    public float weight = 0;
    public boolean isRandom = false;
    public boolean hasUUID =false;

    public ModifierAttriGether(Attribute attribute, AttributeModifier modifier, EquipmentSlot slot) {
        super(attribute, modifier,slot);
    }

    public boolean isHasUUID() {
        return this.modifier.getId().toString().equals("00000000-0000-0000-0000-000000000000");
    }
}
