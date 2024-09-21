package net.exmo.exmodifier.content.modifier;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class EntryItem extends Item {
    public EntryItem(Properties p_41383_) {
        super(p_41383_);
    }
    @Mod.EventBusSubscriber
    public static class CommonEvent {
        @SubscribeEvent
        public static void tooltip(ItemTooltipEvent event) {
            ItemStack stack = event.getItemStack();
            if (stack.getItem() instanceof net.exmo.exmodifier.content.modifier.EntryItem) {
                if (!event.getToolTip().isEmpty()) {

                    String modifierId = stack.getOrCreateTag().getString("modifier_id");
                    if (modifierId.length()<=2)return ;

                    event.getToolTip().set(0, Component.translatable("modifiler.entry." + modifierId.substring(2)));
                    String modifierType = stack.getOrCreateTag().getString("modifier_type");
                    if (!modifierType.isEmpty())
                        event.getToolTip().add(Component.translatable("modifiler.entry.type").append(Component.translatable(modifierType)));
                }
            }
        }
    }

}