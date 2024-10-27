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

    public RefreshMenuScreen(RefreshMenu p_97874_, Inventory p_97875_, Component p_97876_) {
        super(p_97874_, p_97875_, p_97876_, ANVIL_LOCATION);
        this.player = p_97875_.player;

    }

    @Override
    protected void renderErrorIcon(GuiGraphics p_281990_, int p_266822_, int p_267045_) {

    }
}