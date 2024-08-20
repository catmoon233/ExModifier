package net.exmo.exmodifier.content;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.content.suit.ExSuit;
import net.exmo.exmodifier.content.suit.ExSuitHandle;
import net.exmo.exmodifier.network.ExModifiervaV;
import net.exmo.exmodifier.util.EntityAttrUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.CommonEvent.RandomEntry;
import static net.exmo.exmodifier.util.EntityAttrUtil.WearOrTake.TAKE;
import static net.exmo.exmodifier.util.EntityAttrUtil.WearOrTake.WEAR;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class MainEvent {
    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) throws IOException {
        ModifierHandle.readConfig();
        ExSuitHandle.readConfig();

    }
    @Mod.EventBusSubscriber
    public static class CommonEvent {
        @SubscribeEvent
        public static void AtJoinGame(PlayerEvent.PlayerLoggedInEvent event){
            event.getEntity().getPersistentData().putBoolean("LoginGamea", true);
        }
        @SubscribeEvent
        public static void OutGame(PlayerEvent.PlayerLoggedOutEvent event){
            event.getEntity().getPersistentData().putBoolean("LoginGamea", false);
        }
        @SubscribeEvent
        public static void PlayerLiving(TickEvent.PlayerTickEvent event) {

            Player player = event.player;
            player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                for (String id : capability.Suits) {
                    ExSuit suit = ExSuitHandle.LoadExSuit.get(id);
                    for (int i = 1; i <= player.getPersistentData().getInt(id); i++) {
                        // 获取效果列表，检查是否为null
                        Map<Integer, List<MobEffectInstance>> effectMap = suit.getEffect();
                        if (effectMap != null) {
                            List<MobEffectInstance> effects = effectMap.get(i);
                            if (effects != null) {
                                for (MobEffectInstance mobEffectInstance : effects) {
                                    if (player.hasEffect(mobEffectInstance.getEffect())) {
                                        if (Objects.requireNonNull(player.getEffect(mobEffectInstance.getEffect())).getAmplifier() < mobEffectInstance.getAmplifier()) {
                                            player.addEffect(mobEffectInstance);
                                        }
                                    }
                                    if (!player.hasEffect(mobEffectInstance.getEffect())) {
                                        player.addEffect(mobEffectInstance);

                                    }
                                }
                            }
                        }
                    }
                }
            });
        }

        @SubscribeEvent
        public static void armorChange(LivingEquipmentChangeEvent event){
            if (event.getEntity() instanceof Player player) {
                if (player.getPersistentData().getBoolean("LoginGamea")){
                    player.getPersistentData().putBoolean("LoginGamea", false);
                    return;
                }

                if (!event.getEntity().level.isClientSide) {

                    ItemStack stack = event.getTo();
                    if (stack.getOrCreateTag().getInt("exmodifier_armor_modifier_applied") == 0){
                        player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                            capability.Sitemstack = stack;
                            capability.syncPlayerVariables(player);
                        });
                    }
                    RandomEntry(stack, 0, 2);
                    if (stack.getOrCreateTag().getBoolean("modifier_refresh")) {
                        stack.getOrCreateTag().putString("exmodifier_armor_modifier_applied0", "");
                        stack.getOrCreateTag().putString("exmodifier_armor_modifier_applied1", "");
                        stack.getOrCreateTag().putString("exmodifier_armor_modifier_applied2", "");
                        stack.getOrCreateTag().putBoolean("modifier_refresh", false);
                        stack.getOrCreateTag().putInt("exmodifier_armor_modifier_applied", 0);

                        RandomEntry(stack, stack.getOrCreateTag().getInt("modifier_refresh_rarity"), stack.getOrCreateTag().getInt("modifier_refresh_add"));
                    }

                }
                if (event.getEntity().level.isClientSide){
                    player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                        if (!capability.Sitemstack.isEmpty()) {
                            player.setItemSlot(event.getSlot(), capability.Sitemstack);
                            capability.Sitemstack = ItemStack.EMPTY;
                            capability.syncPlayerVariables(player);
                        }
                    });
                }
                ItemStack stack1 = event.getTo();
                for (int i = 0;true; i++)
                {
                    String  a= stack1.getOrCreateTag().getString("exmodifier_armor_modifier_applied"+i); //获取武器的词条
                    if (a.isEmpty())break; //如果没有则退出
                    Exmodifier.LOGGER.debug("Suit a" + a);
                   List<ExSuit> suits = ExSuitHandle.FindExSuit(a); //找到相关的套装
                   for (ExSuit suit:suits) { //遍历找到的套装
                        Exmodifier.LOGGER.debug("Suit found" + suit.id);
                       player.getPersistentData().putInt(suit.id, player.getPersistentData().getInt(suit.id)+1); //套装效果加一
                       Exmodifier.LOGGER.debug("Suit found amout" + player.getPersistentData().getInt(suit.id));
                       List<ModifierAttriGether> attriGethers = suit.attriGether.get(player.getPersistentData().getInt(suit.id));
                       if (attriGethers==null) continue;
                       for (ModifierAttriGether attrGether : attriGethers) { //应用套装效果
                           EntityAttrUtil.entityAddAttrTF(attrGether.attribute,attrGether.getModifier(),player,WEAR);
                       //     Exmodifier.LOGGER.debug("Suit apply attrgether" + attrGether.attribute.getDescriptionId());
                       }

                       player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> { //添加套装列表
                           if (player.getPersistentData().getInt(suit.id)>0)
                        {
                            List<String> aaa = capability.Suits;
                            if (!aaa.contains(suit.id)) aaa.add(suit.id);
                            capability.Suits = aaa;
                            capability.syncPlayerVariables(player);
                        }
                           else if (player.getPersistentData().getInt(suit.id)<=0)
                           {
                               List<String> aaa = capability.Suits;
                               if (aaa.contains(suit.id)) aaa.remove(suit.id);
                               capability.Suits = aaa;
                               capability.syncPlayerVariables(player);
                           }
                       });
                   }
                }
                ItemStack stack2 = event.getFrom();
                if (stack2.isEmpty())return; //!!!!这里有返回！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
                for (int i = 0;true; i++)
                {
                    String  a= stack2.getOrCreateTag().getString("exmodifier_armor_modifier_applied"+i);
                    if (a.isEmpty())break;

                    List<ExSuit> suits = ExSuitHandle.FindExSuit(a);

                    for (ExSuit suit:suits)
                    {
                        player.getPersistentData().putInt(suit.id, player.getPersistentData().getInt(suit.id)-1);
                        List<ModifierAttriGether> attriGethers = suit.attriGether.get(player.getPersistentData().getInt(suit.id) + 1);
                        if (attriGethers==null) continue;
                        for (ModifierAttriGether attrGether : attriGethers) { //移除套装效果
                            EntityAttrUtil.entityAddAttrTF(attrGether.attribute,attrGether.getModifier(),player,TAKE);
                      //      Exmodifier.LOGGER.debug("Suit remove attrgether" + attrGether.attribute.getDescriptionId());
                        }
                        player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                            if (player.getPersistentData().getInt(suit.id)>0)
                            {
                                List<String> aaa = capability.Suits;
                                if (!aaa.contains(suit.id)) aaa.add(suit.id);
                                capability.Suits = aaa;
                                capability.syncPlayerVariables(player);
                            }
                            else if (player.getPersistentData().getInt(suit.id)<=0)
                            {
                                List<String> aaa = capability.Suits;
                                if (aaa.contains(suit.id)) aaa.remove(suit.id);
                                capability.Suits = aaa;
                                capability.syncPlayerVariables(player);
                            }
                        });
                    }

                }
            }
        }
        @SubscribeEvent
        public static void atReload(AddReloadListenerEvent event) throws IOException {
            ModifierHandle.readConfig();
            ExSuitHandle.readConfig();

        }
    }
}
