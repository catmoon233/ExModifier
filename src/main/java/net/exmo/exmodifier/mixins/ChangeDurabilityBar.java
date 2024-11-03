package net.exmo.exmodifier.mixins;

import net.exmo.exmodifier.init.ExAttribute;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ChangeDurabilityBar {

    @Shadow @Deprecated public abstract int getMaxDamage();

    @Inject(at = @At("RETURN"),method = "getBarWidth", cancellable = true)
    public void getBarWidth(ItemStack stack,CallbackInfoReturnable<Integer> cir) {
        if (stack.isDamageableItem()) {
            double durAttr = 0;
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                durAttr += stack.getAttributeModifiers(slot).get(ExAttribute.DURABILITY.get()).stream().mapToDouble(a -> a.getAmount()).sum();
            }
            if (durAttr!=0){
                cir.setReturnValue((int) Math.round(13.0F - (float)stack.getDamageValue() * 13.0F / (getMaxDamage()*(1+durAttr))));
            }
        }
    }
}