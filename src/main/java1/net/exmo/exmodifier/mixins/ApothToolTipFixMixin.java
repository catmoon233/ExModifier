package net.exmo.exmodifier.mixins;

import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import shadows.apotheosis.adventure.client.AdventureModuleClient;

import java.util.ArrayList;
import java.util.List;

@Mixin(AdventureModuleClient.class)
public  class ApothToolTipFixMixin    {

   @ModifyReturnValue(at =@At("RETURN"), method = "getSortedModifiers",remap = false)
   private static Multimap<Attribute, AttributeModifier> getSortedModifiers(Multimap<Attribute, AttributeModifier> original, ItemStack stack,EquipmentSlot slot) {
       List<ModifierEntry> entrys = ModifierHandle.getEntrysFromItemStack(stack);
       List<ModifierAttriGether> attriGethers = new ArrayList<>();

       for (ModifierEntry entry : entrys) {
           if (entry != null) {
               attriGethers.addAll(entry.attriGether);
           }
       }

       // 创建一个临时列表来保存需要删除的 AttributeModifier
       List<AttributeModifier> toRemove = new ArrayList<>();

       // 遍历 multimap 并记录需要删除的 AttributeModifier
       original.forEach((attribute, attributeModifier) -> {
           if (attriGethers.stream().anyMatch(modifierAttriGether -> modifierAttriGether.getModifier().getName().equals(attributeModifier.getName()))) {
               toRemove.add(attributeModifier);
           }
       });

       // 在遍历结束后进行删除
       toRemove.forEach(modifier -> original.values().remove(modifier));

       return original;
   }


    }
