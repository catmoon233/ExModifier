package net.exmo.exmodifier.mixins;

import cc.xypp.damage_number.client.DamageRender;
import cc.xypp.damage_number.data.DamageListItem;

import net.exmo.exmodifier_compat.compat.DamageNumberCompatData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(DamageRender.class)
public class DamageNumberCompat {
    @Unique
    private static long lastTime = 0;
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I",ordinal = 3), method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;FIIZ)V")
    public int render(GuiGraphics instance, Font p_282003_, String p_281403_, int p_282714_, int p_282041_, int p_281908_) {
        Integer neareast = DamageNumberCompatData.findNeareast(lastTime);
        if (neareast==null)neareast =-1;
        return instance.drawString(p_282003_,p_281403_,p_282714_,p_282041_, neareast);
    }
    @Redirect(at = @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/tuple/Pair;getLeft()Ljava/lang/Object;",ordinal = 0),remap = false, method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;FIIZ)V")
    public Object render(Pair<DamageListItem, Long> instance) {
        lastTime = instance.getRight();
        return instance.getLeft();
    }
    @Redirect(at = @At(value = "INVOKE", target = "Ljava/util/List;remove(I)Ljava/lang/Object;"), remap = false,method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;FIIZ)V")
    public Object render(List<Pair<DamageListItem, Long>> instance, int i) {
        DamageNumberCompatData.removeNearestKey(instance.get(i).getRight());
        return instance.remove(i);
    }
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I",ordinal = 4), method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;FIIZ)V")
    public int render1(GuiGraphics instance, Font p_282003_, String p_281403_, int p_282714_, int p_282041_, int p_281908_) {
        Integer neareast = DamageNumberCompatData.lastColor;
        if (neareast==null)neareast =-1;
        return instance.drawString(p_282003_,p_281403_,p_282714_,p_282041_, neareast);
    }
}
