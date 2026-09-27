package net.exmo.exmodifier.mixins;

import net.exmo.exmodifier.init.ExAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {
    @Inject(method = "onHitEntity", at = @At("HEAD"))
    private void exmodifier$addArrowDamage(EntityHitResult result, CallbackInfo ci) {
        AbstractArrow self = (AbstractArrow) (Object) this;
        if (self.getOwner() instanceof LivingEntity owner && owner.getAttributes().hasAttribute(ExAttributes.ARROW_BASE_DAMAGE)) {
            self.setBaseDamage(self.getBaseDamage() + owner.getAttributeValue(ExAttributes.ARROW_BASE_DAMAGE));
        }
    }
}
