package net.exmo.exmodifier.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.util.random.Weight;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


@Mixin(Weight.class)
public abstract class PlaceBoFixer {

    @Inject(method = "validateWeight", at = @At("HEAD"), cancellable = true)
    private static void validateWeight(int p_146285_, CallbackInfo ci) {
        if (p_146285_ < 0) {
            ci.cancel();
        }
    }


    @Inject(method = "of", at = @At("HEAD"), cancellable = true)
    private static void of(int p_146283_, CallbackInfoReturnable<Weight> cir) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (p_146283_ < 0){
            Class<?> clazz = Weight.class;
            Constructor<?> constructor = clazz.getDeclaredConstructor(int.class);

            cir.setReturnValue((Weight)(constructor.newInstance(p_146283_)));
            cir.cancel();
        }
    }
}