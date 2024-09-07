package net.exmo.exmodifier.content.event;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.content.suit.ExSuit;
import net.exmo.exmodifier.events.ExSuitApplyOnChangeEvent;

import net.exmo.exmodifier.util.EntityAttrUtil;
import net.exmo.exmodifier.util.event.ModifierLivingHurtBaseDamage;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Mod.EventBusSubscriber
public class AppointConditionDamage {
    public static boolean pdSlots(List<String> slots,Player player,List<ItemStack> lis){
        for (String slot : (slots)){


                if (lis.contains(player.getItemBySlot(EquipmentSlot.byName(slot.toLowerCase())))) {
                    Exmodifier.LOGGER.debug("Slot "+slot+" has item");
                    return true;
                }


        }
        return false    ;
    }
    @SubscribeEvent
    public static void Apply(ExSuitApplyOnChangeEvent event){
        ExSuit exSuit = event.exSuit;
        Player player = event.getEntity();
        List<ItemStack> WearsId = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()){
            if (!player.getItemBySlot(slot).isEmpty()){
                WearsId.add(player.getItemBySlot(slot));
            }
        }
        LazyOptional<IItemHandlerModifiable> equippedCurios = CuriosApi.getCuriosHelper() .getEquippedCurios(player);
        for (int i = 0; i < equippedCurios.orElse(null).getSlots(); i++){
            if (!equippedCurios.orElse(null).getStackInSlot(i).isEmpty()){
                WearsId.add(equippedCurios.orElse(null).getStackInSlot(i));
            }
        }
        List<String> WearsIds = new ArrayList<>();
        for (ItemStack itemStack : WearsId){
            WearsIds.add(ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString());
        }
     (exSuit.getAttriGetherC()).forEach((key, value) -> {
            for (ModifierAttriGether attriGetherPlus : value) {
                if (attriGetherPlus.OnlyItems.isEmpty())continue;
                EntityAttrUtil.entityAddAttrTF(attriGetherPlus.attribute, attriGetherPlus.modifier, player, EntityAttrUtil.WearOrTake.TAKE);
            } });
       for (int i =0;i<=10;i++){
           if (exSuit.getAttriGetherC().containsKey(i)){
               for (ModifierAttriGether attriGetherPlus : exSuit.getAttriGetherC().get(i)){
                   if (attriGetherPlus.OnlyItems.isEmpty())continue;
                   Exmodifier.LOGGER.debug("Item "+attriGetherPlus.OnlyItems);

                   if (new ArrayList<String>( attriGetherPlus.OnlyItems).retainAll(WearsIds)){
                       if (!attriGetherPlus.OnlySlots.isEmpty()){
                       //  Exmodifier.LOGGER.debug("Slot "+attriGetherPlus.OnlySlots);
                           List<String> _v = new ArrayList<>(attriGetherPlus.OnlySlots);
                          // Exmodifier.LOGGER.debug("Slot1 "+attriGetherPlus.OnlySlots+" has item"+attriGetherPlus.OnlyItems);

                           if ((pdSlots(_v, player, WearsId))){
                               EntityAttrUtil.entityAddAttrTF(attriGetherPlus.attribute, attriGetherPlus.modifier, player, EntityAttrUtil.WearOrTake.WEAR);
                           }
                           //                           Exmodifier.LOGGER.debug("Slot2 "+attriGetherPlus.OnlySlots+" has item"+attriGetherPlus.OnlyItems);

                           //attriGetherPlus.OnlySlots.addAll(_v);
                       }else {
                           Exmodifier.LOGGER.debug("No Slot");
                           EntityAttrUtil.entityAddAttrTF(attriGetherPlus.attribute, attriGetherPlus.modifier, player, EntityAttrUtil.WearOrTake.WEAR);
                       }
                   }
               }
           }
       }

    }
}
