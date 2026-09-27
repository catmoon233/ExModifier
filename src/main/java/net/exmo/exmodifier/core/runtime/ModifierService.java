package net.exmo.exmodifier.core.runtime;

import net.exmo.exmodifier.Config;
import net.exmo.exmodifier.api.ExModifierApi;
import net.exmo.exmodifier.core.component.AppliedModifier;
import net.exmo.exmodifier.core.component.AppliedModifiers;
import net.exmo.exmodifier.core.component.EntryBook;
import net.exmo.exmodifier.core.component.ExModifierComponents;
import net.exmo.exmodifier.core.component.UnlockedSlots;
import net.exmo.exmodifier.core.data.ModifierEntryDefinition;
import net.exmo.exmodifier.core.data.QualityDefinition;
import net.exmo.exmodifier.core.data.RefineMaterialDefinition;
import net.exmo.exmodifier.core.data.RefineRecordDefinition;
import net.exmo.exmodifier.core.data.SlotDefinition;
import net.exmo.exmodifier.core.data.WashingMaterialDefinition;
import net.exmo.exmodifier.core.registry.ExModifierCatalog;
import net.exmo.exmodifier.events.ExCanRefineEvent;
import net.exmo.exmodifier.events.ExOnTableRefreshEntriesEvent;
import net.exmo.exmodifier.events.ExRefineMaxCountEvent;
import net.exmo.exmodifier.events.ExRefreshEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ModifierService {
    private ModifierService() {}

    public static boolean addEntry(ItemStack stack, ResourceLocation entryId, int level, Optional<ResourceLocation> slot) {
        Optional<ModifierEntryDefinition> definition = ExModifierCatalog.current().entry(entryId);
        if (definition.isEmpty() || stack.isEmpty()) return false;
        if (!matchesEntryType(stack, definition.get())) return false;
        int clamped = Math.min(Math.max(1, level), definition.get().maxLevel());
        if (slot.isPresent()) {
            if (ExModifierCatalog.current().slot(slot.get()).isEmpty()) return false;
            if (!ExModifierApi.unlockedSlots(stack).contains(slot.get())) return false;
            if (!definition.get().slots().contains(slot.get())) return false;
            boolean taken = ExModifierApi.appliedModifiers(stack).values().stream()
                    .anyMatch(modifier -> modifier.slotId().filter(slot.get()::equals).isPresent());
            if (taken) return false;
        }
        AppliedModifiers current = ExModifierApi.appliedModifiers(stack);
        Optional<AppliedModifier> existing = current.find(entryId);
        if (existing.isPresent()) {
            int next = Math.min(definition.get().maxLevel(), existing.get().level() + clamped);
            ExModifierApi.setModifiers(stack, current.with(existing.get().withLevel(next)));
            return true;
        }
        ExModifierApi.setModifiers(stack, current.with(new AppliedModifier(entryId, clamped, slot, false)));
        return true;
    }

    public static boolean setEntry(ItemStack stack, ResourceLocation entryId, int level) {
        Optional<ModifierEntryDefinition> definition = ExModifierCatalog.current().entry(entryId);
        if (definition.isEmpty() || stack.isEmpty()) return false;
        if (!matchesEntryType(stack, definition.get())) return false;
        int clamped = Math.min(Math.max(1, level), definition.get().maxLevel());
        AppliedModifiers current = ExModifierApi.appliedModifiers(stack);
        AppliedModifier previous = current.find(entryId).orElse(new AppliedModifier(entryId, clamped, Optional.empty(), false));
        ExModifierApi.setModifiers(stack, current.with(previous.withLevel(clamped)));
        return true;
    }

    public static boolean matchesEntryType(ItemStack stack, ModifierEntryDefinition definition) {
        if (stack == null || stack.isEmpty() || definition == null) return false;
        return definition.itemTypes().isEmpty() || ItemTypeMatcher.matchesAnyType(stack, definition.itemTypes());
    }

    public static boolean removeEntry(ItemStack stack, ResourceLocation entryId) {
        if (stack.isEmpty()) return false;
        AppliedModifiers current = ExModifierApi.appliedModifiers(stack);
        if (current.find(entryId).isEmpty()) return false;
        ExModifierApi.setModifiers(stack, current.without(entryId));
        return true;
    }

    public static boolean addSlot(ItemStack stack, ResourceLocation slotId) {
        if (stack.isEmpty()) return false;
        if (ExModifierCatalog.current().slot(slotId).isEmpty()) return false;
        ExModifierApi.setSlots(stack, ExModifierApi.unlockedSlots(stack).with(slotId));
        return true;
    }

    public static Optional<ItemStack> previewWash(Player player, ItemStack gear, ItemStack material, RandomSource random) {
        return wash(player, gear, material, random, false);
    }

    public static Optional<ItemStack> consumeWash(Player player, ItemStack gear, ItemStack material, RandomSource random) {
        return wash(player, gear, material, random, true);
    }

    private static Optional<ItemStack> wash(Player player, ItemStack gear, ItemStack material, RandomSource random, boolean consume) {
        if (player == null || gear.isEmpty() || material.isEmpty()) return Optional.empty();
        ResourceLocation materialId = BuiltInRegistries.ITEM.getKey(material.getItem());
        Optional<WashingMaterialDefinition> washing = ExModifierCatalog.current().washing(materialId);
        if (washing.isEmpty()) return Optional.empty();
        WashingMaterialDefinition spec = washing.get();
        if (material.getCount() < spec.consumeCount()) return Optional.empty();
        if (player.experienceLevel < spec.xpCost() && !player.getAbilities().instabuild) return Optional.empty();
        ItemStack result = gear.copy();
        AppliedModifiers current = ExModifierApi.appliedModifiers(result);
        List<AppliedModifier> kept = new ArrayList<>();
        List<AppliedModifier> unlocked = new ArrayList<>();
        for (AppliedModifier modifier : current.values()) {
            if (modifier.locked()) kept.add(modifier);
            else unlocked.add(modifier);
        }
        if (Config.refreshReplaceOld) {
            unlocked.clear();
        } else {
            int keep = Math.min(spec.keepEntries(), unlocked.size());
            while (unlocked.size() > keep) unlocked.remove(random.nextInt(unlocked.size()));
        }
        kept.addAll(unlocked);
        List<ResourceLocation> pool = weightedPool(spec.minWeight(), result);
        NeoForge.EVENT_BUS.post(new ExOnTableRefreshEntriesEvent(result, pool));
        int add = Math.max(0, spec.addEntries());
        for (int i = 0; i < add && !pool.isEmpty(); i++) {
            ResourceLocation picked = pool.get(random.nextInt(pool.size()));
            Optional<ModifierEntryDefinition> definition = ExModifierCatalog.current().entry(picked);
            if (definition.isEmpty()) continue;
            int level = 1 + random.nextInt(definition.get().maxLevel());
            kept.removeIf(existing -> existing.entryId().equals(picked));
            kept.add(new AppliedModifier(picked, level, Optional.empty(), false));
        }
        ExModifierApi.setModifiers(result, new AppliedModifiers(kept));
        NeoForge.EVENT_BUS.post(new ExRefreshEvent(player, gear, material, spec.addEntries(), 0));
        if (consume && !player.getAbilities().instabuild) {
            player.giveExperienceLevels(-spec.xpCost());
            material.shrink(spec.consumeCount());
        }
        return Optional.of(result);
    }

    public static int washConsumeCount(ItemStack material) {
        ResourceLocation materialId = BuiltInRegistries.ITEM.getKey(material.getItem());
        return ExModifierCatalog.current().washing(materialId).map(WashingMaterialDefinition::consumeCount).orElse(0);
    }

    public static int washXpCost(ItemStack material) {
        ResourceLocation materialId = BuiltInRegistries.ITEM.getKey(material.getItem());
        return ExModifierCatalog.current().washing(materialId).map(WashingMaterialDefinition::xpCost).orElse(0);
    }

    public static boolean embedBook(ItemStack gear, ItemStack book) {
        Optional<EntryBook> entry = ExModifierApi.entryBook(book);
        if (entry.isEmpty() || gear.isEmpty()) return false;
        Optional<ResourceLocation> slot = firstCompatibleSlot(gear, entry.get().entryId());
        boolean added = addEntry(gear, entry.get().entryId(), entry.get().level(), slot);
        if (added) book.shrink(1);
        return added;
    }

    public static boolean unlockSlot(Player player, ItemStack gear, ItemStack catalyst) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(catalyst.getItem());
        for (var slot : ExModifierCatalog.current().slots().entrySet()) {
            SlotDefinition definition = slot.getValue();
            if (definition.unlockItem().isEmpty()) continue;
            if (!definition.unlockItem().get().equals(itemId)) continue;
            if (catalyst.getCount() < definition.consumeCount()) return false;
            if (player.experienceLevel < definition.xpCost() && !player.getAbilities().instabuild) return false;
            addSlot(gear, slot.getKey());
            if (!player.getAbilities().instabuild) {
                player.giveExperienceLevels(-definition.xpCost());
                catalyst.shrink(definition.consumeCount());
            }
            return true;
        }
        return false;
    }

    private static Optional<ResourceLocation> firstCompatibleSlot(ItemStack gear, ResourceLocation entryId) {
        Optional<ModifierEntryDefinition> definition = ExModifierCatalog.current().entry(entryId);
        if (definition.isEmpty() || definition.get().slots().isEmpty()) return Optional.empty();
        UnlockedSlots unlocked = ExModifierApi.unlockedSlots(gear);
        for (ResourceLocation slot : definition.get().slots()) {
            if (unlocked.contains(slot)) {
                boolean taken = ExModifierApi.appliedModifiers(gear).values().stream()
                        .anyMatch(modifier -> modifier.slotId().filter(slot::equals).isPresent());
                if (!taken) return Optional.of(slot);
            }
        }
        return Optional.empty();
    }

    private static List<ResourceLocation> weightedPool(float minWeight, ItemStack gear) {
        List<ResourceLocation> pool = new ArrayList<>();
        ExModifierCatalog.current().entries().forEach((id, definition) -> {
            if (definition.cantSelect() || !definition.random()) return;
            if (definition.weight() < minWeight) return;
            if (!matchesEntryType(gear, definition)) return;
            int copies = Math.max(1, Math.round(definition.weight()));
            for (int i = 0; i < copies; i++) pool.add(id);
        });
        return pool;
    }

    public static int maxRefine(ItemStack gear) {
        int max = Config.maxRefine;
        Optional<ResourceLocation> quality = ExModifierApi.qualityOn(gear);
        if (quality.isPresent()) {
            max = Math.max(max, ExModifierCatalog.current().quality(quality.get()).map(QualityDefinition::maxRefine).orElse(0));
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(gear.getItem());
        max = Math.max(max, ExModifierCatalog.current().refineRecord(itemId).map(RefineRecordDefinition::maxCount).orElse(0));
        ExRefineMaxCountEvent event = new ExRefineMaxCountEvent(gear, max);
        NeoForge.EVENT_BUS.post(event);
        return event.maxCount();
    }

    public static double refineBonusPerStar(ItemStack gear) {
        double grow = ExModifierApi.qualityOn(gear)
                .flatMap(id -> ExModifierCatalog.current().quality(id))
                .map(QualityDefinition::growValue)
                .orElse(0.0F);
        return grow == 0.0D ? Config.refineEffect : grow;
    }

    public static boolean canRefine(ItemStack gear, ItemStack material) {
        if (!Config.refineSystem || gear.isEmpty() || material.isEmpty()) return false;
        ResourceLocation materialId = BuiltInRegistries.ITEM.getKey(material.getItem());
        boolean allowed = ExModifierCatalog.current().refineMaterial(materialId).isPresent();
        if (!allowed && Config.canUseSameItemRefine && gear.getItem() == material.getItem()) allowed = true;
        QualityDefinition quality = ExModifierApi.qualityOn(gear).flatMap(id -> ExModifierCatalog.current().quality(id)).orElse(null);
        if ((quality != null && quality.refineNeedSameStar()) || Config.refineNeedSameStar) {
            if (ExModifierApi.refineState(gear).count() != ExModifierApi.refineState(material).count()) allowed = false;
        }
        ExCanRefineEvent event = new ExCanRefineEvent(gear, material, allowed);
        NeoForge.EVENT_BUS.post(event);
        if (!event.canRefine()) return false;
        return ExModifierApi.refineState(gear).count() < maxRefine(gear);
    }

    public static int refineConsumeCount(ItemStack material) {
        ResourceLocation materialId = BuiltInRegistries.ITEM.getKey(material.getItem());
        return ExModifierCatalog.current().refineMaterial(materialId).map(RefineMaterialDefinition::consumeCount).orElse(1);
    }

    public static boolean refineInto(ItemStack gear, ItemStack material, boolean consume) {
        if (!canRefine(gear, material)) return false;
        int count = refineConsumeCount(material);
        if (material.getCount() < count) return false;
        ResourceLocation materialId = BuiltInRegistries.ITEM.getKey(material.getItem());
        gear.set(ExModifierComponents.REFINE.get(), ExModifierApi.refineState(gear).incremented(materialId));
        if (consume) material.shrink(count);
        return true;
    }

    public static boolean refine(ItemStack gear, ItemStack material) {
        return refineInto(gear, material, true);
    }

    public static ItemStack copyBook(ResourceLocation entryId, int level) {
        ItemStack stack = new ItemStack(net.exmo.exmodifier.content.ExModifierContent.ENTRY_ITEM.get());
        stack.set(ExModifierComponents.ENTRY_BOOK.get(), new EntryBook(entryId, level));
        return stack;
    }
}
