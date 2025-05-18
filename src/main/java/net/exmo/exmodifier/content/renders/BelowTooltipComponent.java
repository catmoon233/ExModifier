package net.exmo.exmodifier.content.renders;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil.renderTooltipBackground;

@OnlyIn(value = Dist.CLIENT)
public class BelowTooltipComponent extends SideTooltipComponent{
    public BelowTooltipComponent(List<Component> components, int spacing) {
        super(components, spacing);
    }

    @Override
    public void render(GuiGraphics gui, Font font, int baseX, int baseY, int w ,int h) {


        // 右侧定位逻辑


        gui.pose().pushPose();
        renderTooltipBackground(gui, baseX, baseY, w, h+1, 400);
        gui.pose().translate(0, 0, 400);

        AtomicInteger yOffset = new AtomicInteger();
        components.forEach(component -> {
            gui.drawString(
                    font,
                    component,
                    baseX , // 右侧留空5像素
                    baseY + yOffset.getAndAdd(font.lineHeight)+1,
                    0xFFFFFF,
                    false
            );
        });
        gui.pose().popPose();
    }
}
