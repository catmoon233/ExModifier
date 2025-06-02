package net.exmo.exmodifier.content.renders;

import net.exmo.exmodifier.util.ExUtil;
import net.exmo.exmodifier.util.TooltipUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2ic;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil.renderTooltipBackground;
@OnlyIn(value = Dist.CLIENT)
public class SideTooltipComponent extends TooltipComponent {
    public SideTooltipComponent(List<Component> components, int spacing) {
        super(components, spacing);
    }

    @Override
    public int calculateWidth(Font font) {
        return 10; // 右侧留空
    }

    @Override
    public int calculateHeight(Font font) {
        return this.components.size()* font.lineHeight ;
    }

    @Override
    public void render(GuiGraphics gui, Font font, int baseX, int baseY, int w ,int h) {


        // 右侧定位逻辑
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int adjustedX = (baseX + w > screenWidth) ?
                baseX - h : baseX;

        gui.pose().pushPose();
        renderTooltipBackground(gui, adjustedX, baseY+6, w, h, 400);
        gui.pose().translate(0, 6, 400);

        AtomicInteger yOffset = new AtomicInteger();
        components.forEach(component -> {
            TooltipUtil.sprit( component).forEach(
                    component1 ->
                            gui.drawString(
                                    font,
                                    component1,
                                    adjustedX +2 , // 右侧留空5像素
                                    baseY + yOffset.getAndAdd(font.lineHeight),
                                    0xFFFFFF,
                                    false
                            )
            );

        });
        gui.pose().popPose();
    }
}