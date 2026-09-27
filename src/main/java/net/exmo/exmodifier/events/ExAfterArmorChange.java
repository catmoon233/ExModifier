package net.exmo.exmodifier.events;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public final class ExAfterArmorChange extends Event {
    private final LivingEntity entity;
    private final EquipmentSlot slot;
    private final ItemStack from;
    private final ItemStack to;

    public ExAfterArmorChange(LivingEntity entity, EquipmentSlot slot, ItemStack from, ItemStack to) {
        this.entity = entity;
        this.slot = slot;
        this.from = from;
        this.to = to;
    }

    public LivingEntity entity() { return entity; }
    public EquipmentSlot slot() { return slot; }
    public ItemStack from() { return from; }
    public ItemStack to() { return to; }
}
