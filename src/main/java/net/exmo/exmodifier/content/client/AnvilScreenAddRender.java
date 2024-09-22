package net.exmo.exmodifier.content.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class AnvilScreenAddRender {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void eventHandler(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof AnvilScreen) {
            int w = event.getScreen().width;
            int h = event.getScreen().height;
            Level world = null;
            double x = 0;
            double y = 0;
            double z = 0;
            Player entity = Minecraft.getInstance().player;
            if (entity != null) {
                world = entity.level();
                x = entity.getX();
                y = entity.getY();
                z = entity.getZ();
            }
            Font font = Minecraft.getInstance().font;
            GuiGraphics gg =event.getGuiGraphics();
            if (entity != null && entity.getPersistentData().getBoolean("modifier_refresh_not_enough")) {
                Component component = Component.translatable("exmodifier.refresh_not_enough");
                if (component != null) {
                //    int k = (int) (event.getMouseX() +  font.width(component) - 2);
              //     gg.fill(event.getMouseY()- font.lineHeight,event.getMouseX()- font.width(component),event.getMouseX(), event.getMouseY(), -1073741824);
                    gg.drawString(font, component, (int) (event.getScreen().width*0.5 -font.width(component)*0.5), event.getScreen().height/2-20, 16733525);
                }
            }


        }
    }
}