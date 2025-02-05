package net.exmo.exmodifier.events;

import net.exmo.exmodifier.util.AttrGether;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

public class ExCuriosAttributeTooltipEvent extends Event {

    public Player player;
    public ItemStack itemStack;
    public List<Component> tooltip;
    public List<Component> tooltipADD;
    public List<AttrGether> attributeModifiers;
    public boolean isCurios;
    public ExCuriosAttributeTooltipEvent(Player player, ItemStack itemStack, List<Component> tooltip, List<Component> tooltipADD, List<AttrGether> attributeModifiers) {
        this.player = player;
        this.itemStack = itemStack;
        this.tooltip = tooltip;
        this.tooltipADD = tooltipADD;
        this.attributeModifiers = attributeModifiers;
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
