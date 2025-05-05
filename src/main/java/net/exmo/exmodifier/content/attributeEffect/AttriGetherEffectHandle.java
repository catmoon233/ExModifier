package net.exmo.exmodifier.content.attributeEffect;

import com.google.gson.JsonObject;
import net.exmo.exmodifier.network.ExModifiervaV;
import net.exmo.exmodifier.util.gether.AttrGether;
import net.exmo.exmodifier.util.EntityAttrUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.*;

import static net.exmo.exmodifier.network.ExModifiervaV.MapVariables.bossBarList;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class AttriGetherEffectHandle {
    private static Map<ResourceLocation, AttriGetherEffect> attriGetherMap = new HashMap<>();
    private static List<ServerBossEvent> bossBars = new ArrayList<>();

    public static Map<ResourceLocation,AttriGetherEffect> getAttriGetherMap(){
        return attriGetherMap;
    }
    public static AttriGetherEffect getById(ResourceLocation id){
        return attriGetherMap.get(id);
    }
    public static void registerEffect(AttriGetherEffect effect){
        attriGetherMap.put(effect.getId(),effect);
    }
    public static ServerBossEvent findBossBar(UUID uuid) {
        for (Map.Entry<ServerBossEvent, UUID> entry : bossBarList) {
            if (entry.getValue().equals(uuid)) {
                return entry.getKey();
            }
        }
        return null;
    }

@SubscribeEvent
public static void init(FMLCommonSetupEvent event){
        registerEffect(new AttriGetherEffect(BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS,"§6睡眠之力",true,new ResourceLocation("exmodifier","sleep"), List.of(new AttrGether(Attributes.MAX_HEALTH, new AttributeModifier(UUID.fromString("7f7f7f7f-7f7f-7f7f-7f7f-7f7f7f7f7f7f"), "sleep", 10, AttributeModifier.Operation.ADDITION)))).setRandomAttrUUID(true));
        registerEffect(new AttriGetherEffect(BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_6,"§4暴怒",true,new ResourceLocation("exmodifier","angry"), List.of(
                new AttrGether(Attributes.MAX_HEALTH, new AttributeModifier(UUID.fromString("7f7f7f7f-7f7f-7f7f-7f7f-7f7f7f7f7f6f"), "angry", 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL)),
                new AttrGether(Attributes.ATTACK_DAMAGE, new AttributeModifier(UUID.fromString("7f7f7f7f-7f7f-7f7f-7f7f-7f7f7f7f7f5f"), "angry1", 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL)),
                new AttrGether(Attributes.MOVEMENT_SPEED, new AttributeModifier(UUID.fromString("7f7f7f7f-7f7f-7f7f-7f7f-7f7f7f7f7f7f"), "angry2", 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL))
        )).setRandomAttrUUID(true));
}
    public static ServerBossEvent genBossBar(JsonObject jsonObject) {
        return new ServerBossEvent(Component.translatable(jsonObject.get("localDescription").getAsString()), BossEvent.BossBarColor.byName(jsonObject.get("bossBarColor").getAsString()), BossEvent.BossBarOverlay.byName(jsonObject.get("bossBarOverlay").getAsString()));
    }
    public static void addOrReplaceAttriGetherEffect(AttriGetherEffectInstance attriGetherEffectInstance, Player player) {
        AttriGetherEffect attriGetherEffect = attriGetherEffectInstance.attriGetherEffect;
        player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            List<AttriGetherEffectInstance> attriGetherEffectInstances = capability.attriGetherEffectInstances;
            attriGetherEffectInstances.removeIf(attriGetherEffectInstance1 ->{
               if (attriGetherEffectInstance1.getAttriGetherEffect().equals(attriGetherEffectInstance.getAttriGetherEffect())){
                   removeAttriGetherEffect(attriGetherEffectInstance1, player);
                   removeBossBar(attriGetherEffectInstance1.getUuid());
                   return true;
               }
               return false;
            });
            attriGetherEffectInstances.add(attriGetherEffectInstance);
            addAttriGetherEffect(attriGetherEffectInstance, player);
            capability.attriGetherEffectInstances = attriGetherEffectInstances;
            capability.syncPlayerVariables(player);
        });
    }
    public static void removeBossBar(UUID uuid){
        bossBarList.removeIf(entry -> {
            if(entry.getValue().equals(uuid)){
                entry.getKey().removeAllPlayers();
                entry.getKey().setVisible(false);
                return true;
            }
            return false;
        });
    }
    public static UUID GenEffectModifierUUID(AttriGetherEffectInstance attriGetherEffectInstance,UUID uuid){
        return UUID.nameUUIDFromBytes((attriGetherEffectInstance.getUuid().toString()+uuid.toString()).getBytes());
    }
    public static void removeAttriGetherEffect(AttriGetherEffectInstance attriGetherEffectInstance, Player player) {
        for (AttrGether attriGether : attriGetherEffectInstance.getAttriGetherEffect().getAttriGethers()){
            if (attriGetherEffectInstance.randomAttrUUID){
                EntityAttrUtil.entityAddAttrTF(new AttrGether(attriGether.getAttribute(), attriGether.getModifier()).setModifierUUID(GenEffectModifierUUID(attriGetherEffectInstance, attriGether.getModifier().getId())), player, EntityAttrUtil.WearOrTake.TAKE);
            }else EntityAttrUtil.entityAddAttrTF(attriGether, player, EntityAttrUtil.WearOrTake.TAKE);
        }

    }
    public static void addEffectAttriGether(AttriGetherEffectInstance attriGetherEffectInstance, Player player) {
        for (AttrGether attriGether : attriGetherEffectInstance.getAttriGetherEffect().getAttriGethers())
        {
            if (attriGetherEffectInstance.randomAttrUUID){
            EntityAttrUtil.entityAddAttrTF(new AttrGether(attriGether.getAttribute(), attriGether.getModifier()).setModifierUUID(GenEffectModifierUUID(attriGetherEffectInstance,attriGether.getModifier().getId())), player, EntityAttrUtil.WearOrTake.WEAR);
        }
            else EntityAttrUtil.entityAddAttrTF(attriGether, player, EntityAttrUtil.WearOrTake.WEAR);
        }
    }
    public static int getPlayerAttriGetherAmplifier(Player player,AttriGetherEffect attriGetherEffect) {
        return player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).map(data -> data.attriGetherEffectInstances.stream().filter(attriGetherEffectInstance -> attriGetherEffectInstance.getAttriGetherEffect().equals(attriGetherEffect)).mapToInt(attriGetherEffectInstance -> attriGetherEffectInstance.getAmplifier() + 1).sum()).orElse(0);
    }
    public static void addAttriGetherEffect(AttriGetherEffectInstance attriGetherEffectInstance, Player player) {
        if (player.level().isClientSide)return;
        AttriGetherEffect attriGetherEffect = attriGetherEffectInstance.attriGetherEffect;
        ServerBossEvent bossBar = new ServerBossEvent(Component.translatable(attriGetherEffect.getBossBarName()), attriGetherEffect.getBossBarColor(),attriGetherEffect.getBossBarOverlay());
        player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            List<AttriGetherEffectInstance> attriGetherEffectInstances = capability.attriGetherEffectInstances;
            attriGetherEffectInstances.add(attriGetherEffectInstance);
            capability.attriGetherEffectInstances = attriGetherEffectInstances;
            capability.syncPlayerVariables(player);
        });
        bossBar.setVisible(attriGetherEffectInstance.showBossBar);
        bossBar.addPlayer((ServerPlayer) player);
        bossBarList.add(new AbstractMap.SimpleEntry<>(bossBar, attriGetherEffectInstance.uuid));
        addEffectAttriGether(attriGetherEffectInstance, player);
    }
    @Mod.EventBusSubscriber
    public static class CommonEvents{
        @SubscribeEvent
        public static void ResetBossBars(PlayerEvent.PlayerLoggedInEvent event){
            Player player = (Player) event.getEntity();
            player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                List<AttriGetherEffectInstance> attriGetherEffectInstances = capability.attriGetherEffectInstances;
                for (AttriGetherEffectInstance attriGetherEffectInstance : attriGetherEffectInstances){
                    AttriGetherEffect attriGetherEffect = attriGetherEffectInstance.attriGetherEffect;
                    removeBossBar(attriGetherEffectInstance.getUuid());
                    ServerBossEvent bossBar = new ServerBossEvent(Component.translatable(attriGetherEffect.getBossBarName()), attriGetherEffect.getBossBarColor(),attriGetherEffect.getBossBarOverlay());
                    bossBar.setVisible(attriGetherEffectInstance.showBossBar);
                    bossBar.addPlayer((ServerPlayer) player);
                    bossBarList.add(new AbstractMap.SimpleEntry<>(bossBar, attriGetherEffectInstance.uuid));
                }
            });
        }
        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
