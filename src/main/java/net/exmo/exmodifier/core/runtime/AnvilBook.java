package net.exmo.exmodifier.core.runtime;

import net.exmo.exmodifier.api.ExModifierApi;
import net.exmo.exmodifier.content.item.EntryItem;
import net.exmo.exmodifier.core.component.EntryBook;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

/** Entry books combine in the anvil like 1.20.1: book in the second slot embeds its entry. */
public final class AnvilBook {
    private AnvilBook() {}

    @SubscribeEvent
    public static void update(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        if (left.isEmpty() || right.isEmpty()) return;
        if (!(right.getItem() instanceof EntryItem)) return;
        EntryBook book = ExModifierApi.entryBook(right).orElse(null);
        if (book == null) return;
        ItemStack output = left.copy();
        if (!ModifierService.embedBook(output, right.copy())) return;
        event.setOutput(output);
        event.setCost(Math.max(1, book.level()));
        event.setMaterialCost(1);
    }
}
