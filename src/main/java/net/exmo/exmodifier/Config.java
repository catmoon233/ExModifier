package net.exmo.exmodifier;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod.EventBusSubscriber(modid = Exmodifier.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // 配置项定义
    public static final ForgeConfigSpec.IntValue REFRESH_TIME = BUILDER
            .comment("Time interval for refresh (in seconds)")
            .defineInRange("refresh_time", 1, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.BooleanValue COMPACT_TOOLTIP = BUILDER
            .comment("Enable compact tooltip display")
            .define("compact_tooltip", true);

    public static final ForgeConfigSpec.IntValue ADD_LEVEL_SYSTEM_COUNT = BUILDER
            .comment("Number of level system additions")
            .defineInRange("add_level_system_count", 1, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.IntValue CAN_ADD_ENTRY = BUILDER
            .comment("Number of allowed entries to add")
            .defineInRange("canAddEntry", 2, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.BooleanValue DEBUG = BUILDER
            .comment("Enable debug mode")
            .define("Debug", false);

    public static final ForgeConfigSpec.BooleanValue DEBUG_IN_INFO = BUILDER
            .comment("Show debug information in logs")
            .define("DebugInInfo", false);

    public static final ForgeConfigSpec.BooleanValue FIRST_ADD_SLOTS = BUILDER
            .comment("Enable first slot addition")
            .define("FirstAddSlots", false);

    public static final ForgeConfigSpec.BooleanValue STATISTICS = BUILDER
            .comment("Enable statistics tracking")
            .define("Statistics", false);

    public static final ForgeConfigSpec.BooleanValue REFRESH_REPLACE_OLD = BUILDER
            .comment("Replace old entries on refresh")
            .define("RefreshReplaceOld", false);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    // 配置值缓存
    public static int refresh_time;
    public static boolean compact_tooltip;
    public static int add_level_system_count;
    public static int canAddEntry;
    public static boolean Debug;
    public static boolean DebugInInfo;
    public static boolean FirstAddSlots;
    public static boolean Statistics;
    public static boolean RefreshReplaceOld;

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        // 当配置加载时更新缓存值
        refresh_time = REFRESH_TIME.get();
        compact_tooltip = COMPACT_TOOLTIP.get();
        add_level_system_count = ADD_LEVEL_SYSTEM_COUNT.get();
        canAddEntry = CAN_ADD_ENTRY.get();
        Debug = DEBUG.get();
        DebugInInfo = DEBUG_IN_INFO.get();
        FirstAddSlots = FIRST_ADD_SLOTS.get();
        Statistics = STATISTICS.get();
        RefreshReplaceOld = REFRESH_REPLACE_OLD.get();
    }
}
