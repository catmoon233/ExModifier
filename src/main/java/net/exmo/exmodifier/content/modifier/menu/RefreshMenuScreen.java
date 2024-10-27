package net.exmo.exmodifier.content.modifier.menu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RefreshMenuScreen extends ItemCombinerScreen<RefreshMenu> {

    private final Player player;
    private static final ResourceLocation ANVIL_LOCATION = new ResourceLocation("exmodifier:textures/gui/container/refresh_table.png");
    private static final Component TOO_EXPENSIVE_TEXT = Component.translatable("container.repair.expensive");

    public RefreshMenuScreen(RefreshMenu p_97874_, Inventory p_97875_, Component p_97876_) {
        super(p_97874_, p_97875_, p_97876_, ANVIL_LOCATION);
        this.player = p_97875_.player;

    }

    @Override
    protected void renderErrorIcon(GuiGraphics p_281990_, int p_266822_, int p_267045_) {

    }

    @Override
    protected void renderLabels(GuiGraphics p_281442_, int p_282417_, int p_283022_) {
        super.renderLabels(p_281442_, p_282417_, p_283022_);
        if (player != null && player.getPersistentData().getBoolean("modifier_refresh_not_enough")) {
            Component component = Component.translatable("exmodifier.refresh_not_enough");
            if (component != null) {
                //    int k = (int) (eventC.getMouseX() +  font.width(component) - 2);
                //     gg.fill(eventC.getMouseY()- font.lineHeight,eventC.getMouseX()- font.width(component),eventC.getMouseX(), eventC.getMouseY(), -1073741824);

                int k = this.imageWidth - 8 - this.font.width(component) - 2;
                int l = 69;
                p_281442_.fill(k - 2 -40, 67, this.imageWidth - 8 -40, 79, 1325400064);
                p_281442_.drawString(font, component, (int) (k -40 ), 69, 16736352);
            }
        }
        int i = this.menu.getCost();
        if (i > 0) {
            int j = 8453920;
            Component component;
            if (i >= 40 && !this.minecraft.player.getAbilities().instabuild) {
                component = TOO_EXPENSIVE_TEXT;
                j = 16736352;
            } else if (!this.menu.getSlot(2).hasItem()) {
                component = null;
            } else {
                component = Component.translatable("exmodifier.container.refresh.cost", i);
                if (!this.menu.getSlot(2).mayPickup(this.player)) {
                    j = 16736352;
                }
            }

            if (component != null) {
                int k = this.imageWidth - 8 - this.font.width(component) - 2;
                int l = 69;
                p_281442_.fill(k - 2, 67, this.imageWidth - 8, 79, 1325400064);
                p_281442_.drawString(this.font, component, k, 69, j);
            }
        }
    }
}