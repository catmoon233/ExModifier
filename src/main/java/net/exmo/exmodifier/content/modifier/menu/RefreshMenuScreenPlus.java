package net.exmo.exmodifier.content.modifier.menu;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.exmo.exmodifier.Config;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.helper.ModifierEntryHelper;
import net.exmo.exmodifier.content.modifier.EntryItem;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.content.modifier.WashingMaterials;
import net.exmo.exmodifier.content.refine.RefineHelper;
import net.exmo.exmodifier.content.type.ExType;
import net.exmo.exmodifier.network.RefineItemMessage;
import net.exmo.exmodifier.network.RefreshItemMessage;
import net.exmo.exmodifier.util.ExUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
@OnlyIn(value = Dist.CLIENT)

public class RefreshMenuScreenPlus extends AbstractContainerScreen<RefreshMenuPlus> implements ContainerListener {
    private final List<Page> pages = new ArrayList<>();
    private int currentPage = 0;
    private final Player player;
    public ItemStack selectedItemStack = ItemStack.EMPTY;
    public ItemStack selectedRefreshItem = ItemStack.EMPTY;
    public int manageSlot = 0;
    //0->selectedItemStack
    //1->selectedRefreshItem
    // 新增成员变量
    public ItemListViewer itemListViewer;
    public Map<Integer, InfoWidget> infoWeiget;
    private float scrollOffset;
    private boolean isScrolling;
    private static final ResourceLocation SLOT_ICON =
            new ResourceLocation("exmodifier", "textures/gui/slot_icon_0.png");
    private static final ResourceLocation SLOT_ICON_2 =
            new ResourceLocation("exmodifier", "textures/gui/slot_icon_2.png");
    private static final ResourceLocation SLOT_ICON_2_1 =
            new ResourceLocation("exmodifier", "textures/gui/slot_icon_2_1.png");
    private static final ResourceLocation SLOT_ICON_1 =
            new ResourceLocation("exmodifier", "textures/gui/slot_icon_1.png");
    private static final ResourceLocation WIDGETS_TEXTURE =
            new ResourceLocation("exmodifier", "textures/gui/backround.png");
    private static final ResourceLocation WIDGETS_TEXTURE_2 =
            new ResourceLocation("exmodifier", "textures/gui/backround_2.png");
    private static final ResourceLocation WIDGETS_TEXTURE_W =
            new ResourceLocation("exmodifier", "textures/gui/backround_w.png");
    private static final ResourceLocation WIDGETS_TEXTURE_W2 =
            new ResourceLocation("exmodifier", "textures/gui/backround_w2.png");
    private static final ResourceLocation SIMPLE_BUTTON_TEXTURE =
            new ResourceLocation("exmodifier", "textures/gui/simple_button.png");
    private static final ResourceLocation SIMPLE_BUTTON_TEXTURE_OVER =
            new ResourceLocation("exmodifier", "textures/gui/simple_button_over.png");
    private static final int ITEM_SLOT_SIZE = 20;
    private final Map<GuiEventListener, Float> hoverProgress = new HashMap<>();
    private static final ResourceLocation MENU_TEXTURE = new ResourceLocation("exmodifier", "textures/gui/background3.png");

    void initPages() {
        // 添加默认页面（示例）
        addPage("重铸", screen -> {
            screen.addRenderableWidget(new ImageWidget(80, this.topPos - 20, this.width - 160, this.imageHeight, MENU_TEXTURE));
            // screen.addRenderableWidget(new ImageWidget();
            screen.addRenderableWidget(new RefreshWidget(100, this.topPos + 20, this.width / 3, this.imageHeight - 60, MENU_TEXTURE));
            // screen.addRenderableWidget(new Widget(100, this.topPos +20, this.width /3, this.imageHeight-60, MENU_TEXTURE))
            //
//            screen.addRenderableWidget(new Button.Builder(Component.literal("示例按钮"), b -> {})
//                    .pos(100, 50)
//                    .size(100, 20)
//                    .build());
//            screen.addRenderableWidget(new StringWidget(100, 80, 100, 20,
//                    Component.literal("欢迎使用！"), screen.font));
//            infoWeiget = new infoWeiget(100, 80, 100, 20,
//                    Component.literal("欢迎使用！"), screen.font);
            this.itemListViewer = new ItemListViewer(manageSlot == 0 ? filterItems(player.getInventory().items) : filterItems2(player.getInventory().items),
                    120 + this.width / 3, topPos + 20,
                    this.width - 160 - this.width / 3 - 40 - 20, imageHeight - 60);
            // 启用搜索功能
            itemListViewer.setShowSearchBox(false);


            // 自定义搜索框外观
            itemListViewer.setSearchBackground(
                    WIDGETS_TEXTURE_W,
                    0x80000000 // 半透明橙色背景
            );

            textList = new TextListWidget(
                    List.of(),
                    100 + 64 + 10, RefreshMenuScreenPlus.this.topPos + 40,
                    RefreshMenuScreenPlus.this.width / 3 - 82, 45
            );
            if (!selectedItemStack.isEmpty()) {
                textList.entries = getTextEntries();
                textList.calculateLayout();
                ;
            }
            ;
            //  RefreshMenuScreenPlus.this.children().removeIf(widget -> widget instanceof TextListWidget);
            addRenderableWidget(textList);
            addRenderableWidget(itemListViewer);


        });

        if (Config.refine_system) addPage("精炼", screen -> {
            screen.addRenderableWidget(new ImageWidget(80, this.topPos - 20, this.width - 160, this.imageHeight, MENU_TEXTURE));
            screen.addRenderableWidget(new RefreshWidget(100, this.topPos + 20, this.width / 3, this.imageHeight - 60, MENU_TEXTURE));
            ;
            this.itemListViewer = new ItemListViewer(manageSlot == 0 ? filterItems(player.getInventory().items) : filterItems3(player.getInventory().items),
                    120 + this.width / 3, topPos + 20,
                    this.width - 160 - this.width / 3 - 40 - 20, imageHeight - 60);
            // 启用搜索功能
            itemListViewer.setShowSearchBox(false);


            // 自定义搜索框外观
            itemListViewer.setSearchBackground(
                    WIDGETS_TEXTURE_W,
                    0x80000000 // 半透明橙色背景
            );

            textList = new TextListWidget(
                    List.of(),
                    100 + 64 + 10, RefreshMenuScreenPlus.this.topPos + 40,
                    RefreshMenuScreenPlus.this.width / 3 - 82, 45
            );
            if (!selectedItemStack.isEmpty()) {
                textList.entries = getTextEntries();
                textList.calculateLayout();
                ;
            }
            ;
            //  RefreshMenuScreenPlus.this.children().removeIf(widget -> widget instanceof TextListWidget);

            addRenderableWidget(itemListViewer);


        });
    }