//            Player player1 = event.player;
//            if (!player1.level().isClientSide()) { // 确保只在服务端运行
//                if (event.phase == TickEvent.Phase.END) {
//                    boolean bossBarVisible = false;
//                    for (ServerBossEvent bossBar : bossBars) {
//                        if (bossBar.getPlayers().contains(((ServerPlayer) player1))) {
//                            bossBarVisible = true;
//                            float hp = player1.getHealth() / player1.getMaxHealth();
//
//                            if (hp < 0.5f) {
//                                bossBar.setProgress(hp); // 这里只是一个例子，你可以根据需要改变这个值
//                                bossBar.setVisible(true);
//
//                            } else {
//                                bossBar.setVisible(false);
//
//                            }
//                        }
//                    }
//                    if (!bossBarVisible) {
//                        ServerBossEvent e = new ServerBossEvent(Component.literal("\u00a74生命值"), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
//                        e.addPlayer((ServerPlayer) player1);
//                        bossBars.add(e);
//
//                    }
//                }
//            }
        }

        @SubscribeEvent
        public static void onPlayerDig(PlayerSleepInBedEvent event) {
       //     addOrReplaceAttriGetherEffect(new AttriGetherEffectInstance(1200, getById(new ResourceLocation("exmodifier","sleep")), 1, true), event.getEntity());
        }
        @SubscribeEvent
        public static void onPlayerKill(LivingDeathEvent event) {
            if (event.getEntity() instanceof Player player) {
                player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                        for (AttriGetherEffectInstance attriGetherEffectInstance : capability.attriGetherEffectInstances){
                            removeBossBar(attriGetherEffectInstance.getUuid());
                        }

                });
            }

            if (event.getSource().getEntity() instanceof Player player) {
                //    if (getPlayerAttriGetherAmplifier(player, getById(new ResourceLocation("exmodifier","angry")))<10) addAttriGetherEffect(new AttriGetherEffectInstance(200, getById(new ResourceLocation("exmodifier", "angry")), 1, true), player);
            }
        }
        @SubscribeEvent
        public static void PlayerTick(TickEvent.PlayerTickEvent event) {
            Player player = event.player;
            if (player.level().isClientSide)return;
            player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                List<AttriGetherEffectInstance> attriGetherEffectInstances = capability.attriGetherEffectInstances;
                List<AttriGetherEffectInstance> toMove = new ArrayList<>();
                for (AttriGetherEffectInstance _set : attriGetherEffectInstances) {
                    _set.setDuration(_set.getDuration() - 1);
                    ServerBossEvent bossBar = findBossBar(_set.getUuid());
                    if (bossBar != null) {
                        bossBar.setProgress(_set.getDuration() / (float) _set.getStartDuration());
                    }
                    if (_set.getDuration() <= 0) {
                        toMove.add(_set);
                        if (bossBar != null){
                            removeBossBar(_set.getUuid());
                            bossBar.setVisible(false);
                            bossBar.removeAllPlayers();

                        }
                    }


                }
               for (AttriGetherEffectInstance _effect : toMove) {
                   attriGetherEffectInstances.remove(_effect);
                   removeAttriGetherEffect(_effect, player);
               }
                capability.syncPlayerVariables(player);
            });

        }
    }
}