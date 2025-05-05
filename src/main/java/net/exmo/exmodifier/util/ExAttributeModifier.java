package net.exmo.exmodifier.util;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

public class ExAttributeModifier {
    public String name;
    public double amount;
    public AttributeModifier.Operation operation;

    public ExAttributeModifier(String name, double amount, AttributeModifier.Operation operation) {
        this.name = name;
        this.amount = amount;
        this.operation = operation;
    }


    public String getName() {
        return name;
    }

    public ExAttributeModifier setName(String name) {
        this.name = name;
        return this;
    }

    public double getAmount() {
        return amount;
    }

    public ExAttributeModifier setAmount(double amount) {
        this.amount = amount;
        return this;
    }

    public AttributeModifier.Operation getOperation() {
        return operation;
    }

    public ExAttributeModifier setOperation(AttributeModifier.Operation operation) {
        this.operation = operation;
        return this;
    }

    public AttributeModifier toModifier(UUID uuid){
        return new AttributeModifier(uuid, name, amount, operation);
    }
    public AttributeModifier toModifier(){
        return new AttributeModifier( name, amount, operation);
    }
    public AttributeModifier toModifierRandomUUID(){
        return new AttributeModifier( UUID.randomUUID(),name, amount, operation);
    }
    public static ExAttributeModifier fromModifier(AttributeModifier modifier){
        return new ExAttributeModifier(modifier.getName(),modifier.getAmount(),modifier.getOperation());
    }
}
