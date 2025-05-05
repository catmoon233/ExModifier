package net.exmo.exmodifier.util.gether;

import net.exmo.exmodifier.util.ExAttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attribute;

public class AttrSimpleGether {
    public Attribute attribute;
    public ExAttributeModifier exAttributeModifier;

    public AttrSimpleGether(Attribute attribute, ExAttributeModifier exAttributeModifier) {
        this.attribute = attribute;
        this.exAttributeModifier = exAttributeModifier;
    }
}
