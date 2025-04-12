package net.exmo.exmodifier.events;

import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public class ExAddEntryAttrigetherEvent extends Event {

    public ModifierEntry modifierEntry;

    public ModifierAttriGether selectedAttriGether;
    public ExAddEntryAttrigetherEvent(ModifierEntry modifierEntry,ModifierAttriGether selectedAttriGether){
        this.modifierEntry = modifierEntry;

        this.selectedAttriGether = selectedAttriGether;


    }


    public ModifierEntry getModifierEntry() {
        return modifierEntry;
    }

    public void setModifierEntry(ModifierEntry modifierEntry) {
        this.modifierEntry = modifierEntry;
    }



    public ModifierAttriGether getSelectedAttriGether() {
        return selectedAttriGether;
    }

    public void setSelectedAttriGether(ModifierAttriGether selectedAttriGether) {
        this.selectedAttriGether = selectedAttriGether;
    }
}
