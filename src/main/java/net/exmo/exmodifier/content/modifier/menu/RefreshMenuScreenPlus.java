package net.exmo.exmodifier.content.modifier.menu;

import net.exmo.exmodifier.Config;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.helper.ModifierEntryHelper;
import net.exmo.exmodifier.content.modifier.EntryItem;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.content.modifier.WashingMaterials;
import net.exmo.exmodifier.content.refine.RefineHelper;
import net.exmo.exmodifier.content.suit.ExSuit;
import net.exmo.exmodifier.content.suit.ExSuitHandle;
import net.exmo.exmodifier.content.type.ExType;
import net.exmo.exmodifier.network.RefineItemMessage;
import net.exmo.exmodifier.network.RefreshItemMessage;
import net.exmo.exmodifier.util.ExUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Responsive grayscale reforge/refine UI.
 * Layout:
 * - top left: slot panel + material summary
 * - top right: full-height collapsible entry tag list
 * - bottom: full Minecraft inventory (armor on sides, main grid, hotbar, off-hand)
 */
@OnlyIn(Dist.CLIENT)
public class RefreshMenuScreenPlus extends AbstractContainerScreen<RefreshMenuPlus> implements ContainerListener {

    // ======================== Theme (black/white/gray) ========================
    private static final int COL_BG            = 0xEE161616;
    private static final int COL_PANEL         = 0xEE252525;
    private static final int COL_CARD          = 0xEE333333;
    private static final int COL_BORDER        = 0x60FFFFFF;
    private static final int COL_ACCENT        = 0xFFF2F2F2;
    private static final int COL_ACCENT_DIM    = 0x28FFFFFF;
    private static final int COL_TEXT          = 0xFFF0F0F0;
    private static final int COL_TEXT_SEC      = 0xFFB4B4B4;
    private static final int COL_HOVER         = 0x20FFFFFF;
    private static final int COL_SUCCESS       = 0xFF8BC34A;
    private static final int COL_DANGER        = 0xFFE57373;
    private static final int COL_TAB_ACTIVE    = 0xFFFFFFFF;
    private static final int COL_TAB_INACTIVE  = 0xFF8F8F8F;
    private static final int COL_SCROLLBAR     = 0x40000000;
    private static final int COL_SCROLLTHUMB   = 0x90D7D7D7;

    // ======================== Layout constants ========================
    private static final int MARGIN = 10;
    private static final int GAP = 8;
    private static final int TAB_HEIGHT = 24;
    private static final int TAB_WIDTH = 92;
    private static final int ITEM_SLOT_SZ = 22;
    private static final int SCROLLBAR_W = 5;

    private static final int MIN_UI_W = 380;
    private static final int MAX_UI_W = 560;
    private static final int MIN_UI_H = 260;
    private static final int MAX_UI_H = 430;

    // ======================== Core state ========================
    private final Player player;
    private int currentPage = 0;
    public ItemStack selectedItemStack = ItemStack.EMPTY;
    public ItemStack selectedRefreshItem = ItemStack.EMPTY;
    public int manageSlot = 0; // 0 = target, 1 = material

    // ======================== Layout state ========================
    private int contentX;
    private int contentY;
    private int contentW;
    private int contentH;

    private int leftColW;
    private int rightColW;

    private int topRegionH;
    private int inventoryY;
    private int inventoryH;

    private int slotPanelH;
    private int leftEntryY;
    private int leftEntryH;

    private int materialPanelH;
    private int tagPanelY;
    private int tagPanelH;

    // ======================== Animation state ========================
    private float tabIndicatorX;
    private float tabIndicatorTargetX;
    private float openAnim = 0f;
    private float pageSlideAnim = 1f;
    private int slideDir = 1;
    private long lastTick = System.currentTimeMillis();

    // ======================== Widget refs ========================
    private SlotPanel slotPanel;
    private MaterialInfoPanel materialInfoPanel;
    private EntryTagPanel entryTagPanel;
    private FullInventoryPanel fullInventoryPanel;

    // ======================== Tabs ========================
    private record TabDef(String langKey) {}
    private final List<TabDef> tabs = new ArrayList<>();

    // ======================== Overlay and deferred tooltips ========================
    private static final Map<Long, Map.Entry<Component, ItemStack>> overlayMap = new HashMap<>();
    public final List<Runnable> deferredTooltips = new ArrayList<>();

    // ======================== Particles ========================
    private static class Particle {
        float x;
        float y;
        float vx;
        float vy;
        float life;
        float maxLife;
        float size;
        int color;
    }
    private final List<Particle> particles = new ArrayList<>();
    private final Random rng = new Random();

    // ======================== Entry model ========================
    public static class EntryData {
        final Component title;
        final Component tagTitle;
        final List<Component> details;
        final boolean suitFoldEntry;

        public EntryData(Component title, List<Component> details) {
            this(title, title, details, false);
        }

        public EntryData(Component title, Component tagTitle, List<Component> details, boolean suitFoldEntry) {
            this.title = title;
            this.tagTitle = tagTitle == null ? title : tagTitle;
            this.details = details == null ? List.of() : details;
            this.suitFoldEntry = suitFoldEntry;
        }
    }

    public RefreshMenuScreenPlus(RefreshMenuPlus menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.player = inv.player;
        this.imageWidth = 440;
        this.imageHeight = 300;
        overlayMap.clear();
    }

    private void buildTabs() {
        tabs.clear();
        tabs.add(new TabDef("gui.exmodifier.refreshplus.mode.reforge"));
        if (Config.refine_system) {
            tabs.add(new TabDef("gui.exmodifier.refreshplus.mode.refine"));
        }
        if (tabs.isEmpty()) {
            tabs.add(new TabDef("gui.exmodifier.refreshplus.mode.reforge"));
        }
        currentPage = Mth.clamp(currentPage, 0, tabs.size() - 1);
    }

    @Override
    protected void init() {
        super.init();
        clearWidgets();
        buildTabs();

        this.imageWidth = Mth.clamp(this.width - 40, MIN_UI_W, MAX_UI_W);
        this.imageHeight = Mth.clamp(this.height - 40, MIN_UI_H, MAX_UI_H);
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        computeLayout();
        buildPanels();

        tabIndicatorTargetX = getTabX(currentPage);
        if (tabIndicatorX == 0f) {
            tabIndicatorX = tabIndicatorTargetX;
        }
        if (openAnim < 0.01f) {
            openAnim = 0f;
        }
    }

    private void computeLayout() {
        contentX = leftPos + MARGIN;
        contentY = topPos + MARGIN + TAB_HEIGHT + 6;
        contentW = imageWidth - MARGIN * 2;
        contentH = imageHeight - MARGIN * 2 - TAB_HEIGHT - 6;

        leftColW = Mth.clamp((int) (contentW * 0.34f), 140, 220);
        rightColW = contentW - leftColW - GAP;

        // Full inventory panel: header(20) + 3 main rows + gap(4) + hotbar row + bottom padding(4)
        inventoryH = 20 + 3 * ITEM_SLOT_SZ + 4 + ITEM_SLOT_SZ + 4;  // = 116 px
        topRegionH = contentH - inventoryH - GAP;

        int minTopRegion = 140;
        if (topRegionH < minTopRegion) {
            // grow the window height by shrinking nothing – the UI is already at max; just clamp.
            topRegionH = minTopRegion;
        }

        inventoryY = contentY + topRegionH + GAP;

        slotPanelH = Mth.clamp((int) (topRegionH * 0.48f), 94, 136);
        leftEntryY = contentY + slotPanelH + GAP;
        leftEntryH = Math.max(48, topRegionH - slotPanelH - GAP);

        materialPanelH = leftEntryH;
        tagPanelY = contentY;
        tagPanelH = topRegionH;
    }

    private void buildPanels() {
        int rightX = contentX + leftColW + GAP;

        slotPanel = new SlotPanel(contentX, contentY, leftColW, slotPanelH);
        materialInfoPanel = new MaterialInfoPanel(contentX, leftEntryY, leftColW, materialPanelH);
        entryTagPanel = new EntryTagPanel(rightX, tagPanelY, rightColW, tagPanelH);
        fullInventoryPanel = new FullInventoryPanel(contentX, inventoryY, contentW, inventoryH);

        addRenderableWidget(slotPanel);
        addRenderableWidget(materialInfoPanel);
        addRenderableWidget(entryTagPanel);
        addRenderableWidget(fullInventoryPanel);

        syncEntryPanels();
    }

