package net.exmo.exmodifier.content.dynamicAttributes;

import net.exmo.exmodifier.util.exSerialize.ExSerialize;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.Objects;

public final class DynamicAttribute extends Attribute {
    public static final ExSerialize<DynamicAttribute> SERIALIZER = ExSerialize.create(() -> new DynamicAttribute("", new range(0, 0, 0)))
            .withAutoId(dynamicAttribute -> dynamicAttribute.attributeId,(dynamicAttribute, attributeId) -> dynamicAttribute.attributeId = attributeId)
            .addStringField("attributeId", dynamicAttribute -> dynamicAttribute.attributeId,(dynamicAttribute, attributeId) -> dynamicAttribute.attributeId = attributeId)
            .addFloatMapField("range", (dynamicAttribute, range) -> dynamicAttribute.range = new range(range.get("min"), range.get("max"), range.get("default")));
    private  String attributeId;
    private  range range;

    public DynamicAttribute(String attributeId, range range) {
        super(attributeId, range.defaultValue);
        this.attributeId = attributeId;
        this.range = range;
    }

    public record range(double min, double max, double defaultValue) {
    }

    public String translationID() {
        return "exmodifier.attribute." + attributeId;
    }

    public String attributeId() {
        return attributeId;
    }

    public range range() {
        return range;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DynamicAttribute) obj;
        return Objects.equals(this.attributeId, that.attributeId) &&
                Objects.equals(this.range, that.range);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributeId, range);
    }

    @Override
    public String toString() {
        return "DynamicAttribute[" +
                "attributeId=" + attributeId + ", " +
                "range=" + range + ']';
    }

}
