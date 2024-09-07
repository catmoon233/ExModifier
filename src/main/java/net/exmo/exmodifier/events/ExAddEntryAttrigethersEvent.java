package net.exmo.exmodifier.events;

import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.util.WeightedUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

public class ExAddEntryAttrigethersEvent extends Event {
    public ItemStack stack;
    public List<ModifierAttriGether> attriGether;
    public EquipmentSlot slot;
    public WeightedUtil<String> weightedUtil;
    public int refreshments;
    public ModifierEntry TargetModifierEntry;
    public List<ModifierEntry> modifierEntries;
    public boolean isCurios;
    public ExAddEntryAttrigethersEvent(ItemStack stack, WeightedUtil<String> weightedUtil, EquipmentSlot slot, int refreshments, List<ModifierAttriGether> attriGether, ModifierEntry modifierEntry, List<ModifierEntry> modifierEntries){
        this.stack = stack;
        this.weightedUtil = weightedUtil;
        this.slot = slot;
        this.refreshments = refreshments;
        this.attriGether = attriGether;
        this.TargetModifierEntry = modifierEntry;
        this.modifierEntries = modifierEntries;

    }

    public ItemStack getStack() {
        return stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    public List<ModifierAttriGether> getAttriGether() {
        return attriGether;
    }

    public void setAttriGether(List<ModifierAttriGether> attriGether) {
        this.attriGether = attriGether;
    }

    public EquipmentSlot getSlot() {
        return slot;
    }

    public void setSlot(EquipmentSlot slot) {
        this.slot = slot;
    }

    public WeightedUtil<String> getWeightedUtil() {
        return weightedUtil;
    }

    public void setWeightedUtil(WeightedUtil<String> weightedUtil) {
        this.weightedUtil = weightedUtil;
    }

    public int getRefreshments() {
        return refreshments;
    }

    public void setRefreshments(int refreshments) {
        this.refreshments = refreshments;
    }

    public ModifierEntry getTargetModifierEntry() {
        return TargetModifierEntry;
    }

    public void setTargetModifierEntry(ModifierEntry targetModifierEntry) {
        TargetModifierEntry = targetModifierEntry;
    }

    public List<ModifierEntry> getModifierEntries() {
        return modifierEntries;
    }

    public void setModifierEntries(List<ModifierEntry> modifierEntries) {
        this.modifierEntries = modifierEntries;
    }

    public boolean isCurios() {
        return isCurios;
    }

    public void setCurios(boolean curios) {
        isCurios = curios;
    }
}
