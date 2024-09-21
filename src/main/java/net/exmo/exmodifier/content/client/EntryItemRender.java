package net.exmo.exmodifier.content.client;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.modifier.EntryItem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;

public class EntryItemRender implements IItemDecorator {
    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        if (stack.getItem() instanceof EntryItem entryItem){
            String id  = stack.getOrCreateTag().getString("modifier_id");
            if (id.length()<=2)return false;
            Component c = Component.translatable("modifiler.entry."+id.substring(2));
            TextColor color = c.getStyle().getColor();
            int value ;
            if (color==null) value = 0xFFFFFF;
            else value= color.getValue();
            guiGraphics.drawString(font, c, (int) (xOffset +17 - font.width(c)*0.5), yOffset+2 , value);
            return  true;
        }
       return false;
    }
}