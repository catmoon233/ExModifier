package net.exmo.exmodifier.events;

import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

public class ExEntryTooltipEvent extends Event {
    public ModifierEntry modifierEntry;
    public Player player;
    public ItemStack itemStack;
    public List<Component> tooltip;
    public boolean isCurios;
    public ExEntryTooltipEvent(ModifierEntry modifierEntry, Player player, ItemStack itemStack, List<Component> tooltip) {
        this.modifierEntry = modifierEntry;
        this.player = player;
        this.itemStack = itemStack;
        this.tooltip = tooltip;
    }

    public ModifierEntry getModifierEntry() {
        return modifierEntry;
    }

    public void setModifierEntry(ModifierEntry modifierEntry) {
        this.modifierEntry = modifierEntry;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public List<Component> getTooltip() {
        return tooltip;
    }

    public void setTooltip(List<Component> tooltip) {
        this.tooltip = tooltip;
    }

    public boolean isCurios() {
        return isCurios;
    }

    public void setCurios(boolean curios) {
        isCurios = curios;
    }
}
