package net.exmo.exmodifier.init;

import net.exmo.exmodifier.content.modifier.menu.RefreshMenuScreen;
import net.exmo.exmodifier.content.modifier.menu.RefreshMenuScreenPlus;
import net.exmo.exmodifier.content.slot.menu.EmbeddedMenuScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientInit {
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ScreenAbout{
        @SubscribeEvent
        public static void clientLoad(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(RegisterOther.MenuAbout.REFRESH_MENU_PLUS.get(), RefreshMenuScreenPlus::new);
                MenuScreens.register(RegisterOther.MenuAbout.REFRESH_MENU.get(), RefreshMenuScreen::new);
                MenuScreens.register(RegisterOther.MenuAbout.EMBEDDED_MENU.get(), EmbeddedMenuScreen::new);
            });
        }
    }
}