    public @NotNull List<TextListWidget.Entry> getTextEntries() {
        List<TextListWidget.Entry> textEntries = ModifierEntryHelper.of(selectedItemStack).getModifierEntries().stream()
                .map(entry -> {
                    MutableComponent translatable = Component.translatable(entry.getModifierEntry().getDescriptionId());
                    if (entry.getLevel() > 1)
                        translatable.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + entry.getLevel())).withStyle(ChatFormatting.GOLD);
                    return new TextListWidget.Entry(translatable, ModifierHandle.CommonEvent.generateEntryTooltip(entry, player, selectedItemStack, true));
                })
                .collect(Collectors.toList());
        return textEntries;
    }

    public RefreshMenuScreenPlus(RefreshMenuPlus p_97874_, Inventory p_97875_, Component p_97876_) {
        super(p_97874_, p_97875_, p_97876_);
        this.player = p_97875_.player;
        this.imageHeight = 250;
        this.imageWidth = 400;
        overlayMap.clear();
    }

    public void addPage(String title, Consumer<RefreshMenuScreenPlus> contentInitializer) {
        pages.add(new Page(Component.translatable(title), contentInitializer));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        float newOffset = (float) (scrollOffset - delta * 0.1);
        scrollOffset = Mth.clamp(newOffset, 0f, 1f);
        textList.mouseScrolled(mouseX, mouseY, delta);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isScrolling) {
            float delta = (float) (dragY / (itemListViewer.visibleRowsY * ITEM_SLOT_SIZE));
            scrollOffset = Mth.clamp(scrollOffset + delta, 0f, 1f);
            textList.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            return true;
        }
        return false;
    }

    public static List<ItemStack> filterItems(List<ItemStack> items) {
        return items.stream()
                .filter(item -> !item.isEmpty() && !ModifierEntry.getType(item).stream().filter(e -> e != ExType.ALL.get()).toList().isEmpty())
                .toList();
    }

    public List<ItemStack> filterItems2(List<ItemStack> items) {
        return items.stream()
                .filter(item -> item != this.selectedItemStack && !item.isEmpty() && ModifierHandle.materialsList.stream().anyMatch(entry -> entry.ItemId.equals(ExUtil.getItemID(item))) || (item.getItem() instanceof EntryItem && ModifierHandle.modifierEntryMap.containsKey(EntryItem.getModifierID(item))))
                .toList();
    }

    public List<ItemStack> filterItems3(List<ItemStack> items) {
        return items.stream()
                .filter(item -> !item.isEmpty() && item != this.selectedItemStack && RefineHelper.of(item).canRefine(this.selectedItemStack))
                .toList();
    }

    @Override
    public void init() {
        super.init();
        // 在init方法中添加：
        //List<ItemStack> filteredItems = filterItems(player.getInventory().items);
        clearWidgets();
        pages.clear();
        initPages();

        int rightMargin = 80;
        //   int yStart = this.height / 2 - pages.size() * 15;
        // 添加标签栏
        for (int i = 0; i < pages.size(); i++) {
            final int pageIndex = i;

            Button tabButton = new TabButton(rightMargin, this.topPos + 2 + i * 30, 20, 20,
                    pages.get(i).title(),
                    b -> switchPage(pageIndex));
            addRenderableWidget(tabButton);
        }

        // 初始化当前页面内容
        if (!pages.isEmpty()) {
            pages.get(currentPage).contentInitializer().accept(this);
        }
    }

    private void switchPage(int newPage) {
        currentPage = newPage;
        init(minecraft, width, height); // 重新初始化界面
    }

    private static final Map<Long, Map.Entry<Component, ItemStack>> overlayMap = new HashMap<>();

    public static void addOverlay(long time, Component content, ItemStack item) {
        overlayMap.put(time, Map.entry(content, item));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);


        int i = this.leftPos;
        int j = this.topPos;
        this.renderBg(guiGraphics, partialTick, mouseX, mouseY);
        RenderSystem.disableDepthTest();
        Map<Long, Map.Entry<Component, ItemStack>> toRemove = new HashMap<>();
        overlayMap.forEach((k, v) -> {
            if (System.currentTimeMillis() - k > 3200) {
                toRemove.put(k, v);
            }
        });
        toRemove.forEach((k, v) -> overlayMap.remove(k));
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 2000.0F);
        AtomicInteger line = new AtomicInteger();

        overlayMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // 按时间戳排序
                .forEach(entry -> {
                    //long k = entry.getKey();
                    ItemStack itemStack = entry.getValue().getValue();
                    Component content = entry.getValue().getKey();
                    float p281752 = 1 - ((System.currentTimeMillis() - entry.getKey()) / 3200f);
                    guiGraphics.setColor(1, 1, 1, p281752);
                    guiGraphics.drawCenteredString(font, content,
                            this.width / 2,
                            (int) (this.height / 5 + (font.lineHeight + 2) * line.get()),
                            0xFFFFFF);
                    if (!itemStack.isEmpty()) {
                        guiGraphics.pose().pushPose();
                        guiGraphics.pose().scale(0.2f, 0.2f, 0.2f);
                        guiGraphics.renderItem(itemStack,
                                (this.width / 2 - 10) * 5,
                                (int) (this.height / 5 + (font.lineHeight + 2) * line.get()) * 5);
                        guiGraphics.pose().popPose();
                    }
                    guiGraphics.setColor(1, 1, 1, 1);
                    line.getAndIncrement();

                });
        guiGraphics.pose().popPose();
        for (Renderable renderable : this.renderables) {
            if (renderable == null) continue;
//            if (renderable instanceof TabButton tabButton){
//                if (pages.get(currentPage).title == tabButton.getMessage())continue;
//            }
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        this.hoveredSlot = null;
        toRenderTooltipList.forEach(toRenderTooltip -> toRenderTooltip.runnable.run());
        toRenderTooltipList.clear();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate((float) i, (float) j, 0.0F);
        this.renderLabels(guiGraphics, mouseX, mouseY);


        // 更新悬停动画
        for (GuiEventListener widget : this.children()) {
            if (widget instanceof TabButton button) {
                float progress = hoverProgress.getOrDefault(button, 0f);
                boolean isHovered = button.isMouseOver(mouseX, mouseY);
                progress = Mth.clamp(progress + (isHovered ? 0.1f : -0.1f), 0f, 1f);
                hoverProgress.put(button, progress);
                button.setExpandProgress(progress);
            }
        }

        guiGraphics.pose().popPose();
        RenderSystem.enableDepthTest();
    }

    public record ToRenderTooltip(Runnable runnable){}
    public List<ToRenderTooltip> toRenderTooltipList= new  ArrayList<>();
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {

    }

    @Override
    protected void renderLabels(GuiGraphics p_281635_, int p_282681_, int p_283686_) {

    }

    @Override
    protected void renderTooltip(GuiGraphics p_283594_, int p_282171_, int p_281909_) {
        super.renderTooltip(p_283594_, p_282171_, p_281909_);
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public void slotChanged(AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack) {

    }

    @Override
    public void dataChanged(AbstractContainerMenu abstractContainerMenu, int i, int i1) {

    }

    @Override
    public boolean isMouseOver(double x, double y) {
        textList.isMouseOver(x, y);
        return super.isMouseOver(x, y);
    }

    @Override
    public boolean mouseReleased(double p_97812_, double p_97813_, int p_97814_) {
        textList.mouseReleased(p_97812_, p_97813_, p_97814_);
        return super.mouseReleased(p_97812_, p_97813_, p_97814_);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int p_97750_) {
        textList.mouseClicked(mouseX, mouseY, p_97750_);
        if (itemListViewer != null && itemListViewer.contextMenu != null) {
            if (itemListViewer.contextMenu.isMouseOver(mouseX, mouseY)) {
                itemListViewer.contextMenu.clickContextMenuButton(mouseX, mouseY);
            }
        }
        // 关闭上下文菜单
        if (itemListViewer != null && itemListViewer.contextMenu != null && !itemListViewer.contextMenu.isMouseOver(mouseX, mouseY)) {
            itemListViewer.contextMenu = null;
        }
        for (GuiEventListener guiEventListener : this.children()) {
            if (guiEventListener instanceof RefreshWidget refreshWidget) {
                refreshWidget.mouseClicked(mouseX, mouseY, p_97750_);
            }
        }
        if (itemListViewer != null) {
            for (GuiEventListener guiEventListener : itemListViewer.children()) {

                if (guiEventListener instanceof ItemListViewer.ActionButton actionButton) {
                    if (guiEventListener.mouseClicked(mouseX, mouseY, p_97750_)) {
                        actionButton.onPress();

                        return true;
                    }
                }
            }
        }
        if (clickActionM(mouseX, mouseY, p_97750_, this.children())) return true;
        return super.mouseClicked(mouseX, mouseY, p_97750_);
    }

    private boolean clickActionM(double mouseX, double mouseY, int p_97750_, List<? extends GuiEventListener> children) {
        for (GuiEventListener guiEventListener : children) {
            if (guiEventListener instanceof AbstractButton) {
                if (guiEventListener.mouseClicked(mouseX, mouseY, p_97750_)) {
                    this.setFocused(guiEventListener);
                    if (p_97750_ == 0) {
                        this.setDragging(true);
                    }

                    return true;
                }
            }
        }
        return false;
    }

    // 自定义标签按钮类
    private class TabButton extends Button {
        private float expandProgress = 0f;
        private final int baseX;

        public TabButton(int rightX, int y, int width, int height, Component message, OnPress onPress) {
            super(rightX - 20, y, 20, height, message, onPress, DEFAULT_NARRATION);
            this.baseX = rightX; // 记录右侧基准位置
        }

        void setExpandProgress(float progress) {
            this.expandProgress = progress;
            // 动态调整位置和宽度
            this.setWidth((int) Mth.lerp(progress, 20, 80));
            this.setX(baseX - this.getWidth());
        }

        public int getColor() {
            if (expandProgress > 0) {
                return Color.lightGray.getRGB();
            }
            if (currentPage == getIndex() + 1) {
                return Color.gray.getRGB();
            } else {
                return Color.DARK_GRAY.getRGB();
            }
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // 动态背景
            guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + height, getColor());
            //0x80000000 | Mth.hsvToRgb(currentPage == getIndex() ? 0.6f : 0.3f, 0.6f, 0.8f));

            // 文字渲染（带展开动画）
            int textWidth = font.width(getMessage());
            float textAlpha = Mth.clamp(expandProgress * 3f, 0f, 1f); // 更快的文字渐现

            int textX = getX() + (getWidth() - textWidth) / 2;
            int textY = getY() + (height - 8) / 2;

            guiGraphics.drawString(font, getMessage(),
                    textX, textY,
                    0xFFFFFF | ((int) (textAlpha * 255) << 24),
                    false);
        }

        private int getIndex() {
            return (getY() - (height / 2 - pages.size() * 15)) / 30;
        }
    }

    // 页面数据结构
    private record Page(Component title, Consumer<RefreshMenuScreenPlus> contentInitializer) {
    }

    public TextListWidget textList = new TextListWidget(List.of(), 0, 0, 0, 0);

    // 自定义按钮类
    private class ExSlotButton extends AbstractButton {
        private final ResourceLocation normalTexture;
        private final ResourceLocation pressTexture;
        private final int textureWidth;
        private final int textureHeight;
        private final int slotIndex;

        public ExSlotButton(int x, int y, int width, int height,
                            ResourceLocation normalTexture, ResourceLocation pressTexture, int textureWidth, int textureHeight,
                            int slotIndex, Runnable onPress) {
            super(x, y, width, height, Component.empty());
            this.normalTexture = normalTexture;
            this.pressTexture = pressTexture;
            this.textureWidth = textureWidth;
            this.textureHeight = textureHeight;
            this.slotIndex = slotIndex;
            this.onPress = onPress;

        }

        private ExSlotButton setComponent(Component component) {
            this.setMessage(component);
            return this;
        }

        private final Runnable onPress;

        @Override
        public void onPress() {
            onPress.run();
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

            // 绘制背景纹理
            guiGraphics.blit(manageSlot == slotIndex ? pressTexture : normalTexture,
                    getX(), getY(),
                    0, 0,
                    width, height,
                    textureWidth, textureHeight);

            // 绘制文本
            int textX = getX() + (width - font.width(getMessage())) / 2;
            int textY = getY() + (height - 8) / 2;
            guiGraphics.drawString(font, getMessage(), textX, textY, 0xFFFFFF);
            // 选中效果（半透明覆盖层）
//            if (manageSlot == slotIndex) {
//                guiGraphics.fill(getX(), getY(),
//                        getX() + width, getY() + height,
//                        0x80808080);
//            }

            // 悬停效果（可选）
            if (isHovered()) {
                guiGraphics.fill(getX(), getY(),
                        getX() + width, getY() + height,
                        0x20FFFFFF);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            output.add(NarratedElementType.TITLE, Component.translatable("narration.exmodifier.button.slot_" + slotIndex));
        }
    }

    //重铸组件
    private class RefreshWidget extends AbstractContainerEventHandler implements Renderable, NarratableEntry {
        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private final ResourceLocation menuTexture;

        private final ExSlotButton itemButton;
        private final ExSlotButton refreshButton;
        private final ExSlotButton infoButton;
        private final ExSlotButton materialButton;

        public int findSlotMatchingItemS(ItemStack stack, Inventory inventory) {
            for (int i = 0; i < inventory.items.size(); ++i) {
                ItemStack itemStack = inventory.items.get(i);
                if (itemStack == RefreshMenuScreenPlus.this.selectedRefreshItem) continue;
                if (!itemStack.isEmpty() && itemStack.getCount() == stack.getCount() && ItemStack.isSameItemSameTags(stack, itemStack)) {
                    return i;
                }
            }

            return -1;
        }

        public int findSlotMatchingItemM(ItemStack stack, Inventory inventory) {
            for (int i = 0; i < inventory.items.size(); ++i) {
                ItemStack itemStack = inventory.items.get(i);
                if (itemStack == RefreshMenuScreenPlus.this.selectedItemStack) continue;
                if (!itemStack.isEmpty() && itemStack.getCount() == stack.getCount() && ItemStack.isSameItemSameTags(stack, itemStack)) {
                    return i;
                }
            }

            return -1;
        }

        public RefreshWidget(int x, int y, int w, int h, ResourceLocation menuTexture) {
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
            this.menuTexture = menuTexture;

            // 初始化按钮（带4x缩放）
            int baseButtonSize = 16;
            int scaledSize = baseButtonSize * 4;
            this.itemButton = new ExSlotButton(
                    x + 2, y + 2,
                    scaledSize, scaledSize,
                    WIDGETS_TEXTURE, WIDGETS_TEXTURE_2, scaledSize, scaledSize,
                    0,
                    () -> {
                        manageSlot = 0;
                        RefreshMenuScreenPlus.this.itemListViewer.items =
                                filterItems(getMinecraft().player.getInventory().items);
                    }
            ) {
                @Override
                public void render(GuiGraphics gg, int p_93658_, int p_93659_, float p_93660_) {
                    if (isHovered) {
                        ItemStack selectedItemStack1 = RefreshMenuScreenPlus.this.selectedItemStack;
                        if (!selectedItemStack1.isEmpty()) {
                            RefreshMenuScreenPlus.this.toRenderTooltipList.add(
                                   new ToRenderTooltip(
                                           ()->{
                                               gg.pose().pushPose();
                                               gg.pose().translate(0, 0, 2100);
                                               gg.renderTooltip(RefreshMenuScreenPlus.this.font, selectedItemStack1, p_93658_, p_93659_);
                                               gg.pose().popPose();
                                           }
                                   )
                            );
                        }
                    }
                    super.render(gg, p_93658_, p_93659_, p_93660_);
                }
            };
            int baseButtonSize2Y = 24;
            int baseButtonSize2X = (w - 45) / 2;

            this.refreshButton = new ExSlotButton(
                    x + 20, y + h - baseButtonSize2Y - 16,
                    baseButtonSize2X, baseButtonSize2Y,
                    WIDGETS_TEXTURE_W, WIDGETS_TEXTURE_W2, baseButtonSize2X, baseButtonSize2Y,
                    -1,
                    () -> {
                        if (currentPage == 0) {
                            RefreshItemMessage msg = new RefreshItemMessage(
                                    findSlotMatchingItemM(selectedRefreshItem, player.getInventory()),
                                    findSlotMatchingItemS(selectedItemStack, player.getInventory())
                            );
                            Exmodifier.PACKET_HANDLER.sendToServer(msg);
                        } else if (currentPage == 1) {
                            RefineItemMessage msg = new RefineItemMessage(
                                    findSlotMatchingItemM(selectedRefreshItem, player.getInventory()),
                                    findSlotMatchingItemS(selectedItemStack, player.getInventory())
                            );
                            Exmodifier.PACKET_HANDLER.sendToServer(msg);
                        }

                    }
            ).setComponent(currentPage == 1 ? Component.translatable("gui.exmodifier.refresh_2") : Component.translatable("gui.exmodifier.refresh_0"));
            this.infoButton = new ExSlotButton(
                    x + 20 + baseButtonSize2X + 5, y + h - baseButtonSize2Y - 16,
                    baseButtonSize2X, baseButtonSize2Y,
                    WIDGETS_TEXTURE_W, WIDGETS_TEXTURE_W2, baseButtonSize2X, baseButtonSize2Y,
                    -1,
                    () -> {
                        //todo 重铸信息
                    }
            ).setComponent(currentPage == 1 ? Component.translatable("gui.exmodifier.refresh_3") : Component.translatable("gui.exmodifier.refresh_1"));


            int materialBtnWidth = width - 40;
            int materialBtnHeight = 40;
            this.materialButton = new ExSlotButton(
                    x + 20, y + scaledSize + 24, // 64 + 20 with scaling
                    materialBtnWidth, materialBtnHeight,
                    WIDGETS_TEXTURE_W, WIDGETS_TEXTURE_W2, materialBtnWidth, materialBtnHeight,
                    1,
                    () -> {
                        manageSlot = 1;
                        if (currentPage == 0) {
                            RefreshMenuScreenPlus.this.itemListViewer.items =
                                    filterItems2(getMinecraft().player.getInventory().items);
                        } else if (currentPage == 1) {
                            RefreshMenuScreenPlus.this.itemListViewer.items =
                                    filterItems3(getMinecraft().player.getInventory().items);
                        }
                    }
            );
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // 渲染主背景
            guiGraphics.blit(menuTexture,
                    x, y,
                    0, 0,
                    width, height,
                    width, height);

            // 渲染按钮
            itemButton.render(guiGraphics, mouseX, mouseY, partialTick);
            materialButton.render(guiGraphics, mouseX, mouseY, partialTick);
            refreshButton.render(guiGraphics, mouseX, mouseY, partialTick);
            infoButton.render(guiGraphics, mouseX, mouseY, partialTick);


            // 渲染选中物品
            renderSelectedItem(guiGraphics);

            // 渲染材料槽内容
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(-5, 0, 0);
            renderMaterialSlot(guiGraphics);
            guiGraphics.pose().popPose();
        }

        private void renderSelectedItem(GuiGraphics guiGraphics) {
            if (!selectedItemStack.isEmpty()) {
                guiGraphics.pose().pushPose();
                try {
                    // 4倍缩放渲染
                    guiGraphics.pose().translate(2, 2, 2);
                    guiGraphics.pose().scale(4, 4, 0);
                    guiGraphics.renderItem(selectedItemStack, x / 4, y / 4);
                    guiGraphics.renderItemDecorations(font, selectedItemStack, x / 4, y / 4);
                } finally {
                    guiGraphics.pose().popPose();
                }

                // 渲染物品名称（带自动截断）

            }
            renderItemLabel(guiGraphics, selectedItemStack);
        }

        private void renderItemLabel(GuiGraphics guiGraphics, ItemStack stack) {
            Component text = stack.isEmpty() ? Component.translatable("gui.exmodifier.refresh_empty") : stack.getHoverName();
            int maxWidth = (int) ((width - 64 - 12) / 1.75);

            FormattedText trimmed = font.substrByWidth(text, maxWidth);
            Component displayText = Component.literal(trimmed.getString())
                    .withStyle(text.getStyle());

            if (font.width(text) > maxWidth) {
                displayText = Component.empty()
                        .append(displayText)
                        .append(Component.literal("...").withStyle(Style.EMPTY));
            }

            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(1.75f, 1.75f, 0);
            guiGraphics.drawString(
                    font,
                    displayText,
                    (int) ((x + 64 + 10) / 1.75),
                    (int) ((y + 2) / 1.75),
                    0xFFFFFF,
                    false
            );
            guiGraphics.pose().popPose();
            if (currentPage == 1) {
                if (!RefreshMenuScreenPlus.this.selectedItemStack.isEmpty()) {
                    Component refineTooltip = RefineHelper.of(selectedItemStack).getRefineTooltip(true);
                    if (refineTooltip != null) {
                        guiGraphics.pose().pushPose();
                        guiGraphics.pose().translate(0, 15, 0);
                        guiGraphics.pose().scale(1.5f, 1.5f, 0);
                        guiGraphics.drawString(font,
                                refineTooltip,
                                (int) ((x + 64 + 10) / 1.5),
                                (int) ((y + 2) / 1.5),
                                Color.YELLOW.getRGB(),
                                false
                        );
                        guiGraphics.pose().popPose();
                    }
                }
            }
        }

        private void renderMaterialSlot(GuiGraphics guiGraphics) {
            if (selectedRefreshItem.isEmpty()) {

                Component text = Component.translatable(
                        "gui.exmodifier.refresh.select_" + (manageSlot == 1 ? 0 : 1) + currentPage);
                int textWidth = font.width(text);
                int centerX = materialButton.getX() + (materialButton.getWidth() - textWidth) / 2 + 6;
                int centerY = materialButton.getY() + (materialButton.getHeight() - 8) / 2;
                guiGraphics.drawString(font, text, centerX, centerY, 0xFFFFFF);
            } else {
                renderItemInSlot(guiGraphics, selectedRefreshItem,
                        materialButton.getX(), materialButton.getY(),
                        materialButton.getWidth(), materialButton.getHeight());

            }

        }

        private void renderItemInSlot(GuiGraphics guiGraphics, ItemStack stack,
                                      int x, int y, int width, int height) {
            guiGraphics.pose().pushPose();
            try {
                float scale = 1.5f;
                guiGraphics.pose().scale(scale, scale, 0);


                int itemX = (int) ((x + (width - 16) / 2) / scale);
                int itemY = (int) ((y + (height - 16) / 2) / scale);
                // 绘制物品槽
                guiGraphics.blit(SLOT_ICON_1, itemX - 2, itemY - 2,
                        0, 0, ITEM_SLOT_SIZE, ITEM_SLOT_SIZE, ITEM_SLOT_SIZE, ITEM_SLOT_SIZE);
                guiGraphics.renderItem(stack, itemX, itemY);
                //guiGraphics.renderItemDecorations(font, stack, itemX, itemY);
                int count = stack.getCount();
                if (count != 1) {
                    Optional<WashingMaterials> first = ModifierHandle.materialsList.stream().filter(m -> m.ItemId.equals(ExUtil.getItemID(stack))).findFirst();
                    boolean present = first.isPresent();
                    int needCount = present ? first.get().NeedCount : 1;
                    String s = "";
                    if (needCount > count) {
                        s = "§4" + count + "/" + needCount;
                    } else {
                        s = "§a" + count + "/" + needCount;
                    }
                    guiGraphics.pose().translate(0.0F, 0.0F, 200.0F);
                    guiGraphics.drawString(font, s, itemX + 19 - 2 - font.width(s), itemY + 6 + 3, 16777215, true);
                }
               renderItemDecorationsWithoutCount(guiGraphics,font, selectedRefreshItem, materialButton.getX(), materialButton.getY());
            } finally {
                guiGraphics.pose().popPose();
            }
        }

        public  void renderItemDecorationsWithoutCount( GuiGraphics guiGraphics,Font p_282005_, ItemStack p_283349_, int p_282641_, int p_282146_) {
            if (!p_283349_.isEmpty()) {
                guiGraphics.pose().pushPose();


                if (p_283349_.isBarVisible()) {
                    int l = p_283349_.getBarWidth();
                    int i = p_283349_.getBarColor();
                    int j = p_282641_ + 2;
                    int k = p_282146_ + 13;
                    guiGraphics.fill(RenderType.guiOverlay(), j, k, j + 13, k + 2, -16777216);
                    guiGraphics.fill(RenderType.guiOverlay(), j, k, j + l, k + 1, i | -16777216);
                }

                Minecraft minecraft1 = RefreshMenuScreenPlus.this.minecraft;
                LocalPlayer localplayer = minecraft1.player;
                float f = localplayer == null ? 0.0F : localplayer.getCooldowns().getCooldownPercent(p_283349_.getItem(), minecraft1.getFrameTime());
                if (f > 0.0F) {
                    int i1 = p_282146_ + Mth.floor(16.0F * (1.0F - f));
                    int j1 = i1 + Mth.ceil(16.0F * f);
                    guiGraphics.fill(RenderType.guiOverlay(), p_282641_, i1, p_282641_ + 16, j1, Integer.MAX_VALUE);
                }

                guiGraphics.pose().popPose();
                net.minecraftforge.client.ItemDecoratorHandler.of(p_283349_).render(guiGraphics, p_282005_, p_283349_, p_282641_, p_282146_);
            }
        }
        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(itemButton, materialButton, refreshButton, infoButton);
        }

        @Override
        public NarrationPriority narrationPriority() {
            return NarrationPriority.HOVERED;
        }

        @Override
        public void updateNarration(NarrationElementOutput output) {
            output.add(NarratedElementType.TITLE, Component.translatable("gui.exmodifier.refresh.widget"));
        }
    }

    // 其他必要方法

    // 在RefreshMenuScreenPlus类中添加内部类
    public class TextListWidget extends AbstractContainerEventHandler implements Renderable, NarratableEntry {
        public static class Entry {
            private final Component text;
            private final List<Component> tooltips;

            public Entry(Component text, List<Component> tooltips) {
                this.text = text;
                this.tooltips = tooltips;
            }
        }

        // 样式参数
        private static final int ENTRY_SPACING = 2;
        private static final int SCROLLBAR_WIDTH = 6;
        private static final int HIGHLIGHT_COLOR = 0x80808080;
        private static final int BACKGROUND_COLOR = 0x80000000;

        public List<Entry> entries;
        private final List<Integer> entryHeights = new ArrayList<>();
        private final Rectangle bounds;
        private int scrollOffset;
        private boolean isScrolling;
        private int totalContentHeight;
        private int visibleItemCount;
        private int selectedIndex = -1;
        private int hoveredIndex = -1;

        public TextListWidget(List<Entry> entries, int x, int y, int width, int height) {
            this.entries = new ArrayList<>(entries);
            this.bounds = new Rectangle(x, y, width, height);
            calculateLayout();
        }

        public void calculateLayout() {
            entryHeights.clear();
            totalContentHeight = 0;

            for (Entry entry : entries) {
                int height = font.wordWrapHeight(entry.text, getContentWidth()) + ENTRY_SPACING;
                entryHeights.add(height);
                totalContentHeight += height;
            }

            visibleItemCount = calculateVisibleItemCount();
        }

        private int getContentWidth() {
            return bounds.width - SCROLLBAR_WIDTH - 4;
        }

        private int calculateVisibleItemCount() {
            int count = 0;
            int currentHeight = 0;
            for (Integer h : entryHeights) {
                if (currentHeight + h > bounds.height) break;
                currentHeight += h;
                count++;
            }
            return Math.max(count, 1);
        }

        private void enableScissor(float x0, float y0, float x1, float y1) {
            Window window = Minecraft.getInstance().getWindow();
            int screenHeight = window.getScreenHeight();
            int scissorX = (int) (x0 * window.getGuiScale());
            int scissorY = (int) (screenHeight - (y1 * window.getGuiScale()));
            int scissorWidth = (int) ((x1 - x0) * window.getGuiScale());
            int scissorHeight = (int) ((y1 - y0) * window.getGuiScale());
            RenderSystem.enableScissor(scissorX, scissorY, scissorWidth, scissorHeight);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.pose().pushPose();

            try {
                renderBackground(guiGraphics);
                enableScissor(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height);
                renderEntries(guiGraphics, mouseX, mouseY);
                RenderSystem.disableScissor();
                renderScrollBar(guiGraphics);
            } finally {
                guiGraphics.pose().popPose();
            }

//            guiGraphics.pose().pushPose();
//            guiGraphics.pose().translate(0, 0, 500);
            renderTooltip(guiGraphics, mouseX, mouseY);
    //        guiGraphics.pose().popPose();
        }

        private void setupClipRegion(GuiGraphics guiGraphics) {
            guiGraphics.enableScissor(
                    bounds.x, bounds.y,
                    bounds.x + bounds.width,
                    bounds.y + bounds.height
            );
        }

        private void renderBackground(GuiGraphics guiGraphics) {
            guiGraphics.fill(
                    bounds.x, bounds.y,
                    bounds.x + bounds.width,
                    bounds.y + bounds.height,
                    BACKGROUND_COLOR
            );
        }

        private void renderEntries(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            int renderY = bounds.y - scrollOffset;
            hoveredIndex = -1;

            for (int i = 0; i < entries.size(); i++) {
                Entry entry = entries.get(i);
                int entryHeight = entryHeights.get(i);

                if (renderY + entryHeight < bounds.y) {
                    renderY += entryHeight;
                    continue;
                }

                if (renderY > bounds.y + bounds.height) break;

                // 高亮逻辑
                if (i == selectedIndex) {
                    renderEntryHighlight(guiGraphics, renderY, entryHeight);
                }

                // 文字渲染
                guiGraphics.drawWordWrap(
                        font, entry.text,
                        bounds.x + 2, renderY + 1,
                        getContentWidth(),
                        0xFFFFFF
                );

                // 检测悬停
                if (isMouseOverEntry(mouseX, mouseY, renderY, entryHeight)) {
                    hoveredIndex = i;
                }

                renderY += entryHeight;
            }
        }

        private void renderEntryHighlight(GuiGraphics guiGraphics, int yPos, int height) {
            guiGraphics.fill(
                    bounds.x + 1, yPos,
                    bounds.x + bounds.width - SCROLLBAR_WIDTH - 1,
                    yPos + height,
                    HIGHLIGHT_COLOR
            );
        }

        private void renderScrollBar(GuiGraphics guiGraphics) {
            if (totalContentHeight <= bounds.height) {
                // 渲染完整滑块（无需滚动条时）
                int fullBarHeight = bounds.height;
                int fullBarY = bounds.y;

                // 轨道背景（完整滑块样式）
                guiGraphics.fill(
                        bounds.x + bounds.width - SCROLLBAR_WIDTH, bounds.y,
                        bounds.x + bounds.width, bounds.y + bounds.height,
                        0x40000000 // 半透明灰色
                );

                // 滑块（完整高度）
                guiGraphics.fill(
                        bounds.x + bounds.width - SCROLLBAR_WIDTH, fullBarY,
                        bounds.x + bounds.width, fullBarY + fullBarHeight,
                        0x80808080 // 半透明浅灰色
                );
                return;
            }

            // 原有滚动条渲染逻辑保持不变
            int scrollHeight = (int) ((float) bounds.height / totalContentHeight * bounds.height);
            scrollHeight = Mth.clamp(scrollHeight, 10, bounds.height);

            int scrollY = bounds.y + (int) ((float) scrollOffset / totalContentHeight * bounds.height);
            scrollY = Mth.clamp(scrollY, bounds.y, bounds.y + bounds.height - scrollHeight);

            // 轨道
            guiGraphics.fill(
                    bounds.x + bounds.width - SCROLLBAR_WIDTH, bounds.y,
                    bounds.x + bounds.width, bounds.y + bounds.height,
                    0x40000000
            );

            // 滑块
            guiGraphics.fill(
                    bounds.x + bounds.width - SCROLLBAR_WIDTH, scrollY,
                    bounds.x + bounds.width, scrollY + scrollHeight,
                    0x80808080
            );
        }


        private void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            if (hoveredIndex != -1 && entries.get(hoveredIndex).tooltips != null) {
                toRenderTooltipList.add(new ToRenderTooltip(()->{
                     guiGraphics.renderTooltip(font, entries.get(hoveredIndex).tooltips, ItemStack.EMPTY.getTooltipImage(), mouseX, mouseY);

                }));
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!isMouseOver(mouseX, mouseY)) return false;

            if (isMouseOverScrollBar(mouseX)) {
                isScrolling = true;
                return true;
            }

            selectedIndex = getEntryAtPosition(mouseX, mouseY);
            return selectedIndex != -1;
        }

        // 其他输入处理方法保持不变
        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
            if (isMouseOver(mouseX, mouseY)) {
                scrollOffset = (int) Mth.clamp(
                        scrollOffset - (delta > 0 ? 20 : -20),
                        0,
                        Math.max(totalContentHeight - bounds.height, 0)
                );
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (isScrolling) {
                double ratio = (mouseY - bounds.y) / bounds.height;
                scrollOffset = (int) Mth.clamp(
                        ratio * totalContentHeight,
                        0,
                        totalContentHeight - bounds.height
                );
                return true;
            }
            return false;
        }

        // 辅助方法
        private boolean isMouseOverEntry(double mouseX, double mouseY, int entryY, int entryHeight) {
            return mouseX >= bounds.x && mouseX <= bounds.x + bounds.width - SCROLLBAR_WIDTH &&
                    mouseY >= entryY && mouseY <= entryY + entryHeight;
        }

        private int getEntryAtPosition(double mouseX, double mouseY) {
            if (!isMouseOver(mouseX, mouseY)) return -1;

            int contentY = (int) (mouseY - bounds.y) + scrollOffset;
            int accumulatedHeight = 0;
            for (int i = 0; i < entries.size(); i++) {
                int entryHeight = entryHeights.get(i);
                if (contentY >= accumulatedHeight && contentY < accumulatedHeight + entryHeight) {
                    return i;
                }
                accumulatedHeight += entryHeight;
            }
            return -1;
        }

        private boolean isMouseOverScrollBar(double mouseX) {
            return mouseX >= bounds.x + bounds.width - SCROLLBAR_WIDTH &&
                    mouseX <= bounds.x + bounds.width;
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return bounds.contains(mouseX, mouseY);
        }

        // 其他必要实现
        @Override
        public List<? extends GuiEventListener> children() {
            return List.of();
        }

        @Override
        public NarrationPriority narrationPriority() {
            return NarrationPriority.HOVERED;
        }

        @Override
        public void updateNarration(NarrationElementOutput output) {
        }
    }


    // 物品列表查看器组件
    public class ItemListViewer extends AbstractContainerEventHandler implements Renderable, NarratableEntry {

        public List<ItemStack> items;
        private final int x;
        private int y;
        private final int width;
        private final int totalSlotsPerPage;
        private final int visibleRowsY;
        private final int visibleRowsX;
        private int selected = -1;
        private ContextMenu contextMenu;
        private boolean showSearchBox = false;
        private EditBox searchBox;
        private ResourceLocation searchBackground = WIDGETS_TEXTURE;
        private int searchBgColor = 0xAA000000;
        private final List<ItemStack> originalItems;
        private final int originalY;

        public ItemListViewer(List<ItemStack> items, int x, int y, int width, int height) {
            this.originalItems = new ArrayList<>(items); // 保存原始列表
            this.items = new ArrayList<>(items);
            this.x = x;
            this.originalY = this.y = y;
            this.visibleRowsY = (height - (showSearchBox ? 25 : 0)) / ITEM_SLOT_SIZE;
            this.visibleRowsX = width / ITEM_SLOT_SIZE;
            this.width = width;
            this.totalSlotsPerPage = visibleRowsX * visibleRowsY;

            // 初始化搜索框
            this.searchBox = new EditBox(
                    font,
                    x + width - 125, // 初始位置（会在render中调整）
                    y + 5,
                    120,
                    18,
                    Component.translatable("gui.exmodifier.search")
            );
            this.searchBox.setMaxLength(32);
            this.searchBox.setBordered(false);
            this.searchBox.setVisible(false);
            this.searchBox.setResponder(text -> filterItems());
        }

        private String removeColorCodes(String text) {
            // 使用正则表达式匹配并替换掉所有颜色代码
            return text.replaceAll("§[0-9A-FK-ORa-fk-or]", "");
        }

        private void filterItems() {
            String query = searchBox.getValue().toLowerCase().trim();
            items = originalItems.stream()
                    .filter(stack -> !stack.isEmpty())
                    .filter(stack -> {
                        // 名称匹配
                        String name = stack.getHoverName().getString().toLowerCase();
                        String cleanText = removeColorCodes(name);
                        if (cleanText.contains(query.toLowerCase())) return true;

                        // 标签匹配（可选）
                        return stack.getTags()
                                .anyMatch(tag -> tag.location().toString().toLowerCase().contains(query.toLowerCase()));
                    })
                    .collect(Collectors.toList());
        }

        // 搜索框可见性控制
        public void setShowSearchBox(boolean show) {
            this.showSearchBox = show;
            this.searchBox.setVisible(show);
            if (show) {
                this.searchBox.setFocused(true);
                this.searchBox.setValue(""); // 清空搜索条件
            }
        }

        // 设置搜索框背景
        public void setSearchBackground(ResourceLocation texture, int color) {
            this.searchBackground = texture;
            this.searchBgColor = color;
        }

        record TooltipToRender(Font font, ItemStack itemStack, int x, int y) {
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // 调整Y坐标（为搜索框留出空间）
            if (showSearchBox) {
                renderSearchBox(guiGraphics, mouseX, mouseY, partialTick);
            }

            // 绘制背景框
            guiGraphics.blit(WIDGETS_TEXTURE, x, y, 0, 0,
                    visibleRowsX * ITEM_SLOT_SIZE + 10,
                    visibleRowsY * ITEM_SLOT_SIZE + 10,
                    visibleRowsX * ITEM_SLOT_SIZE + 10, visibleRowsY * ITEM_SLOT_SIZE + 10);

            // 物品渲染逻辑
            TooltipToRender tooltipToRender = null;
            int startIndex = (int) (scrollOffset * (items.size() - totalSlotsPerPage));
            startIndex = Math.max(0, startIndex);

            for (int i = 0; i < totalSlotsPerPage; i++) {
                int index = startIndex + i;
                if (index >= items.size()) break;

                int row = i / visibleRowsX;
                int col = i % visibleRowsX;
                int slotX = x + 5 + col * ITEM_SLOT_SIZE;
                int slotY = y + 5 + row * ITEM_SLOT_SIZE;

                boolean isHovered = isMouseOverSlot(mouseX, mouseY, slotX, slotY);

                // 绘制物品槽
                if (isHovered) {
                    guiGraphics.blit(SLOT_ICON_2_1, slotX, slotY,
                            0, 0, ITEM_SLOT_SIZE, ITEM_SLOT_SIZE, ITEM_SLOT_SIZE, ITEM_SLOT_SIZE);
                } else {
                    guiGraphics.blit(SLOT_ICON_2, slotX, slotY,
                            0, 0, ITEM_SLOT_SIZE, ITEM_SLOT_SIZE, ITEM_SLOT_SIZE, ITEM_SLOT_SIZE);
                }
                // 绘制物品
                ItemStack stack = items.get(index);
                guiGraphics.renderItem(stack, slotX + 2, slotY + 2);
                guiGraphics.renderItemDecorations(font, stack, slotX + 2, slotY + 2);
                // renderItemCount(guiGraphics, stack, slotX, slotY);

                // 悬停效果
                if (isHovered && (contextMenu == null || contextMenu.buttons.isEmpty())) {
                    guiGraphics.fillGradient(RenderType.guiOverlay(),
                            slotX, slotY,
                            slotX + ITEM_SLOT_SIZE, slotY + ITEM_SLOT_SIZE,
                            -2130706433, -2130706433, 20);
                    tooltipToRender = new TooltipToRender(font, stack, slotX, slotY);
                }
            }


            // 渲染提示信息
            if (tooltipToRender != null) {
                guiGraphics.renderTooltip(font, tooltipToRender.itemStack(), mouseX, mouseY);
            }

            // 上下文菜单渲染
            renderContextMenu(guiGraphics, mouseX, mouseY);
        }

        private void renderItemCount(GuiGraphics guiGraphics, ItemStack stack, int slotX, int slotY) {
            int count = stack.getCount();
            if (count > 1) {
                String countText = String.valueOf(count);
                int textX = slotX + 16 - font.width(countText) - 1;
                int textY = slotY + 16 - 8;
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 200);
                guiGraphics.drawString(font, countText, textX, textY, 0xFFFFFF, false);
                guiGraphics.pose().popPose();
            }
        }

        private void renderContextMenu(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            if (contextMenu != null) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 600);
                contextMenu.render(guiGraphics, mouseX, mouseY, 0);
                guiGraphics.pose().popPose();
            }
        }

        private void renderSearchBox(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // 更新搜索框位置
            searchBox.setX(x + width - 125);
            searchBox.setY(originalY + 30);

            // 绘制背景
            guiGraphics.fillGradient(
                    x + width - 130, originalY + 2,
                    x + width - 10, originalY + 22,
                    searchBgColor, searchBgColor
            );
            guiGraphics.blit(
                    searchBackground,
                    x + width - 130, originalY + 2,
                    0, 0,
                    120, 20,
                    120, 20
            );

            // 渲染搜索框
            searchBox.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        private boolean isMouseOverSlot(int mouseX, int mouseY, int slotX, int slotY) {
            return mouseX >= slotX && mouseX <= slotX + ITEM_SLOT_SIZE &&
                    mouseY >= slotY && mouseY <= slotY + ITEM_SLOT_SIZE;
        }
//        private boolean isMouseOverSlot(int mouseX, int mouseY, int index) {
//            int startIndex = (int)(scrollOffset * (items.size() - visibleRowsY));
//            int slotY = y + 5 + (index - startIndex) * ITEM_SLOT_SIZE;
//            return mouseX >= x + 5 && mouseX <= x + 25 &&
//                    mouseY >= slotY && mouseY <= slotY + ITEM_SLOT_SIZE;
//        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            List<GuiEventListener> children = new ArrayList<>();
            if (contextMenu != null) children.addAll(contextMenu.buttons);
            if (showSearchBox) children.add(searchBox);
            return children;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (showSearchBox && searchBox.isMouseOver(mouseX, mouseY)) {
                contextMenu = null; // 关闭上下文菜单
                searchBox.mouseClicked(mouseX, mouseY, button);
            }
            int startIndex = (int) (scrollOffset * (items.size() - totalSlotsPerPage));
            startIndex = Math.max(0, startIndex);


            // 二维坐标检测
            for (int i = 0; i < totalSlotsPerPage; i++) {
                int index = startIndex + i;
                if (index >= items.size()) break;

                int row = i / visibleRowsX;
                int col = i % visibleRowsX;

                int slotX = x + 5 + col * ITEM_SLOT_SIZE;
                int slotY = y + 5 + row * ITEM_SLOT_SIZE;

                if (isMouseOverSlot((int) mouseX, (int) mouseY, slotX, slotY)) {
                    selected = index;
//                    if (button == 0) {
//                            putItem();
//                    }
                    showContextMenu((int) mouseX, (int) mouseY);
                    return true;
                }

            }
            return false;
        }

        @Override
        public @NotNull Optional<GuiEventListener> getChildAt(double p_94730_, double p_94731_) {

            return super.getChildAt(p_94730_, p_94731_);
        }

        private void showContextMenu(int x, int y) {
            contextMenu = new ContextMenu(x, y, Arrays.asList(
                    new ActionButton("gui.exmodifier.refresh_menu.selected", b -> {
                        putItem();

//                        SystemToast.add(Minecraft.getInstance().getToasts(),
//                                SystemToast.SystemToastIds.PACK_COPY_FAILURE, Component.literal("使用操作"), Component.literal("选择物品"));
                        contextMenu = null;
                    }),
                    new ActionButton("gui.exmodifier.refresh_menu.look", b ->{}

                            //            SystemToast.add(Minecraft.getInstance().getToasts(), SystemToast.SystemToastIds.PACK_COPY_FAILURE, Component.literal("使用操作"), Component.literal("使用物品"))
                    )
            ));
        }

        private void putItem() {
            if (manageSlot == 0) {
                selectedItemStack = items.get(selected);
                if (!selectedItemStack.isEmpty()) {
                    textList.entries = getTextEntries();
                    textList.calculateLayout();
                }
            }
            if (manageSlot == 1) {
                selectedRefreshItem = items.get(selected);
            }
        }


        @Override
        public @NotNull NarrationPriority narrationPriority() {
            return NarrationPriority.HOVERED;
        }

        @Override
        public void updateNarration(NarrationElementOutput narrationElementOutput) {

        }

        // 上下文菜单实现
        private class ContextMenu {
            private final List<ActionButton> buttons;
            private final int x;
            private final int y;

            public ContextMenu(int x, int y, List<ActionButton> buttons) {
                this.buttons = buttons;
                this.x = x;
                this.y = y;

                // 布局按钮
                for (int i = 0; i < buttons.size(); i++) {
                    buttons.get(i).setPosition(x, y + i * 20);
                }
            }

            public void clickContextMenuButton(double x, double y) {
                for (ActionButton button : this.buttons) {
                    if (button.isMouseOver(x, y)) {
                        button.onPress();
                        break;
                    }
                }
            }

            public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                // 绘制背景
                guiGraphics.blit(WIDGETS_TEXTURE, x, y, 0, 0, 80, buttons.size() * 21, 80, buttons.size() * 21);

                // 渲染按钮
                buttons.forEach(btn -> btn.render(guiGraphics, mouseX, mouseY, partialTick));
            }

            public boolean isMouseOver(double mouseX, double mouseY) {
                return mouseX >= x && mouseX <= x + 100 &&
                        mouseY >= y && mouseY <= y + buttons.size() * 22;
            }
        }

        // 操作按钮组件
        public static class ActionButton extends Button {
            public ActionButton(String text, OnPress onPress) {
                super(builder(Component.translatable(text), onPress)
                        .size(80, 20)
                        .createNarration(supplier -> Component.empty()));
            }

            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                // 使用自定义按钮贴图
                if (isHoveredOrFocused()) {
                    guiGraphics.blit(SIMPLE_BUTTON_TEXTURE_OVER, getX(), getY(), 0, 0, width, height, width, height);
                } else guiGraphics.blit(SIMPLE_BUTTON_TEXTURE, getX(), getY(), 0, 0, width, height, width, height);


                guiGraphics.drawCenteredString(Minecraft.getInstance().font, getMessage(),
                        getX() + width / 2, getY() + (height - 8) / 2, 0xFFFFFF);
            }
        }
    }

    public class InfoWidget extends AbstractContainerEventHandler implements Renderable, NarratableEntry {

        private List<Component> textList;
        private int width;
        private int height;
        private int x;
        private int y;
        private ResourceLocation resourceLocation;
        private boolean isShowed = false;
        private ItemListViewer.ActionButton enterButton;

        public InfoWidget(int width, int height, int x, int y, ResourceLocation resourceLocation) {
            this.width = width;
            this.height = height;
            this.x = x;
            this.y = y;
            this.resourceLocation = resourceLocation;
            enterButton = new ItemListViewer.ActionButton("gui.exmodifier.refresh_menu.enter", b -> {
                if (isShowed) {
                    this.isShowed = false;
                }
            });
        }

        @Override
        public void render(GuiGraphics gg, int mx, int my, float pts) {
            if (isShowed) {
                gg.blit(resourceLocation, x, y, 0, 0, width, height, width, height);

            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(enterButton);
        }

        @Override
        public NarrationPriority narrationPriority() {
            return NarrationPriority.FOCUSED;
        }

        @Override
        public void updateNarration(NarrationElementOutput p_169152_) {

        }
    }
}
