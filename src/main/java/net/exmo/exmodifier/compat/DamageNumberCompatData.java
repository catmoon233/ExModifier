package net.exmo.exmodifier.compat;

import cc.xypp.damage_number.data.DamageListItem;
import com.google.common.cache.Cache;
import org.apache.commons.lang3.tuple.Pair;

public class DamageNumberCompatData {
    public static Cache<Long, Integer> cache = com.google.common.cache.CacheBuilder.newBuilder().build();
    public static int lastColor = -1;

    public static Integer findNeareast(long key) {
        if (cache.size() == 0) {
            return null;
        }

        // 初始化最小差值和对应的值
        long minDiff = Long.MAX_VALUE;
        Integer nearestValue = null;

        // 遍历缓存中的所有键值对
        for (Long cachedKey : cache.asMap().keySet()) {
            long diff = Math.abs(cachedKey - key);
            if (diff < minDiff) {
                minDiff = diff;
                nearestValue = cache.getIfPresent(cachedKey);
            }
        }

        // 返回最接近的值
        return nearestValue;
    }

    // 删除与指定 key 最接近的缓存项
    public static void removeNearestKey(long key) {
        // 调用 findNearest 方法找到最接近的键
        Long nearestKey = null;
        long minDiff = Long.MAX_VALUE;

        for (Long cachedKey : cache.asMap().keySet()) {
            long diff = Math.abs(cachedKey - key);
            if (diff < minDiff) {
                minDiff = diff;
                nearestKey = cachedKey;
            }
        }

        // 如果找到了最接近的键，则从缓存中移除
        if (nearestKey != null) {
            cache.invalidate(nearestKey);
        }
    }
}