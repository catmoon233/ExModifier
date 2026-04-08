package net.exmo.exmodifier.content.event.main;

import net.exmo.exmodifier.content.client.LanguageLoader;
import net.exmo.exmodifier.content.element.ExElementHandle;
import net.exmo.exmodifier.content.element.DefaultEntityPreparableReloadListener;
import net.exmo.exmodifier.content.element.DefaultItemPreparableReloadListener;
import net.exmo.exmodifier.content.element.ElementPreparableReloadListener;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.content.modifier.ModifierPreparableReloadListener;
import net.exmo.exmodifier.content.modifier.RefreshContainItemHandle;
import net.exmo.exmodifier.content.modifier.RefreshContainTagHandle;
import net.exmo.exmodifier.content.modifier.WashingMaterialsPreparableReloadListener;
import net.exmo.exmodifier.content.quality.ItemQualityHandle;
import net.exmo.exmodifier.content.refine.RefineHandle;
import net.exmo.exmodifier.content.refine.RefineItemRecordPreparableReloadListener;
import net.exmo.exmodifier.content.resources.ZipHandle;
import net.exmo.exmodifier.content.selected.BaseItemSelected;
import net.exmo.exmodifier.content.slot.ModifierSlotHandle;
import net.exmo.exmodifier.content.suit.ExSuitHandle;
import net.exmo.exmodifier.content.suit.ExSuitPreparableReloadListener;
import net.exmo.exmodifier.content.type.ExTypeHandle;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.util.HashMap;

import static net.exmo.exmodifier.commands.ExModifierReloadCommand.sendUpdatedModifiersToClients;
import static net.exmo.exmodifier.content.event.MainEvent.clearOldData;
import static net.exmo.exmodifier.content.event.MainEvent.clearReadTempData;

public final class MainReloadService {
    private MainReloadService() {
    }

    public static Runnable init(Runnable runnable) throws IOException {
        clearOldData();
        BaseItemSelected.IDS = new HashMap<>();
        RefreshContainTagHandle.readConfig();
        RefreshContainItemHandle.readConfig();
        ModifierHandle.sendClearDataToAllClient();
        ExTypeHandle.readConfig();
        ItemQualityHandle.init();
        ExElementHandle.init();
        ZipHandle.ZipFunction zipFunction = ZipHandle.init();
        if (runnable != null) {
            runnable.run();
        }

        ItemQualityHandle.init2();
        ModifierHandle.readConfig();
        ExSuitHandle.readConfig();
        ModifierSlotHandle.reload();

        ExElementHandle.init2();
        ExElementHandle.init3();
        RefineHandle.init();
        zipFunction.elementDefault().forEach(Runnable::run);
        zipFunction.defaultEntry().forEach(Runnable::run);
        zipFunction.suit().forEach(Runnable::run);

        ModifierHandle.EEMatchQueueHandle();
        LanguageLoader.load(LanguageLoader.LANGUAGES_FILE_PATH);
        clearReadTempData();

        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer != null) {
            if (runnable != null) {
                return () -> sendUpdatedModifiersToClients(currentServer);
            }
            sendUpdatedModifiersToClients(currentServer);
        }
        return null;
    }

    public static void registerReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new ExSuitPreparableReloadListener());
        event.addListener(new WashingMaterialsPreparableReloadListener());
        event.addListener(new ModifierPreparableReloadListener());
        event.addListener(new ElementPreparableReloadListener());
        event.addListener(new DefaultEntityPreparableReloadListener());
        event.addListener(new RefineItemRecordPreparableReloadListener());
        event.addListener(new DefaultItemPreparableReloadListener());
    }
}
