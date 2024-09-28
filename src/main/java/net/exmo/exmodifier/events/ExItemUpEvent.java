package net.exmo.exmodifier.events;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public class ExItemUpEvent extends Event {
    public ItemStack stack;
    public int nowLevel;
    public int beforeLevel;
    public int needXpUp;
    public String LevelId;

    public ExItemUpEvent(ItemStack stack, int nowLevel, int beforeLevel, int needXpUp, String levelId) {
        this.stack = stack;
        this.nowLevel = nowLevel;
        this.beforeLevel = beforeLevel;
        this.needXpUp = needXpUp;
        LevelId = levelId;
    }
}