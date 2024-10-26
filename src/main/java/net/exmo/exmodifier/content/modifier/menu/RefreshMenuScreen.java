package net.exmo.exmodifier.content.modifier.menu;

import net.minecraft.client.gui.GuiGraphics;
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
    private static final ResourceLocation ANVIL_LOCATION = new ResourceLocation("textures/gui/container/anvil.png");
    public RefreshMenuScreen(RefreshMenu p_98901_, Inventory p_98902_, Component p_98903_, ResourceLocation p_98904_) {
        super(p_98901_, p_98902_, p_98903_, p_98904_);
        this.player = p_98902_.player;
    }

    @Override
    protected void renderErrorIcon(GuiGraphics p_281990_, int p_266822_, int p_267045_) {

    }
}