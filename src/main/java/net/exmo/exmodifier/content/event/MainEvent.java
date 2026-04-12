package net.exmo.exmodifier.content.event;

import net.exmo.exmodifier.content.client.LanguageLoader;
import net.exmo.exmodifier.content.element.DefaultEntityElement;
import net.exmo.exmodifier.content.element.DefaultItemElement;
import net.exmo.exmodifier.content.element.ExElement;
import net.exmo.exmodifier.content.element.ExElementEntityData;
import net.exmo.exmodifier.content.element.ExElementHandle;
import net.exmo.exmodifier.content.event.main.MainArmorService;
import net.exmo.exmodifier.content.event.main.MainReloadService;
import net.exmo.exmodifier.content.event.main.MainSuitService;
import net.exmo.exmodifier.content.event.parameter.EventParameter;
import net.exmo.exmodifier.content.helper.EntryTooltipHelper;
import net.exmo.exmodifier.content.level.ItemLevelHandle;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.content.quality.ItemQualityHandle;
import net.exmo.exmodifier.content.slot.ModifierSlotHandle;
import net.exmo.exmodifier.content.suit.ExSuit;
import net.exmo.exmodifier.content.suit.ExSuitHandle;
import net.exmo.exmodifier.content.type.ExType;
import net.exmo.exmodifier.content.type.ExTypeHandle;
import net.exmo.exmodifier.network.sync.lang.LangMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static net.exmo.exmodifier.content.element.ExElementHandle.FoundDefaultElementConfigs;
import static net.exmo.exmodifier.content.element.ExElementHandle.FoundEntityDefaultElementConfigs;
import static net.exmo.exmodifier.content.modifier.ModifierHandle.itemsDefaultEntry;

public class MainEvent {

    private static final List<Runnable> OLD_DATA_CLEAR_MODULES = List.of(
        ExSuitHandle.LoadExSuit::clear,
        ExElementEntityData.defaultEntityAttributes::clear,
        LanguageLoader.LANGUAGES::clear,
        ModifierHandle.modifierEntryMap::clear,
        () -> ExTypeHandle.itemTypes.values().removeIf(e -> !ExType.defaultTypes.contains(e.name())),
        ItemLevelHandle.ItemLevels::clear,
        ModifierHandle.onlyCanRefreshPointEntryItemIds::clear,
        ModifierHandle.cantWashItemIds::clear,
        ExElementHandle.elementDefaultMap::clear,
        ExElementHandle.elementDefaultMap2::clear,
        ExElementHandle.exElements::clear,
        itemsDefaultEntry::clear,
        ModifierHandle.materialsList::clear,
        ModifierSlotHandle.registerSlots::clear,
        ModifierSlotHandle.unLockSlotItems::clear,
        ItemQualityHandle.itemQualityMap::clear,
        ItemQualityHandle.itemDefaultQualityMap::clear
    );

    private static final List<Runnable> READ_TEMP_CLEAR_MODULES = List.of(
        ModifierHandle.Foundmoconfigs::clear,
        ItemLevelHandle.Foundlvconfigs::clear,
        ExSuitHandle.FoundSuitConfigs::clear,
        ItemQualityHandle.FoundQualityConfigs::clear,
        ItemQualityHandle.FoundDefaultQualityConfigs::clear,
        ExTypeHandle.FoundTypeConfigs::clear,
        ExElementHandle.FoundElementConfigs::clear,
        FoundEntityDefaultElementConfigs::clear,
        FoundDefaultElementConfigs::clear
    );

    public static class CommonEvent {
        public static final List<String> UnMatchingModIDs = new ArrayList<>();

        static {
            UnMatchingModIDs.add("umapyoi");
        }

        public static float damageBoost = 1;
        public static float damageNumber = 0;
        public static boolean hasDamageBoost = false;
        public static boolean hasDamageNumber = false;
        public static boolean skipInvulnerableTime = false;

        public static int cache_invulnerableTime_time = 0;

        public static Pair<List<Component>, Integer> EntryInfoTooltip(ItemStack stack, List<Component> tooltip, Player player) {
            return EntryTooltipHelper.appendEntryInfo(stack, tooltip, player);
        }

