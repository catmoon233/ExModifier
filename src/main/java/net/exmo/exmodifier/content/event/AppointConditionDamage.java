package net.exmo.exmodifier.content.event;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.content.suit.ExSuit;
import net.exmo.exmodifier.content.suit.ExSuitHandle;
import net.exmo.exmodifier.events.ExAfterArmorChange;
import net.exmo.exmodifier.events.ExSuitApplyOnChangeEvent;

import net.exmo.exmodifier.network.ExModifiervaV;
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

import java.util.*;

@Mod.EventBusSubscriber
public class AppointConditionDamage {
    public static boolean containsCommonElements(List<String> list, List<String> str) {
        // 使用 HashSet 进行高效的查找操作
        Set<String> set = new HashSet<>(list);

        // 遍历 str 列表，检查是否存在 set 中的元素
        for (String s : str) {
            if (set.contains(s)) {
                return true; // 找到重复元素，直接返回 true
            }
        }

        return false; // 没有找到重复元素，返回 false
    }

    public static boolean pdSlots(Map<String,String> items,List<String> slots,List<String> onlyitem){
        boolean flag = false;
        for (String slot : (slots)){
            //
            if (onlyitem.contains(items.get(slot))){
                flag=true;
                break;
            }


        }
        return flag    ;
    }
    public static void Operate(ExSuit exSuit,Player player){
        Map<String,String> items = new HashMap<>();
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values())
        {
            ItemStack is= player.getItemBySlot(equipmentSlot);
            if (!is.isEmpty()) {
                items.put(equipmentSlot.getName(), ForgeRegistries.ITEMS.getKey(is.getItem()).toString());
            }
        }
        exSuit.attriGether.values().forEach(ea ->{
            for (ModifierAttriGether e : ea.stream().filter(e -> !e.getOnlyItems().isEmpty()).toList()){
                EntityAttrUtil.entityAddAttrTF(e.attribute, e.getModifier(), player, EntityAttrUtil.WearOrTake.TAKE);
            }
        });
        ExModifiervaV.PlayerVariables vars = player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).orElse(new ExModifiervaV.PlayerVariables());
        if (vars.SuitsNum.get(exSuit.id)!=null){
            for (int i = 1; i <=vars.SuitsNum.get(exSuit.id) ; i++) {
                List<ModifierAttriGether> attriGethers = exSuit.attriGether.get(i);
                if (attriGethers != null) {
                    for (ModifierAttriGether e : attriGethers.stream().filter(e -> !e.getOnlyItems().isEmpty()).toList()) {
                        List<String> onlyitems = e.getOnlyItems();
                        List<String> onlyslots = e.getOnlySlots();
                    //    Exmodifier.LOGGER.debug(" 1 " + onlyitems);
                        boolean flag = false;
                        if (!onlyitems.isEmpty()) {
                            if (containsCommonElements(onlyitems, new ArrayList<>(items.values()))) {
                                flag = true;
                              //  Exmodifier.LOGGER.debug("OnlyItems:" + onlyitems + " items " + items + " is true");
                            }
                        }
                        if (!onlyslots.isEmpty()) {
                            if (!pdSlots(items, onlyslots, onlyitems)) {
                                flag = false;
                            //    Exmodifier.LOGGER.debug("OnlySlots:" + onlyslots + " is false");
                            }

                        }
                     //   Exmodifier.LOGGER.debug("flag:" + flag);
                        if (flag) {
                            EntityAttrUtil.entityAddAttrTF(e.attribute, e.getModifier(), player, EntityAttrUtil.WearOrTake.WEAR);
                        }

                    }
                }
            }
        }
    }
    @SubscribeEvent
    public static void AfterArmorChange(ExAfterArmorChange exAfterArmorChange){
        if (!exAfterArmorChange.isSuitOperate){
            Player player = (Player) exAfterArmorChange.event.getEntity();
            ExModifiervaV.PlayerVariables vars = player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).orElse(new ExModifiervaV.PlayerVariables());
            for (String exSuit : vars.Suits){
                ExSuitHandle.FindExSuit(exSuit).forEach(suit -> {
                    Operate(suit,player);

                });
            }
        }

    }
    @SubscribeEvent
    public static void Apply(ExSuitApplyOnChangeEvent event){
        ExSuit exSuit = event.exSuit;
        Player player = (Player) event.getEntity();
        Operate(exSuit,player);

    }
}
