package net.exmo.exmodifier.content.helper;

import com.google.common.collect.Multimap;
import net.exmo.exmodifier.Config;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.level.ItemLevelHandle;
import net.exmo.exmodifier.content.modifier.*;

import net.exmo.exmodifier.content.type.ItemType;
import net.exmo.exmodifier.events.ExOnTableRefreshEntriesEvent;
import net.exmo.exmodifier.events.ExRefreshEvent;
import net.exmo.exmodifier.network.PlayerRefreshScreenOverMessageMessage;
import net.exmo.exmodifier.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.CommonEvent.*;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.modifierEntryMap;
import static net.exmo.exmodifier.content.modifier.menu.RefreshMenu.compareItemType;

public class ModifierEntryHelper extends ExHelper {
    public static final String MES = "ModifierEntry";
    public static final String MEID = "EntryID";
    public static final String SLOT = "slot";

    public static ModifierEntryHelper of(ItemStack itemStack) {
        return new ModifierEntryHelper(itemStack);
    }


    public static class refreshContent {

        public static ItemStack applyRefreshEffect(Player player, ItemStack inputItem, ItemStack washItem) {
            if (inputItem.isEmpty() || washItem.isEmpty()) return ItemStack.EMPTY;
            if (player instanceof ServerPlayer serverPlayer) {

                ItemStack result = inputItem;
                boolean effectApplied = false;

                // 处理洗涤材料逻辑
                for (WashingMaterials material : ModifierHandle.materialsList) {
                    if (material.item.equals(washItem.getItem()) && washItem.getCount() >= material.NeedCount) {
                        if (!checkMaterialConditions(result, material)) continue;

                        ModifierEntryHelper modifierHelper = ModifierEntryHelper.of(result);

                        processExistingEntries(modifierHelper, material);

                        int finalRarity = calculateFinalRarity(material);
                        applyNewEntries(player, result, material, finalRarity);
                        processItemLevels(result, material);
                        washItem.shrink(material.NeedCount);
                        effectApplied = handleRefreshEvents(player, inputItem, washItem, result, material);
                        break;
                    }
                }

                // 处理词条物品逻辑
                if (!effectApplied && washItem.getItem() instanceof EntryItem) {
                    effectApplied = handleEntryItem(player, result, washItem);

                }
                PlayerRefreshScreenOverMessageMessage message1;
                if (effectApplied) {
                    message1 = new PlayerRefreshScreenOverMessageMessage(ItemStack.EMPTY, Component.translatable("gui.exmodifier.refresh_success"));
                } else {
                    message1 = new PlayerRefreshScreenOverMessageMessage(ItemStack.EMPTY, Component.translatable("gui.exmodifier.refresh_fail"));
                }
                Exmodifier.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> serverPlayer), message1);


