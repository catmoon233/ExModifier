package net.exmo.exmodifier.content.modifier;

import net.exmo.exmodifier.content.suit.ExSuit;
import net.exmo.exmodifier.content.suit.ExSuitHandle;
import net.exmo.exmodifier.content.type.ItemType;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.ListTag;
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

    public static String getModifierID(ItemStack stack) {
        if (stack.getTag() == null) return "";
        return stack.getTag().getString("modifier_id");
    }

    public static int getModifierLevel(ItemStack stack) {
        if (stack.getTag() == null) return 1;
        return stack.getTag().getInt("modifier_level");
    }
    public static double getModifierChance(ItemStack stack) {
        if (stack.getTag() == null) return 0;
        return stack.getTag().getDouble("modifier_possibility")* 100;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("modifier.entry." + getModifierID(stack).substring(2));
    }

    @Mod.EventBusSubscriber
    public static class CommonEvent {
        public static final DecimalFormat df = new DecimalFormat("#.####");


        @SubscribeEvent
        public static void tooltip(ItemTooltipEvent event) {
            ItemStack stack = event.getItemStack();
            if (stack.getTag() == null) return;
            if (stack.getItem() instanceof net.exmo.exmodifier.content.modifier.EntryItem) {
                if (!event.getToolTip().isEmpty()) {
                    List<Component> lc = new ArrayList<>();
                    String modifierId = getModifierID(stack);
                    ModifierEntry modifierEntry = ModifierHandle.modifierEntryMap.get(modifierId);
                    if (modifierEntry == null) {
                        lc.add(Component.translatable("modifier.entry.unknown_modifier"));
                        return;
                    }
                    if (modifierId.length() <= 2) return;
                    lc.add(Component.translatable(ModifierEntry.getDescriptionId(modifierId)));
                    if (!Screen.hasShiftDown()) {
                        ListTag types = stack.getTag().getList("modifier_types",8);
                        double possibility = stack.getTag().getDouble("modifier_possibility") * 100;
                        lc.add(Component.translatable("modifier.entry.possibility").append(df.format(possibility)).append("%"));
                        lc.add(Component.translatable("modifier.entry.level").append(String.valueOf(getModifierLevel(stack))));
                        lc.add(Component.translatable("modifier.entry.maxlevel").append(String.valueOf(modifierEntry.maxLevel)));


                        if (!modifierEntry.localDescription.isEmpty()) lc.add(Component.translatable("modifier.entry.desc").append(Component.translatable(modifierEntry.localDescription)));
                        if (!modifierEntry.Slots.isEmpty()) {

                            if (modifierEntry.Slots.size()==1){
                                lc.add(Component.translatable("modifier.entry.slot").append(Component.translatable("modifier.slot." + modifierEntry.Slots.get(0))));
                            }else {
                              //  lc.add(Component.translatable("modifier.entry.slot"));
                                for (var slot : modifierEntry.Slots) {
                                    lc.add(Component.literal(" §7¦ §r").append(Component.translatable("modifier.slot." + slot)));
                                }
                            }
                        }
                        boolean hasTyoe = false;
                        if (types.size() > 1) {

                            for (
                                    var type : types
                            ) {
                                if (!hasTyoe) {
                                    lc.add(Component.translatable("modifier.entry.type"));
                                    hasTyoe = true;
                                }
                                lc.add(Component.literal(" §7¦ §r").append(Component.translatable("modifier.entry.type").append(type.getAsString())));

                            }

                        }else {
                            if (!types.isEmpty()) lc.add(Component.translatable("modifier.entry.type").append(types.get(0).getAsString()));

                        }
                            lc.add(Component.literal(" "));
                            lc.add(Component.translatable("modifier.entry.look_more_shift"));
                        } else {

                            if (modifierEntry == null)
                                lc.add(Component.translatable("modifier.entry.unknown_modifier"));
                            else lc.addAll(modifierEntry.GenerateItemTooltip());
                        }
                        List<Component> ToRemove = event.getToolTip();
                        event.getToolTip().removeAll(ToRemove);
                        event.getToolTip().addAll(lc);
                    }
                }
            }
        }


}