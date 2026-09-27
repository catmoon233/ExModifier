package net.exmo.exmodifier.mixins;

import net.exmo.exmodifier.Config;
import net.exmo.exmodifier.api.AppliedModifierView;
import net.exmo.exmodifier.api.ExModifierApi;
import net.exmo.exmodifier.core.data.QualityDefinition;
import net.exmo.exmodifier.core.registry.ExModifierCatalog;
import net.exmo.exmodifier.init.ExAttributes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
    private void exmodifier$prefixName(CallbackInfoReturnable<Component> cir) {
        ItemStack self = (ItemStack) (Object) this;
        Component name = cir.getReturnValue();
        var quality = ExModifierApi.qualityOn(self);
        if (quality.isPresent()) {
            QualityDefinition definition = ExModifierCatalog.current().quality(quality.get()).orElse(null);
            if (definition != null && definition.showInHeadTooltip() && definition.tooltipKey().isPresent()) {
                name = Component.translatable(definition.tooltipKey().get()).append(" ").append(name);
            }
        }
        var views = ExModifierApi.modifiersOn(self);
        if (!views.isEmpty() && Config.alwaysUnderName) {
            AppliedModifierView first = views.getFirst();
            name = Component.translatable("tooltip.exmodifier.name_prefix",
                    Component.translatable("tooltip.exmodifier.entry." + first.entryId().getPath()),
                    name);
        }
        cir.setReturnValue(name);
    }
    private static  LivingEntity cache_entity = null;
    @Inject(method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V", at = @At("HEAD"))
    private void exmodifier$durability(int p_41623_, LivingEntity p_41624_, EquipmentSlot p_319898_, CallbackInfo ci) {
       cache_entity = p_41624_;
    }

    @ModifyVariable(method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int exmodifier$durability(int amount) {
        if (cache_entity != null && cache_entity.getAttributes().hasAttribute(ExAttributes.DURABILITY)) {
            double multiplier = cache_entity.getAttributeValue(ExAttributes.DURABILITY);
            if (multiplier > 0 && multiplier != 1.0D) {
                return Math.max(0, (int) Math.round(amount / multiplier));
            }
        }
        return amount;
    }
}