    private void rebuildUi() {
        if (minecraft != null) {
            init(minecraft, width, height);
        }
    }

    private int getTabX(int index) {
        int gap = 6;
        int totalW = tabs.size() * TAB_WIDTH + Math.max(0, tabs.size() - 1) * gap;
        int startX = leftPos + (imageWidth - totalW) / 2;
        return startX + index * (TAB_WIDTH + gap);
    }

    private List<ItemStack> getActiveItemSource() {
        List<ItemStack> filtered;
        if (manageSlot == 0) {
            filtered = filterEquipItems();
        } else {
            filtered = currentPage == 0 ? filterMaterialItems() : filterRefineItems();
        }
        if (!filtered.isEmpty()) {
            return filtered;
        }
        // 避免筛选后列表为空导致“背包消失”的观感，回退到基础非空背包列表。
        return player.getInventory().items.stream().filter(it -> !it.isEmpty() && it != selectedItemStack).toList();
    }

    private void syncEntryPanels() {
        List<EntryData> normalEntries = buildModifierEntryList();
        if (entryTagPanel != null) {
            List<EntryData> tagEntries = new ArrayList<>(normalEntries);
            EntryData suitEntry = buildSuitFoldEntry();
            if (suitEntry != null) {
                tagEntries.add(suitEntry);
            }
            entryTagPanel.refreshEntries(tagEntries);
        }
    }

    public @NotNull List<EntryData> buildModifierEntryList() {
        if (selectedItemStack.isEmpty()) {
            return List.of();
        }
        return ModifierEntryHelper.of(selectedItemStack).getModifierEntries().stream()
                .map(mi -> {
                    MutableComponent txt = Component.translatable(mi.getModifierEntry().getDescriptionId());
                    if (mi.getLevel() > 1) {
                        txt.append(CommonComponents.SPACE)
                                .append(Component.translatable("enchantment.level." + mi.getLevel()))
                                .withStyle(ChatFormatting.GOLD);
                    }
                    List<Component> tips = ModifierHandle.CommonEvent.generateEntryTooltip(mi, player, selectedItemStack, true,true);
                    Component relatedTag = txt;
                    if (!mi.getModifierEntry().localDescription.isEmpty()) {
                        relatedTag = Component.translatable(mi.getModifierEntry().localDescription);
                    }
                    return new EntryData(txt, relatedTag, tips, false);
                })
                .collect(Collectors.toList());
    }

    private EntryData buildSuitFoldEntry() {
        if (selectedItemStack.isEmpty()) {
            return null;
        }

        Map<String, ExSuit> suitMap = new LinkedHashMap<>();
        ModifierEntryHelper.of(selectedItemStack).getModifierEntries().forEach(mi -> {
            for (String suitId : mi.getModifierEntry().exsuits) {
                for (ExSuit suit : ExSuitHandle.FindExSuit(suitId)) {
                    if (suit != null && suit.visible) {
                        suitMap.putIfAbsent(suit.id, suit);
                    }
                }
            }
        });

        if (suitMap.isEmpty()) {
            return null;
        }

        List<Component> suitDetails = new ArrayList<>();
        int suitIndex = 0;
        for (ExSuit suit : suitMap.values()) {
            int equipped = ExSuitHandle.getPlayerLevelFromExSuitId(player, suit.id);
            int maxLevel = Math.max(suit.getMaxLevel(), 1);

            MutableComponent suitName = Component.translatable("modifier.entry.suit." + suit.id)
                    .append(CommonComponents.SPACE)
                    .append(Component.literal("(" + equipped + "/" + maxLevel + ")").withStyle(ChatFormatting.GOLD));
            suitDetails.add(suitName);

            if (!suit.LocalDescription.isEmpty()) {
                suitDetails.add(Component.translatable(suit.LocalDescription));
            }

            Set<Integer> levelSet = new TreeSet<>();
            levelSet.addAll(suit.getAttriGether().keySet());
            levelSet.addAll(suit.getEffect().keySet());
            levelSet.addAll(suit.getCommands().keySet());
            levelSet.addAll(suit.getTriggers().keySet());
            levelSet.addAll(suit.getLevelDescription().keySet());
            levelSet.addAll(suit.getEffectLocalDescription().keySet());
            if (levelSet.isEmpty()) {
                for (int i = 1; i <= maxLevel; i++) {
                    levelSet.add(i);
                }
            }

            for (int level : levelSet) {
                boolean activated = equipped >= level;
                Component levelTitle = Component.translatable("gui.exmodifier.refreshplus.suit.level", String.valueOf(level))
                        .copy()
                        .withStyle(activated ? ChatFormatting.GREEN : ChatFormatting.GRAY);
                suitDetails.add(levelTitle);

                String descKey = suit.getLevelDescription().get(level);
                if ((descKey == null || descKey.isEmpty()) && suit.getEffectLocalDescription().containsKey(level)) {
                    descKey = suit.getEffectLocalDescription().get(level);
                }
                if (descKey != null && !descKey.isEmpty()) {
                    suitDetails.add(Component.translatable(descKey));
                }
            }

            if (suitIndex < suitMap.size() - 1) {
                suitDetails.add(Component.empty());
            }
            suitIndex++;
        }

        Component suitEntryTitle = Component.translatable("gui.exmodifier.refreshplus.suit.entry");
        return new EntryData(suitEntryTitle, suitEntryTitle, suitDetails, true);
    }

    public static List<ItemStack> filterEquipItems() {
        Player p = Minecraft.getInstance().player;
        if (p == null) {
            return List.of();
        }
        return java.util.stream.Stream.of(
                        p.getInventory().items.stream(),
                        p.getInventory().armor.stream(),
                        p.getInventory().offhand.stream())
                .flatMap(s -> s)
                .filter(it -> !it.isEmpty() && !ModifierEntry.getType(it).stream()
                        .filter(e -> e != ExType.ALL.get()).toList().isEmpty())
                .toList();
    }

    public List<ItemStack> filterMaterialItems() {
        return java.util.stream.Stream.concat(
                        player.getInventory().items.stream(),
                        player.getInventory().offhand.stream())
                .filter(it -> it != selectedItemStack && !it.isEmpty()
                        && (ModifierHandle.materialsList.stream()
                        .anyMatch(m -> m.ItemId.equals(ExUtil.getItemID(it)))
                        || (it.getItem() instanceof EntryItem
                        && ModifierHandle.modifierEntryMap.containsKey(EntryItem.getModifierID(it)))))
                .toList();
    }

    public List<ItemStack> filterRefineItems() {
        return java.util.stream.Stream.concat(
                        player.getInventory().items.stream(),
                        player.getInventory().offhand.stream())
                .filter(it -> it != selectedItemStack && !it.isEmpty()
                        && RefineHelper.of(selectedItemStack).canRefine(it))
                .toList();
    }

    private int findSlot(ItemStack target, ItemStack exclude) {
        Inventory inv = player.getInventory();

        // First pass: prefer exact stack object identity, so duplicate items do not pick a wrong slot.
        for (int i = 0; i < inv.items.size(); i++) {
            ItemStack s = inv.items.get(i);
            if (s == exclude) {
                continue;
            }
            if (s == target) {
                return i;
            }
        }
        for (int i = 0; i < inv.armor.size(); i++) {
            ItemStack s = inv.armor.get(i);
            if (s == exclude) {
                continue;
            }
            if (s == target) {
                return inv.items.size() + i;
            }
        }
        for (int i = 0; i < inv.offhand.size(); i++) {
            ItemStack s = inv.offhand.get(i);
            if (s == exclude) {
                continue;
            }
            if (s == target) {
                return inv.items.size() + inv.armor.size() + i;
            }
        }

        // Fallback: match by item+tag and count.
        for (int i = 0; i < inv.items.size(); i++) {
            ItemStack s = inv.items.get(i);
            if (s == exclude) {
                continue;
            }
            if (!s.isEmpty() && s.getCount() == target.getCount() && ItemStack.isSameItemSameTags(target, s)) {
                return i;
            }
        }
        for (int i = 0; i < inv.armor.size(); i++) {
            ItemStack s = inv.armor.get(i);
            if (s == exclude) {
                continue;
            }
            if (!s.isEmpty() && s.getCount() == target.getCount() && ItemStack.isSameItemSameTags(target, s)) {
                return inv.items.size() + i;
            }
        }
        for (int i = 0; i < inv.offhand.size(); i++) {
            ItemStack s = inv.offhand.get(i);
            if (s == exclude) {
                continue;
            }
            if (!s.isEmpty() && s.getCount() == target.getCount() && ItemStack.isSameItemSameTags(target, s)) {
                return inv.items.size() + inv.armor.size() + i;
            }
        }
        return -1;
    }

