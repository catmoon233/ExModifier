package net.exmo.exmodifier.core.runtime;

import net.exmo.exmodifier.Config;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

public final class AnvilRefine {
    private AnvilRefine() {}

    @SubscribeEvent
    public static void update(AnvilUpdateEvent event) {
        if (!Config.refineSystem) return;
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        if (left.isEmpty() || right.isEmpty()) return;
        if (!ModifierService.canRefine(left, right)) return;
        ItemStack output = left.copy();
        int consume = ModifierService.refineConsumeCount(right);
        if (!ModifierService.refineInto(output, right, false)) return;
        event.setOutput(output);
        event.setCost(Math.max(1, consume));
        event.setMaterialCost(Math.max(1, consume));
    }
}
