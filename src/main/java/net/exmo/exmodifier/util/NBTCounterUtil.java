package net.exmo.exmodifier.util;

import net.minecraft.nbt.CompoundTag;

public class NBTCounterUtil {
    /**
     * 处理带有冷却的计数器逻辑
     * @param tag 物品或玩家之类的nbt
     * @param threshold 触发阈值
     * @param counterKey NBT存储键
     * @param onTrigger 达到阈值时的回调
     */
    public static void handleCounter(CompoundTag tag, int threshold, String counterKey, Runnable onTrigger) {
        int current = tag.getInt(counterKey);

        if (current >= threshold) {
            tag.putInt(counterKey, 0);
            onTrigger.run();
        }

        tag.putInt(counterKey, current + 1);
    }

}