    private void switchPage(int newPage) {
        if (newPage == currentPage || newPage < 0 || newPage >= tabs.size()) {
            return;
        }
        slideDir = newPage > currentPage ? 1 : -1;
        pageSlideAnim = 0f;
        currentPage = newPage;
        manageSlot = 0;
        tabIndicatorTargetX = getTabX(currentPage);
        rebuildUi();
    }

    public static void addOverlay(long time, Component content, ItemStack item) {
        overlayMap.put(time, Map.entry(content, item));
    }

    private static float easeOutCubic(float t) {
        return 1f - (float) Math.pow(1f - t, 3);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private float deltaTime() {
        long now = System.currentTimeMillis();
        float dt = (now - lastTick) / 1000f;
        lastTick = now;
        return Math.min(dt, 0.1f);
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float pt) {
        renderBackground(gg);
        float dt = deltaTime();

        openAnim = Math.min(openAnim + dt * 3.4f, 1f);
        pageSlideAnim = Math.min(pageSlideAnim + dt * 4f, 1f);
        tabIndicatorX = lerp(tabIndicatorX, tabIndicatorTargetX, dt * 12f);
        updateParticles(dt);

        float easedOpen = easeOutCubic(openAnim);

        gg.pose().pushPose();
        float scale = 0.86f + 0.14f * easedOpen;
        float cx = width / 2f;
        float cy = height / 2f;
        gg.pose().translate(cx, cy, 0);
        gg.pose().scale(scale, scale, 1f);
        gg.pose().translate(-cx, -cy, 0);

        int alpha = (int) (easedOpen * 240);
        if (alpha > 5) {
            renderMainPanel(gg, mouseX, mouseY, pt, alpha);
        }
        gg.pose().popPose();

        renderOverlayNotifications(gg);
        renderParticles(gg);

        gg.pose().pushPose();
        gg.pose().translate(0, 0, 3000);
        deferredTooltips.forEach(Runnable::run);
        deferredTooltips.clear();
        gg.pose().popPose();
    }

    private void renderMainPanel(GuiGraphics gg, int mx, int my, float pt, int alpha) {
        int px = leftPos;
        int py = topPos;

        drawSoftShadow(gg, px - 4, py - 4, imageWidth + 8, imageHeight + 8, 8);

        int bgA = Math.min(alpha, 240);
        gg.fill(px, py, px + imageWidth, py + imageHeight, (bgA << 24) | (COL_BG & 0x00FFFFFF));
        drawBorder(gg, px, py, imageWidth, imageHeight, COL_BORDER);

        gg.drawString(font, Component.translatable("gui.exmodifier.refreshplus.title"),
                px + MARGIN + 2, py + 2, COL_TEXT, false);

        renderTabs(gg, mx, my);

        float slideE = easeOutCubic(pageSlideAnim);
        int slideOff = (int) ((1f - slideE) * 28 * slideDir);

        gg.enableScissor(contentX, contentY, contentX + contentW, contentY + contentH);
        gg.pose().pushPose();
        gg.pose().translate(slideOff, 0, 0);
        for (Renderable r : this.renderables) {
            if (r != null) {
                r.render(gg, mx, my, pt);
            }
        }
        gg.pose().popPose();
        gg.disableScissor();

        this.hoveredSlot = null;
    }

    private void renderTabs(GuiGraphics gg, int mx, int my) {
        int gap = 6;
        int totalW = tabs.size() * TAB_WIDTH + Math.max(0, tabs.size() - 1) * gap;
        int startX = leftPos + (imageWidth - totalW) / 2;
        int y = topPos + MARGIN;

        gg.fill(startX - 6, y - 2, startX + totalW + 6, y + TAB_HEIGHT + 2, COL_PANEL);
        drawBorder(gg, startX - 6, y - 2, totalW + 12, TAB_HEIGHT + 4, COL_BORDER);

        int indicatorW = TAB_WIDTH - 14;
        int ix = (int) tabIndicatorX + 7;
        gg.fill(ix, y + TAB_HEIGHT - 3, ix + indicatorW, y + TAB_HEIGHT - 1, COL_ACCENT);

        for (int i = 0; i < tabs.size(); i++) {
            int tx = startX + i * (TAB_WIDTH + gap);
            boolean hovered = mx >= tx && mx < tx + TAB_WIDTH && my >= y && my < y + TAB_HEIGHT;
            boolean active = i == currentPage;

            gg.fill(tx, y, tx + TAB_WIDTH, y + TAB_HEIGHT, active ? COL_ACCENT_DIM : COL_PANEL);
            drawBorder(gg, tx, y, TAB_WIDTH, TAB_HEIGHT, active ? COL_ACCENT_DIM : COL_BORDER);

            int col = active ? COL_TAB_ACTIVE : (hovered ? COL_TEXT : COL_TAB_INACTIVE);
            Component label = Component.translatable(tabs.get(i).langKey);
            gg.drawString(font, label, tx + (TAB_WIDTH - font.width(label)) / 2,
                    y + (TAB_HEIGHT - font.lineHeight) / 2, col, false);

            if (hovered && !active) {
                gg.fill(tx, y, tx + TAB_WIDTH, y + TAB_HEIGHT, COL_HOVER);
            }
        }
    }

    private void renderOverlayNotifications(GuiGraphics gg) {
        long now = System.currentTimeMillis();
        overlayMap.entrySet().removeIf(e -> now - e.getKey() > 2800);

        int line = 0;
        gg.pose().pushPose();
        gg.pose().translate(0, 0, 2500);

        for (Map.Entry<Long, Map.Entry<Component, ItemStack>> entry : overlayMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()).toList()) {
            float life = 1f - (now - entry.getKey()) / 2800f;
            float fade = Math.min(life * 4f, 1f) * Math.min((1f - life) * 4f + 0.45f, 1f);
            int a = (int) (fade * 255);
            if (a <= 0) {
                continue;
            }

            Component content = entry.getValue().getKey();
            ItemStack item = entry.getValue().getValue();
            int y = (int) (height * 0.18f + line * (font.lineHeight + 11));
            int textW = font.width(content);
            int boxW = textW + (item.isEmpty() ? 8 : 26);
            int x = (width - boxW) / 2;

            gg.fill(x - 8, y - 4, x + boxW  , y + font.lineHeight + 5, (a << 24) | 0x00323232);
            drawBorder(gg, x - 8, y - 4, boxW +8, font.lineHeight + 9, (a / 3 << 24) | 0x00FFFFFF);

            if (!item.isEmpty()) {
                gg.pose().pushPose();
                gg.pose().scale(0.75f, 0.75f, 1f);
                gg.renderItem(item, (int) (x / 0.75f), (int) ((y - 2) / 0.75f));
                gg.pose().popPose();
            }
            gg.drawString(font, content, x + (item.isEmpty() ? 0 : 18), y, (a << 24) | (COL_TEXT & 0x00FFFFFF), false);
            line++;
        }
        gg.pose().popPose();
    }

