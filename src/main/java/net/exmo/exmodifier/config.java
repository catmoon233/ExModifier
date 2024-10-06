package net.exmo.exmodifier;

import net.exmo.exmodifier.content.modifier.MoConfig;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.util.ExConfigHandle;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;

@Mod.EventBusSubscriber
public class config {
    public static int refresh_time = 1;
    public static boolean compact_tooltip = false;
    public static final Path ConfigPath = FMLPaths.GAMEDIR.get().resolve("config/exmo/Exmodifier.json");
    public static int canAddEntry = 1;
    public static boolean Debug = false;
    public static boolean DebugInInfo = false;
@SubscribeEvent
    public static void ConfigConfig(AddReloadListenerEvent event) throws FileNotFoundException {
    ExConfigHandle exConfigHandle = new ExConfigHandle();
    Exmodifier.LOGGER.debug(ModifierHandle.ConfigPath.toString());
    if (!new File(ConfigPath.toString()).exists()) exConfigHandle.copyResourceToFile("/data/exmodifier/Exmodifier.json", "config/exmo/Exmodifier.json");
    MoConfig MainConfig = new MoConfig(ConfigPath);
    refresh_time =  MainConfig.readSetting("refresh_time").getAsInt();
    compact_tooltip = MainConfig.readSetting("compact_tooltip").getAsBoolean();
    if (MainConfig.AlljsonObject.has("canAddEntry")){
        canAddEntry = MainConfig.readSetting("canAddEntry").getAsInt();
    }
    if (MainConfig.AlljsonObject.has("Debug")){
        Debug = MainConfig.readSetting("Debug").getAsBoolean();
    }
    if (MainConfig.AlljsonObject.has("DebugInInfo")){
        DebugInInfo = MainConfig.readSetting("DebugInInfo").getAsBoolean();
    }
}
}
