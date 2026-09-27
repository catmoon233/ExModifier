package net.exmo.exmodifier.client;

import net.exmo.exmodifier.api.ExModifierApi;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.IItemDecorator;

/** Paints the book level in the corner of entry-book icons. */
@OnlyIn(Dist.CLIENT)
public final class EntryItemDecorator implements IItemDecorator {
    @Override
    public boolean render(GuiGraphics graphics, Font font, ItemStack stack, int x, int y) {
        return ExModifierApi.entryBook(stack).map(book -> {
            String text = book.level() > 99 ? "99+" : String.valueOf(book.level());
            graphics.drawString(font, text, x + 10, y + 7, 0xFFFFD700);
            return true;
        }).orElse(false);
    }
}
