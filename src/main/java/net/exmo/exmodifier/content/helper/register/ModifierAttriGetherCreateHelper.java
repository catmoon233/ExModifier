package net.exmo.exmodifier.content.helper.register;

import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

public class ModifierAttriGetherCreateHelper {
    private ModifierCreateHelper modifierCreateHelper;
    private ModifierAttriGether modifierAttriGether;
    private String AttributeName;
    private Attribute attribute;
    private UUID uuid;
    private String name="";
    private double amount;
    private AttributeModifier.Operation operation;
    private String slot;
    private final int index ;

    public ModifierAttriGetherCreateHelper(ModifierCreateHelper modifierCreateHelper,int index) {
        this.index = index;
        this.modifierCreateHelper = modifierCreateHelper;

    }

    public ModifierCreateHelper getModifierCreateHelper() {
        return modifierCreateHelper;
    }

    public ModifierAttriGetherCreateHelper setModifierCreateHelper(ModifierCreateHelper modifierCreateHelper) {
        this.modifierCreateHelper = modifierCreateHelper;
        return this;
    }
    public ModifierAttriGetherCreateHelper setModifierCreateHelper(String AttributeName,double amount,AttributeModifier.Operation operation,String slot) {
        this.AttributeName = AttributeName;
        this.amount = amount;
        this.operation = operation;
        this.slot = slot;
        return this;
    }
    public ModifierAttriGetherCreateHelper setModifierCreateHelper(Attribute attribute,double amount,AttributeModifier.Operation operation,String slot) {
        this.attribute = attribute;
        this.amount = amount;
        this.operation = operation;
        this.slot = slot;

        return this;
    }
    public ModifierAttriGetherCreateHelper setModifierCreateHelper(Attribute attribute,double amount,AttributeModifier.Operation operation) {
        this.attribute = attribute;
        this.amount = amount;
        this.operation = operation;
        this.slot = "auto";

        return this;
    }
    public ModifierAttriGetherCreateHelper setWeight(float weight) {
        this.modifierAttriGether.weight = weight;
        return this;
    }

    public ModifierCreateHelper finish_add(){
        if (name.isEmpty())name = modifierCreateHelper.modifierEntry.id + index;
        if (uuid==null)uuid = UUID.nameUUIDFromBytes(name.getBytes());
        if (attribute==null)if(!AttributeName.isEmpty())attribute= ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(AttributeName));
        AttributeModifier modifier = new AttributeModifier(uuid, name, amount, operation);
        if (modifierAttriGether==null)modifierAttriGether = new ModifierAttriGether(attribute,modifier);
        if (!slot.equals("auto")){
           modifierAttriGether.slot = EquipmentSlot.valueOf(slot);
        }else modifierAttriGether.IsAutoEquipmentSlot = true;
//        modifierAttriGether.attribute = attribute;
//        modifierAttriGether.modifier = modifier;
        modifierCreateHelper.addModifierAttriGether(modifierAttriGether);
        return modifierCreateHelper;
    }
}
