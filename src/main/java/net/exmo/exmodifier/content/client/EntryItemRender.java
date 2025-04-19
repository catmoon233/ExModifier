package net.exmo.exmodifier.content.client;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.modifier.EntryItem;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;

import java.util.HashMap;
import java.util.Map;

public class EntryItemRender implements IItemDecorator {
    public static final ResourceLocation bookIcon = new ResourceLocation(Exmodifier.MODID, "textures/item/entry_item.png");
    private static final int CYCLE_INTERVAL = 500;

    private static class TextAnimState {
        int visibleStartIndex;
        long lastUpdate;
    }

    private final Map<String, TextAnimState> textAnimStates = new HashMap<>();

    private String getStyledSubstring(String text, int visibleStart, int length) {
        StringBuilder result = new StringBuilder();
        int totalVisible = (int) text.chars().filter(c -> c != '§').count() / 2;
        if (totalVisible == 0) return text;

        // 创建样式代码映射表
        Map<Integer, String> activeStyles = new HashMap<>();
        int visibleCount = 0;
        int i = 0;

        // 预扫描样式代码
        while (i < text.length() && visibleCount <= visibleStart + length) {
            if (text.charAt(i) == '§') {
                if (i + 1 < text.length()) {
                    String styleCode = text.substring(i, i + 2);
                    // 记录当前生效的样式
                    activeStyles.put(visibleCount, styleCode);
                    i += 2;
                } else {
                    i++;
                }
            } else {
                // 应用最近的样式代码
                if (visibleCount >= visibleStart) {

                    String style = activeStyles.values().stream().findFirst().orElse("");
                    result.append(style);
                }
                if (visibleCount >= visibleStart && visibleCount < visibleStart + length) {
                    result.append(text.charAt(i));
                }
                i++;
                visibleCount++;
            }
        }
        return result.toString();
    }




    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        String id = stack.getOrCreateTag().getString("modifier_id");
        if (id.isEmpty()) return false;

        // 图标渲染
        ModifierEntry modifierEntry = ModifierHandle.findModifierEntry(id);
        if (modifierEntry != null && !modifierEntry.icon.isEmpty()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 200);
            guiGraphics.blit(new ResourceLocation(modifierEntry.icon), xOffset, yOffset, 16, 16, 0, 0, 16, 16, 16, 16);
            guiGraphics.pose().popPose();
        }

        if (Screen.hasShiftDown() && stack.getItem() instanceof EntryItem) {
            if (id.length() <= 2) return false;

            Component c = modifierEntry != null ?
                    Component.translatable(modifierEntry.getDescriptionId()) :
                    Component.translatable("tooltip.exmodifier.entry_book.none");

            String text = c.getString();
            TextAnimState state = textAnimStates.computeIfAbsent(text, k -> new TextAnimState());
            long now = System.currentTimeMillis();

            // 初始化状态
            if (state.lastUpdate == 0) {
                state.lastUpdate = now;
            }

            // 计算可见字符总数
            int totalVisible = text.replaceAll("§.", "").length();
            if (totalVisible < 2) return false; // 至少需要2个可见字符

            // 更新动画状态
            long elapsed = now - state.lastUpdate;
            if (elapsed >= CYCLE_INTERVAL) {
                state.visibleStartIndex = (state.visibleStartIndex + 1) % (totalVisible - 1);
                state.lastUpdate = now;
            }

            // 生成显示文本
            String displayedString = getStyledSubstring(text, state.visibleStartIndex, 2);

            // 渲染文本
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 200);
            guiGraphics.drawString(
                    font,
                    Component.literal(displayedString), // 不再叠加全局样式
                    xOffset + 8 - font.width(displayedString) / 2,
                    yOffset + 8 - font.lineHeight / 2,
                    0xFFFFFF,
                    true // 必须启用样式解析
            );
            guiGraphics.pose().popPose();
            return true;
        }
        return false;
    }
}
