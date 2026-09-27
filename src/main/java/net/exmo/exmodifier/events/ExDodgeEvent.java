package net.exmo.exmodifier.events;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public final class ExDodgeEvent extends Event implements ICancellableEvent {
    public enum Result { MISS, HIT }

    private final LivingEntity victim;
    private final LivingEntity attacker;
    private final double chance;
    private Result result;

    public ExDodgeEvent(LivingEntity victim, LivingEntity attacker, double chance, Result result) {
        this.victim = victim;
        this.attacker = attacker;
        this.chance = chance;
        this.result = result;
    }

    public LivingEntity victim() { return victim; }
    public LivingEntity attacker() { return attacker; }
    public double chance() { return chance; }
    public Result result() { return result; }
    public void result(Result result) { this.result = result; }
}
