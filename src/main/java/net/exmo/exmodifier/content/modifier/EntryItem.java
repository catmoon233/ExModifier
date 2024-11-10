package net.exmo.exmodifier.content.modifier;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class EntryItem extends Item {
    public EntryItem(Properties p_41383_) {
        super(p_41383_);
    }
    public ModifierEntry getModifierEntry(ItemStack stack) {
        return ModifierHandle.modifierEntryMap.get(stack.getOrCreateTag().getString("modifier_id"));
    }
    @Mod.EventBusSubscriber
    public static class CommonEvent {
        public static final DecimalFormat df = new DecimalFormat("#.###");


        @SubscribeEvent
        public static void tooltip(ItemTooltipEvent event) {
            ItemStack stack = event.getItemStack();
            if (stack.getTag()==null)return;
            if (stack.getItem() instanceof net.exmo.exmodifier.content.modifier.EntryItem) {
                if (!event.getToolTip().isEmpty()) {
                    List<Component> lc = new ArrayList<>();
                    String modifierId = stack.getTag().getString("modifier_id");
                    ModifierEntry modifierEntry = ModifierHandle.modifierEntryMap.get(modifierId);
                    if (modifierId.length()<=2)return ;
                    lc.add(Component.translatable("modifier.entry." + modifierId.substring(2)));
                    if (!Screen.hasShiftDown()) {
                        String modifierType = stack.getTag().getString("modifier_type");
                        double possibility = stack.getTag().getDouble("modifier_possibility") * 100;
                        lc.add(Component.translatable("modifier.entry.possibility").append(df.format(possibility)).append("%"));
                        lc.add(Component.translatable("modifier.entry.maxlevel").append(String.valueOf(modifierEntry.maxLevel)));
                        lc.add(Component.translatable("modifier.entry.desc").append(Component.translatable(modifierEntry.localDescription)));

                        if (!modifierType.isEmpty())
                            lc.add(Component.translatable("modifier.entry.type").append(Component.translatable(modifierType)));
                        lc.add(Component.literal(" "));
                        lc.add(Component.translatable("modifier.entry.look_more_shift"));
                    }else {

                        if (modifierEntry==null)lc.add(Component.translatable("modifier.entry.unknown_modifier"));
                        else lc.addAll(modifierEntry.GenerateItemTooltip());
                    }
                List<Component> ToRemove =event.getToolTip();
                event.getToolTip().removeAll(ToRemove);
                event.getToolTip().addAll(lc);
                }
            }
        }
    }

}