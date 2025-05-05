package net.exmo.exmodifier.compat.compat.apoth;

import com.google.common.collect.Multimap;
import dev.shadowsoffire.attributeslib.api.client.GatherSkippedAttributeTooltipsEvent;
import net.exmo.exmodifier.Config;
import net.exmo.exmodifier.content.helper.ItemLevelHelper;
import net.exmo.exmodifier.content.helper.ModifierEntryHelper;
import net.exmo.exmodifier.content.level.ItemLevel;
import net.exmo.exmodifier.content.level.ItemLevelInstant;
import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

public class ApothCompat {

    public void SkinAttr(GatherSkippedAttributeTooltipsEvent e) {
        if (Config.entryFold && !Screen.hasShiftDown())return ;

        ItemStack stack = e.getStack();

        List<Multimap<Attribute, AttributeModifier>> mapList = new ArrayList<>();

        EquipmentSlot[] var3 = EquipmentSlot.values();
        //int var4 = var3.length;

        for (EquipmentSlot equipmentSlot : var3) {
            mapList.add(stack.getAttributeModifiers(equipmentSlot));
        }

        List<ModifierEntry> modifiers = ModifierEntryHelper.of(stack).getModifierEntriesB();

        for (Multimap<Attribute, AttributeModifier> map : mapList) {
            for (ModifierEntry modifier : modifiers) {
                for (AttributeModifier m : map.values()) {
                    if (m.getName().equals("exmodifier_refine")){
                        e.skipUUID(m.getId());
                        continue;
                    }
                    for (ModifierAttriGether modifierEntry : modifier.attriGether) {
                        if (m.getName().equals(modifierEntry.modifier.getName()) ) {
                            e.skipUUID(m.getId());
                        }
                    }
                    for (ItemLevel entry : ItemLevelHelper.of(stack).getItemLevels()) {
                        if (entry != null) {
                            for (ModifierAttriGether attriGether : entry.getAttriGethers()) {
                                if (m.getName().equals(attriGether.modifier.getName())) {
                                    e.skipUUID(m.getId());
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



