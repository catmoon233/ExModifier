package net.exmo.exmodifier.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

public class EntityAttrUtil {
    public static enum WearOrTake {
        WEAR,
        TAKE,

    }
    public static void TakeEntityNoValue(Player player,Attribute attribute, AttributeModifier attributeModifier){
        if (attribute==null)return;
        if(attributeModifier==null)return;
        AttributeMap attributes = player.getAttributes();
        if(attributes.hasAttribute(attribute)){
            AttributeInstance instance = attributes.getInstance(attribute);
            if (instance!=null) {
                if (!instance.getModifiers().isEmpty()) {
                    instance.getModifiers().forEach(modifier -> {
                        if (modifier.getName().equals(attributeModifier.getName())&& modifier.getOperation() == attributeModifier.getOperation()&& modifier.getId().equals(attributeModifier.getId())){
                            instance.removeModifier(modifier);
                        }
                    });
                }
            }
        }

    }
    public static void entityAddAttrTF(Attribute attribute, AttributeModifier attributeModifier, LivingEntity entity, WearOrTake wearOrTake) {
    if (attribute==null)return;
    if(attributeModifier==null)return;
        switch (wearOrTake) {
            case WEAR:
                if (!(entity.getAttribute(attribute).hasModifier(attributeModifier)))   entity.getAttribute(attribute).addPermanentModifier(attributeModifier);

                break;
            case TAKE:

                if ((entity.getAttribute(attribute).hasModifier(attributeModifier)))
                    entity.getAttribute(attribute).removeModifier(attributeModifier);

                break;
        }
    }
}
