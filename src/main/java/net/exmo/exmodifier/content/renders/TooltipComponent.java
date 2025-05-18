package net.exmo.exmodifier.content.renders;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
@OnlyIn(value = Dist.CLIENT)

public abstract class TooltipComponent {
    public final List<Component> components;
    public final int spacing;

    public TooltipComponent(List<Component> components, int spacing) {
        this.components = components;
        this.spacing = spacing;
    }

    public abstract int calculateWidth(Font font);
    public abstract int calculateHeight(Font font);
    public abstract void render(GuiGraphics gui, Font font, int baseX, int baseY,int weight,int height);
}