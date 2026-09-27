package net.exmo.exmodifier.client;

import net.exmo.exmodifier.Config;
import net.exmo.exmodifier.api.AppliedModifierView;
import net.exmo.exmodifier.api.ExModifierApi;
import net.exmo.exmodifier.content.ExModifierContent;
import net.exmo.exmodifier.core.component.ItemLevelState;
import net.exmo.exmodifier.core.component.RefineState;
import net.exmo.exmodifier.core.data.ItemLevelDefinition;
import net.exmo.exmodifier.core.data.QualityDefinition;
import net.exmo.exmodifier.core.registry.ExModifierCatalog;
import net.exmo.exmodifier.core.runtime.ModifierService;
import net.exmo.exmodifier.events.ExEntryTooltipEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

public final class ExModifierClient {
    private static volatile boolean vanillaItemTooltipDump = true;

    private ExModifierClient() {}

    public static void register(IEventBus bus) {
        bus.addListener(ExModifierClient::screens);
        bus.addListener(ExModifierClient::renderers);
        bus.addListener(ExModifierClient::decorators);
        bus.addListener(ExModifierClient::guiLayers);
    }

    private static void renderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ExModifierContent.REFRESH_TABLE_ENTITY.get(), RefreshTableEntityRenderer::new);
        event.registerBlockEntityRenderer(ExModifierContent.EMBEDDED_TABLE_ENTITY.get(), EmbeddedTableEntityRenderer::new);
    }

    private static void decorators(net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent event) {
        event.register(ExModifierContent.ENTRY_ITEM.get(), new EntryItemDecorator());
    }

    private static void guiLayers(net.neoforged.neoforge.client.event.RegisterGuiLayersEvent event) {
        CustomEffectsGui.register(event);
    }

    /** When false, skip the default id/level dump so a themed tooltip can present the same data. */
    public static void setVanillaItemTooltipDump(boolean enabled) {
        vanillaItemTooltipDump = enabled;
    }

    public static boolean vanillaItemTooltipDump() {
        return vanillaItemTooltipDump;
    }

    private static void screens(RegisterMenuScreensEvent event) {
        event.register(ExModifierContent.REFRESH_MENU.get(), RefreshScreen::new);
        event.register(ExModifierContent.EMBEDDED_MENU.get(), EmbeddedScreen::new);
    }

    @EventBusSubscriber(value = Dist.CLIENT)
    public static final class Tooltips {
        @SubscribeEvent
        public static void tooltip(ItemTooltipEvent event) {
            if (!vanillaItemTooltipDump()) return;
            ItemStack stack = event.getItemStack();
            List<Component> lines = event.getToolTip();
            ExModifierApi.qualityOn(stack).ifPresent(quality -> {
                QualityDefinition definition = ExModifierCatalog.current().quality(quality).orElse(null);
                Component name = definition != null && definition.tooltipKey().isPresent()
                        ? Component.translatable(definition.tooltipKey().get())
                        : Component.literal(quality.toString());
                lines.add(Component.translatable("tooltip.exmodifier.quality", name).withStyle(ChatFormatting.GOLD));
            });
            RefineState refine = ExModifierApi.refineState(stack);
            int max = ModifierService.maxRefine(stack);
            if (refine.count() > 0 || Config.refineAlwaysTooltip) {
                lines.add(stars(refine.count(), Math.max(refine.count(), max)));
            }
            ExModifierApi.itemLevel(stack).ifPresent(state -> {
                ItemLevelDefinition definition = ExModifierCatalog.current().itemLevel(state.tableId()).orElse(null);
                int need = definition == null ? 50 : definition.xpNeeded(state.level());
                lines.add(Component.translatable("tooltip.exmodifier.level", state.level(), definition == null ? "?" : definition.maxLevel(), state.xp(), need)
                        .withStyle(ChatFormatting.AQUA));
            });
            List<AppliedModifierView> views = ExModifierApi.modifiersOn(stack);
            if (Config.entryFold && !Screen.hasShiftDown() && !views.isEmpty()) {
                lines.add(Component.translatable("tooltip.exmodifier.entries_folded", views.size()).withStyle(ChatFormatting.DARK_GRAY));
            } else {
                for (AppliedModifierView view : views) {
                    MutableComponent line = Component.translatable("tooltip.exmodifier.applied_entry",
                            Component.translatable("tooltip.exmodifier.entry." + view.entryId().getPath()), view.level());
                    if (Config.entryColor) line.withStyle(ChatFormatting.BLUE);
                    if (Config.entryUnderLine) line.withStyle(ChatFormatting.UNDERLINE);
                    lines.add(line);
                }
            }
            ExModifierApi.elementsOn(stack).forEach((id, amount) ->
                    lines.add(Component.translatable("tooltip.exmodifier.element", id.getPath(), amount).withStyle(ChatFormatting.RED)));
            List<ResourceLocation> unlocked = ExModifierApi.unlockedSlots(stack).slots();
            if (!unlocked.isEmpty()) {
                MutableComponent slots = Component.translatable("tooltip.exmodifier.entry.slot")
                        .withStyle(ChatFormatting.LIGHT_PURPLE);
                for (int i = 0; i < unlocked.size(); i++) {
                    if (i > 0) slots.append(Component.literal(" §7¦§r "));
                    slots.append(Component.translatable("slot.exmodifier." + unlocked.get(i).getPath()));
                }
                lines.add(slots);
            }
            NeoForge.EVENT_BUS.post(new ExEntryTooltipEvent(stack, lines));
        }

        private static Component stars(int current, int max) {
            if (max > 10) {
                return Component.translatable("tooltip.exmodifier.refine.compact", current, max).withStyle(ChatFormatting.GOLD);
            }
            MutableComponent line = Component.empty();
            for (int i = 0; i < max; i++) {
                line.append(Component.literal(i < current ? "★" : "☆").withStyle(i < current ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY));
            }
            return Component.translatable("tooltip.exmodifier.refine", line);
        }
    }
}
