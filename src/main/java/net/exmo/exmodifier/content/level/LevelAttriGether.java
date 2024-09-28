package net.exmo.exmodifier.content.level;

import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.util.AttriGether;
import net.exmo.exmodifier.util.DynamicExpressionEvaluator;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import javax.script.ScriptException;

public class LevelAttriGether extends ModifierAttriGether {
    public String Expression;

    @Override
    public String toString() {
        return "LevelAttriGether{" +
                "Expression='" + Expression + '\'' +
                ", weight=" + weight +
                ", isRandom=" + isRandom +
                ", reserveDouble=" + reserveDouble +
                ", hasUUID=" + hasUUID +
                ", minValue=" + minValue +
                ", maxValue=" + maxValue +
                ", simpleWeight=" + simpleWeight +
                ", OnlyItems=" + OnlyItems +
                ", OnlySlots=" + OnlySlots +
                ", slot=" + slot +
                ", IsAutoEquipmentSlot=" + IsAutoEquipmentSlot +
                ", attribute=" + attribute +
                ", modifier=" + modifier +
                '}';
    }

    @Override
    public Attribute getAttribute() {
        return super.getAttribute();
    }

    public AttributeModifier getModifier(int Level, int rate ,DynamicExpressionEvaluator dynamicExpressionEvaluator) throws ScriptException {
        return new AttributeModifier(this.modifier.getId(), this.modifier.getName(), dynamicExpressionEvaluator.evaluate(Expression), this.modifier.getOperation());
    }

    public LevelAttriGether(Attribute attribute, AttributeModifier modifier, EquipmentSlot slot) {
        super(attribute, modifier, slot);
    }

    public LevelAttriGether(Attribute attribute, AttributeModifier modifier) {
        super(attribute, modifier);
    }


}