    private void spawnParticles(float cx, float cy, int count, int color) {
        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            p.x = cx;
            p.y = cy;
            p.vx = (rng.nextFloat() - 0.5f) * 80f;
            p.vy = (rng.nextFloat() - 0.5f) * 80f - 25f;
            p.maxLife = 0.45f + rng.nextFloat() * 0.5f;
            p.life = p.maxLife;
            p.size = 1.3f + rng.nextFloat() * 1.8f;
            p.color = color;
            particles.add(p);
        }
    }

    private void updateParticles(float dt) {
        particles.removeIf(p -> p.life <= 0f);
        for (Particle p : particles) {
            p.life -= dt;
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            p.vy += 75f * dt;
        }
    }

    private void renderParticles(GuiGraphics gg) {
        for (Particle p : particles) {
            float alpha = Math.max(p.life / p.maxLife, 0f);
            int a = (int) (easeOutCubic(alpha) * 190);
            if (a <= 0) {
                continue;
            }
            int size = (int) (p.size * (0.5f + alpha * 0.6f));
            gg.fill((int) p.x, (int) p.y, (int) p.x + size, (int) p.y + size,
                    (a << 24) | (p.color & 0x00FFFFFF));
        }
    }

    private static void drawBorder(GuiGraphics gg, int x, int y, int w, int h, int color) {
        gg.fill(x, y, x + w, y + 1, color);
        gg.fill(x, y + h - 1, x + w, y + h, color);
        gg.fill(x, y, x + 1, y + h, color);
        gg.fill(x + w - 1, y, x + w, y + h, color);
    }

    private static void drawRoundedRect(GuiGraphics gg, int x, int y, int w, int h, int color) {
        gg.fill(x + 1, y, x + w - 1, y + h, color);
        gg.fill(x, y + 1, x + 1, y + h - 1, color);
        gg.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    private void drawSoftShadow(GuiGraphics gg, int x, int y, int w, int h, int blur) {
        for (int i = blur; i >= 1; i--) {
            int a = (int) (11f * (1f - (float) i / blur));
            gg.fill(x - i, y - i, x + w + i, y + h + i, (a << 24));
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int tabY = topPos + MARGIN;
        for (int i = 0; i < tabs.size(); i++) {
            int tx = getTabX(i);
            if (mx >= tx && mx < tx + TAB_WIDTH && my >= tabY && my < tabY + TAB_HEIGHT) {
                switchPage(i);
                spawnParticles((float) (tx + TAB_WIDTH / 2), tabY + TAB_HEIGHT - 2, 10, 0xFFFFFF);
                return true;
            }
        }

        for (GuiEventListener child : this.children()) {
            if (child.mouseClicked(mx, my, button)) {
                this.setFocused(child);
                if (button == 0) {
                    this.setDragging(true);
                }
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        for (GuiEventListener child : this.children()) {
            if (child instanceof AbstractContainerEventHandler ace && ace.mouseScrolled(mx, my, delta)) {
                return true;
            }
        }
        return super.mouseScrolled(mx, my, delta);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        for (GuiEventListener child : this.children()) {
            if (child instanceof AbstractContainerEventHandler ace) {
                if (ace.mouseDragged(mx, my, btn, dx, dy)) {
                    return true;
                }
            }
        }
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        for (GuiEventListener child : this.children()) {
            if (child instanceof AbstractContainerEventHandler ace) {
                ace.mouseReleased(mx, my, btn);
            }
        }
        return super.mouseReleased(mx, my, btn);
    }

    @Override
    protected void renderBg(GuiGraphics gg, float pt, int mx, int my) {}

    @Override
    protected void renderLabels(GuiGraphics gg, int mx, int my) {}

    @Override
    protected void renderTooltip(GuiGraphics gg, int mx, int my) {}

    public Player getPlayer() {
        return player;
    }

    @Override
    public void slotChanged(AbstractContainerMenu m, int slot, ItemStack stack) {}

    @Override
    public void dataChanged(AbstractContainerMenu m, int id, int val) {}

    // ====================================================================
    // Slot panel (left-top)
    // ====================================================================
    public class SlotPanel extends AbstractContainerEventHandler implements Renderable, NarratableEntry {
        private final int x;
        private final int y;
        private final int w;
        private final int h;

        private final int tgtX;
        private final int tgtY;
        private final int tgtW;
        private final int tgtH;

        private final int matX;
        private final int matY;
        private final int matW;
        private final int matH;

        private final int exeX;
        private final int exeY;
        private final int exeW;
        private final int exeH;

        private final int infX;
        private final int infY;
        private final int infW;
        private final int infH;

        private float targetHover = 0f;
        private float materialHover = 0f;
        private float execHover = 0f;
        private float infoHover = 0f;
        private long lastRender = System.currentTimeMillis();

        public SlotPanel(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;

            int slotSize = Math.min((w - 18) / 2, 48);
            int slotY = y + 18;

            tgtX = x + 6;
            tgtY = slotY;
            tgtW = slotSize;
            tgtH = slotSize;

            matX = tgtX + slotSize + 6;
            matY = slotY;
            matW = slotSize;
            matH = slotSize;

            int btnY = slotY + slotSize + 8;
            int btnW = (w - 16) / 2;
            exeX = x + 6;
            exeY = btnY;
            exeW = btnW;
            exeH = 18;

            infX = exeX + btnW + 4;
            infY = btnY;
            infW = btnW;
            infH = 18;
        }

        @Override
        public void render(GuiGraphics gg, int mx, int my, float pt) {
            long now = System.currentTimeMillis();
            float dt = Math.min((now - lastRender) / 1000f, 0.1f);
            lastRender = now;

            drawRoundedRect(gg, x, y, w, h, COL_CARD);
            drawBorder(gg, x, y, w, h, COL_BORDER);
            gg.drawString(font, Component.translatable("gui.exmodifier.refreshplus.summary.mode")
                            .append(CommonComponents.SPACE)
                            .append(Component.translatable(tabs.get(currentPage).langKey)),
                    x + 6, y + 4, COL_TEXT_SEC, false);

            boolean tgtHov = mx >= tgtX && mx < tgtX + tgtW && my >= tgtY && my < tgtY + tgtH;
            boolean matHov = mx >= matX && mx < matX + matW && my >= matY && my < matY + matH;
            boolean exeHov = mx >= exeX && mx < exeX + exeW && my >= exeY && my < exeY + exeH;
            boolean infHov = mx >= infX && mx < infX + infW && my >= infY && my < infY + infH;

            targetHover = lerp(targetHover, tgtHov ? 1f : 0f, dt * 10f);
            materialHover = lerp(materialHover, matHov ? 1f : 0f, dt * 10f);
            execHover = lerp(execHover, exeHov ? 1f : 0f, dt * 10f);
            infoHover = lerp(infoHover, infHov ? 1f : 0f, dt * 10f);

            renderSlotArea(gg, tgtX, tgtY, tgtW, tgtH, selectedItemStack,
                    manageSlot == 0, targetHover,
                    Component.translatable("gui.exmodifier.refreshplus.slot.target"), mx, my);
            renderSlotArea(gg, matX, matY, matW, matH, selectedRefreshItem,
                    manageSlot == 1, materialHover,
                    Component.translatable("gui.exmodifier.refreshplus.slot.material"), mx, my);

            renderButton(gg, exeX, exeY, exeW, exeH,
                    Component.translatable("gui.exmodifier.refreshplus.action.execute"),
                    COL_ACCENT, execHover, COL_BG);
            renderButton(gg, infX, infY, infW, infH,
                    Component.translatable("gui.exmodifier.refreshplus.action.info"),
                    COL_TEXT_SEC, infoHover, COL_TEXT);

            Component hint = manageSlot == 0
                    ? Component.translatable("gui.exmodifier.refreshplus.hint.target")
                    : Component.translatable("gui.exmodifier.refreshplus.hint.material");
            List<FormattedCharSequence> lines = font.split(hint, w - 12);
            int textY = exeY + exeH + 4;
            for (int i = 0; i < lines.size() && textY + font.lineHeight < y + h - 2; i++) {
                gg.drawString(font, lines.get(i), x + 6, textY, COL_TEXT_SEC, false);
                textY += font.lineHeight;
            }
        }

        private void renderSlotArea(GuiGraphics gg, int sx, int sy, int sw, int sh,
                                    ItemStack stack, boolean active, float hover,
                                    Component label, int mx, int my) {
            gg.fill(sx, sy, sx + sw, sy + sh, COL_PANEL);
            if (active) {
                gg.fill(sx, sy, sx + sw, sy + sh, COL_ACCENT_DIM);
            }
            if (hover > 0.01f) {
                gg.fill(sx, sy, sx + sw, sy + sh, ((int) (hover * 32) << 24) | 0x00FFFFFF);
            }
            drawBorder(gg, sx, sy, sw, sh, active ? COL_ACCENT : COL_BORDER);

            if (!stack.isEmpty()) {
                int ox = sx + (sw - 16) / 2;
                int oy = sy + (sh - 16) / 2;
                gg.renderItem(stack, ox, oy);
                gg.renderItemDecorations(font, stack, ox, oy);

                if (mx >= sx && mx < sx + sw && my >= sy && my < sy + sh) {
                    final int fmx = mx;
                    final int fmy = my;
                    deferredTooltips.add(() -> gg.renderTooltip(font, stack, fmx, fmy));
                }
            } else {
                String text = font.plainSubstrByWidth(label.getString(), sw - 6);
                gg.drawString(font, text, sx + (sw - font.width(text)) / 2,
                        sy + (sh - font.lineHeight) / 2, COL_TEXT_SEC, false);
            }
        }

        private void renderButton(GuiGraphics gg, int bx, int by, int bw, int bh,
                                  Component text, int baseColor, float hover, int textColor) {
            int h = (int) (hover * 26);
            int r = Math.min(((baseColor >> 16) & 0xFF) + h, 255);
            int g = Math.min(((baseColor >> 8) & 0xFF) + h, 255);
            int b = Math.min((baseColor & 0xFF) + h, 255);
            int fill = 0xFF000000 | (r << 16) | (g << 8) | b;

            drawRoundedRect(gg, bx, by, bw, bh, fill);
            String title = text.getString();
            if (font.width(title) > bw - 8) {
                title = font.plainSubstrByWidth(title, bw - 12) + "..";
            }
            gg.drawString(font, title, bx + (bw - font.width(title)) / 2,
                    by + (bh - font.lineHeight) / 2, textColor, false);
        }

        private void showSummaryOverlay() {
            long now = System.currentTimeMillis();
            if (selectedItemStack.isEmpty() || selectedRefreshItem.isEmpty()) {
                addOverlay(now,
                        Component.translatable("gui.exmodifier.refreshplus.toast.need_select"),
                        ItemStack.EMPTY);
                return;
            }
            if (currentPage == 0) {
                Optional<WashingMaterials> mat = ModifierHandle.materialsList.stream()
                        .filter(m -> m.ItemId.equals(ExUtil.getItemID(selectedRefreshItem))).findFirst();
                if (mat.isPresent()) {
                    WashingMaterials wm = mat.get();
                    addOverlay(now,
                            Component.translatable("gui.exmodifier.refreshplus.summary.needcount",
                                    String.valueOf(selectedRefreshItem.getCount()),
                                    String.valueOf(wm.NeedCount)),
                            selectedRefreshItem);
                    return;
                }
            }
            if (currentPage == 1) {
                int chance = RefineHelper.of(selectedItemStack).getRefineSuccessChance(selectedRefreshItem);
                addOverlay(now,
                        Component.translatable("exmodifier.refine.description.success_chance", String.valueOf(chance)),
                        selectedRefreshItem);
                return;
            }
            addOverlay(now, Component.translatable("gui.exmodifier.refreshplus.summary.none"), ItemStack.EMPTY);
        }

        private void executeAction(float fx, float fy) {
            if (selectedItemStack.isEmpty() || selectedRefreshItem.isEmpty()) {
                addOverlay(System.currentTimeMillis(),
                        Component.translatable("gui.exmodifier.refreshplus.toast.need_select"),
                        ItemStack.EMPTY);
                spawnParticles(fx, fy, 8, 0xFFFFFF);
                return;
            }
            int matSlot = findSlot(selectedRefreshItem, selectedItemStack);
            int tgtSlot = findSlot(selectedItemStack, selectedRefreshItem);
            if (currentPage == 0) {
                Exmodifier.PACKET_HANDLER.sendToServer(new RefreshItemMessage(matSlot, tgtSlot));
            } else {
                Exmodifier.PACKET_HANDLER.sendToServer(new RefineItemMessage(matSlot, tgtSlot));
            }
//            addOverlay(System.currentTimeMillis(),
//                    Component.translatable("gui.exmodifier.refreshplus.toast.sent"),
//                    selectedRefreshItem);
            spawnParticles(fx, fy, 14, 0xFFFFFF);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn) {
            if (mx >= tgtX && mx < tgtX + tgtW && my >= tgtY && my < tgtY + tgtH) {
                manageSlot = 0;
                spawnParticles((float) (tgtX + tgtW / 2), (float) (tgtY + tgtH / 2), 7, 0xFFFFFF);
                rebuildUi();
                return true;
            }
            if (mx >= matX && mx < matX + matW && my >= matY && my < matY + matH) {
                manageSlot = 1;
                spawnParticles((float) (matX + matW / 2), (float) (matY + matH / 2), 7, 0xFFFFFF);
                rebuildUi();
                return true;
            }
            if (mx >= exeX && mx < exeX + exeW && my >= exeY && my < exeY + exeH) {
                executeAction((float) (exeX + exeW / 2), (float) (exeY + exeH / 2));
                return true;
            }
            if (mx >= infX && mx < infX + infW && my >= infY && my < infY + infH) {
                showSummaryOverlay();
                spawnParticles((float) (infX + infW / 2), (float) (infY + infH / 2), 6, 0xFFFFFF);
                return true;
            }
            return false;
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return List.of();
        }

        @Override
        public NarrationPriority narrationPriority() {
            return NarrationPriority.HOVERED;
        }

        @Override
        public void updateNarration(NarrationElementOutput out) {
            out.add(NarratedElementType.TITLE, Component.translatable("gui.exmodifier.refreshplus.title"));
        }
    }

    // ====================================================================
    // Material panel (right-top)
    // ====================================================================
    public class MaterialInfoPanel extends AbstractContainerEventHandler implements Renderable, NarratableEntry {
        private final int x;
        private final int y;
        private final int w;
        private final int h;

        public MaterialInfoPanel(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        @Override
        public void render(GuiGraphics gg, int mx, int my, float pt) {
            drawRoundedRect(gg, x, y, w, h, COL_CARD);
            drawBorder(gg, x, y, w, h, COL_BORDER);

            gg.drawString(font, Component.translatable("gui.exmodifier.refreshplus.panel.material"),
                    x + 6, y + 4, COL_TEXT_SEC, false);

            if (selectedRefreshItem.isEmpty()) {
                Component empty = Component.translatable("gui.exmodifier.refreshplus.summary.none");
                gg.drawString(font, empty, x + 6, y + 4 + font.lineHeight + 2, COL_TEXT_SEC, false);
                return;
            }

            int iconX = x + 6;
            int iconY = y + 4 + font.lineHeight + 2;
            gg.renderItem(selectedRefreshItem, iconX, iconY);
            gg.renderItemDecorations(font, selectedRefreshItem, iconX, iconY);

            String name = font.plainSubstrByWidth(selectedRefreshItem.getHoverName().getString(), w - 32);
            gg.drawString(font, name, iconX + 20, iconY + 2, COL_TEXT, false);

            int lineY = iconY + 20;
            if (currentPage == 0) {
                Optional<WashingMaterials> mat = ModifierHandle.materialsList.stream()
                        .filter(m -> m.ItemId.equals(ExUtil.getItemID(selectedRefreshItem))).findFirst();
                if (mat.isPresent()) {
                    WashingMaterials wm = mat.get();
                    gg.drawString(font,
                            Component.translatable("gui.exmodifier.refreshplus.summary.rarity", String.valueOf(wm.rarity)),
                            x + 6, lineY, COL_TEXT, false);
                    lineY += font.lineHeight;
                    gg.drawString(font,
                            Component.translatable("gui.exmodifier.refreshplus.summary.exp", String.valueOf(wm.CostExp)),
                            x + 6, lineY, COL_TEXT, false);
                    lineY += font.lineHeight;
                    int countCol = selectedRefreshItem.getCount() >= wm.NeedCount ? COL_SUCCESS : COL_DANGER;
                    gg.drawString(font,
                            Component.translatable("gui.exmodifier.refreshplus.summary.needcount",
                                    String.valueOf(selectedRefreshItem.getCount()),
                                    String.valueOf(wm.NeedCount)),
                            x + 6, lineY, countCol, false);
                }
            } else {
                if (!selectedItemStack.isEmpty()) {
                    RefineHelper helper = RefineHelper.of(selectedItemStack);
                    Component stars = helper.getRefineTooltip(true);
                    if (stars != null) {
                        gg.drawString(font, stars, x + 6, lineY, 0xFFFFCC66, false);
                        lineY += font.lineHeight;
                    }
                    int chance = helper.getRefineSuccessChance(selectedRefreshItem);
                    gg.drawString(font,
                            Component.translatable("exmodifier.refine.description.success_chance", String.valueOf(chance)),
                            x + 6, lineY, chance >= 50 ? COL_SUCCESS : COL_DANGER, false);
                }
            }

            if (mx >= iconX && mx < iconX + 16 && my >= iconY && my < iconY + 16) {
                final int fmx = mx;
                final int fmy = my;
                deferredTooltips.add(() -> gg.renderTooltip(font, selectedRefreshItem, fmx, fmy));
            }
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return List.of();
        }

        @Override
        public NarrationPriority narrationPriority() {
            return NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(NarrationElementOutput out) {}
    }

    // ====================================================================
    // Compact entry list (left-bottom of top area)
    // ====================================================================
    public class EntryListPanel extends AbstractContainerEventHandler implements Renderable, NarratableEntry {
        private final int x;
        private final int y;
        private final int w;
        private final int h;

        private List<EntryData> entries = new ArrayList<>();
        private int scrollOff = 0;
        private int totalHeight = 0;
        private int hoverIndex = -1;

        public EntryListPanel(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        public void refreshEntries(List<EntryData> list) {
            entries = new ArrayList<>(list);
            totalHeight = entries.size() * (font.lineHeight + 4);
            scrollOff = Mth.clamp(scrollOff, 0, Math.max(totalHeight - (h - 8), 0));
        }

        @Override
        public void render(GuiGraphics gg, int mx, int my, float pt) {
            drawRoundedRect(gg, x, y, w, h, COL_CARD);
            drawBorder(gg, x, y, w, h, COL_BORDER);
            gg.drawString(font, Component.translatable("gui.exmodifier.refreshplus.panel.details"),
                    x + 6, y + 4, COL_TEXT_SEC, false);

            int bodyY = y + font.lineHeight + 8;
            int bodyH = h - (font.lineHeight + 12);
            if (bodyH <= 0) {
                return;
            }

            if (entries.isEmpty()) {
                Component msg = Component.translatable("gui.exmodifier.refreshplus.empty.entries");
                gg.drawString(font, msg, x + 6, bodyY, COL_TEXT_SEC, false);
                return;
            }

            hoverIndex = -1;
            gg.enableScissor(x + 1, bodyY, x + w - 1, bodyY + bodyH);

            int ry = bodyY - scrollOff;
            int rowH = font.lineHeight + 4;
            for (int i = 0; i < entries.size(); i++) {
                if (ry + rowH < bodyY) {
                    ry += rowH;
                    continue;
                }
                if (ry > bodyY + bodyH) {
                    break;
                }

                boolean hover = mx >= x + 2 && mx < x + w - SCROLLBAR_W - 2 && my >= ry && my < ry + rowH;
                if (hover) {
                    hoverIndex = i;
                    gg.fill(x + 2, ry, x + w - SCROLLBAR_W - 2, ry + rowH, COL_HOVER);
                }

                String t = entries.get(i).title.getString();
                t = font.plainSubstrByWidth(t, w - SCROLLBAR_W - 16);
                gg.drawString(font, (i + 1) + ". " + t, x + 6, ry + 2, COL_TEXT, false);
                ry += rowH;
            }
            gg.disableScissor();

            if (totalHeight > bodyH) {
                int tx = x + w - SCROLLBAR_W;
                gg.fill(tx, bodyY, tx + SCROLLBAR_W, bodyY + bodyH, COL_SCROLLBAR);
                int thumbH = Math.max((int) ((float) bodyH / totalHeight * bodyH), 12);
                int thumbY = bodyY + (int) ((float) scrollOff / (totalHeight - bodyH) * (bodyH - thumbH));
                gg.fill(tx, thumbY, tx + SCROLLBAR_W, thumbY + thumbH, COL_SCROLLTHUMB);
            }

            if (hoverIndex >= 0 && hoverIndex < entries.size()) {
                EntryData e = entries.get(hoverIndex);
                if (!e.details.isEmpty()) {
                    final int fmx = mx;
                    final int fmy = my;
                    deferredTooltips.add(() -> gg.renderTooltip(font, e.details, ItemStack.EMPTY.getTooltipImage(), fmx, fmy));
                }
            }
        }

        @Override
        public boolean mouseScrolled(double mx, double my, double delta) {
            int bodyY = y + font.lineHeight + 8;
            int bodyH = h - (font.lineHeight + 12);
            if (mx >= x && mx < x + w && my >= bodyY && my < bodyY + bodyH && totalHeight > bodyH) {
                scrollOff = (int) Mth.clamp(scrollOff - (delta > 0 ? 14 : -14), 0, Math.max(totalHeight - bodyH, 0));
                return true;
            }
            return false;
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return List.of();
        }

        @Override
        public NarrationPriority narrationPriority() {
            return NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(NarrationElementOutput out) {}
    }

    // ====================================================================
    // Entry tag panel (right, scroll + collapse)
    // ====================================================================
    public class EntryTagPanel extends AbstractContainerEventHandler implements Renderable, NarratableEntry {
        private final int x;
        private final int y;
        private final int w;
        private final int h;

        private List<EntryData> entries = new ArrayList<>();
        private final Set<Integer> expanded = new HashSet<>();
        private final List<Integer> rowHeights = new ArrayList<>();
        private int totalHeight = 0;
        private int scrollOff = 0;
        private boolean draggingScrollbar = false;

        public EntryTagPanel(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        public void refreshEntries(List<EntryData> list) {
            entries = new ArrayList<>(list);
            expanded.removeIf(i -> i < 0 || i >= entries.size());
            recalc();
        }

        private int calcDetailHeight(EntryData entry, int width) {
            int lines = 0;
            for (Component c : entry.details) {
                if (c.getString().isEmpty()) {
                    lines += 1;
                    continue;
                }
                lines += Math.max(1, font.wordWrapHeight(c, width) / font.lineHeight);
            }
            return lines * font.lineHeight + 2;
        }

        private void recalc() {
            rowHeights.clear();
            totalHeight = 0;
            int contentW = w - SCROLLBAR_W - 14;
            for (int i = 0; i < entries.size(); i++) {
                int rh = 22;
                if (expanded.contains(i)) {
                    rh += calcDetailHeight(entries.get(i), contentW);
                }
                rh += 4;
                rowHeights.add(rh);
                totalHeight += rh;
            }
            scrollOff = Mth.clamp(scrollOff, 0, Math.max(totalHeight - (h - (font.lineHeight + 12)), 0));
        }

        @Override
        public void render(GuiGraphics gg, int mx, int my, float pt) {
            drawRoundedRect(gg, x, y, w, h, COL_CARD);
            drawBorder(gg, x, y, w, h, COL_BORDER);
            gg.drawString(font, Component.translatable("gui.exmodifier.refreshplus.panel.tags"),
                    x + 6, y + 4, COL_TEXT_SEC, false);

            int bodyY = y + font.lineHeight + 8;
            int bodyH = h - (font.lineHeight + 12);
            if (bodyH <= 0) {
                return;
            }

            if (entries.isEmpty()) {
                Component msg = Component.translatable("gui.exmodifier.refreshplus.empty.entries");
                gg.drawString(font, msg, x + 6, bodyY, COL_TEXT_SEC, false);
                return;
            }

            gg.enableScissor(x + 1, bodyY, x + w - 1, bodyY + bodyH);

            int ry = bodyY + 2 - scrollOff;
            int cardW = w - SCROLLBAR_W - 10;

            for (int i = 0; i < entries.size(); i++) {
                int rh = rowHeights.get(i);
                if (ry + rh < bodyY) {
                    ry += rh;
                    continue;
                }
                if (ry > bodyY + bodyH) {
                    break;
                }

                EntryData entry = entries.get(i);
                boolean isExpanded = expanded.contains(i);
                int headerH = 20;
                boolean hoverHeader = mx >= x + 3 && mx < x + 3 + cardW && my >= ry && my < ry + headerH;

                drawRoundedRect(gg, x + 3, ry, cardW, rh - 2, COL_PANEL);
                drawBorder(gg, x + 3, ry, cardW, rh - 2, COL_BORDER);
                if (hoverHeader) {
                    gg.fill(x + 4, ry + 1, x + 3 + cardW - 1, ry + headerH, COL_HOVER);
                }

                String marker = isExpanded ? "v" : ">";
                gg.drawString(font, marker, x + 8, ry + 6, COL_TEXT_SEC, false);
                String title = font.plainSubstrByWidth(entry.tagTitle.getString(), cardW - 24);
                gg.drawString(font, title, x + 16, ry + 6, COL_TEXT, false);

                if (isExpanded && !entry.details.isEmpty()) {
                    int lineY = ry + headerH;
                    int maxW = cardW - 8;
                    for (Component c : entry.details) {
                        if (c.getString().isEmpty()) {
                            lineY += font.lineHeight;
                            continue;
                        }
                        List<FormattedCharSequence> wrapped = font.split(c, maxW);
                        for (FormattedCharSequence seq : wrapped) {
                            if (lineY + font.lineHeight > ry + rh - 2) {
                                break;
                            }
                            gg.drawString(font, seq, x + 7, lineY, 0xFFFFFFFF, false);
                            lineY += font.lineHeight;
                        }
                    }
                }

                ry += rh;
            }

            gg.disableScissor();

            if (totalHeight > bodyH) {
                int tx = x + w - SCROLLBAR_W - 2;
                gg.fill(tx, bodyY, tx + SCROLLBAR_W, bodyY + bodyH, COL_SCROLLBAR);
                int thumbH = Math.max((int) ((float) bodyH / totalHeight * bodyH), 12);
                int thumbY = bodyY + (int) ((float) scrollOff / (totalHeight - bodyH) * (bodyH - thumbH));
                gg.fill(tx, thumbY, tx + SCROLLBAR_W, thumbY + thumbH, COL_SCROLLTHUMB);
            }
        }

        @Override
        public boolean mouseScrolled(double mx, double my, double delta) {
            int bodyY = y + font.lineHeight + 8;
            int bodyH = h - (font.lineHeight + 12);
            if (mx >= x && mx < x + w && my >= bodyY && my < bodyY + bodyH && totalHeight > bodyH) {
                scrollOff = (int) Mth.clamp(scrollOff - (delta > 0 ? 16 : -16), 0, Math.max(totalHeight - bodyH, 0));
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseClicked(double mx, double my, int button) {
            int bodyY = y + font.lineHeight + 8;
            int bodyH = h - (font.lineHeight + 12);
            if (mx < x || mx >= x + w || my < bodyY || my >= bodyY + bodyH) {
                return false;
            }

            if (mx >= x + w - SCROLLBAR_W - 2 && mx < x + w - 2) {
                draggingScrollbar = true;
                return true;
            }

            int ry = bodyY + 2 - scrollOff;
            int cardW = w - SCROLLBAR_W - 10;
            for (int i = 0; i < entries.size(); i++) {
                int rh = rowHeights.get(i);
                int headerH = 20;
                if (mx >= x + 3 && mx < x + 3 + cardW && my >= ry && my < ry + headerH) {
                    if (expanded.contains(i)) {
                        expanded.remove(i);
                    } else {
                        expanded.add(i);
                    }
                    recalc();
                    spawnParticles((float) mx, (float) my, 7, 0xFFFFFF);
                    return true;
                }
                ry += rh;
            }
            return false;
        }

        @Override
        public boolean mouseDragged(double mx, double my, int b, double dx, double dy) {
            if (!draggingScrollbar) {
                return false;
            }
            int bodyY = y + font.lineHeight + 8;
            int bodyH = h - (font.lineHeight + 12);
            if (totalHeight <= bodyH) {
                scrollOff = 0;
                return true;
            }
            float t = (float) ((my - bodyY) / (double) bodyH);
            scrollOff = (int) Mth.clamp(t * (totalHeight - bodyH), 0, totalHeight - bodyH);
            return true;
        }

        @Override
        public boolean mouseReleased(double mx, double my, int btn) {
            draggingScrollbar = false;
            return false;
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return List.of();
        }

        @Override
        public NarrationPriority narrationPriority() {
            return NarrationPriority.HOVERED;
        }

        @Override
        public void updateNarration(NarrationElementOutput out) {}
    }

    // ====================================================================
    // Full Inventory Panel (bottom) - full MC inventory layout
    // Left column  : armor[3]=helmet, armor[2]=chestplate, armor[1]=leggings (rows 0-2)
    //                armor[0]=boots (hotbar row)
    // Centre       : inv.items[9..35] in 3 rows + inv.items[0..8] hotbar
    // Right column : inv.offhand[0] (hotbar row)
    // Valid/selectable items  → bright with animated pulsing border
    // Invalid items           → dark dim overlay
    // Currently selected item → glowing animated border
    // ====================================================================
    public class FullInventoryPanel extends AbstractContainerEventHandler implements Renderable, NarratableEntry {

        private static final int SZ           = ITEM_SLOT_SZ; // 22 px per slot
        private static final int ARMOR_GAP    = 4;  // gap between armor col and main grid
        private static final int OFFHAND_GAP  = 4;  // gap between main grid and offhand col
        private static final int HOTBAR_GAP   = 4;  // vertical gap between main rows and hotbar

        private final int x;
        private final int y;
        private final int w;
        private final int h;

        // Grid anchor positions (computed once in constructor)
        private final int armorColX;   // x of the 1-wide armor column
        private final int mainGridX;   // x of the 9-wide main/hotbar grid
        private final int offhandX;    // x of the off-hand slot
        private final int mainGridY;   // y of the first main-inventory row
        private final int hotbarY;     // y of the hotbar / boots / offhand row

        // Per-slot hover animation (slot key 0-40, see getSlotPos())
        private final Map<Integer, Float> slotHoverAnim = new HashMap<>();

        // Global animation counters
        private float validPulse   = 0f;   // drives the pulsing border on valid slots
        private float selGlow      = 0f;   // drives the glow on the selected slot
        private float scanPhase    = 0f;   // slow scanline across valid items

        private int hoverSlot = -1;
        private long lastRender = System.currentTimeMillis();

        public FullInventoryPanel(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;

            // Total grid width = armorCol + gap + 9×SZ + gap + offhandCol = 22+4+198+4+22 = 250
            int gridW      = SZ + ARMOR_GAP + SZ * 9 + OFFHAND_GAP + SZ;
            int offsetX    = Math.max(6, (w - gridW) / 2);
            armorColX  = x + offsetX;
            mainGridX  = armorColX + SZ + ARMOR_GAP;
            offhandX   = mainGridX + SZ * 9 + OFFHAND_GAP;
            mainGridY  = y + 20;           // 20 px header
            hotbarY    = mainGridY + 3 * SZ + HOTBAR_GAP;
        }

        // ---- Slot key mapping (mirrors MC slot indices) -----------------
        // 0-8   : hotbar      (inv.items[0..8])
        // 9-35  : main inv    (inv.items[9..35])
        // 36-39 : armor       (inv.armor[0..3], 36=boots, 39=helmet)
        // 40    : off-hand    (inv.offhand[0])

        private ItemStack getSlotStack(int key) {
            Inventory inv = player.getInventory();
            if (key <= 35)          return inv.items.get(key);
            if (key <= 39)          return inv.armor.get(key - 36);
            if (key == 40)          return inv.offhand.isEmpty() ? ItemStack.EMPTY : inv.offhand.get(0);
            return ItemStack.EMPTY;
        }

        /** Returns {sx, sy} for the top-left pixel of a slot, or null if not shown. */
        private int[] getSlotPos(int key) {
            if (key >= 0 && key <= 8) {
                // Hotbar
                return new int[]{mainGridX + key * SZ, hotbarY};
            }
            if (key >= 9 && key <= 35) {
                int row = (key - 9) / 9;
                int col = (key - 9) % 9;
                return new int[]{mainGridX + col * SZ, mainGridY + row * SZ};
            }
            if (key >= 36 && key <= 39) {
                // 36=boots→hotbar row, 37=legs→row2, 38=chest→row1, 39=helmet→row0
                int armorIdx = key - 36;
                int sy = (armorIdx == 0) ? hotbarY : mainGridY + (3 - armorIdx) * SZ;
                return new int[]{armorColX, sy};
            }
            if (key == 40) {
                return new int[]{offhandX, hotbarY};
            }
            return null;
        }

        /** Build an identity-based set of valid/selectable ItemStacks. */
        private Set<ItemStack> buildSelectableSet() {
            List<ItemStack> list;
            if (manageSlot == 0) {
                list = filterEquipItems();
            } else {
                list = currentPage == 0 ? filterMaterialItems() : filterRefineItems();
            }
            Set<ItemStack> set = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
            set.addAll(list);
            return set;
        }

        @Override
        public void render(GuiGraphics gg, int mx, int my, float pt) {
            long now = System.currentTimeMillis();
            float dt = Math.min((now - lastRender) / 1000f, 0.1f);
            lastRender = now;

            validPulse += dt * 2.8f;
            selGlow    += dt * 5.0f;
            scanPhase  += dt * 0.5f;

            // Panel background
            drawRoundedRect(gg, x, y, w, h, COL_CARD);
            drawBorder(gg, x, y, w, h, COL_BORDER);
            gg.drawString(font, Component.translatable("gui.exmodifier.refreshplus.panel.inventory"),
                    x + 6, y + 5, COL_TEXT_SEC, false);

            // Subtle separator between armor column and main grid
            gg.fill(armorColX + SZ + 1, mainGridY - 1, armorColX + SZ + 2, hotbarY + SZ + 1, COL_BORDER);
            // Separator between main grid and offhand column
            gg.fill(offhandX - 2, mainGridY - 1, offhandX - 1, hotbarY + SZ + 1, COL_BORDER);
            // Horizontal separator between main rows and hotbar
            gg.fill(armorColX, hotbarY - 2, offhandX + SZ, hotbarY - 1, COL_BORDER);

            Set<ItemStack> validSet = buildSelectableSet();
            ItemStack activeSelected = manageSlot == 0 ? selectedItemStack : selectedRefreshItem;

            hoverSlot = -1;
            for (int key = 0; key <= 40; key++) {
                int[] pos = getSlotPos(key);
                if (pos == null) continue;
                int sx = pos[0], sy = pos[1];

                ItemStack stack = getSlotStack(key);
                boolean hov = mx >= sx && mx < sx + SZ && my >= sy && my < sy + SZ;
                float ha = lerp(slotHoverAnim.getOrDefault(key, 0f), hov ? 1f : 0f, dt * 14f);
                slotHoverAnim.put(key, ha);

                if (hov && !stack.isEmpty()) hoverSlot = key;

                boolean valid    = !stack.isEmpty() && validSet.contains(stack);
                boolean selected = !stack.isEmpty() && stack == activeSelected;

                renderInventorySlot(gg, sx, sy, stack, valid, selected, ha);
            }

            // Armor column small labels (top-left corner of each slot)
            renderArmorLabels(gg);

            // Off-hand label
            renderOffhandLabel(gg);

            // Tooltip for hovered slot
            if (hoverSlot >= 0) {
                ItemStack hs = getSlotStack(hoverSlot);
                if (!hs.isEmpty()) {
                    final int fmx = mx, fmy = my;
                    deferredTooltips.add(() -> gg.renderTooltip(font, hs, fmx, fmy));
                }
            }
        }

        private void renderInventorySlot(GuiGraphics gg, int sx, int sy,
                                         ItemStack stack, boolean valid,
                                         boolean selected, float hoverAnim) {
            // --- Slot background ---
            gg.fill(sx, sy, sx + SZ, sy + SZ, COL_PANEL);

            // --- Selected: glowing animated border (drawn before the item) ---
            if (selected) {
                float g = 0.55f + 0.35f * (float) Math.sin(selGlow);
                int ga = (int) (g * 255);
                int gc = (ga << 24) | 0x00FFFFFF;
                // Outer glow ring
                gg.fill(sx - 1, sy - 1, sx + SZ + 1, sy,          gc);
                gg.fill(sx - 1, sy + SZ, sx + SZ + 1, sy + SZ + 1, gc);
                gg.fill(sx - 1, sy,      sx,           sy + SZ,     gc);
                gg.fill(sx + SZ, sy,     sx + SZ + 1,  sy + SZ,     gc);
                // Inner fill tint
                int ig = (int) (g * 0.25f * 255);
                gg.fill(sx, sy, sx + SZ, sy + SZ, (ig << 24) | 0x00FFFFFF);
            }

            // --- Item rendering ---
            if (!stack.isEmpty()) {
                int ox = sx + (SZ - 16) / 2;
                int oy = sy + (SZ - 16) / 2;
                gg.renderItem(stack, ox, oy);
                gg.renderItemDecorations(font, stack, ox, oy);

                if (!valid && !selected) {
                    // Invalid: dark desaturating overlay
                    gg.fill(sx, sy, sx + SZ, sy + SZ, 0xAA000000);
                }
            }

            // --- Valid-item animated pulsing border ---
            if (valid && !selected && !stack.isEmpty()) {
                float pa = 0.18f + 0.12f * (float) Math.sin(validPulse + sx * 0.07f);
                int pv = (int) (pa * 255);
                int pc = (pv << 24) | 0x00FFFFFF;
                gg.fill(sx,          sy,          sx + SZ,     sy + 1,      pc);
                gg.fill(sx,          sy + SZ - 1, sx + SZ,     sy + SZ,     pc);
                gg.fill(sx,          sy,          sx + 1,      sy + SZ,     pc);
                gg.fill(sx + SZ - 1, sy,          sx + SZ,     sy + SZ,     pc);

                // Subtle scanline shimmer across valid items
                // SCAN_WIDTH = 9 main cols + 1 offhand = SZ * 10
                float scanPos = (scanPhase % 1.0f) * (SZ * 10);  // moves across slots
                float distToScan = Math.abs((sx - mainGridX) - scanPos);
                if (distToScan < SZ * 1.5f) {
                    float shimmer = (1f - distToScan / (SZ * 1.5f)) * 0.18f;
                    int sv = (int) (shimmer * 255);
                    gg.fill(sx + 1, sy + 1, sx + SZ - 1, sy + SZ - 1, (sv << 24) | 0x00FFFFFF);
                }
            }

            // --- Hover highlight (drawn on top) ---
            if (hoverAnim > 0.01f) {
                gg.fill(sx, sy, sx + SZ, sy + SZ, ((int) (hoverAnim * 55) << 24) | 0x00FFFFFF);
                int ba = (int) (hoverAnim * 210);
                int bc = (ba << 24) | 0x00FFFFFF;
                gg.fill(sx,          sy,          sx + SZ,     sy + 1,      bc);
                gg.fill(sx,          sy + SZ - 1, sx + SZ,     sy + SZ,     bc);
                gg.fill(sx,          sy,          sx + 1,      sy + SZ,     bc);
                gg.fill(sx + SZ - 1, sy,          sx + SZ,     sy + SZ,     bc);
            }

            // Default border (drawn last so it frames everything cleanly)
            if (!selected) {
                drawBorder(gg, sx, sy, SZ, SZ, COL_BORDER);
            }
        }

        /** Small "A" markers at the corner of armor slots to identify them. */
        private void renderArmorLabels(GuiGraphics gg) {
            // Keys: 39=helmet (row 0), 38=chest (row 1), 37=legs (row 2), 36=boots (hotbar row)
            int[] armorKeys = {39, 38, 37, 36};
            for (int key : armorKeys) {
                int[] pos = getSlotPos(key);
                if (pos == null) continue;
                // 1-pixel mini marker in top-left corner of slot
                gg.fill(pos[0] + 1, pos[1] + 1, pos[0] + 3, pos[1] + 3, 0x55FFFFFF);
            }
        }

        /** Small marker on the off-hand slot. */
        private void renderOffhandLabel(GuiGraphics gg) {
            int[] pos = getSlotPos(40);
            if (pos == null) return;
            gg.fill(pos[0] + 1, pos[1] + 1, pos[0] + 3, pos[1] + 3, 0x5500D0FF);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn) {
            for (int key = 0; key <= 40; key++) {
                int[] pos = getSlotPos(key);
                if (pos == null) continue;
                int sx = pos[0], sy = pos[1];
                if (mx >= sx && mx < sx + SZ && my >= sy && my < sy + SZ) {
                    ItemStack stack = getSlotStack(key);
                    if (!stack.isEmpty()) {
                        selectSlot(stack, sx + SZ / 2f, sy + SZ / 2f);
                        return true;
                    }
                }
            }
            return false;
        }

        private void selectSlot(ItemStack stack, float fx, float fy) {
            if (manageSlot == 0) {
                selectedItemStack = stack;
                syncEntryPanels();
            } else {
                selectedRefreshItem = stack;
            }
            spawnParticles(fx, fy, 9, 0xFFFFFF);
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return List.of();
        }

        @Override
        public NarrationPriority narrationPriority() {
            return NarrationPriority.HOVERED;
        }

        @Override
        public void updateNarration(NarrationElementOutput out) {
            out.add(NarratedElementType.TITLE, Component.translatable("gui.exmodifier.refreshplus.panel.inventory"));
        }
    }
}
