package net.exmo.exmodifier.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.exmo.exmodifier.content.helper.ItemQualityHelper;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.exmo.exmodifier.Config.refresh_time;

@Mixin(Item.class)
public abstract class ItemMixin {
//    @ModifyReturnValue(at = @org.spongepowered.asm.mixin.injection.At("RETURN"), method = "getDefaultInstance")
//    public ItemStack getDefaultInstance(ItemStack original)
//    {
//        ModifierHandle.CommonEvent.RandomEntry(original,0, refresh_time, "none");
//        return original;
//    }
    @Inject(method = "getName(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/network/chat/Component;", at = @At("RETURN"), cancellable = true)
    private void onGetDisplayName(ItemStack stack, CallbackInfoReturnable<Component> cir) {
        for (var q : ItemQualityHelper.of(stack).getQualityEntriesTooltip()) {
            Component component = cir.getReturnValue();
            if (q.isShowInHeadTooltip) {
                component = Component.translatable(q.mutableComponent.getString()).append(" ").append(component).setStyle(q.mutableComponent.getStyle());

            }
            cir.setReturnValue(component);
        }

    }
}
