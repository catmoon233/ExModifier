package net.exmo.exmodifier;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = Exmodifier.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue REFRESH_TIME = BUILDER.comment("Refresh interval in seconds").defineInRange("refresh_time", 1, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.BooleanValue COMPACT_TOOLTIP = BUILDER.comment("Compact tooltip").define("compact_tooltip", true);
    private static final ModConfigSpec.IntValue ADD_LEVEL_SYSTEM_COUNT = BUILDER.comment("How many level systems to roll onto obtained gear. 0 disables the level system.").defineInRange("add_level_system_count", 0, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue CAN_ADD_ENTRY = BUILDER.comment("Max extra entries from books").defineInRange("canAddEntry", 2, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.BooleanValue ELEMENT_SYSTEM = BUILDER.comment("Enable element combat").define("ELEMENT_SYSTEM", true);
    private static final ModConfigSpec.BooleanValue DEBUG = BUILDER.comment("Debug").define("Debug", false);
    private static final ModConfigSpec.BooleanValue DEBUG_IN_INFO = BUILDER.comment("Debug in info log").define("DebugInInfo", false);
    private static final ModConfigSpec.BooleanValue FIRST_ADD_SLOTS = BUILDER.comment("Unlock a slot on first apply").define("FirstAddSlots", false);
    private static final ModConfigSpec.BooleanValue STATISTICS = BUILDER.comment("Statistics").define("Statistics", false);
    private static final ModConfigSpec.BooleanValue REFRESH_REPLACE_OLD = BUILDER.comment("Replace old entries on wash").define("RefreshReplaceOld", false);
    private static final ModConfigSpec.BooleanValue TOOLTIP_RIGHT = BUILDER.comment("Render extra tooltip on the right").define("ExMoTooltipRenderInRight", false);
    private static final ModConfigSpec.BooleanValue ALWAYS_UNDER_NAME = BUILDER.comment("Always show modifier names under item name").define("ALWAYS_DISPLAY_MODIFIER_NAME_UNDER_ITEM_NAME", false);
    private static final ModConfigSpec.BooleanValue ENTRY_COLOR = BUILDER.comment("Colored entries").define("ENTRY_COLOR", true);
    private static final ModConfigSpec.BooleanValue ENTRY_FOLD = BUILDER.comment("Fold entries").define("ENTRY_FOLD", false);
    private static final ModConfigSpec.BooleanValue ENTRY_UNDER_LINE = BUILDER.comment("Underline entries").define("ENTRY_LINE", true);
    private static final ModConfigSpec.BooleanValue ENTRY_SHOW_UNDER_LEVEL = BUILDER.comment("Show level under folded entries").define("ENTRY_SHOW_UNDER_LEVEL", true);
    private static final ModConfigSpec.BooleanValue ELEMENT_DEBUG = BUILDER.comment("Element debug").define("ELEMENT_DEBUG", false);
    private static final ModConfigSpec.IntValue MAX_REFINE = BUILDER.comment("Default max refine").defineInRange("MAX_REFINE", 5, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.DoubleValue REFINE_EFFECT = BUILDER.comment("Refine bonus per star").defineInRange("REFINE_EFFECT", 0.1D, 0.0D, Double.MAX_VALUE);
    private static final ModConfigSpec.BooleanValue REFINE_SYSTEM = BUILDER.comment("Enable refine").define("REFINE_SYSTEM", true);
    private static final ModConfigSpec.BooleanValue REFINE_ALWAYS_TOOLTIP = BUILDER.comment("Always show refine stars").define("REFINE_ALAWAYS_DISPLAY_TOOLTIP", false);
    private static final ModConfigSpec.BooleanValue REFINE_NEED_SAME_STAR = BUILDER.comment("Refine needs matching star").define("REFINE_NEED_SAME_STAR", false);
    private static final ModConfigSpec.ConfigValue<String> REFINE_ATTRIBUTE = BUILDER.comment("Refine attribute").define("REFINE_ATTRIBUTE", "minecraft:generic.attack_damage");
    private static final ModConfigSpec.BooleanValue USE_NEW_DAMAGE_FORMULA = BUILDER.comment("Use new element formula").define("USE_NEW_DAMAGE_FORMULA", true);
    private static final ModConfigSpec.BooleanValue AUTO_WEAPON_ELEMENT = BUILDER.comment("Auto assign weapon elements").define("AUTO_WEAPON_ELEMENT", false);
    private static final ModConfigSpec.BooleanValue CAN_USE_SAME_ITEM_REFINE = BUILDER.comment("Same item can refine").define("CAN_USE_SAME_ITEM_REFINE", false);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static int refreshTime = 1;
    public static boolean compactTooltip = true;
    public static int addLevelSystemCount = 0;
    public static int canAddEntry = 2;
    public static boolean elementSystem = true;
    public static boolean debug;
    public static boolean debugInInfo;
    public static boolean firstAddSlots;
    public static boolean statistics;
    public static boolean refreshReplaceOld;
    public static boolean tooltipRight;
    public static boolean alwaysUnderName;
    public static boolean entryColor = true;
    public static boolean entryFold;
    public static boolean entryUnderLine = true;
    public static boolean entryShowUnderLevel = true;
    public static boolean elementDebug;
    public static int maxRefine = 5;
    public static double refineEffect = 0.1D;
    public static boolean refineSystem = true;
    public static boolean refineAlwaysTooltip;
    public static boolean refineNeedSameStar;
    public static String refineAttribute = "minecraft:generic.attack_damage";
    public static boolean useNewDamageFormula = true;
    public static boolean autoWeaponElement;
    public static boolean canUseSameItemRefine;

    private Config() {}

    @SubscribeEvent
    public static void onLoad(ModConfigEvent event) {
        refreshTime = REFRESH_TIME.get();
        compactTooltip = COMPACT_TOOLTIP.get();
        addLevelSystemCount = ADD_LEVEL_SYSTEM_COUNT.get();
        canAddEntry = CAN_ADD_ENTRY.get();
        elementSystem = ELEMENT_SYSTEM.get();
        debug = DEBUG.get();
        debugInInfo = DEBUG_IN_INFO.get();
        firstAddSlots = FIRST_ADD_SLOTS.get();
        statistics = STATISTICS.get();
        refreshReplaceOld = REFRESH_REPLACE_OLD.get();
        tooltipRight = TOOLTIP_RIGHT.get();
        alwaysUnderName = ALWAYS_UNDER_NAME.get();
        entryColor = ENTRY_COLOR.get();
        entryFold = ENTRY_FOLD.get();
        entryUnderLine = ENTRY_UNDER_LINE.get();
        entryShowUnderLevel = ENTRY_SHOW_UNDER_LEVEL.get();
        elementDebug = ELEMENT_DEBUG.get();
        maxRefine = MAX_REFINE.get();
        refineEffect = REFINE_EFFECT.get();
        refineSystem = REFINE_SYSTEM.get();
        refineAlwaysTooltip = REFINE_ALWAYS_TOOLTIP.get();
        refineNeedSameStar = REFINE_NEED_SAME_STAR.get();
        refineAttribute = REFINE_ATTRIBUTE.get();
        useNewDamageFormula = USE_NEW_DAMAGE_FORMULA.get();
        autoWeaponElement = AUTO_WEAPON_ELEMENT.get();
        canUseSameItemRefine = CAN_USE_SAME_ITEM_REFINE.get();
    }
}
