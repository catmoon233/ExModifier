package net.exmo.exmodifier.mixins;

import net.exmo.exmodifier.init.ExAttribute;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.world.entity.projectile.AbstractArrow.class)
public abstract class AbstractArrowMixin extends Projectile {
    @Shadow
    private double baseDamage;
    protected AbstractArrowMixin(EntityType<? extends Projectile> p_37248_, Level p_37249_) {
        super(p_37248_, p_37249_);
    }

    @Inject(at = @At("HEAD"),method = "onHitEntity")
    protected void onHitEntity(EntityHitResult p_36757_, CallbackInfo ci) {
        if (this.getOwner() instanceof LivingEntity entity){
            if(   entity.getAttributes().hasAttribute(ExAttribute.ARROWBASEDAMAGE.get())){
                baseDamage += entity.getAttributeValue(ExAttribute.ARROWBASEDAMAGE.get());
            }
        }
    }

}
