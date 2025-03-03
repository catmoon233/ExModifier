package net.exmo.exmodifier.content.client;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.modifier.EntryItem;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;

public class EntryItemRender implements IItemDecorator {
    public static final ResourceLocation bookIcon = new ResourceLocation(Exmodifier.MODID, "textures/item/entry_item.png");
    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        String id = stack.getOrCreateTag().getString("modifier_id");
        if (id.isEmpty()) return false;
        ModifierEntry modifierEntry = ModifierHandle.findModifierEntry(id);
        if (modifierEntry!=null){
            if (!modifierEntry.icon.isEmpty()){
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 200);
                guiGraphics.blit(new ResourceLocation(modifierEntry.icon), xOffset , yOffset , 16, 16, 0, 0, 16, 16, 16, 16);
                guiGraphics.pose().popPose();
            }else {
           //     guiGraphics.blit(bookIcon, xOffset , yOffset , 16, 16, 0, 0, 16, 16, 16, 16);
            }
        }
        if (Screen.hasShiftDown()) {
            if (stack.getItem() instanceof EntryItem entryItem) {


                if (id.length() <= 2) return false;
                Component c = Component.translatable("modifier.entry." + id.substring(2));
                TextColor color = c.getStyle().getColor();
                int value;
                if (color == null) value = 0xFFFFFF;
                else value = color.getValue();
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 200);
                guiGraphics.drawString(font, c, (int) (xOffset + 8 - font.width(c) * 0.5), yOffset + 2, value);
                guiGraphics.pose().popPose();
                return true;
            }

        }
        return false;
    }
}