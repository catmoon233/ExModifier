package net.exmo.exmodifier.mixins;

import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.exmo.exmodifier.content.event.MainEvent;
import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.util.CuriosUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.getEntrysFromItemStack;

@Mixin(ItemStack.class)
public abstract class ToolTipMixin {
    @ModifyVariable(at =@At("STORE"), method = "getTooltipLines", ordinal = 0)
    private Multimap<Attribute, AttributeModifier> changev(Multimap<Attribute, AttributeModifier> multimap) {
        List<ModifierEntry> entrys = getEntrysFromItemStack((ItemStack)(Object)this);
        List<ModifierAttriGether> attriGethers = new ArrayList<>();

        for (ModifierEntry entry : entrys) {
            if (entry != null) {
                attriGethers.addAll(entry.attriGether);
            }
        }

        // 创建一个临时列表来保存需要删除的 AttributeModifier
        List<AttributeModifier> toRemove = new ArrayList<>();

        // 遍历 multimap 并记录需要删除的 AttributeModifier
        multimap.forEach((attribute, attributeModifier) -> {
         //   Exmodifier.LOGGER.debug("Attribute: " + attribute + ", Modifier: " + attributeModifier + "id " + attributeModifier.getId());
            if (attriGethers.stream().anyMatch(modifierAttriGether -> modifierAttriGether.getModifier().getName().equals(attributeModifier.getName()))) {
                toRemove.add(attributeModifier);
            }
        });

        // 在遍历结束后进行删除
        toRemove.forEach(modifier -> multimap.values().remove(modifier));

        return multimap;
    }
//    @ModifyReturnValue(at =@At("RETURN"), method = "getTooltipLines")
//    public List<Component> getTooltipLines(List<Component> tooltip, Player player, TooltipFlag flag) {
//
//    }

}