        public static void addx(Player player, List<EventParameter<?>> eventParameters, String name) {
            ItemLevelHandle.ItemAddXpAuto(player, eventParameters, name);
        }

        public static void ApplySuitEffect(Player player, ExSuit.Trigger trigger) {
            MainSuitService.applySuitEffect(player, trigger);
        }

        public static boolean hasAttrOrBow(ItemStack stack) {
            return MainArmorService.hasAttrOrBow(stack);
        }

        public static boolean handleArmorChange(Player player, ItemStack fromStack, ItemStack toStack, boolean isClientSide) throws ScriptException {
            return MainArmorService.handleArmorChange(player, fromStack, toStack, isClientSide);
        }

        public static boolean SuitOperate(@NotNull Player player, ItemStack stack1, ItemStack stack2) {
            return MainSuitService.suitOperate(player, stack1, stack2);
        }

        public static Runnable init(Runnable runnable) throws IOException {
            return MainReloadService.init(runnable);
        }
    }

    public record DataCache(
            List<ExSuit> exSuits,
            List<ModifierEntry> modifierEntries,
            List<ExElement> exElements,
            List<DefaultItemElement> defaultItemElements,
            List<DefaultEntityElement> defaultEntityElements,
            LangMessage.LangMessageHandler langMessageHandler
    ) {
        private static List<ExSuit> createExSuits() {
            return new ArrayList<>(ExSuitHandle.LoadExSuit.values());
        }

        private static List<ModifierEntry> createModifierEntries() {
            return new ArrayList<>(ModifierHandle.modifierEntryMap.values());
        }

        private static List<ExElement> createExElements() {
            return new ArrayList<>(ExElementHandle.exElements.values());
        }

        private static List<DefaultItemElement> createDefaultItemElements() {
            return new ArrayList<>(ExElementHandle.elementDefaultMap.values());
        }

        private static List<DefaultEntityElement> createDefaultEntityElements() {
            return new ArrayList<>(ExElementHandle.elementDefaultMap2.values());
        }

        private static LangMessage.LangMessageHandler createLangHandler() {
            return new LangMessage.LangMessageHandler(LanguageLoader.LANGUAGES);
        }

        public static DataCache create() {
            return new DataCache(
                    createExSuits(),
                    createModifierEntries(),
                    createExElements(),
                    createDefaultItemElements(),
                    createDefaultEntityElements(),
                    createLangHandler()
            );
        }
    }

    private static void runClearModules(List<Runnable> modules) {
        for (Runnable module : modules) {
            module.run();
        }
    }

    public static DataCache createDataCache() {
        return DataCache.create();
    }

    public static void clearAllDataOneClick() {
        runClearModules(OLD_DATA_CLEAR_MODULES);
        runClearModules(READ_TEMP_CLEAR_MODULES);
    }

    public static void sendExmoServerDataToServerPlayer(ServerPlayer entity, DataCache dataCache) {
        for (ExSuit exSuit : dataCache.exSuits()) {
            ModifierHandle.sendExSuitToClient(exSuit, entity);
        }
        for (ModifierEntry modifierEntry : dataCache.modifierEntries()) {
            ModifierHandle.sendModifierEntryToClient(modifierEntry, entity);
        }
        for (ExElement exElement : dataCache.exElements()) {
            ModifierHandle.sendElementToClient(exElement, entity);
        }
        for (DefaultItemElement defaultItemElement : dataCache.defaultItemElements()) {
            ModifierHandle.sendDefaultItemElementToClient(defaultItemElement, entity);
        }
        for (DefaultEntityElement defaultEntityElement : dataCache.defaultEntityElements()) {
            ModifierHandle.sendDefaultEntityElementToClient(defaultEntityElement, entity);
        }
        ModifierHandle.sendLangMessageToClient(dataCache.langMessageHandler(), entity);
    }

    public static void clearOldData() {
        runClearModules(OLD_DATA_CLEAR_MODULES);
        clearReadTempData();
    }

    public static void clearReadTempData() {
        runClearModules(READ_TEMP_CLEAR_MODULES);
    }
}
