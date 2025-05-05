package net.exmo.exmodifier.content.attributeEffect.modern;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class EffectRenderer {
    private static final ResourceLocation GUI_ICONS = new ResourceLocation("textures/gui/icons.png");

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.screen != null) return;

        Player player = mc.player;
        if (player == null) return;

        player.getCapability(CapabilityRegistration.CUSTOM_EFFECTS_CAP).ifPresent(cap -> {
            int x = 10;
            int y = 10;
            for (CustomEffectInstance effect : cap.getEffects()) {
                RenderSystem.setShaderTexture(0, effect.getIcon());
                GuiGraphics gui = event.getGuiGraphics();
                gui.blit(GUI_ICONS, x, y, 0, 0, 18, 18, 18, 18);
                // 绘制时间文本
                gui.drawString(mc.font, String.valueOf(effect.getDuration()), x + 20, y + 5, 0xFFFFFF);
                y += 20;
            }
        });
    }
}