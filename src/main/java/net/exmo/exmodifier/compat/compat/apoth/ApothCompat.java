package net.exmo.exmodifier.compat.compat.apoth;

import com.google.common.collect.Multimap;
import dev.shadowsoffire.attributeslib.api.client.GatherSkippedAttributeTooltipsEvent;
import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ApothCompat {
    public  void SkinAttr(GatherSkippedAttributeTooltipsEvent e) {
        ItemStack stack = e.getStack();
        List<Multimap<Attribute, AttributeModifier>> mapList = new ArrayList();
        EquipmentSlot[] var3 = EquipmentSlot.values();
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            EquipmentSlot equipmentSlot = var3[var5];
            mapList.add(stack.getAttributeModifiers(equipmentSlot));
        }

        List<ModifierEntry> modifiers = ModifierHandle.getEntrysFromItemStack(stack);
        Iterator var13 = mapList.iterator();

        while(var13.hasNext()) {
            Multimap<Attribute, AttributeModifier> map = (Multimap)var13.next();
            Iterator var15 = modifiers.iterator();

            while(var15.hasNext()) {
                ModifierEntry modifier = (ModifierEntry)var15.next();
                Iterator var8 = map.values().iterator();

                while(var8.hasNext()) {
                    AttributeModifier m = (AttributeModifier)var8.next();
                    Iterator var10 = modifier.attriGether.iterator();

                    while(var10.hasNext()) {
                        ModifierAttriGether modifierEntry = (ModifierAttriGether)var10.next();
                        if (m.getName().equals(modifierEntry.modifier.getName())) {
                            e.skipUUID(m.getId());
                        }
                    }
                }
            }
        }

    }
}

