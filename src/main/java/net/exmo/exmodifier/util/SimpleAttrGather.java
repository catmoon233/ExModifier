package net.exmo.exmodifier.util;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record SimpleAttrGather(
        String attribute,
        String name,
        AttributeModifier.Operation operation
) {
    // 可以添加辅助方法
    public AttributeModifier createModifier(double value) {
        return new AttributeModifier(
                name,
                value,
                operation
        );
    }
}
