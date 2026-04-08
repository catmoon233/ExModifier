package net.exmo.exmodifier.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.exmo.exmodifier.content.helper.ModifierEntryHelper;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.content.suit.ExSuit;
import net.exmo.exmodifier.content.suit.ExSuitHandle;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class SuitDebugCommand {

    private static final SuggestionProvider<CommandSourceStack> SUGGESTION_ENTRIES =
            (ctx, builder) -> SharedSuggestionProvider.suggest(ModifierHandle.modifierEntryMap.keySet(), builder);

    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("exmo-debug-suit")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("hand")
                                .executes(ctx -> debugMainHand(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> debugMainHand(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                        .then(Commands.literal("entry")
                                .then(Commands.argument("entryId", StringArgumentType.word())
                                        .suggests(SUGGESTION_ENTRIES)
                                        .executes(ctx -> debugEntry(ctx.getSource(), StringArgumentType.getString(ctx, "entryId")))))
        );
    }

    private static int debugMainHand(CommandSourceStack source, ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(Component.literal("[exmodifier] target player's main hand is empty"));
            return 0;
        }

        List<ModifierEntry> entries = ModifierEntryHelper.of(stack).getModifierEntriesB();
        if (entries.isEmpty()) {
            source.sendFailure(Component.literal("[exmodifier] no modifier entries found on main hand item"));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("[exmodifier] main hand item: " + stack.getHoverName().getString()), false);
        for (ModifierEntry entry : entries) {
            sendEntrySuitDebug(source, entry, player);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int debugEntry(CommandSourceStack source, String entryIdInput) {
        ModifierEntry entry = resolveEntry(entryIdInput);
        if (entry == null) {
            source.sendFailure(Component.literal("[exmodifier] modifier entry not found: " + entryIdInput));
            return 0;
        }

        sendEntrySuitDebug(source, entry, null);
        return Command.SINGLE_SUCCESS;
    }

    private static ModifierEntry resolveEntry(String entryIdInput) {
        if (entryIdInput == null || entryIdInput.isEmpty()) {
            return null;
        }

        ModifierEntry direct = ModifierHandle.findModifierEntry(entryIdInput);
        if (direct != null) {
            return direct;
        }

        if (entryIdInput.length() > 2) {
            ModifierEntry stripped = ModifierHandle.findModifierEntry(entryIdInput.substring(2));
            if (stripped != null) {
                return stripped;
            }
        }

        for (ModifierEntry candidate : ModifierHandle.modifierEntryMap.values()) {
            if (candidate == null || candidate.id == null || candidate.id.isEmpty()) {
                continue;
            }
            if (candidate.id.endsWith(entryIdInput) || entryIdInput.endsWith(candidate.id)) {
                return candidate;
            }
        }
        return null;
    }

    private static void sendEntrySuitDebug(CommandSourceStack source, ModifierEntry entry, ServerPlayer player) {
        List<String> configuredIds = entry.exsuits == null ? List.of() : entry.exsuits;
        List<ExSuit> matchedSuits = ExSuitHandle.FindExSuitFromEntry(entry.id);

        String configured = configuredIds.isEmpty() ? "none" : String.join(", ", configuredIds);
        String matched = formatMatchedSuitIds(matchedSuits, player);

        source.sendSuccess(() -> Component.literal(
                "[exmodifier] entry=" + entry.id + " configured=[" + configured + "] matched=[" + matched + "]"
        ), false);
    }

    private static String formatMatchedSuitIds(List<ExSuit> suits, ServerPlayer player) {
        if (suits == null || suits.isEmpty()) {
            return "none";
        }

        List<String> values = new ArrayList<>();
        for (ExSuit suit : suits) {
            if (suit == null || suit.id == null || suit.id.isEmpty()) {
                continue;
            }

            if (player != null) {
                int currentLevel = ExSuitHandle.getPlayerLevelFromExSuitId(player, suit.id);
                values.add(suit.id + "(level=" + currentLevel + ")");
            } else {
                values.add(suit.id);
            }
        }

        if (values.isEmpty()) {
            return "none";
        }
        return String.join(", ", values);
    }
}