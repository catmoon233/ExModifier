package net.exmo.exmodifier.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.Optional;

public record AppliedModifierView(ResourceLocation entryId, int level, Optional<ResourceLocation> slotId, boolean locked) {
    public AppliedModifierView {
        level = Math.max(1, level);
        slotId = slotId == null ? Optional.empty() : slotId;
    }
}
