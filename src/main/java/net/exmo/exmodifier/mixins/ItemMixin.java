package net.exmo.exmodifier.mixins;

import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(at = @org.spongepowered.asm.mixin.injection.At("RETURN"), method = "getDefaultInstance", cancellable = true)
    public void getDefaultInstance(CallbackInfoReturnable<ItemStack> cir)
    {
        ItemStack itemStack = cir.getReturnValue();
        ModifierHandle.CommonEvent.RandomEntry(itemStack, 0, 2);
        cir.setReturnValue(itemStack);
    }
}
