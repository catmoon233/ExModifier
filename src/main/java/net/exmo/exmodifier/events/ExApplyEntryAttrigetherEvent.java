package net.exmo.exmodifier.events;

import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public class ExApplyEntryAttrigetherEvent extends Event {
    public ItemStack stack;
    public ModifierAttriGether attriGether;
    public boolean isCurios;
    public String curiosSlot;
    public EquipmentSlot slot;
    public ExApplyEntryAttrigetherEvent(ItemStack stack, ModifierAttriGether attriGether, EquipmentSlot slot){
        this.stack = stack;
        this.attriGether = attriGether;
        this.slot = slot;

    }

    public ExApplyEntryAttrigetherEvent(ItemStack stack, ModifierAttriGether attriGether, boolean isCurios, String curiosSlot) {
        this.stack = stack;
        this.attriGether = attriGether;
        this.isCurios = isCurios;
        this.curiosSlot = curiosSlot;
    }

    public ItemStack getStack() {
        return stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    public ModifierAttriGether getAttriGether() {
        return attriGether;
    }

    public void setAttriGether(ModifierAttriGether attriGether) {
        this.attriGether = attriGether;
    }

    public boolean isCurios() {
        return isCurios;
    }

    public void setCurios(boolean curios) {
        isCurios = curios;
    }

    public String getCuriosSlot() {
        return curiosSlot;
    }

    public void setCuriosSlot(String curiosSlot) {
        this.curiosSlot = curiosSlot;
    }

    public EquipmentSlot getSlot() {
        return slot;
    }

    public void setSlot(EquipmentSlot slot) {
        this.slot = slot;
    }
}
