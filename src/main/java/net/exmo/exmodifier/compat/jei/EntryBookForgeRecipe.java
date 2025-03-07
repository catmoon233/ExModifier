package net.exmo.exmodifier.compat.jei;

import net.exmo.exmodifier.content.modifier.WashingMaterials;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record EntryBookForgeRecipe(List< ItemStack > refresh_items,List <ItemStack >entry_output
                                ) {


    public boolean isValid() {
        return !refresh_items.isEmpty() && !entry_output.isEmpty();
    }
}
