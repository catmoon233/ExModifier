package net.exmo.exmodifier.mixins;

import net.exmo.exmodifier.init.ExAttribute;
import net.exmo.exmodifier.util.ItemAttrUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.items.modular.ModularItem;

import java.util.List;

@Mixin(Item.class)
public abstract class ChangeDurabilityBar {

    @Shadow @Deprecated public abstract int getMaxDamage();

    @Inject(at = @At("HEAD"),method = "getBarWidth", cancellable = true)
    public void getBarWidth(ItemStack stack,CallbackInfoReturnable<Integer> cir) {
        if (ModList.get().isLoaded("tetra"))
        {
            if (((Item)((Object)this)) instanceof  ModularItem)return;
        }
        if (stack.isDamageableItem()) {
            double durAttr = 0;

            durAttr += ItemAttrUtil.getAttributeModifiers(stack, ExAttribute.DURABILITY.get()).stream().mapToDouble(AttributeModifier::getAmount).sum();

            if (durAttr!=0){
                cir.cancel();
                cir.setReturnValue((int) Math.round(13.0F - (float)stack.getDamageValue() * 13.0F / (getMaxDamage()*(1+durAttr))));
            }
        }
    }
    @ModifyVariable(at =  @At("STORE"),method = "getBarColor", ordinal = 1)
    private float stackMaxDamage(float value,ItemStack stack) {
        if (ModList.get().isLoaded("tetra"))
        {
            if (((Item)((Object)this)) instanceof  ModularItem)return value;
        }
        if (stack.isDamageableItem()) {
            double durAttr = 0;
            durAttr += ItemAttrUtil.getAttributeModifiers(stack, ExAttribute.DURABILITY.get()).stream().mapToDouble(AttributeModifier::getAmount).sum();

            if (durAttr!=0){
                return (float) (value*(1+durAttr));
            }


        }
        return value;
    }
}