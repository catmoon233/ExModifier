package net.exmo.exmodifier.content.item;

import net.exmo.exmodifier.api.ExModifierApi;
import net.exmo.exmodifier.core.component.EntryBook;
import net.exmo.exmodifier.core.data.AttributeSpec;
import net.exmo.exmodifier.core.data.ModifierEntryDefinition;
import net.exmo.exmodifier.core.registry.ExModifierCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.text.DecimalFormat;
import java.util.List;

/** Entry book: shows the embedded entry, its level/probability, slots/types and Shift detail. */
public final class EntryItem extends Item {
    private static final DecimalFormat PERCENT = new DecimalFormat("#.##");

    public EntryItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return ExModifierApi.entryBook(stack)
                .map(book -> Component.translatable("item.exmodifier.entry_item.named",
                        entryName(book.entryId()), book.level()))
                .orElseGet(() -> Component.translatable(getDescriptionId()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ExModifierApi.entryBook(stack).ifPresent(book -> {
            ModifierEntryDefinition definition = ExModifierCatalog.current().entry(book.entryId()).orElse(null);
            if (definition == null) {
                tooltip.add(Component.translatable("tooltip.exmodifier.entry.unknown").withStyle(ChatFormatting.RED));
                return;
            }
            tooltip.add(entryName(book.entryId()).withStyle(ChatFormatting.GOLD));
            book.probability().ifPresent(probability ->
                    tooltip.add(Component.translatable("tooltip.exmodifier.entry.probability",
                            PERCENT.format(probability * 100.0D)).withStyle(ChatFormatting.GRAY)));
            tooltip.add(Component.translatable("tooltip.exmodifier.entry.level", book.level())
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip.exmodifier.entry.max_level", definition.maxLevel())
                    .withStyle(ChatFormatting.AQUA));
            if (!definition.slots().isEmpty()) {
                MutableComponent slots = Component.translatable("tooltip.exmodifier.entry.slot")
                        .withStyle(ChatFormatting.LIGHT_PURPLE);
                for (int i = 0; i < definition.slots().size(); i++) {
                    if (i > 0) slots.append(Component.literal(" §7¦§r "));
                    slots.append(slotName(definition.slots().get(i)));
                }
                tooltip.add(slots);
            } else {
                tooltip.add(Component.translatable("tooltip.exmodifier.entry.no_slot").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
            if (!definition.itemTypes().isEmpty()) {
                MutableComponent types = Component.translatable("tooltip.exmodifier.entry.types")
                        .withStyle(ChatFormatting.DARK_GREEN);
                for (int i = 0; i < definition.itemTypes().size(); i++) {
                    if (i > 0) types.append(Component.literal(" §7¦§r "));
                    types.append(Component.translatable("tooltip.exmodifier.type." + definition.itemTypes().get(i).getPath()));
                }
                tooltip.add(types);
            }
            if (definition.descriptionKey().isPresent()) {
                tooltip.add(Component.translatable("tooltip.exmodifier.entry.desc").withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable(definition.descriptionKey().get()).withStyle(ChatFormatting.GRAY));
            }
            if (Screen.hasShiftDown()) {
                if (!definition.attributes().isEmpty()) {
                    tooltip.add(Component.translatable("tooltip.exmodifier.entry.attributes").withStyle(ChatFormatting.AQUA));
                    for (AttributeSpec spec : definition.attributes()) {
                        tooltip.add(attributeLine(spec, book.level()));
                    }
                }
                if (!definition.specialEffects().isEmpty()) {
                    tooltip.add(Component.translatable("tooltip.exmodifier.entry.special_effects").withStyle(ChatFormatting.LIGHT_PURPLE));
                    for (ResourceLocation effect : definition.specialEffects()) {
                        tooltip.add(Component.literal(" §7¦§r ").append(Component.translatable(
                                "tooltip.exmodifier.effect." + effect.getPath())));
                    }
                }
            } else {
                tooltip.add(Component.literal(" "));
                tooltip.add(Component.translatable("tooltip.exmodifier.entry.look_more_shift").withStyle(ChatFormatting.DARK_GRAY));
            }
        });
    }

    public static MutableComponent entryName(ResourceLocation entryId) {
        return Component.translatable("tooltip.exmodifier.entry." + entryId.getPath());
    }

    private static Component slotName(ResourceLocation slotId) {
        return Component.translatable("slot.exmodifier." + slotId.getPath());
    }

    private static MutableComponent attributeLine(AttributeSpec spec, int level) {
        double amount = spec.amountAt(level);
        boolean percent = spec.operation() != net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE;
        double shown = percent ? amount * 100.0D : amount;
        String sign = shown >= 0 ? "+" : "";
        String value = shown == Math.rint(shown) ? String.valueOf((long) shown) : String.valueOf(shown);
        if (percent) value += "%";
        return Component.literal(sign + value + " ")
                .append(Component.translatable(attributeKey(spec.attribute())))
                .withStyle(ChatFormatting.BLUE);
    }

    private static String attributeKey(ResourceLocation id) {
        if (id == null) return "attribute.name.unknown";
        if ("minecraft".equals(id.getNamespace())) return "attribute.name." + id.getPath();
        return "attribute." + id.getNamespace() + "." + id.getPath();
    }

    public static ItemStack create(EntryBook book) {
        ItemStack stack = new ItemStack(net.exmo.exmodifier.content.ExModifierContent.ENTRY_ITEM.get());
        stack.set(net.exmo.exmodifier.core.component.ExModifierComponents.ENTRY_BOOK.get(), book);
        return stack;
    }
}
