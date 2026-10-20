package net.exmo.exmodifier.client.pack;

import net.exmo.exmodifier.pack.PackNetwork;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

public final class PackEditorClient {
    public static final KeyMapping OPEN = new KeyMapping(
            "key.exmodifier.pack_editor", GLFW.GLFW_KEY_K, "key.categories.exmodifier");

    private PackEditorClient() {}

    public static void register(IEventBus bus) {
        bus.addListener(PackEditorClient::keys);
        NeoForge.EVENT_BUS.addListener(PackEditorClient::tick);
        PackNetwork.clientReceiver = PackEditorScreen::accept;
    }

    private static void keys(RegisterKeyMappingsEvent event) {
        event.register(OPEN);
    }

    private static void tick(ClientTickEvent.Post event) {
        while (OPEN.consumeClick()) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) continue;
            if (minecraft.screen instanceof PackEditorScreen) minecraft.setScreen(null);
            else PackNetwork.request("open", "{}");
        }
    }
}
