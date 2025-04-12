package net.exmo.exmodifier.content.renders;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil.renderTooltipBackground;

public class AboveTooltipComponent extends TooltipComponent {
    public AboveTooltipComponent(List<Component> components, int spacing) {
        super(components, spacing);
    }

    @Override
    public int calculateWidth(Font font) {
        return components.stream()
                .mapToInt(font::width)
                .max()
                .orElse(0);
    }

    @Override
    public int calculateHeight(Font font) {
        return components.size() * font.lineHeight + spacing;
    }

    @Override
    public void render(GuiGraphics gui, Font font, int baseX, int baseY, int w ,int h) {


        // 右侧定位逻辑
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int adjustedX = (baseX + w > screenWidth) ?
                baseX - h : baseX;

        gui.pose().pushPose();
        renderTooltipBackground(gui, adjustedX, baseY, w+2, h, 400);
        gui.pose().translate(0, 0, 400);

        AtomicInteger yOffset = new AtomicInteger();
        components.forEach(component -> {
            gui.drawString(
                    font,
                    component,
                    adjustedX +2 , // 右侧留空5像素
                    baseY + yOffset.getAndAdd(font.lineHeight),
                    0xFFFFFF,
                    false
            );
        });
        gui.pose().popPose();
    }
}