package net.exmo.exmodifier.util;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.CommonEvent.selectModifierAttributes;

public class ModifierSelector {


    public record SelectionResult(List<ModifierEntry> entries, List<ModifierAttriGether> attributes,
                                  boolean hasExclusiveEntry) {
    }

    public static SelectionResult selectEntries(WeightedUtil<String> weightedUtil,
                                                int refreshments,
                                                Function<String, ModifierEntry> entryGetter) {
        List<ModifierEntry> selectedEntries = new ArrayList<>();
        List<ModifierAttriGether> attributes = new ArrayList<>();
        boolean hasExclusive = false;

        int remaining = Math.min(refreshments, weightedUtil.weights.size());
        WeightedUtil<String> workingUtil = weightedUtil.copy();

        while (selectedEntries.size() < remaining && !hasExclusive) {
            String selectedKey = workingUtil.selectRandomKeyBasedOnWeights();
            ModifierEntry entry = entryGetter.apply(selectedKey);

            if (entry == null || selectedEntries.contains(entry)) {
                Exmodifier.LOGGER.Logger.debug("Skipping invalid or duplicate entry: {}", selectedKey);
                continue;
            }

            workingUtil.removeKey(selectedKey);
            selectedEntries.add(entry);
            Exmodifier.LOGGER.Logger.debug("Added modifier entry: {}", entry.id);

            if (entry.OnlyHasThisEntry) {
                hasExclusive = true;
                attributes.clear();
                attributes.addAll(processAttributes(entry));
                break;
            }

            attributes.addAll(processAttributes(entry));
        }

        return new SelectionResult(selectedEntries, attributes, hasExclusive);
    }

    // 在ModifierSelector类中添加
    public static class SimpleSelectionResult {
        public final List<ModifierEntry> entries;
        public final boolean hasExclusiveEntry;

        public SimpleSelectionResult(List<ModifierEntry> entries,
                                     boolean hasExclusiveEntry) {
            this.entries = entries;
            this.hasExclusiveEntry = hasExclusiveEntry;
        }
    }

    public static SimpleSelectionResult selectEntriesOnly(
            WeightedUtil<String> weightedUtil,
            int maxEntries,
            Function<String, ModifierEntry> entryGetter) {

        List<ModifierEntry> selectedEntries = new ArrayList<>();
        boolean hasExclusive = false;
        int remaining = Math.min(maxEntries, weightedUtil.weights.size());
        WeightedUtil<String> workingUtil = weightedUtil.copy();

        while (selectedEntries.size() < remaining && !hasExclusive) {
            String selectedKey = workingUtil.selectRandomKeyBasedOnWeights();
            ModifierEntry entry = entryGetter.apply(selectedKey);

            // 跳过无效或重复词条
            if (entry == null || selectedEntries.contains(entry)) {
                Exmodifier.LOGGER.Logger.debug("跳过无效或重复词条: {}", selectedKey);
                continue;
            }

            workingUtil.removeKey(selectedKey);
            selectedEntries.add(entry);
            Exmodifier.LOGGER.Logger.debug("已选择词条: {}", entry.id);

            // 处理独占逻辑
            if (entry.OnlyHasThisEntry) {
                hasExclusive = true;
                // 清除后续可能已选的词条（如果需要严格独占）
                if (selectedEntries.size() > 1) {
                    selectedEntries = List.of(entry); // 保留唯一词条
                }
                break;
            }
        }

        return new SimpleSelectionResult(selectedEntries, hasExclusive);
    }



    private static List<ModifierAttriGether> processAttributes(ModifierEntry entry) {
        // 通用属性处理逻辑
        return selectModifierAttributes(entry);
    }

    // 原始selectModifierAttributes方法可移动到这里

}