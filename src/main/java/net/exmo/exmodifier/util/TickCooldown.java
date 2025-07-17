package net.exmo.exmodifier.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber
public class TickCooldown {
    public static final Map<Player,Map<String, Integer>> cooldowns = new HashMap<>();
    public static final List<String> displayCooldowns =new ArrayList<>();
    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (player.level().isClientSide)return;
        if (cooldowns.containsKey(player)){
            for (Map.Entry<String, Integer> entry : cooldowns.get(player).entrySet()) {
                if (entry.getValue() > 0) {
                    entry.setValue(entry.getValue() - 1);
                }
                if (entry.getValue() ==1){
                    if (displayCooldowns.contains(entry.getKey())){
                        if (player instanceof ServerPlayer serverPlayer){
                            serverPlayer.sendSystemMessage(Component.translatable("exmodifier.cooldown.end", Component.translatable("cooldown."+entry.getKey())));
                            serverPlayer.playNotifySound(SoundEvents.DISPENSER_LAUNCH, SoundSource.PLAYERS, 0.55f, 1f);
                        }
                    }
                }
            }
        }
    }
    public static boolean isCooldown(Player player, String key) {
        if (cooldowns.containsKey(player)) {
            if (cooldowns.get(player).containsKey(key)) {
                return cooldowns.get(player).get(key) > 0;
            }
        }
        return false;
    }
    public static boolean cooldownOk(Player player, String key, int cooldown) {
        if (cooldowns.containsKey(player)) {
            if (cooldowns.get(player).containsKey(key)) {
                if (cooldowns.get(player).get(key) <= 0) {
                    cooldowns.get(player).put(key, cooldown);
                    return true;
                }
            } else {
                cooldowns.get(player).put(key, cooldown);
            }
        }else {
            cooldowns.put(player, new HashMap<>());
            cooldowns.get(player).put(key, cooldown);
        }
        return false;
    }
    public static boolean cooldownOkDis(Player player, String key, int cooldown) {
        if (cooldowns.containsKey(player)) {
            if (cooldowns.get(player).containsKey(key)) {
                if (cooldowns.get(player).get(key) <= 0) {
                    cooldowns.get(player).put(key, cooldown);
                    return true;
                }
            } else {
                cooldowns.get(player).put(key, cooldown);
            }
        }else {
            cooldowns.put(player, new HashMap<>());
            cooldowns.get(player).put(key, cooldown);
            if(!displayCooldowns.contains(key)) displayCooldowns.add(key);
        }
        return false;
    }
}
