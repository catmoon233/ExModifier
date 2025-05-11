package net.exmo.exmodifier.content.dynamicAttributes;

import java.util.HashMap;
import java.util.Map;

public class DynamicAttributeRegister {
    public static Map<String, DynamicAttribute> dynamicAttributes = new HashMap<>();

    public static void registerDynamicAttribute(DynamicAttribute dynamicAttribute) {
        dynamicAttributes.put(dynamicAttribute.attributeId(), dynamicAttribute);
    }
}
