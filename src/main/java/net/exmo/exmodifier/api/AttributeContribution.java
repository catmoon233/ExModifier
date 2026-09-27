package net.exmo.exmodifier.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record AttributeContribution(
        ResourceLocation attribute,
        double amount,
        AttributeModifier.Operation operation,
        ResourceLocation sourceEntry
) {}
