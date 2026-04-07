package net.exmo.exmodifier.content.helper;

import net.exmo.exmodifier.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;

import java.util.List;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.CommonEvent.generateEntryTooltip;

public class EntryTooltipHelper {
    public static Pair<List<Component>, Integer> appendEntryInfo(ItemStack stack, List<Component> tooltip, Player player) {
        if (stack.getTag() != null) {
            ModifierEntryHelper modifierEntryHelper = ModifierEntryHelper.of(stack);
            if (stack.getTag().getBoolean("UNKNOWN")) {
                tooltip.add(Component.translatable("null"));
                tooltip.add(Component.translatable("modifier.entry.UNKNOWN"));
            } else {
                if (modifierEntryHelper.getModifierEntriesSize() > 0) {
                    for (ModifierInstant modifierEntry : new ItemInfo(stack).getModifierEntryHelper().getModifierEntries()) {
                        if (modifierEntry.getSlot().isPresent()) continue;
                        if (!Config.compact_tooltip) tooltip.add(Component.translatable("null"));
                        tooltip.addAll(generateEntryTooltip(modifierEntry, player, stack, false));
                    }
                }
                if (stack.getTag().getBoolean("can_add_max"))
                    tooltip.add(Component.translatable("modifier.entry.can_add_max"));

            }
        }

        return new Pair<>(tooltip, tooltip.size());
    }
}
