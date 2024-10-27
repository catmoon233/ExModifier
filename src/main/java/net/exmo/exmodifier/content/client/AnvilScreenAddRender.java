package net.exmo.exmodifier.content.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class AnvilScreenAddRender {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void eventHandler(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            int w = event.getWindow().getGuiScaledWidth();
            int h = event.getWindow().getGuiScaledHeight();
            Level world = null;
            double x = 0;
            double y = 0;
            double z = 0;
            Player entity = Minecraft.getInstance().player;
            if (entity != null) {
                world = entity.level;
                x = entity.getX();
                y = entity.getY();
                z = entity.getZ();
            }
            Font font = Minecraft.getInstance().font;

            if (entity != null && entity.getPersistentData().getBoolean("modifier_refresh_not_enough")) {
                Component component = new TranslatableComponent("exmodifier.refresh_not_enough");
                if (component != null) {
                //    int k = (int) (eventC.getMouseX() +  font.width(component) - 2);
              //     gg.fill(eventC.getMouseY()- font.lineHeight,eventC.getMouseX()- font.width(component),eventC.getMouseX(), eventC.getMouseY(), -1073741824);
                   Minecraft.getInstance().font.draw(event.getMatrixStack(), component, (int) (w*0.5 -font.width(component)*0.5), h/2-20, 16733525);
                }
            }


        }
    }
}