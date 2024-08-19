package net.exmo.exmodifier.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public class ItemMixin {
    @ModifyReturnValue(at = @org.spongepowered.asm.mixin.injection.At("RETURN"), method = "getDefaultInstance",remap = false)
    public ItemStack getDefaultInstance(ItemStack original)
    {
        ModifierHandle.CommonEvent.RandomEntry(original, 0, 2);
        return original;
    }
}