                return effectApplied ? cleanupTags(result) : ItemStack.EMPTY;
            }
            return ItemStack.EMPTY;
        }


        private static boolean checkMaterialConditions(ItemStack item, WashingMaterials material) {
            if (!material.OnlyTypes.isEmpty() && !ModifierEntry.containItemTypes(item, material.OnlyTypes))
                return false;
            if (material.OnlyItems != null && !material.OnlyItems.contains(ExUtil.getItemID(item)))
                return false;
            return material.OnlyTags == null || material.containTag(item);
        }

        private static void processExistingEntries(ModifierEntryHelper helper, WashingMaterials material) {
            int keepEntries = Math.max(0, material.getKeepEntries());
            if (keepEntries == 0) {
                helper.removeAllEntry(true, List.of(ModifierEntry.defaultTag));
            } else {
                List<ModifierInstant> entries = helper.getModifierEntries();
                int totalEntries = entries.size();
                int keepCount = Math.min(keepEntries, totalEntries);

                if (material.isKeepEntriesFromEnd()) {
                    for (int i = totalEntries - keepCount - 1; i >= 0; i--) {
                        helper.removeModifierEntryAt(i, true);
                    }
                } else {
                    for (int i = totalEntries - 1; i >= keepCount; i--) {
                        helper.removeModifierEntryAt(i, true);
                    }
                }
            }
        }

        private static int calculateFinalRarity(WashingMaterials material) {
            if (material.MinRandomTime <= 0 || material.MaxRandomTime <= 0) return material.rarity;
            return material.rarity + new Random().nextInt(material.MaxRandomTime - material.MinRandomTime) + material.MinRandomTime;
        }

        private static void applyNewEntries(Player player, ItemStack result, WashingMaterials material, int rarity) {
            if (material.additionEntry > 0) {
                if (CuriosUtil.isCuriosItem2(result,false)) {
                    RandomEntryCurios(result, rarity, material.additionEntry, material.ItemId);
                } else {
                    ModifierHandle.CommonEvent.RandomEntry(
                            result,
                            rarity,
                            material.additionEntry,
                            material.ItemId,
                            material.getKeepEntries()
                    );
                }

                if (!player.level().isClientSide) {
                    MinecraftForge.EVENT_BUS.post(new ExRefreshEvent(
                            player,
                            material.additionEntry,
                            rarity,
                            material.ItemId
                    ));
                }
            }
        }

        private static void processItemLevels(ItemStack result, WashingMaterials material) {
            if (material.randomLevelSystemCount != 0) {
                ItemLevelHandle.ItemLevelRefresh(
                        result,
                        material.randomLevelSystemCount,
                        1,
                        material.ItemId
                );
            }
        }

        private static boolean handleRefreshEvents(Player player, ItemStack input, ItemStack washItem, ItemStack result, WashingMaterials material) {
            ExOnTableRefreshEntriesEvent event = new ExOnTableRefreshEntriesEvent(material, input, washItem, result);
            MinecraftForge.EVENT_BUS.post(event);
            return !event.isCanceled();
        }

        private static boolean handleEntryItem(Player player, ItemStack result, ItemStack washItem) {
            if (!compareItemType(result, washItem)) return false;

            ModifierEntryHelper helper = ModifierEntryHelper.of(result);
            ModifierEntry entry = ((EntryItem) washItem.getItem()).getModifierEntry(washItem);
            if (player instanceof ServerPlayer serverPlayer) {
                if (helper.getModifierEntriesSize() >= Config.canAddEntry) {
                    PlayerRefreshScreenOverMessageMessage message1;
                    message1 = new PlayerRefreshScreenOverMessageMessage(ItemStack.EMPTY, Component.translatable("gui.exmodifier.refresh_fail_limit"));
                    Exmodifier.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> serverPlayer), message1);
                    return false;
                }
            }
            if (helper.getModifierEntryLevel(entry.id) >= entry.maxLevel) return false;

            CompoundTag tag = result.getOrCreateTag();
            int entryCount = tag.getInt("entryitem_add");

            if (entryCount >= Config.canAddEntry) {
                tag.putBoolean("can_add_max", true);
                return true;
            }

            helper.addModifierEntry(new ModifierInstant(entry, EntryItem.getModifierLevel(washItem)), true, true);
            tag.putInt("entryitem_add", entryCount + 1);
            washItem.shrink(1);
            return true;
        }

        private static ItemStack cleanupTags(ItemStack result) {
            CompoundTag tag = result.getOrCreateTag();
            tag.remove("modifier_refresh");
            tag.remove("entry_item_add");
            tag.remove("modifier_refresh_rarity");
            tag.remove("random_level_system_count");
            tag.remove("NeedCount");
            tag.remove("wash_item");
            tag.remove("keepEntries");
            tag.remove("modifier_refresh_add");
            tag.remove("UNKNOWN");
            return result;
        }


    }

    public static void moveOldEntry(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag == null) return;
        if (tag.contains("exmodifier_armor_modifier_applied")) {
            ModifierEntryHelper modifierEntryHelper = new ModifierEntryHelper(itemStack);
            for (ModifierEntry modifierEntry : oldFunc.getEntrysFromItemStack_old(itemStack)) {
                modifierEntryHelper.addModifierEntry(new ModifierInstant(modifierEntry, 1), true, true);

            }
            oldFunc.clearEntry_old(itemStack);
            tag.remove("exmodifier_armor_modifier_applied");
        }
    }

    //    public boolean containEntry(String id){
