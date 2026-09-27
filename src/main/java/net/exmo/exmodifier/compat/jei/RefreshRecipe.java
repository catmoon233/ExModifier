package net.exmo.exmodifier.compat.jei;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public record RefreshRecipe(List<ItemStack> materials, List<ItemStack> outputs) {
    public RefreshRecipe {
        materials = List.copyOf(materials == null ? List.of() : materials);
        outputs = List.copyOf(outputs == null ? List.of() : outputs);
    }

    public boolean valid() {
        return !materials.isEmpty() && !outputs.isEmpty();
    }
}
