package net.exmo.exmodifier.content.helper;

import net.exmo.exmodifier.api.ExModifierApi;
import net.exmo.exmodifier.core.component.ItemLevelState;
import net.exmo.exmodifier.core.runtime.ItemLevelRuntime;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class ItemLevelHelper {
    private final ItemStack stack;

    public ItemLevelHelper(ItemStack stack) {
        this.stack = stack;
    }

    public static ItemLevelHelper of(ItemStack stack) {
        return new ItemLevelHelper(stack);
    }

    public Optional<ItemLevelState> state() {
        return ExModifierApi.itemLevel(stack);
    }

    public void applyIfAbsent() {
        ItemLevelRuntime.applyIfAbsent(stack);
    }

    public void addXp(LivingEntity entity, String event, double amount) {
        ItemLevelRuntime.addXp(entity, stack, event, amount);
    }
}
