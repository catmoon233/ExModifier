package net.exmo.exmodifier.content.modifier.menu;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.xkmc.l2damagetracker.contents.curios.TotemUseToClient;
import net.exmo.exmodifier.content.event.MainEvent;
import net.exmo.exmodifier.content.helper.ModifierEntryHelper;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.content.type.ExType;
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
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RefreshMenuScreenPlus extends AbstractContainerScreen<RefreshMenuPlus> implements ContainerListener {
    private  final List<Page> pages = new ArrayList<>();
    private int currentPage = 0;
    private final Player player;
    private ItemStack selectedItemStack = ItemStack.EMPTY;
    private ItemStack selectedRefreshItem = ItemStack.EMPTY;
    private int manageSlot = 0;
    //0->selectedItemStack
    //1->selectedRefreshItem
    // 新增成员变量
    private ItemListViewer itemListViewer;
    private float scrollOffset;
    private boolean isScrolling;
    private static final ResourceLocation WIDGETS_TEXTURE =
            new ResourceLocation("exmodifier", "textures/gui/backround.png");
    private static final ResourceLocation SIMPLE_BUTTON_TEXTURE =
            new ResourceLocation("exmodifier", "textures/gui/simple_button.png");
    private static final ResourceLocation SIMPLE_BUTTON_TEXTURE_OVER =
            new ResourceLocation("exmodifier", "textures/gui/simple_button_over.png");
    private static final int ITEM_SLOT_SIZE = 20;
    private final Map<GuiEventListener, Float> hoverProgress = new HashMap<>();
    private static final ResourceLocation MENU_TEXTURE = new ResourceLocation("exmodifier", "textures/gui/background3.png");
    void initPages(){
        // 添加默认页面（示例）
        addPage("重铸", screen -> {
            screen.addRenderableWidget(new ImageWidget(80, this.topPos-20, this.width-160, this.imageHeight, MENU_TEXTURE));
           // screen.addRenderableWidget(new ImageWidget();
            screen.addRenderableWidget(new RefreshWidget(100, this.topPos +20, this.width /3, this.imageHeight-60, MENU_TEXTURE));
           // screen.addRenderableWidget(new Widget(100, this.topPos +20, this.width /3, this.imageHeight-60, MENU_TEXTURE))
            //
//            screen.addRenderableWidget(new Button.Builder(Component.literal("示例按钮"), b -> {})
//                    .pos(100, 50)
//                    .size(100, 20)
//                    .build());
//            screen.addRenderableWidget(new StringWidget(100, 80, 100, 20,
//                    Component.literal("欢迎使用！"), screen.font));
            this.itemListViewer = new ItemListViewer(filterItems(player.getInventory().items),
                    120 + this.width/3, topPos + 20,
                    this.width-160 -this.width /3-40 -20, imageHeight - 60);

                textList = new TextListWidget(
                        List.of(),
                        100 + 64 + 10, RefreshMenuScreenPlus.this.topPos + 40,
                        RefreshMenuScreenPlus.this.width / 3 -82, 45
                );
                if (!selectedItemStack.isEmpty()) {
                    textList.entries = getTextEntries();
                    textList.calculateLayout();;
                };
          //  RefreshMenuScreenPlus.this.children().removeIf(widget -> widget instanceof TextListWidget);
            addRenderableWidget(textList);
            addRenderableWidget(itemListViewer);


        });

        addPage("设置", screen -> {
            screen.addRenderableWidget(new ImageWidget(80, this.topPos-20, this.width-160, this.imageHeight, MENU_TEXTURE));

            screen.addRenderableWidget(new CycleButton.Builder<Boolean>(b ->{
                if (b){
                    return Component.literal("已开启");
                } else {
                    return Component.literal("已关闭");
                }

            })
                    .withValues(true, false)
                    .create(100, 50, 100, 20,
                            Component.literal("选项开关"),
                            (b, v) -> {}));
        });
    }

    private @NotNull List<TextListWidget.Entry> getTextEntries() {
        List<TextListWidget.Entry> textEntries = ModifierEntryHelper.of(selectedItemStack).getModifierEntries().stream()
                .map(entry -> {
                    MutableComponent translatable = Component.translatable(entry.getModifierEntry().getDescriptionId());
                    if (entry.getLevel()>1)translatable.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + entry.getLevel())).withStyle(ChatFormatting.GOLD);
                    return new TextListWidget.Entry(translatable,ModifierHandle.CommonEvent.generateEntryTooltip(entry, player, selectedItemStack) );
                })
                .collect(Collectors.toList());
        return textEntries;
    }

    public RefreshMenuScreenPlus(RefreshMenuPlus p_97874_, Inventory p_97875_, Component p_97876_) {
        super(p_97874_, p_97875_, p_97876_);
        this.player = p_97875_.player;
        this.imageHeight = 250;
        this.imageWidth = 400;
    }

    public  void addPage(String title, Consumer<RefreshMenuScreenPlus> contentInitializer) {
        pages.add(new Page(Component.literal(title), contentInitializer));
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        float newOffset = (float)(scrollOffset - delta * 0.1);
        scrollOffset = Mth.clamp(newOffset, 0f, 1f);
        textList.mouseScrolled(mouseX, mouseY, delta);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isScrolling) {
            float delta = (float)(dragY / (itemListViewer.visibleRowsY * ITEM_SLOT_SIZE));
            scrollOffset = Mth.clamp(scrollOffset + delta, 0f, 1f);
            textList.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            return true;
        }
        return false;
    }
    public static List<ItemStack> filterItems(List<ItemStack> items){
        return items.stream()
                .filter(item -> !item.isEmpty() && !ModifierEntry.getType(item).stream().filter(e -> e != ExType.ALL.get()).toList().isEmpty())
                .toList();
    }
    public static List<ItemStack> filterItems2(List<ItemStack> items){
        return items.stream()
                .filter(item -> !item.isEmpty() && ModifierHandle.materialsList.stream().anyMatch(entry -> entry.ItemId.equals(ExUtil.getItemID(item))))
                .toList();
    }
    @Override
    protected void init() {
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

            Button tabButton = new TabButton( rightMargin, this.topPos +2 + i * 30, 20, 20,
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

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);


        int i = this.leftPos;
        int j = this.topPos;
        this.renderBg(guiGraphics, partialTick, mouseX, mouseY);
        RenderSystem.disableDepthTest();
        for(Renderable renderable : this.renderables) {
            if (renderable ==null)continue;
//            if (renderable instanceof TabButton tabButton){
//                if (pages.get(currentPage).title == tabButton.getMessage())continue;
//            }
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        this.hoveredSlot = null;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate((float)i, (float)j, 0.0F);
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
        textList.isMouseOver(x,y);
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
        if (itemListViewer!=null && itemListViewer.contextMenu!=null){
            if (itemListViewer.contextMenu.isMouseOver(mouseX, mouseY)){
                itemListViewer.contextMenu.clickContextMenuButton(mouseX, mouseY);
            }
        }
        // 关闭上下文菜单
        if (itemListViewer != null && itemListViewer.contextMenu != null && !itemListViewer.contextMenu.isMouseOver(mouseX, mouseY)) {
            itemListViewer.contextMenu = null;
        }
        for (GuiEventListener guiEventListener : this.children()){
            if (guiEventListener instanceof RefreshWidget refreshWidget){
                refreshWidget.mouseClicked(mouseX, mouseY, p_97750_);
            }
        }
        if (itemListViewer!=null ) {
        for(GuiEventListener guiEventListener : itemListViewer.children()) {

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
        for(GuiEventListener guiEventListener : children) {
            if (guiEventListener instanceof AbstractButton ) {
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
        public int getColor(){
            if (expandProgress >0){
                return Color.lightGray.getRGB();
            }
            if (currentPage == getIndex()+1){
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
                    0xFFFFFF | ((int)(textAlpha * 255) << 24),
                    false);
        }

        private int getIndex() {
            return (getY() - (height / 2 - pages.size() * 15)) / 30;
        }
    }

    // 页面数据结构
    private record Page(Component title, Consumer<RefreshMenuScreenPlus> contentInitializer) {}
    private  TextListWidget textList = new TextListWidget(List.of(),0,0,0,0);
    //重铸组件
    private class RefreshWidget extends AbstractContainerEventHandler implements Renderable , NarratableEntry{
        private int x;
        private int y;
        private int width;
        private int height;
        private ResourceLocation menuTexture;

        public RefreshWidget(int x, int y, int w, int h, ResourceLocation menuTexture) {
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
            this.menuTexture = menuTexture;
        }



        @Override
        public void render(GuiGraphics guiGraphics, int i, int i1, float v) {
            guiGraphics.blit(menuTexture, x, y, 0, 0, width, height, width, height);

            int iconSize = 16*4;
            guiGraphics.blit(WIDGETS_TEXTURE, x + 2, y + 2, 0, 0, iconSize, iconSize, iconSize, iconSize);
            if (manageSlot ==0){
                //white full
                guiGraphics.fill(x+2, y+2, x + iconSize+2, y + iconSize+2, 0x80808080);
            }
            int maxWidth = (int) ((width - 64-12)/1.75);
            // 在页面初始化时创建文本列表

            if (!selectedItemStack.isEmpty()) {
                Component text = selectedItemStack.getHoverName();
                // 修复后的文本截断逻辑
                var trimmed =Component.literal(font.substrByWidth(text, maxWidth).getString());
                if (font.width(text) > maxWidth) {
                    trimmed = Component.empty()
                            .append(trimmed.copy().withStyle(text.getStyle()))
                            .append(Component.literal("...").withStyle(Style.EMPTY));
                }

                //int textWidth = font.width(trimmed);
                int xPos = 100+64+10;//x + 2 + (width - textWidth) / 2;
                int yPos = y +2;
                guiGraphics.pose().pushPose();
                guiGraphics.pose().scale(1.75f,1.75f,0);
                guiGraphics.drawString(font, trimmed, ((int) (xPos / 1.75)), ((int) (yPos / 1.75)), 0xFFFFFF, false); // 添加缺失的分号
                guiGraphics.pose().popPose();

            }

            int startX = x + 20;
            int startY = y + 64 + 20;
            int width1 = width - 40;
            int height1 = 40;
            guiGraphics.blit(WIDGETS_TEXTURE, startX, startY, 0, 0, width1, height1, width1, height1);
            //选择 白色叠层
            if (manageSlot==1){
                guiGraphics.fill(startX, startY, startX + width1, startY + height1, 0x80808080);
            }
            if (selectedRefreshItem.isEmpty()) {
                // 渲染“请选择重铸物品”的文本在贴图中间
                Component text;
                if (manageSlot == 1) {
                    text = Component.translatable("gui.exmodifier.refresh.select_0");
                } else {
                    text = Component.translatable("gui.exmodifier.refresh.select_1");
                }
                int textWidth = font.width(text);
                int textX = startX + (width1 - textWidth) / 2; // 计算文本的X坐标，使其居中
                int textY = startY + (height1 - 8) / 2; // 计算文本的Y坐标，使其居中
                guiGraphics.drawString(font, text, textX, textY, 0xFFFFFF); // 绘制文本
            } else {
                // 物品渲染在中间
                guiGraphics.pose().pushPose();
                guiGraphics.pose().scale(1.5f, 1.5f, 0);
                int itemX = startX + (width1 - 16) / 2; // 计算物品的X坐标，使其居中
                int itemY = startY + (height1 - 16) / 2; // 计算物品的Y坐标，使其居中
                guiGraphics.renderItem(selectedRefreshItem, (int) (itemX / 1.5), (int) (itemY / 1.5)); // 渲染物品

                // 渲染物品数量
                int count = selectedRefreshItem.getCount();
                if (count > 1) {
                    String countText = String.valueOf(count);
                    int textX = (int) (itemX / 1.5) + 16 - font.width(countText) - 2; // 计算文本的X坐标，使其位于物品右下角
                    int textY = (int) (itemY / 1.5) + 16 - 8; // 计算文本的Y坐标，使其位于物品右下角
                    guiGraphics.drawString(font, countText, textX, textY, 0xFFFFFF, false); // 绘制数量文本
                }

                guiGraphics.pose().popPose();
            }



            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(2, 2, 2);
            guiGraphics.pose().scale(4, 4f, 0);
            guiGraphics.renderItem(selectedItemStack, x / 4, y / 4);
            guiGraphics.pose().popPose();
        }

        @Override
        public boolean mouseClicked(double x, double y, int p_94697_) {
            if (p_94697_ == 0) {
                //在this.x-this.x+64 this.y-this.y+64 内
                if (x >= this.x+2 && x <= this.x + 64+2 && y >= this.y+2 && y <= this.y + 64+2) {
                    manageSlot = 0;
                    RefreshMenuScreenPlus.this.itemListViewer.items = filterItems(player.getInventory().items);
                    return true;
                }
                int startX = this.x + 20;
                int startY = this.y + 64 + 20;
                int width1 = width - 40;
                int height1 = 40;
                if (x >= startX && x <= startX + width1 && y >= startY && y <= startY + height1) {
                    manageSlot = 1;
                    RefreshMenuScreenPlus.this.itemListViewer.items = filterItems2(player.getInventory().items);
                    return true;
                }
            }
            return super.mouseClicked(x, y, p_94697_);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of();

        }

        @Override
        public NarrationPriority narrationPriority() {
            return NarrationPriority.HOVERED;
        }

        @Override
        public void updateNarration(NarrationElementOutput narrationElementOutput) {

        }
    }
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

        private void calculateLayout() {
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

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.pose().pushPose();

            try {
                renderBackground(guiGraphics);
                renderEntries(guiGraphics, mouseX, mouseY);
                renderScrollBar(guiGraphics);
            } finally {
                guiGraphics.pose().popPose();
            }

            renderTooltip(guiGraphics, mouseX, mouseY);
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
            if (totalContentHeight <= bounds.height) return;

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
                guiGraphics.renderTooltip(font, entries.get(hoveredIndex).tooltips, ItemStack.EMPTY.getTooltipImage(), mouseX, mouseY);
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
        @Override public List<? extends GuiEventListener> children() { return List.of(); }
        @Override public NarrationPriority narrationPriority() { return NarrationPriority.HOVERED; }
        @Override public void updateNarration(NarrationElementOutput output) {}
    }



    // 物品列表查看器组件
    private class ItemListViewer extends AbstractContainerEventHandler implements Renderable , NarratableEntry {
        public  List<ItemStack> items;
        private final int x;
        private final int y;
        private final int width;
        private final int totalSlotsPerPage;
        private final int visibleRowsY;
        private final int visibleRowsX;
        private int selected = -1;
        private ContextMenu contextMenu;

        public ItemListViewer(List<ItemStack> items, int x, int y, int width, int height) {
            this.items = items;
            this.x = x;
            this.y = y;
            this.visibleRowsY = height / ITEM_SLOT_SIZE;
            this.visibleRowsX = width / ITEM_SLOT_SIZE;
            this.width = width;
            this.totalSlotsPerPage = visibleRowsX * visibleRowsY;

        }

        record TooltipToRender(Font font, ItemStack itemStack, int x, int y){}
        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // 绘制背景框（调整尺寸参数）
            guiGraphics.blit(WIDGETS_TEXTURE, x, y, 0, 0,
                    visibleRowsX * ITEM_SLOT_SIZE + 10,
                    visibleRowsY * ITEM_SLOT_SIZE + 10,
                    visibleRowsX * ITEM_SLOT_SIZE + 10, visibleRowsY * ITEM_SLOT_SIZE + 10);

            // 计算起始索引
            int startIndex = (int)(scrollOffset * (items.size() - totalSlotsPerPage));
            startIndex = Math.max(0, startIndex);

            TooltipToRender tooltipToRender = null;
            // 二维布局渲染
            Optional<GuiEventListener> childAt1 = getChildAt(mouseX, mouseY);
            boolean isSelectedItem = false;
            for (int i = 0; i < totalSlotsPerPage; i++) {
                int index = startIndex + i;
                if (index >= items.size()) break;

                // 计算行列位置
                int row = i / visibleRowsX;
                int col = i % visibleRowsX;

                int slotX = x + 5 + col * ITEM_SLOT_SIZE;
                int slotY = y + 5 + row * ITEM_SLOT_SIZE;

                boolean isHovered = isMouseOverSlot(mouseX, mouseY, slotX, slotY);

                // 绘制物品槽
                guiGraphics.blit(WIDGETS_TEXTURE, slotX, slotY,
                        0, 0,
                        ITEM_SLOT_SIZE, ITEM_SLOT_SIZE,
                        ITEM_SLOT_SIZE, ITEM_SLOT_SIZE)
                ;

                // 绘制物品
                guiGraphics.renderItem(items.get(index), slotX+2 , slotY+2 );
                int count = items.get(index).getCount();
                if (count > 1) {
                    String countText = String.valueOf(count);
                    int textX = slotX + 2 + 16 - font.width(countText) - 2; // 计算文本的X坐标，使其位于物品右下角
                    int textY = slotY + 2 + 16 - 8; // 计算文本的Y坐标，使其位于物品右下角
                    guiGraphics.drawString(font, countText, textX, textY, 0xFFFFFF, false); // 绘制数量文本
                }
                if (!isSelectedItem && isHovered &&(contextMenu ==null || contextMenu.buttons.stream().filter(button -> button.isMouseOver(mouseX, mouseY)).toList().isEmpty())){
                    guiGraphics.fillGradient(RenderType.guiOverlay(), slotX, slotY, slotX + 20, slotY + 20, -2130706433, -2130706433, 20);
                    tooltipToRender = new TooltipToRender(font,items.get(index),slotX,slotY);

                }
            }
            if (tooltipToRender != null) {
                guiGraphics.renderTooltip(tooltipToRender.font(), tooltipToRender.itemStack(), mouseX, mouseY);
            }
            if (!selectedItemStack.isEmpty()) {

                textList.entries = getTextEntries();
                textList.calculateLayout();;
            }
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 600);
            // 绘制上下文菜单
            if (contextMenu != null) {
                contextMenu.render(guiGraphics, mouseX, mouseY, partialTick);

            }
            guiGraphics.pose().popPose();
            if (childAt1.isPresent()){
                if (childAt1.get().isMouseOver(mouseX, mouseY)) {
                    if (childAt1.get() instanceof ActionButton actionButton) {
                        if (actionButton.getMessage().contains(Component.literal("查看"))) {
                            guiGraphics.pose().pushPose();
                            guiGraphics.pose().translate(0, 0, 601);
                            guiGraphics.renderTooltip(font, items.get(selected), mouseX, mouseY);
                            guiGraphics.pose().popPose();
                        }
                    }
                }
            }
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
        public List<? extends GuiEventListener> children() {
            if (contextMenu == null) return Collections.emptyList();
            return contextMenu.buttons;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            int startIndex = (int)(scrollOffset * (items.size() - totalSlotsPerPage));
            startIndex = Math.max(0, startIndex);


            // 二维坐标检测
            for (int i = 0; i < totalSlotsPerPage; i++) {
                int index = startIndex + i;
                if (index >= items.size()) break;

                int row = i / visibleRowsX;
                int col = i % visibleRowsX;

                int slotX = x + 5 + col * ITEM_SLOT_SIZE;
                int slotY = y + 5 + row * ITEM_SLOT_SIZE;

                if (isMouseOverSlot((int)mouseX, (int)mouseY, slotX, slotY)) {
                    selected = index;
                    showContextMenu((int)mouseX, (int)mouseY);
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
                    new ActionButton("选择", b -> {
                        if (manageSlot==0) {
                            selectedItemStack = items.get(selected);
                        }
                        if (manageSlot==1){
                            selectedRefreshItem = items.get(selected);
                        }

                        SystemToast.add(new ToastComponent(Minecraft.getInstance()),
                                SystemToast.SystemToastIds.PACK_COPY_FAILURE,Component.literal("使用操作"), Component.literal("选择物品"));
                        contextMenu = null;
                    }),
                    new ActionButton("查看", b ->
                            SystemToast.add(new ToastComponent(Minecraft.getInstance()), SystemToast.SystemToastIds.PACK_COPY_FAILURE,Component.literal("使用操作"), Component.literal("使用物品"))
)
            ));
        }


        @Override
        public NarrationPriority narrationPriority() {
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
            public  void clickContextMenuButton(double x, double y){
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
        private class ActionButton extends Button {
            public ActionButton(String text, OnPress onPress) {
                super(builder(Component.translatable(text), onPress)
                        .size(80, 20)
                        .createNarration(supplier -> Component.empty()));
            }

            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                // 使用自定义按钮贴图
                if (isHoveredOrFocused()){
                    guiGraphics.blit(SIMPLE_BUTTON_TEXTURE_OVER, getX(), getY(), 0, 0, width, height, width, height);
                }else guiGraphics.blit(SIMPLE_BUTTON_TEXTURE, getX(), getY(), 0, 0, width, height, width, height);


                guiGraphics.drawCenteredString(font, getMessage(),
                        getX() + width/2, getY() + (height - 8)/2, 0xFFFFFF);
            }
        }
    }
}
