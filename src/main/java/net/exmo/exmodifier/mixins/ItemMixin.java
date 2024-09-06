package net.exmo.exmodifier.mixins;

import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public class ItemMixin {
//    @ModifyReturnValue(at = @org.spongepowered.asm.mixin.injection.At("RETURN"), method = "getDefaultInstance")
//    public ItemStack getDefaultInstance(ItemStack original)
//    {
//        ModifierHandle.CommonEvent.RandomEntry(original, 0, 2);
//        return original;
//    }
}
