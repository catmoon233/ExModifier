package net.exmo.exmodifier.mixins;

import dev.shadowsoffire.apotheosis.adventure.affix.effect.DurableAffix;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.init.ExAttribute;
import net.exmo.exmodifier.util.ItemAttrUtil;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.items.modular.impl.bow.ModularBowItem;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ChangeDurability {
    @Shadow public abstract boolean isDamageableItem();

    @Inject(at = @At("RETURN"),method = "getMaxDamage", cancellable = true)
    public void getMaxDamage(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (isDamageableItem()) {

                    double durAttr = 0;
                    durAttr += ItemAttrUtil.getAttributeModifiers(stack, ExAttribute.DURABILITY.get()).stream().mapToDouble(AttributeModifier::getAmount).sum();

                   // durAttr += stack.getAttributeModifiers(slot).get(ExAttribute.DURABILITY.get()).stream().mapToDouble(AttributeModifier::getAmount).sum();

                if (durAttr != 0) cir.setReturnValue((int) (cir.getReturnValue() * (1 + durAttr)));



        }
    }
}