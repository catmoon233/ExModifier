package net.exmo.exmodifier.util;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class AttriGether {
    public EquipmentSlot slot;
    public boolean IsAutoEquipmentSlot = false;
    public Attribute attribute;
    public AttributeModifier modifier;
    public AttriGether(Attribute attribute, AttributeModifier modifier,EquipmentSlot slot) {

        this.attribute = attribute;
        this.modifier = modifier;
        this.slot =     slot;
        ItemStack itemStack;

    }


    public AttriGether(Attribute attribute, AttributeModifier modifier) {
        this.attribute = attribute;
        this.modifier = modifier;
    }
    public AttriGether(Attribute attribute, AttributeModifier modifier,boolean isAutoEquipmentSlot) {
        this.attribute = attribute;
        this.modifier = modifier;
        this.IsAutoEquipmentSlot = isAutoEquipmentSlot;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public AttributeModifier getModifier() {
        return modifier;
    }
}
