package net.exmo.exmodifier.mixins;

import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.exmo.exmodifier.Config;
import net.exmo.exmodifier.content.event.MainEvent;
import net.exmo.exmodifier.content.event.TooltipFixer;
import net.exmo.exmodifier.content.helper.ItemLevelHelper;
import net.exmo.exmodifier.content.helper.ModifierEntryHelper;
import net.exmo.exmodifier.content.level.ItemLevel;
import net.exmo.exmodifier.content.level.ItemLevelHandle;
import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.util.CuriosUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;



@Mixin(ItemStack.class)
public abstract class ToolTipMixin {
    private static int equipmentlotCount = 0;
    @Inject(at =@At("HEAD"), method = "getTooltipLines")
    private void getTooltipLines(Player p_41652_, TooltipFlag p_41653_, CallbackInfoReturnable<List<Component>> cir) {
        equipmentlotCount = 0;
    }

    @ModifyVariable(at =@At("STORE"), method = "getTooltipLines", ordinal = 0)
    private Multimap<Attribute, AttributeModifier> changev(Multimap<Attribute, AttributeModifier> multimap) {
        equipmentlotCount++;
        ItemStack stack = (ItemStack) (Object) this;
        if (Config.entryFold && !Screen.hasShiftDown())return multimap;
        if (stack.getTag()==null)return multimap;
        List<ModifierEntry> entries = new ModifierEntryHelper(stack).getModifierEntriesB();
        List<ModifierAttriGether> attriGethers = new ArrayList<>();

        for (ModifierEntry entry : entries) {
            if (entry != null) {
                attriGethers.addAll(entry.attriGether);
            }
        }
        for (ItemLevel entry : ItemLevelHelper.of(stack).getItemLevels()) {
            if (entry != null) {
                attriGethers.addAll(entry.attriGethers);
            }
        }
        // 创建一个临时列表来保存需要删除的 AttributeModifier
        List<AttributeModifier> toRemove = new ArrayList<>();

        // 遍历 multimap 并记录需要删除的 AttributeModifier
        multimap.forEach((attribute, attributeModifier) -> {

         //   Exmodifier.LOGGER.debug("Attribute: " + attribute + ", Modifier: " + attributeModifier + "Id " + attributeModifier.getId());
            if (attriGethers.stream().anyMatch(modifierAttriGether -> modifierAttriGether.getModifier().getName().equals(attributeModifier.getName())) || attributeModifier.getName().equals("exmodifier_refine")) {
                toRemove.add(attributeModifier);
            }
        });

        // 在遍历结束后进行删除
        toRemove.forEach(modifier -> multimap.values().remove(modifier));
        if (multimap.isEmpty()){
            TooltipFixer.isFixing = true;
            TooltipFixer.fixList.add(getEquipmentSlot(equipmentlotCount));
        }

        return multimap;
    }
    private EquipmentSlot getEquipmentSlot(int i){
        return switch ( i) {
            case 1 -> EquipmentSlot.MAINHAND;
            case 2 -> EquipmentSlot.OFFHAND;
            case 3 -> EquipmentSlot.FEET;
            case 4 -> EquipmentSlot.LEGS;
            case 5 -> EquipmentSlot.CHEST;
            case 6 -> EquipmentSlot.HEAD;
            default -> EquipmentSlot.MAINHAND;
        };
    }
//    @ModifyReturnValue(at =@At("RETURN"), method = "getTooltipLines")
//    public List<Component> getTooltipLines(List<Component> tooltip, Player player, TooltipFlag flag) {
//
//    }

}