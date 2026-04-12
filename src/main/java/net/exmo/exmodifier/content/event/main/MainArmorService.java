package net.exmo.exmodifier.content.event.main;

import net.exmo.exmodifier.Config;
import net.exmo.exmodifier.content.event.MainEvent;
import net.exmo.exmodifier.content.helper.ItemInfo;
import net.exmo.exmodifier.content.helper.ItemLevelHelper;
import net.exmo.exmodifier.content.helper.ModifierEntryHelper;
import net.exmo.exmodifier.content.helper.ModifierSlotHelper;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.RefreshContainItemHandle;
import net.exmo.exmodifier.content.modifier.RefreshContainTagHandle;
import net.exmo.exmodifier.content.slot.ModifierSlotHandle;
import net.exmo.exmodifier.content.type.ExType;
import net.exmo.exmodifier.util.AttributeCuriosHandle;
import net.exmo.exmodifier.util.CuriosUtil;
import net.exmo.exmodifier.util.ExUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import javax.script.ScriptException;

import static net.exmo.exmodifier.Config.refresh_time;
import static net.exmo.exmodifier.content.level.ItemLevelHandle.ItemLevelRefresh;
import static net.exmo.exmodifier.content.modifier.ModifierHandle.CommonEvent.RandomEntry;
import static net.exmo.exmodifier.content.modifier.ModifierHandle.CommonEvent.RandomEntryCurios;

public final class MainArmorService {
    private MainArmorService() {
    }

    public static void handleCuriosChange(CurioChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack stack = event.getTo();
        if (isUnmatchedModItem(stack)) {
            return;
        }

        ModifierEntryHelper modifierEntryHelper = ModifierEntryHelper.of(stack);
        if (stack.getTag() == null || modifierEntryHelper.getModifierEntriesSize() <= 0) {
            RandomEntryCurios(stack, 0, refresh_time, "none");
        }

        CompoundTag refreshTag = consumeRefreshTag(stack);
        if (refreshTag != null) {
            RandomEntryCurios(
                    stack,
                    refreshTag.getInt("modifier_refresh_rarity"),
                    refreshTag.getInt("modifier_refresh_add"),
                    refreshTag.getString("wash_item")
            );
            AttributeCuriosHandle.handleCurios(new CurioChangeEvent(player, event.getIdentifier(), event.getSlotIndex(), event.getFrom(), stack));
        }

        MainSuitService.suitOperate(player, event.getTo(), event.getFrom());
    }

    public static boolean hasAttrOrBow(ItemStack stack) {
        if (stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem) {
            return true;
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!stack.getAttributeModifiers(slot).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static boolean handleArmorChange(Player player, ItemStack fromStack, ItemStack toStack, boolean isClientSide) throws ScriptException {
        handleArmorChangeExpectSuit(toStack, isClientSide);

        // Suit layers are rebuilt from current equipment on each equipment change,
        // which avoids stale cached levels when incremental add/remove misses an edge case.
        if (!isClientSide) {
            MainSuitService.rebuildSuitState(player);
            return true;
        }
        return false;
    }

    static boolean handleArmorChangeExpectSuit(ItemStack toStack, boolean isClientSide) {
        if (isClientSide) {
            return false;
        }

        ItemInfo itemInfo = ItemInfo.of(toStack);
        ModifierEntryHelper modifierEntryHelper = itemInfo.getModifierEntryHelper();
        ModifierEntryHelper.moveOldEntry(toStack);
        ItemLevelHelper.moveOldLevel(toStack);

        String itemId = ExUtil.getItemID(toStack);
        for (String modId : MainEvent.CommonEvent.UnMatchingModIDs) {
            if (itemId.startsWith(modId)) {
                return true;
            }
        }

        if (CuriosUtil.isCuriosItem2(toStack, false)) {
            randomCuriosEntryIfNeeded(toStack, modifierEntryHelper);
        } else {
            randomNormalEntryIfNeeded(toStack, modifierEntryHelper, itemId);
        }

        int addLevelSystemCount = Config.add_level_system_count;
        if (addLevelSystemCount != 0) {
            ItemLevelRefresh(toStack, 0, addLevelSystemCount, "none");
        }

        return false;
    }

    private static void randomCuriosEntryIfNeeded(ItemStack toStack, ModifierEntryHelper modifierEntryHelper) {
        if (toStack.getTag() == null || modifierEntryHelper.getModifierEntriesSize() <= 0) {
            RandomEntryCurios(toStack, 0, refresh_time, "none");
        }

        CompoundTag refreshTag = consumeRefreshTag(toStack);
        if (refreshTag != null) {
            RandomEntryCurios(
                    toStack,
                    refreshTag.getInt("modifier_refresh_rarity"),
                    refreshTag.getInt("modifier_refresh_add"),
                    refreshTag.getString("wash_item")
            );
        }
    }

    private static void randomNormalEntryIfNeeded(ItemStack toStack, ModifierEntryHelper modifierEntryHelper, String itemId) {
        boolean hasRefreshTag = toStack.getTags().anyMatch(tag -> RefreshContainTagHandle.refreshContainTag.contains(tag.toString()));
        boolean shouldRandomEntry = hasRefreshTag || RefreshContainItemHandle.refreshContainItem.contains(itemId);
        if (!shouldRandomEntry && hasAttrOrBow(toStack) && toStack.getItem().getMaxStackSize(toStack) == 1) {
            shouldRandomEntry = ModifierEntry.getType(toStack).stream().anyMatch(type -> type != ExType.UNKNOWN.get());
        }

        if (!shouldRandomEntry) {
            return;
        }

        if (toStack.getTag() == null || modifierEntryHelper.getModifierEntriesSize() <= 0) {
            ModifierSlotHelper modifierSlotHelper = ModifierSlotHelper.of(toStack);
            if (Config.FirstAddSlots && !modifierSlotHelper.validList()) {
                modifierSlotHelper.addSlot(ModifierSlotHandle.getSlot(ResourceLocation.tryParse("exmodifier:front")));
                modifierSlotHelper.addSlot(ModifierSlotHandle.getSlot(ResourceLocation.tryParse("exmodifier:centre")));
            }
            RandomEntry(toStack, 0, refresh_time, "none", 0);
        }

        CompoundTag refreshTag = consumeRefreshTag(toStack);
        if (refreshTag != null) {
            RandomEntry(
                    toStack,
                    refreshTag.getInt("modifier_refresh_rarity"),
                    refreshTag.getInt("modifier_refresh_add"),
                    refreshTag.getString("wash_item"),
                    0
            );
        }
    }

    private static boolean isUnmatchedModItem(ItemStack stack) {
        ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemKey == null) {
            return false;
        }

        String itemId = itemKey.toString();
        for (String modId : MainEvent.CommonEvent.UnMatchingModIDs) {
            if (itemId.startsWith(modId)) {
                return true;
            }
        }
        return false;
    }

    private static CompoundTag consumeRefreshTag(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("modifier_refresh") || !tag.getBoolean("modifier_refresh")) {
            return null;
        }

        tag.remove("modifier_refresh");
        tag.remove("UNKNOWN");
        return tag;
    }
}
