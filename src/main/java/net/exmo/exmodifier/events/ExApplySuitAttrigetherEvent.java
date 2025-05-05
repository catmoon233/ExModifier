package net.exmo.exmodifier.events;

import net.exmo.exmodifier.util.gether.AttriGetherNormal;
import net.exmo.exmodifier.util.EntityAttrUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public class ExApplySuitAttrigetherEvent extends Event {
    public Player player;
    public ItemStack stack;
    public EntityAttrUtil.WearOrTake effectType;

    public AttriGetherNormal attriGether;


    public ExApplySuitAttrigetherEvent(Player player, ItemStack stack, EntityAttrUtil.WearOrTake effectType, AttriGetherNormal attriGether) {
        this.player = player;
        this.stack = stack;
        this.effectType = effectType;
        this.attriGether = attriGether;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public ItemStack getStack() {
        return stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    public EntityAttrUtil.WearOrTake getEffectType() {
        return effectType;
    }

    public void setEffectType(EntityAttrUtil.WearOrTake effectType) {
        this.effectType = effectType;
    }

    public AttriGetherNormal getAttriGether() {
        return attriGether;
    }

    public void setAttriGether(AttriGetherNormal attriGether) {
        this.attriGether = attriGether;
    }
}
