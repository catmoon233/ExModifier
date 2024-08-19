package net.exmo.exmodifier.mixins;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.common.MinecraftForge;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mixin(ItemStack.class)
public abstract class ToolTipMixin {
    @ModifyVariable(at =@At("STORE"), method = "getTooltipLines", ordinal = 0)
    private Multimap<Attribute, AttributeModifier> changev(Multimap<Attribute, AttributeModifier> multimap) {
        List<ModifierEntry> entrys = ModifierHandle.getEntrysFromItemStack((ItemStack)(Object)this);
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
            if (attriGethers.stream().anyMatch(modifierAttriGether -> modifierAttriGether.getModifier().getName().equals(attributeModifier.getName()))) {
                toRemove.add(attributeModifier);
            }
        });

        // 在遍历结束后进行删除
        toRemove.forEach(modifier -> multimap.values().remove(modifier));

        return multimap;
    }
}