package net.exmo.exmodifier.core.runtime;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

public final class AnvilWash {
    private AnvilWash() {}

    @SubscribeEvent
    public static void update(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        Player player = event.getPlayer();
        if (left.isEmpty() || right.isEmpty() || player == null) return;
        if (ModifierService.washConsumeCount(right) <= 0) return;
        if (ModifierService.canRefine(left, right)) return;
        ItemStack preview = ModifierService.previewWash(player, left, right, RandomSource.create(ItemStack.hashItemAndComponents(left))).orElse(ItemStack.EMPTY);
        if (preview.isEmpty()) return;
        event.setOutput(preview);
        event.setCost(Math.max(1, ModifierService.washXpCost(right)));
        event.setMaterialCost(Math.max(1, ModifierService.washConsumeCount(right)));
    }
}
