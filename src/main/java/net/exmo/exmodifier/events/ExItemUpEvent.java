package net.exmo.exmodifier.events;

import net.exmo.exmodifier.core.component.ItemLevelState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public final class ExItemUpEvent extends Event {
    private final ItemStack stack;
    private final LivingEntity entity;
    private final ItemLevelState before;
    private final ItemLevelState after;

    public ExItemUpEvent(ItemStack stack, LivingEntity entity, ItemLevelState before, ItemLevelState after) {
        this.stack = stack;
        this.entity = entity;
        this.before = before;
        this.after = after;
    }

    public ItemStack stack() { return stack; }
    public LivingEntity entity() { return entity; }
    public ItemLevelState before() { return before; }
    public ItemLevelState after() { return after; }
}
