package net.exmo.exmodifier.mixins;

import net.exmo.exmodifier.content.client.TableCraftData;
import net.exmo.exmodifier.network.ExModifiervaV;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(CraftingScreen.class)
public class TableCraftScreenMixin {

    @Inject(at = @At("HEAD"), method = "renderBg")
    public void render(GuiGraphics gg, float p, int p_283078_, int p_283647_, CallbackInfo ci) {
        CraftingScreen craftingScreen = (CraftingScreen) (Object) this;
        if (Minecraft.getInstance().player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY).orElseGet(null).craftIndex !=-2) {
            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 3; ++j) {
                    int slotIndex = j + i * 3;
                    if (slotIndex == Minecraft.getInstance().player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY).orElse(null).craftIndex) {
                        Slot slot = craftingScreen.getMenu().slots.get(slotIndex);
                        //高亮
                        if (slot != null) {
                            int a = craftingScreen.getGuiLeft();
                            int b = craftingScreen.getGuiTop();
                            gg.pose().pushPose();
                            gg.pose().translate(a , b, 100);
                            MutableComponent translatable = Component.translatable("gui.exmodifier.inherit_tip");
                            Font font = craftingScreen.getMinecraft().font;
                        //    int width = font.width(translatable);
                            gg.fill(4, 68, 172, 68 + font.lineHeight, 0x20FFFFFF);
                            gg.drawString(font, translatable, 4, 68, Color.green.getRGB());
                            gg.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, Color.green.getRGB());
                            gg.pose().popPose();
                        }
                    }
                }
            }
        }
    }
}
