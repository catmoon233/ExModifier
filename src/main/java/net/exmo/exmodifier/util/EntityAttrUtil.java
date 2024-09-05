package net.exmo.exmodifier.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class EntityAttrUtil {
    public static enum WearOrTake {
        WEAR,
        TAKE,

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
