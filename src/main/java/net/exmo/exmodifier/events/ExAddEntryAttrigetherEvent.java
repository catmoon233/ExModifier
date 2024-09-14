package net.exmo.exmodifier.events;

import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public class ExAddEntryAttrigetherEvent extends Event {

    public ModifierEntry modifierEntry;
    public ItemStack stack;
    public ModifierAttriGether selectedAttriGether;
    public ExAddEntryAttrigetherEvent(ModifierEntry modifierEntry,ModifierAttriGether selectedAttriGether,ItemStack stack){
        this.modifierEntry = modifierEntry;
        this.stack = stack;
        this.selectedAttriGether = selectedAttriGether;


    }


    public ModifierEntry getModifierEntry() {
        return modifierEntry;
    }

    public void setModifierEntry(ModifierEntry modifierEntry) {
        this.modifierEntry = modifierEntry;
    }

    public ItemStack getStack() {
        return stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    public ModifierAttriGether getSelectedAttriGether() {
        return selectedAttriGether;
    }

    public void setSelectedAttriGether(ModifierAttriGether selectedAttriGether) {
        this.selectedAttriGether = selectedAttriGether;
    }
}
