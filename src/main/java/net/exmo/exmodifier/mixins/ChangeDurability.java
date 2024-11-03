package net.exmo.exmodifier.mixins;

import dev.shadowsoffire.apotheosis.adventure.affix.effect.DurableAffix;
import net.exmo.exmodifier.init.ExAttribute;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ChangeDurability {
    @Shadow public abstract boolean isDamageableItem();

    @Inject(at = @At("RETURN"),method = "getMaxDamage", cancellable = true)
    public void getMaxDamage(CallbackInfoReturnable<Integer> cir) {
        if (isDamageableItem()) {

            double dur = cir.getReturnValue();
            double durAttr = 0;
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                durAttr += ((ItemStack) (Object) this).getAttributeModifiers(slot).get(ExAttribute.DURABILITY.get()).stream().mapToDouble(a -> a.getAmount()).sum();
            }
            cir.setReturnValue((int) (dur * (1 + durAttr)));
        }
    }
}