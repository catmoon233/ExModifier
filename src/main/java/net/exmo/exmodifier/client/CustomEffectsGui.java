package net.exmo.exmodifier.client;

import net.exmo.exmodifier.core.attachment.CustomEffects;
import net.exmo.exmodifier.core.attachment.ExModifierAttachments;
import net.exmo.exmodifier.core.data.AttriEffectDefinition;
import net.exmo.exmodifier.core.runtime.CustomEffectsRuntime;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/** HUD overlay for active custom attribute effects (top-left above the hotbar). */
@OnlyIn(Dist.CLIENT)
public final class CustomEffectsGui {
    private CustomEffectsGui() {}

    public static void register(RegisterGuiLayersEvent event) {
        event.registerAboveAll(net.exmo.exmodifier.Exmodifier.id("custom_effects"),
                (GuiGraphics graphics, DeltaTracker delta) -> render(graphics));
    }

    private static void render(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.screen != null) return;
        CustomEffects effects = player.getData(ExModifierAttachments.CUSTOM_EFFECTS);
        if (effects.values().isEmpty()) return;
        Font font = minecraft.font;
        int y = 5;
        for (var instance : effects.values()) {
            AttriEffectDefinition definition = CustomEffectsRuntime.definitions().get(instance.id());
            String name = definition != null && definition.descriptionKey().isPresent()
                    ? Component.translatable(definition.descriptionKey().get()).getString()
                    : instance.id().getPath();
            int seconds = Math.max(1, instance.duration() / 20);
            graphics.drawString(font, name + " " + seconds + "s", 5, y, 0xFFFFFF, true);
            y += 10;
        }
    }
}
