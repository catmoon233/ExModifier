package net.exmo.exmodifier.content.modifier;

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
    @Mod.EventBusSubscriber
    public static class CommonEvent {
        public static final DecimalFormat df = new DecimalFormat("#.###");

        @SubscribeEvent
        public static void tooltip(ItemTooltipEvent event) {
            ItemStack stack = event.getItemStack();
            if (stack.getItem() instanceof net.exmo.exmodifier.content.modifier.EntryItem) {
                if (!event.getToolTip().isEmpty()) {
                    List<Component> lc = new ArrayList<>();
                    String modifierId = stack.getOrCreateTag().getString("modifier_id");
                    if (modifierId.length()<=2)return ;

                    lc.add( Component.translatable("modifiler.entry." + modifierId.substring(2)));
                    String modifierType = stack.getOrCreateTag().getString("modifier_type");
                    double possibility = stack.getOrCreateTag().getDouble("modifier_possibility")*100;
                    lc.add(Component.translatable("modifiler.entry.possibility").append(df.format(possibility)).append("%"));

                    if (!modifierType.isEmpty())
                        lc.add(Component.translatable("modifiler.entry.type").append(Component.translatable(modifierType)));
                List<Component> ToRemove =event.getToolTip();
                event.getToolTip().removeAll(ToRemove);
                event.getToolTip().addAll(lc);
                }
            }
        }
    }

}