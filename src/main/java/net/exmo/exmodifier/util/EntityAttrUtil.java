package net.exmo.exmodifier.util;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.util.gether.AttrGether;
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

    private static boolean canApply(Attribute attribute, AttributeModifier attributeModifier, LivingEntity entity, WearOrTake wearOrTake) {
        if (entity == null || wearOrTake == null || attribute == null || attributeModifier == null) {
            return false;
        }
        if (attributeModifier.getOperation() == null) {
            Exmodifier.LOGGER.Logger.warn("Skip apply attribute modifier '{}' because operation is null", attributeModifier.getName());
            return false;
        }
        return true;
    }

    public static void TakeEntityNoValue(Player player,Attribute attribute, AttributeModifier attributeModifier){
        if (player == null || attribute == null || attributeModifier == null) return;
        if (attributeModifier.getOperation() == null) {
            Exmodifier.LOGGER.Logger.warn("Skip remove attribute modifier '{}' because operation is null", attributeModifier.getName());
            return;
        }
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
    public static void entityAddAttrTF(AttrGether attrGether, LivingEntity entity, WearOrTake wearOrTake) {
        if (attrGether == null) return;
        Attribute attribute = attrGether.attribute;
        AttributeModifier attributeModifier = attrGether.attributeModifier;
        if (!canApply(attribute, attributeModifier, entity, wearOrTake)) return;

        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) return;

        switch (wearOrTake) {
            case WEAR:
                if (!instance.hasModifier(attributeModifier)) {
                    instance.addPermanentModifier(attributeModifier);
                }

                break;
            case TAKE:
                if (instance.hasModifier(attributeModifier)) {
                    instance.removeModifier(attributeModifier);
                }

                break;
        }
    }
//    public static void entityAddAttrTF(AttriGether attriGether, LivingEntity entity, WearOrTake wearOrTake) {
//        Attribute attribute = attriGether.getAttribute();
//        AttributeModifier attributeModifier = attriGether.getModifier();
//
//        switch (wearOrTake) {
//            case WEAR:
//                if (entity.getAttributes().hasAttribute(attribute)) if (!(entity.getAttribute(attribute).hasModifier(attributeModifier)))   entity.getAttribute(attribute).addPermanentModifier(attributeModifier);
//
//                break;
//            case TAKE:
//                if (entity.getAttributes().hasAttribute(attribute)) if ((entity.getAttribute(attribute).hasModifier(attributeModifier)))
//                    entity.getAttribute(attribute).removeModifier(attributeModifier);
//
//                break;
//        }
//    }
    public static void entityAddAttrTF(Attribute attribute, AttributeModifier attributeModifier, LivingEntity entity, WearOrTake wearOrTake) {
    if (!canApply(attribute, attributeModifier, entity, wearOrTake)) return;
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) return;

        switch (wearOrTake) {
            case WEAR:
                if (!instance.hasModifier(attributeModifier)) {
                    instance.addPermanentModifier(attributeModifier);
                }

                break;
            case TAKE:

                if (instance.hasModifier(attributeModifier)) {
                    instance.removeModifier(attributeModifier);
                }

                break;
        }
    }
}
