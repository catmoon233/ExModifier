package net.exmo.exmodifier.events;

import net.exmo.exmodifier.content.level.ItemLevel;
import net.exmo.exmodifier.content.level.ItemLevelInstant;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public class ExItemUpEvent extends Event {
    public ItemStack stack;
    public int nowLevel;
    public LivingEntity entity;
    public int beforeLevel;
    public int needXpUp;
    public ItemLevelInstant LevelId;

    public ExItemUpEvent(ItemStack stack, int nowLevel, int beforeLevel, int needXpUp, ItemLevelInstant itemLevel) {
        this.stack = stack;
        this.nowLevel = nowLevel;
        this.beforeLevel = beforeLevel;
        this.needXpUp = needXpUp;
        LevelId = itemLevel;
    }

    public ExItemUpEvent(ItemStack stack, int nowLevel, LivingEntity entity, int beforeLevel, int needXpUp, ItemLevelInstant levelId) {
        this.stack = stack;
        this.nowLevel = nowLevel;
        this.entity = entity;
        this.beforeLevel = beforeLevel;
        this.needXpUp = needXpUp;
        LevelId = levelId;
    }
}