//
//    }
    public boolean ValidModifierEntry() {
        return ValidMainNbt() && getMainNbt().contains(MES);
    }

    public ModifierEntryHelper createModifierEntryNbt() {
        createNbt();
        if (ValidModifierEntry()) return this;
        getMainNbt().put(MES, new ListTag());

        return this;

    }


    public static Map<String, Double> item_old_number_cache = new HashMap<>();

    public ModifierEntryHelper copyOtherHelper(ModifierEntryHelper other) {
        if (!other.ValidModifierEntry()) return this;

        other.getModifierEntriesB().forEach(
                modifierEntry -> {
                    modifierEntry.attriGether.forEach(
                            attriGether -> {
                                item_old_number_cache.put(attriGether.modifier.getName(), ItemAttrUtil.getAmountFromAttributeName(other.itemStack, attriGether.attribute, attriGether.modifier.getName()));
                            }
                    );
                }
        );
        for (ModifierInstant modifierEntry : other.getModifierEntries()) {

            addModifierEntry(modifierEntry.lock(), true, false, modifierEntry);
        }
        removeAllEntry(true);
        List<ModifierInstant> modifierEntries = getModifierEntries().stream().map(ModifierInstant::unlock).toList();
        removeAllEntrySkinLock( true,List.of());
        modifierEntries.forEach(e->addModifierEntry(e,true,false));
        item_old_number_cache.clear();
        return this;
    }

    public ModifierEntryHelper removeAllEntry(boolean removeAttribute, List<TagKey<ModifierEntry>> onlyTags) {
        for (ModifierInstant modifierInstant : getModifierEntries()) {
            if (!modifierInstant.isLock() && (onlyTags.isEmpty() || onlyTags.stream().anyMatch(tag -> modifierInstant.getModifierEntry().tags.contains(tag)))) {
                removeModifierEntry(modifierInstant, removeAttribute);

            }
        }
        //    getMainNbt().put(MES, new ListTag());
        return this;
    }
    public ModifierEntryHelper removeAllEntrySkinLock(boolean removeAttribute, List<TagKey<ModifierEntry>> onlyTags) {
        for (ModifierInstant modifierInstant : getModifierEntries()) {
            if ( (onlyTags.isEmpty() || onlyTags.stream().anyMatch(tag -> modifierInstant.getModifierEntry().tags.contains(tag)))) {
                removeModifierEntry(modifierInstant, removeAttribute);

            }
        }
        //    getMainNbt().put(MES, new ListTag());
        return this;
    }

    public ModifierEntryHelper removeAllEntry(boolean removeAttribute) {
        removeAllEntry(removeAttribute, List.of());
        return this;
    }

    public static int getLivingEntityEntryLevel(String entryID, LivingEntity e) {
        int level = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack itemBySlot = e.getItemBySlot(slot);
            if (itemBySlot.isEmpty()) continue;
            if (!CuriosUtil.isCuriosItem2(itemBySlot,false)) {
                ModifierEntryHelper modifierEntryHelper = new ModifierEntryHelper(itemBySlot);
                level += modifierEntryHelper.getModifierEntryLevel(entryID);

            }
        }
        return level;
    }

    public static int getLivingEntitySubEntryLevel(String entryID, LivingEntity e) {
        int level = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack itemBySlot = e.getItemBySlot(slot);
            if (itemBySlot.isEmpty()) continue;
            if (!CuriosUtil.isCuriosItem2(itemBySlot,false)) {
                ModifierEntryHelper modifierEntryHelper = new ModifierEntryHelper(itemBySlot);
                level += modifierEntryHelper.getSubModifierEntryLevel(entryID);

            }
        }
        return level;
    }

    public List<ModifierEntry> randomEntry(int rarity, String material, int refreshTime) {


        Map<ItemType, EquipmentSlot[]> typeEquipmentSlotMap = typeSlotMap();
        WeightedUtil<String> weightedUtil = new WeightedUtil<>(new HashMap<>());


        for (Map.Entry<ItemType, EquipmentSlot[]> entry : typeEquipmentSlotMap.entrySet()) {
            ItemType type = entry.getKey();

            if (!isValidForType(itemStack, type)) {
                continue;
            }


            weightedUtil.merge(new WeightedUtil<>(
                    modifierEntryMap.entrySet().stream()
                            .filter(e -> {
                                if (e.getValue().weight == 0) return false;
                                var modifier = e.getValue();
                                boolean hasWashItem = ModifierHandle.materialsList.stream()
                                        .anyMatch(m -> m.ItemId.equals(material) && !m.OnlyHasWashEntry);


                                List<String> onlyWashItems = modifier.getModifierItemSelector().getOnlyWashItems();
                                return modifier.types.stream().map(ItemType::name).toList().contains(type.name()) &&
                                        modifier.getModifierItemSelector().containItem(itemStack) &&
                                        !modifier.cantSelect &&
                                        (modifier.Slots.isEmpty()) &&
                                        (modifier.needFreshValue == 0 || modifier.needFreshValue <= rarity) &&
                                        (onlyWashItems.isEmpty() || onlyWashItems.contains(material) || hasWashItem);
                            })
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    e -> e.getValue().weight
                            ))
            ));
        }

        if (!weightedUtil.weights.isEmpty()) {
            weightedUtil.increaseWeightsByRarity(rarity);
            try {
                return ModifierSelector.selectEntriesOnly(
                        weightedUtil, refreshTime,
                        key -> modifierEntryMap.get(key)
                ).entries;
            } catch (Exception e) {
                // 处理异常
            }
        }
        return new ArrayList<>();
    }

    public ModifierEntryHelper setModifierEntry(int index, ModifierInstant instant) {
        ListTag modifierEntriesNbt = getModifierEntriesNbt();
        modifierEntriesNbt.set(index, instant.serializeNBT());
        getMainNbt().put(MES, modifierEntriesNbt);
        return this;
    }

    public ModifierEntryHelper setModifierEntryLevel(String entryID, int level) {
        for (ModifierInstant modifierInstant : getModifierEntries()) {
            if (modifierInstant.getModifierEntry().id.equals(entryID)) {
                modifierInstant.setLevel(level);
                return this;
            }
        }
        return this;
    }

    public int getSubModifierEntryLevel(String entryID) {
        ListTag modifierEntriesNbt = getModifierEntriesNbt();
        for (int i = 0; i < modifierEntriesNbt.size(); i++) {
            if (modifierEntriesNbt.getCompound(i).getString(MEID).substring(2).equals(entryID.substring(2)))
                return modifierEntriesNbt.getCompound(i).getInt("Level");
        }
        return 0;
    }

    public int getModifierEntryLevel(String entryID) {
        ListTag modifierEntriesNbt = getModifierEntriesNbt();
        for (int i = 0; i < modifierEntriesNbt.size(); i++) {
            if (modifierEntriesNbt.getCompound(i).getString(MEID).equals(entryID))
                return modifierEntriesNbt.getCompound(i).getInt("Level");
        }
        return 0;
    }

    public Optional<String> getModifierEntrySlot(String entryID) {
        ListTag modifierEntriesNbt = getModifierEntriesNbt();
        for (int i = 0; i < modifierEntriesNbt.size(); i++) {
            if (modifierEntriesNbt.getCompound(i).getString(MEID).equals(entryID))
                return Optional.of(modifierEntriesNbt.getCompound(i).getString(SLOT));
        }
        return Optional.empty();
    }

    public ModifierEntryHelper setModifierEntrySlot(String entryID, String slot) {
        ListTag modifierEntriesNbt = getModifierEntriesNbt();
        for (int i = 0; i < modifierEntriesNbt.size(); i++) {
            if (modifierEntriesNbt.getCompound(i).getString(MEID).equals(entryID)) {
                modifierEntriesNbt.getCompound(i).putString(SLOT, slot);
                return this;
            }
        }
        return this;
    }

    public ModifierEntryHelper(ItemStack itemStack) {
        super(itemStack);
    }

    public int getModifierEntriesSize() {
        CompoundTag mainNbt = getMainNbt();
        if (!ValidMainNbt()) return 0;
        return mainNbt.getList(MES, 10).size();
    }

    public boolean gatherModifierInstant(ModifierInstant modifierInstant) {
        List<ModifierInstant> modifierEntries = getModifierEntries();
        AtomicInteger level = new AtomicInteger(modifierInstant.getLevel());
        List<ModifierInstant> toRemove = new ArrayList<>();
        modifierEntries.forEach(x -> {
            if (x.getModifierEntry().id.equals(modifierInstant.getModifierEntry().id)) {
                level.set(modifierInstant.getLevel() + x.getLevel());
                toRemove.add(x);
            }
        });

        if (level.get() == modifierInstant.getLevel()) return false;
        for (ModifierInstant re : toRemove){
            removeModifierEntryUnLock(re, true);
        }

        addModifierEntry(modifierInstant.setLevel(level.get()), true, false);
        return level.get() != modifierInstant.getLevel();
    }

    public ListTag getModifierEntriesNbt() {
        return getMainNbt().getList(MES, 10);
    }

    public List<ModifierInstant> getModifierEntries() {
        List<ModifierInstant> modifierEntries = new ArrayList<>();
        if (ValidMainNbt()) {
            CompoundTag tag = getMainNbt();
            if (tag.contains(MES)) {
                ListTag modifiersList = tag.getList(MES, 10);
                for (int i = 0; i < modifiersList.size(); i++) {
                    CompoundTag tag1 = modifiersList.getCompound(i);
                    ModifierEntry modifierEntry = modifierEntryMap.get(tag1.getString(MEID));
                    if (modifierEntry != null) {
                        int level = 1;
                        String slot = "";
                        if (tag1.contains("Level")) level = tag1.getInt("Level");
                        if (tag1.contains(SLOT)) slot = tag1.getString(SLOT);
                        CompoundTag tag2 = tag1.copy();
                        tag2.remove("Level");
                        tag2.remove(SLOT);
                        tag2.remove(MEID);
                        modifierEntries.add(new ModifierInstant(modifierEntry, level)
                                .setData(tag2).setSlot(slot).setLock(tag1.contains("islock") && tag1.getBoolean("islock"))
                        );
                    }
                }
            }
        }
        return modifierEntries;


    }

    public List<ModifierEntry> getModifierEntriesB() {
        List<ModifierEntry> modifierEntries = new ArrayList<>();
        for (ModifierInstant modifierInstant : getModifierEntries()) {
            modifierEntries.add(modifierInstant.getModifierEntry());
        }
        return modifierEntries;
    }

    public ModifierEntryHelper addModifierEntry(ModifierInstant modifierInstant, boolean addAttribute, boolean gather, ModifierInstant oldInstant) {
        createNbt();
        if (!ValidMainNbt()) createMainNbt();
        createModifierEntryNbt();
        if (gather) {
            if (gatherModifierInstant(modifierInstant)) return this;
        }

        CompoundTag tag1 = new CompoundTag();
        tag1.putString(MEID, modifierInstant.getModifierEntry().id);
        if (modifierInstant.isItemQualityLock()) tag1.putBoolean("ItemQualityLock", true);
        if (modifierInstant.isLock()) tag1.putBoolean("islock", true);
        tag1.putInt("Level", modifierInstant.getLevel());
        if (modifierInstant.getSlot().isPresent()) tag1.putString(SLOT, modifierInstant.getSlot().get());
        ListTag modifiersList = getModifierEntriesNbt();
        modifiersList.add(tag1);
        if (addAttribute) {

            List<ModifierAttriGether> addTo = selectModifierAttributes(modifierInstant.getModifierEntry());

            if (CuriosUtil.isCuriosItem2(this.itemStack,false))
                applyModifiersCurios(itemStack, addTo, CuriosUtil.getSlotsFromItemstack(itemStack,false), oldInstant);
            else applyModifiers(itemStack, addTo, getEquipmentSlot(itemStack), modifierInstant, oldInstant);
        }
        return this;
    }

    public ModifierEntryHelper addModifierEntry(ModifierInstant modifierInstant, boolean addAttribute, boolean gather) {
        addModifierEntry(modifierInstant, addAttribute, gather, null);
        return this;
    }

    public ModifierEntryHelper removeModifierEntryLevel(ModifierInstant modifierInstant, boolean removeAttribute) {
        var old_modifier_level = getModifierEntryLevel(modifierInstant.getModifierEntry().id);
        if (old_modifier_level > modifierInstant.getLevel()) {
            return this.setModifierEntryLevel(modifierInstant.getModifierEntry().id, old_modifier_level - modifierInstant.getLevel());
        } else return this.removeModifierEntry(modifierInstant, removeAttribute);

    }

    public ModifierEntryHelper removeModifierEntry(ModifierInstant modifierInstant, boolean removeAttribute) {
        createNbt();
        if (!ValidMainNbt()) return this;
        ListTag modifiersList = getModifierEntriesNbt();
        for (int i = modifiersList.size() - 1; i >= 0; i--) {
            CompoundTag tag1 = modifiersList.getCompound(i);
            if (tag1.getString(MEID).equals(modifierInstant.getModifierEntry().id)) {
                if (!tag1.getBoolean("CantRemove") || !tag1.getBoolean("itemQualityLock")) {
                    modifiersList.remove(i);
                }
            }
        }
        if (removeAttribute) {
            if (CuriosUtil.isCuriosItem2(itemStack,false)) {
                for (ModifierAttriGether modifierAttriGether : modifierInstant.getModifierEntry().attriGether) {
                    if (modifierAttriGether.attribute != null)
                        CuriosUtil.removeAttributeModifierAffix(itemStack, ExUtil.getAttributeID(modifierAttriGether.attribute).toString(), modifierAttriGether.modifier.getName());
                }
            }
            for (ModifierAttriGether modifierAttriGether : modifierInstant.getModifierEntry().attriGether) {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    ItemAttrUtil.removeAttributeModifierNoAmout(itemStack, modifierAttriGether.attribute, modifierAttriGether.modifier, slot);
                }
            }
        }
        return this;
    }

    public ModifierEntryHelper removeModifierEntryAt(int index, boolean removeAttribute) {
        createNbt();
        if (!ValidMainNbt()) return this;

        ListTag modifiersList = getModifierEntriesNbt();
        if (index < 0 || index >= modifiersList.size()) return this;

        CompoundTag removedTag = modifiersList.getCompound(index).copy();
        modifiersList.remove(index);

        if (removeAttribute) {
            ModifierEntry removedEntry = modifierEntryMap.get(removedTag.getString(MEID));
            if (removedEntry != null) {
                if (CuriosUtil.isCuriosItem2(itemStack, false)) {
                    for (ModifierAttriGether modifierAttriGether : removedEntry.attriGether) {
                        if (modifierAttriGether.attribute != null) {
                            CuriosUtil.removeAttributeModifierAffix(
                                    itemStack,
                                    ExUtil.getAttributeID(modifierAttriGether.attribute).toString(),
                                    modifierAttriGether.modifier.getName()
                            );
                        }
                    }
                }
                for (ModifierAttriGether modifierAttriGether : removedEntry.attriGether) {
                    for (EquipmentSlot slot : EquipmentSlot.values()) {
                        ItemAttrUtil.removeAttributeModifierNoAmout(itemStack, modifierAttriGether.attribute, modifierAttriGether.modifier, slot);
                    }
                }
            }
        }
        return this;
    }


    public ModifierEntryHelper removeModifierEntryUnLock(ModifierInstant modifierInstant, boolean removeAttribute) {
        createNbt();
        if (!ValidMainNbt()) return this;
        ListTag modifiersList = getModifierEntriesNbt();
        for (int i = 0; i < modifiersList.size(); i++) {
            CompoundTag tag1 = modifiersList.getCompound(i);
            if (tag1.getString(MEID).equals(modifierInstant.getModifierEntry().id)) {
                modifiersList.remove(i);
                break;

            }
        }
        if (removeAttribute) {
            for (ModifierAttriGether modifierAttriGether : modifierInstant.getModifierEntry().attriGether) {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    ItemAttrUtil.removeAttributeModifierNoAmout(itemStack, modifierAttriGether.attribute, modifierAttriGether.modifier, slot);
                }
            }
        }
        return this;
    }

    public static ModifierEntry getEntry(String entryName) {
        return modifierEntryMap.get(entryName);
    }

    public static List<ModifierAttriGether> getEntryAttriGether(ModifierEntry entry) {
        return entry.attriGether;
    }

    public static List<ModifierAttriGether> getEntryAttriGether(String entryID) {
        return getEntry(entryID).attriGether;
    }

    public static class oldFunc {
        @Deprecated(since = "0.033", forRemoval = true)
        public static int getItemStackEntryCount_old(ItemStack stack) {
            if (stack.getTag() == null) return 0;
            for (int i = 0; true; i++) {
                if (stack.getTag().getString("exmodifier_armor_modifier_applied" + i).isEmpty()) {
                    return i;
                }

            }
        }

        @Deprecated(since = "0.033", forRemoval = true)
        public static List<ModifierEntry> getEntrysFromItemStack_old(ItemStack stack) {
            List<ModifierEntry> modifierEntries = new ArrayList<>();
            if (stack.getTag() == null) return modifierEntries;
            for (ModifierEntry modifierAttriGether : modifierEntryMap.values().stream().filter(Objects::nonNull).toList()) {
                String id;
                for (int i = 0; true; i++) {
                    id = stack.getTag().getString("exmodifier_armor_modifier_applied" + i);
                    if (id.isEmpty()) break;
                    if (id.equals(modifierAttriGether.getId())) {
                        modifierEntries.add(modifierAttriGether);
                    }

                }
            }
            return modifierEntries;
        }

        @Deprecated(since = "0.033", forRemoval = true)
        public static void clearEntry_old(ItemStack stack) {
            if (stack.getTag() == null) return;
            if (stack.getTag().getInt("exmodifier_armor_modifier_applied") == 0) return;
            //  List<ItemType> types = ModifierEntry.getType(stack);
            List<String> curiosType = CuriosUtil.getSlotsFromItemstack(stack,false);
            List<ModifierEntry> hasAttriGether = getEntrysFromItemStack_old(stack);
            for (int i = 0; i < hasAttriGether.size(); i++) {
                ModifierEntry modifierAttriGether = hasAttriGether.get(i);
                for (ModifierAttriGether modifierAttriGether1 : modifierAttriGether.attriGether) {
                    EquipmentSlot slot = modifierAttriGether1.slot;
                    if (modifierAttriGether1.IsAutoEquipmentSlot) {
                        List<ItemType> type = ModifierEntry.getType(stack);
                        if (!type.isEmpty()) slot = ModifierEntry.TypeToEquipmentSlot(type.get(0));
                    }
                    if (curiosType.isEmpty())
                        ItemAttrUtil.removeAttributeModifierNoAmout(stack, modifierAttriGether1.getAttribute(), modifierAttriGether1.getModifier(), slot);
                    else {
                        for (String curioType : curiosType) {
                            if (ForgeRegistries.ATTRIBUTES.containsValue(modifierAttriGether1.getAttribute()) && ExUtil.getAttributeID(modifierAttriGether1.getAttribute()) != null)
                                CuriosUtil.removeAttributeModifierAffix(stack, ExUtil.getAttributeID(modifierAttriGether1.getAttribute()).toString(), modifierAttriGether1.getModifier().getName());
                        }
                    }
                    stack.getOrCreateTag().remove("exmodifier_armor_modifier_applied" + i);
                }

            }
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                Multimap<Attribute, AttributeModifier> attributeModifiers = stack.getAttributeModifiers(equipmentSlot);
                if (attributeModifiers.isEmpty()) attributeModifiers.clear();
            }
        }
    }
}

