package net.exmo.exmodifier.util;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/** Per-entity named cooldown clocks, keyed by UUID so Player references do not leak. */
public final class TickCooldown {
    private static final Map<UUID, Map<String, Integer>> COOLDOWNS = new HashMap<>();

    private TickCooldown() {}

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        Map<String, Integer> map = COOLDOWNS.get(event.getEntity().getUUID());
        if (map == null || map.isEmpty()) return;
        Iterator<Map.Entry<String, Integer>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> entry = it.next();
            int next = entry.getValue() - 1;
            if (next <= 0) it.remove();
            else entry.setValue(next);
        }
    }

    public static boolean ready(LivingEntity entity, String key) {
        if (entity == null) return false;
        Map<String, Integer> map = COOLDOWNS.get(entity.getUUID());
        return map == null || map.getOrDefault(key, 0) <= 0;
    }

    public static boolean tryUse(LivingEntity entity, String key, int ticks) {
        if (entity == null) return false;
        Map<String, Integer> map = COOLDOWNS.computeIfAbsent(entity.getUUID(), id -> new HashMap<>());
        if (map.getOrDefault(key, 0) > 0) return false;
        map.put(key, Math.max(1, ticks));
        return true;
    }
}
