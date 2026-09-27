package net.exmo.exmodifier.content.helper;

import net.exmo.exmodifier.api.AppliedModifierView;
import net.exmo.exmodifier.api.ExModifierApi;
import net.exmo.exmodifier.core.component.AppliedModifiers;
import net.exmo.exmodifier.core.runtime.ModifierService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

/** Component-backed replacement for the 1.20.1 NBT helper. */
public final class ModifierEntryHelper {
    private final ItemStack stack;

    public ModifierEntryHelper(ItemStack stack) {
        this.stack = stack;
    }

    public static ModifierEntryHelper of(ItemStack stack) {
        return new ModifierEntryHelper(stack);
    }

    public AppliedModifiers modifiers() {
        return ExModifierApi.appliedModifiers(stack);
    }

    public List<AppliedModifierView> views() {
        return ExModifierApi.modifiersOn(stack);
    }

    public int size() {
        return modifiers().values().size();
    }

    public boolean add(ResourceLocation entryId, int level) {
        return ModifierService.addEntry(stack, entryId, level, Optional.empty());
    }

    public boolean remove(ResourceLocation entryId) {
        return ModifierService.removeEntry(stack, entryId);
    }

    public boolean set(ResourceLocation entryId, int level) {
        return ModifierService.setEntry(stack, entryId, level);
    }
}
