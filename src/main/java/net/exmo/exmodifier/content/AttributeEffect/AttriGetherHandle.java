package net.exmo.exmodifier.content.AttributeEffect;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Mod.EventBusSubscriber
public class AttriGetherHandle {
    Map<String, AttriGetherEffect> attriGetherMap = new HashMap<>();
    private static List<ServerBossEvent> bossBars = new ArrayList<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player1 = event.player;
        if (!player1.level().isClientSide()) { // 确保只在服务端运行
            if (event.phase == TickEvent.Phase.END) {
                boolean bossBarVisible = false;
                for (ServerBossEvent bossBar : bossBars) {
                    if (bossBar.getPlayers().contains(((ServerPlayer) player1))) {
                        bossBarVisible = true;
                        float hp = player1.getHealth() / player1.getMaxHealth();

                        if (hp < 0.5f) {
                            bossBar.setProgress(hp); // 这里只是一个例子，你可以根据需要改变这个值
                            bossBar.setVisible(true);

                        } else {
                            bossBar.setVisible(false);

                        }
                    }
                }
                if (!bossBarVisible) {
                    ServerBossEvent e = new ServerBossEvent(Component.literal("\u00a74生命值"), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
                    e.addPlayer((ServerPlayer) player1);
                    bossBars.add(e);

                }
            }
        }
    }
}