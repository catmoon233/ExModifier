package net.exmo.exmodifier.content.event;

import net.exmo.exmodifier.Config;
import net.exmo.exmodifier.content.renders.AboveTooltipComponent;
import net.exmo.exmodifier.content.renders.BelowTooltipComponent;
import net.exmo.exmodifier.content.renders.SideTooltipComponent;
import net.exmo.exmodifier.content.renders.TooltipComponent;
import net.exmo.exmodifier.content.slot.ModifierSlotHandle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;


@Mod.EventBusSubscriber
public class ExTooltipRenderEvent {

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void RenderExtraTooltip(RenderTooltipEvent.Pre event) {
        // 基础信息获取
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null) return;
        var g = event.getGraphics();
        List<TooltipComponent> extraComponents = new ArrayList<>();

        // 1. 添加示例组件（显示在上方）
        if (Config.ExMoTooltipRenderInRightValue) {
            List<Component> components = MainEvent.CommonEvent.EntryInfoTooltip(event.getItemStack(), new ArrayList<>(), mc.player).getA();
            if (!components.isEmpty()) {
                extraComponents.add(new BelowTooltipComponent(
                        components,
                        10 // 间隔
                ));
            }
        }
        // 2. 添加原始侧边组件
        List<Component> modifierTips = ModifierSlotHandle.getTooltip(event.getItemStack(), mc.player);
        if (!modifierTips.isEmpty()) {
            extraComponents.add(new SideTooltipComponent(modifierTips, 10));
        }

        // 计算总高度并调整原tooltip位置
        Font font = mc.font;
//        int totalHeight = extraComponents.stream()
//                .filter(c -> c instanceof AboveTooltipComponent)
//                .mapToInt(c -> c.calculateHeight(font))
//                .sum();
//
//        event.setY(event.getY() + totalHeight); // 下移原tooltip
        // 渲染所有额外组件
        int currentY = event.getY() ;
        int lineH = event.getComponents().size() * font.lineHeight;
        if (event.getY() + lineH > event.getScreenHeight()){
            currentY -= event.getScreenHeight() - lineH - event.getY();
        }
        for (TooltipComponent component : extraComponents) {
            int extraWidth = 0;
            for (Component components : component.components) {
                extraWidth = Math.max(extraWidth, font.width(components));
            }
            int height = component.components.size() * font.lineHeight;


            if (component instanceof AboveTooltipComponent) {

                component.render(event.getGraphics(), font, event.getX(), currentY, extraWidth, height);
                currentY += component.calculateHeight(font);
            } else if (component instanceof BelowTooltipComponent belowTooltipComponent){
                belowTooltipComponent.render(event.getGraphics(), font,event.getX(), event.getY() + lineH,extraWidth, height);
            }
            else if (component instanceof SideTooltipComponent) {

             //   Vector2ic vector2ic = event.getTooltipPositioner().positionTooltip(g.guiWidth(), g.guiHeight(), event.getX(), event.getY(),  extraWidth, 0);
                component.render(event.getGraphics(), font,event.getX()- extraWidth, currentY,extraWidth, height);
                currentY += height +component.spacing;
            }

        }
    }
//    @OnlyIn(Dist.CLIENT)
//    @SubscribeEvent
//    public static void RenderExtraTooltip(RenderTooltipEvent.Pre event) {
//        var g = event.getGraphics();
//        var mc = Minecraft.getInstance();
//        if (mc.screen != null) {
//            List<Component> extraComponents = ModifierSlotHandle.getTooltip(event.getItemStack(), mc.player);
//            if (extraComponents.isEmpty())return;
//            @NotNull List<ClientTooltipComponent> originalComponents = event.getComponents();
//
//            Font font = mc.font;
//
//            // Calculate the width and height of the original tooltip
//            int originalWidth = 30;
//            for (ClientTooltipComponent component : originalComponents) {
//                originalWidth = Math.max(originalWidth, (component.getWidth(font)));
//            }
//            // int originalHeight = originalComponents.size() * font.lineHeight;
//
//            // Calculate the width and height of the extra tooltip
//            int extraWidth = 0;
//            for (Component component : extraComponents) {
//                extraWidth = Math.max(extraWidth, font.width(component));
//            }
//            int extraHeight = extraComponents.size() * font.lineHeight;
//            Vector2ic vector2ic = event.getTooltipPositioner().positionTooltip(g.guiWidth(), g.guiHeight(), event.getX(), event.getY(), extraWidth, extraHeight);
//            //     int screenW = mc.screen.width;
//            //     int screenH = mc.screen.height;
//            int tooltipX = vector2ic.x();
//            int tooltipY = vector2ic.y() ; // Start at the same Y as the original tooltip
//
//            // Determine the position for the extra tooltip
////                if (tooltipX + originalWidth + extraWidth > screenW) {
////                    // Not enough space on the right, try the left
////                    if (tooltipX - extraWidth < 0) {
////                        // Not enough space on the left, render on the right
////                        tooltipX = event.getX() + originalWidth+5;
////                    } else {
////                        // Render on the left
//            tooltipX = event.getX() - extraWidth  ;
////                   }
////                }
//
//            // Adjust X position based on longest line in respective tooltips
//            //     if (tooltipX == event.getX()) {
//            // Rendering on the right, adjust by longest line in original tooltip
//            //        tooltipX += originalWidth -10;
//            //    } else {
//            // Rendering on the left, adjust by longest line in extra tooltip
//            //        tooltipX -= extraWidth -50;
//            //    }
//
//            // Render the extra tooltip text first with a darker color
//            AtomicInteger line = new AtomicInteger();
//            int finalTooltipX = tooltipX;
//            g.pose().pushPose();
//            renderTooltipBackground(g, tooltipX, tooltipY, extraWidth, extraHeight, 400);
//            g.pose().translate(0.0F, 0.0F, 400.0F);
//
//
//
//            extraComponents.forEach(component -> {
//                ClientTooltipComponent clientTooltipComponent = ClientTooltipComponent.create(component.getVisualOrderText());
//                clientTooltipComponent.renderText(font, finalTooltipX, tooltipY + line.get() * font.lineHeight, g.pose().last().pose(), g.bufferSource());
//                line.getAndIncrement();
//            });
//            g.pose().popPose();
//            // Render the extra tooltip background above the text
//
//            // Update the event's Y position to account for the extra tooltip
//            //    event.setY(tooltipY + extraHeight);
//        }
//    }
}
