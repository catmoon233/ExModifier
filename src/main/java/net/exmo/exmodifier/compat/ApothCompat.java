package net.exmo.exmodifier.compat;

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
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber
public class ApothCompat {
    public static void SkinAttr()
{
//    if (ModList.get().isLoaded("attributeslib")) {
//        ItemStack stack = e.getStack();
//        List<Multimap<Attribute, AttributeModifier>> mapList = new ArrayList<>();
//        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
//            mapList.add(stack.getAttributeModifiers(equipmentSlot));
//        }
//        List<ModifierEntry> modifiers = ModifierHandle.getEntrysFromItemStack(stack);
//        for (Multimap<Attribute, AttributeModifier> map : mapList) {
//            for (ModifierEntry modifier : modifiers) {
//                for (AttributeModifier m : map.values()) {
//                    for (ModifierAttriGether modifierEntry : modifier.attriGether) {
//                        if (m.getName().equals(modifierEntry.modifier.getName()))
//                            e.skipUUID(m.getId());
//                    }
//                }
//            }
//        }
//    }
}
}
