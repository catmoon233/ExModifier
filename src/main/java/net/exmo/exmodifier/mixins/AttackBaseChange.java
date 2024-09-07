package net.exmo.exmodifier.mixins;

import net.exmo.exmodifier.util.event.ModifierLivingHurtBaseDamage;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class AttackBaseChange {
    @ModifyVariable(at = @At("STORE"), method = "hurt", ordinal = 0, argsOnly = true)
    private float onAttackBaseChange(float damage){
        ModifierLivingHurtBaseDamage modifierLivingHurtBaseDamage = new ModifierLivingHurtBaseDamage((LivingEntity)(Object)this,damage);
        MinecraftForge.EVENT_BUS.post(modifierLivingHurtBaseDamage);
        return modifierLivingHurtBaseDamage.getDamage();
    }
}
