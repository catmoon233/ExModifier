package net.exmo.exmodifier.content.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.exmo.exmodifier.api.ExModifierApi;
import net.exmo.exmodifier.core.attachment.EntityElements;
import net.exmo.exmodifier.core.component.EntryBook;
import net.exmo.exmodifier.core.component.ItemLevelState;
import net.exmo.exmodifier.core.registry.ExModifierCatalog;
import net.exmo.exmodifier.core.runtime.CustomEffectsRuntime;
import net.exmo.exmodifier.core.runtime.ItemLevelRuntime;
import net.exmo.exmodifier.core.runtime.ModifierService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

public final class ExModifierCommands {
    private ExModifierCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal("exmodifier")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("entry")
                        .then(Commands.literal("add")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("entry", ResourceLocationArgument.id())
                                                .suggests(suggestEntries())
                                                .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                                        .executes(ctx -> addEntry(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
                                                                ResourceLocationArgument.getId(ctx, "entry"),
                                                                IntegerArgumentType.getInteger(ctx, "level"), Optional.empty()))
                                                        .then(Commands.argument("slot", ResourceLocationArgument.id())
                                                                .suggests(suggestSlots())
                                                                .executes(ctx -> addEntry(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
                                                                        ResourceLocationArgument.getId(ctx, "entry"),
                                                                        IntegerArgumentType.getInteger(ctx, "level"),
                                                                        Optional.of(ResourceLocationArgument.getId(ctx, "slot")))))))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("entry", ResourceLocationArgument.id())
                                                .suggests(suggestEntries())
                                                .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                                        .executes(ctx -> setEntry(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
                                                                ResourceLocationArgument.getId(ctx, "entry"),
                                                                IntegerArgumentType.getInteger(ctx, "level")))))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("entry", ResourceLocationArgument.id())
                                                .suggests(suggestEntries())
                                                .executes(ctx -> removeEntry(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
                                                        ResourceLocationArgument.getId(ctx, "entry")))))))
                .then(Commands.literal("slot")
                        .then(Commands.literal("add")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("slot", ResourceLocationArgument.id())
                                                .suggests(suggestSlots())
                                                .executes(ctx -> addSlot(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
                                                        ResourceLocationArgument.getId(ctx, "slot")))))))
                .then(Commands.literal("element")
                        .then(Commands.literal("add")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("element", ResourceLocationArgument.id())
                                                .suggests(suggestElements())
                                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                                        .executes(ctx -> addElement(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
                                                                ResourceLocationArgument.getId(ctx, "element"),
                                                                IntegerArgumentType.getInteger(ctx, "amount"))))))))
                .then(Commands.literal("quality")
                        .then(Commands.literal("set")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("quality", ResourceLocationArgument.id())
                                                .suggests(suggestQualities())
                                                .executes(ctx -> setQuality(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
                                                        ResourceLocationArgument.getId(ctx, "quality")))))))
                .then(Commands.literal("level")
                        .then(Commands.literal("set")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                                .executes(ctx -> setLevel(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
                                                        IntegerArgumentType.getInteger(ctx, "level"))))))
                        .then(Commands.literal("xp")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("xp", IntegerArgumentType.integer(1))
                                                .executes(ctx -> addXp(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
                                                        IntegerArgumentType.getInteger(ctx, "xp")))))))
                .then(Commands.literal("refine")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> refine(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("effect")
                        .then(Commands.literal("add")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("effect", ResourceLocationArgument.id())
                                                .suggests(suggestEffects())
                                                .then(Commands.argument("amplifier", IntegerArgumentType.integer(0))
                                                        .then(Commands.argument("duration", IntegerArgumentType.integer(1))
                                                                .executes(ctx -> addEffect(ctx.getSource(),
                                                                        EntityArgument.getPlayer(ctx, "player"),
                                                                        ResourceLocationArgument.getId(ctx, "effect"),
                                                                        IntegerArgumentType.getInteger(ctx, "amplifier"),
                                                                        IntegerArgumentType.getInteger(ctx, "duration")))))))))
                .then(Commands.literal("book")
                        .then(Commands.literal("give")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("entry", ResourceLocationArgument.id())
                                                .suggests(suggestEntries())
                                                .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                                        .executes(ctx -> giveBook(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
                                                                ResourceLocationArgument.getId(ctx, "entry"),
                                                                IntegerArgumentType.getInteger(ctx, "level"))))))))
                .then(Commands.literal("suit")
                        .then(Commands.literal("debug")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> suitDebug(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"))))))
                .then(Commands.literal("reload")
                        .executes(ctx -> {
                            ExModifierCatalog.bind(ctx.getSource().getServer().registryAccess());
                            ctx.getSource().sendSuccess(() -> Component.translatable("command.exmodifier.reloaded"), true);
                            return 1;
                        }));
        dispatcher.register(root);
        registerLegacyAliases(dispatcher);
    }

    private static void registerLegacyAliases(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("addHandItemEntry").requires(s -> s.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("entry", ResourceLocationArgument.id())
                                .suggests(suggestEntries())
                                .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                        .executes(ctx -> addEntry(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
                                                ResourceLocationArgument.getId(ctx, "entry"),
                                                IntegerArgumentType.getInteger(ctx, "level"), Optional.empty()))))));
        dispatcher.register(Commands.literal("addHandItemSlot").requires(s -> s.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("slot", ResourceLocationArgument.id())
                                .suggests(suggestSlots())
                                .executes(ctx -> addSlot(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
                                        ResourceLocationArgument.getId(ctx, "slot"))))));
        dispatcher.register(Commands.literal("addHandElement").requires(s -> s.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("element", ResourceLocationArgument.id())
                                .suggests(suggestElements())
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(ctx -> addElement(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
                                                ResourceLocationArgument.getId(ctx, "element"),
                                                IntegerArgumentType.getInteger(ctx, "amount")))))));
        dispatcher.register(Commands.literal("addHandItemQuality").requires(s -> s.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("quality", ResourceLocationArgument.id())
                                .suggests(suggestQualities())
                                .executes(ctx -> setQuality(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
                                        ResourceLocationArgument.getId(ctx, "quality"))))));
    }

    private static SuggestionProvider<CommandSourceStack> suggestEntries() {
        return suggest(() -> ExModifierCatalog.current().entries().keySet());
    }

    private static SuggestionProvider<CommandSourceStack> suggestSlots() {
        return suggest(() -> ExModifierCatalog.current().slots().keySet());
    }

    private static SuggestionProvider<CommandSourceStack> suggestElements() {
        return suggest(() -> ExModifierCatalog.current().elements().keySet());
    }

    private static SuggestionProvider<CommandSourceStack> suggestQualities() {
        return suggest(() -> ExModifierCatalog.current().qualities().keySet());
    }

    private static SuggestionProvider<CommandSourceStack> suggestEffects() {
        return suggest(() -> CustomEffectsRuntime.definitions().keySet());
    }

    private static SuggestionProvider<CommandSourceStack> suggest(Supplier<Collection<ResourceLocation>> ids) {
        return (ctx, builder) -> SharedSuggestionProvider.suggest(
                ids.get().stream().map(ResourceLocation::toString).toList(), builder);
    }

    private static int addEntry(CommandSourceStack source, ServerPlayer player, ResourceLocation entry, int level, Optional<ResourceLocation> slot) {
        ItemStack stack = player.getMainHandItem();
        if (ModifierService.addEntry(stack, entry, level, slot)) {
            source.sendSuccess(() -> Component.translatable("command.exmodifier.entry_added", entry.toString()), true);
            return 1;
        }
        source.sendFailure(Component.translatable("command.exmodifier.entry_failed"));
        return 0;
    }

    private static int setEntry(CommandSourceStack source, ServerPlayer player, ResourceLocation entry, int level) {
        if (ModifierService.setEntry(player.getMainHandItem(), entry, level)) {
            source.sendSuccess(() -> Component.translatable("command.exmodifier.entry_set", entry.toString(), level), true);
            return 1;
        }
        source.sendFailure(Component.translatable("command.exmodifier.entry_failed"));
        return 0;
    }

    private static int removeEntry(CommandSourceStack source, ServerPlayer player, ResourceLocation entry) {
        if (ModifierService.removeEntry(player.getMainHandItem(), entry)) {
            source.sendSuccess(() -> Component.translatable("command.exmodifier.entry_removed", entry.toString()), true);
            return 1;
        }
        source.sendFailure(Component.translatable("command.exmodifier.entry_failed"));
        return 0;
    }

    private static int addSlot(CommandSourceStack source, ServerPlayer player, ResourceLocation slot) {
        if (ModifierService.addSlot(player.getMainHandItem(), slot)) {
            source.sendSuccess(() -> Component.translatable("command.exmodifier.slot_added", slot.toString()), true);
            return 1;
        }
        source.sendFailure(Component.translatable("command.exmodifier.entry_failed"));
        return 0;
    }

    private static int addElement(CommandSourceStack source, ServerPlayer player, ResourceLocation element, int amount) {
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            EntityElements current = new EntityElements(ExModifierApi.elementsOn(player));
            ExModifierApi.setEntityElements(player, current.with(element, amount));
        } else {
            ExModifierApi.setElements(stack, new net.exmo.exmodifier.core.component.ItemElements(ExModifierApi.elementsOn(stack)).with(element, amount));
        }
        source.sendSuccess(() -> Component.translatable("command.exmodifier.element_added", element.toString(), amount), true);
        return 1;
    }

    private static int refine(CommandSourceStack source, ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        if (ModifierService.refine(stack, off)) {
            source.sendSuccess(() -> Component.translatable("command.exmodifier.refined"), true);
            return 1;
        }
        source.sendFailure(Component.translatable("command.exmodifier.entry_failed"));
        return 0;
    }

    private static int setQuality(CommandSourceStack source, ServerPlayer player, ResourceLocation quality) {
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(Component.translatable("command.exmodifier.entry_failed"));
            return 0;
        }
        ExModifierApi.setQuality(stack, quality);
        source.sendSuccess(() -> Component.translatable("command.exmodifier.quality_set", quality.toString()), true);
        return 1;
    }

    private static int setLevel(CommandSourceStack source, ServerPlayer player, int level) {
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(Component.translatable("command.exmodifier.entry_failed"));
            return 0;
        }
        ItemLevelRuntime.applyTable(stack);
        var state = ExModifierApi.itemLevel(stack).orElse(ItemLevelState.EMPTY);
        ExModifierApi.setItemLevel(stack, state.withXp(state.xp(), level));
        source.sendSuccess(() -> Component.translatable("command.exmodifier.level_set", level), true);
        return 1;
    }

    private static int addXp(CommandSourceStack source, ServerPlayer player, int xp) {
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(Component.translatable("command.exmodifier.entry_failed"));
            return 0;
        }
        ItemLevelRuntime.applyTable(stack);
        var state = ExModifierApi.itemLevel(stack).orElse(null);
        if (state == null) {
            source.sendFailure(Component.translatable("command.exmodifier.entry_failed"));
            return 0;
        }
        String event = ExModifierCatalog.current().itemLevel(state.tableId()).map(def -> def.upEvent()).orElse("KILL");
        ItemLevelRuntime.addXp(player, stack, event, xp);
        source.sendSuccess(() -> Component.translatable("command.exmodifier.xp_added", xp), true);
        return 1;
    }

    private static int addEffect(CommandSourceStack source, ServerPlayer player, ResourceLocation effect, int amplifier, int duration) {
        if (CustomEffectsRuntime.add(player, effect, amplifier, duration, true)) {
            source.sendSuccess(() -> Component.translatable("command.exmodifier.effect_added", effect.toString()), true);
            return 1;
        }
        source.sendFailure(Component.translatable("command.exmodifier.entry_failed"));
        return 0;
    }

    private static int giveBook(CommandSourceStack source, ServerPlayer player, ResourceLocation entry, int level) {
        if (ExModifierCatalog.current().entry(entry).isEmpty()) {
            source.sendFailure(Component.translatable("command.exmodifier.entry_failed"));
            return 0;
        }
        ItemStack book = ModifierService.copyBook(entry, level);
        if (!player.getInventory().add(book)) {
            player.drop(book, false);
        }
        source.sendSuccess(() -> Component.translatable("command.exmodifier.book_given", entry.toString(), level), true);
        return 1;
    }

    private static int suitDebug(CommandSourceStack source, ServerPlayer player) {
        ExModifierApi.countSuitPieces(player).forEach((id, count) ->
                source.sendSuccess(() -> Component.literal(id + " x" + count), false));
        return 1;
    }
}
