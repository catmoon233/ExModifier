package net.exmo.exmodifier.content.event.main;

import net.exmo.exmodifier.content.event.MainEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

final class MainSuitRuntimeContext {
    private static final ThreadLocal<LivingHurtEvent> CURRENT_HURT_EVENT = new ThreadLocal<>();

    private MainSuitRuntimeContext() {
    }

    static void withLivingHurtEvent(LivingHurtEvent event, Runnable action) {
        CURRENT_HURT_EVENT.set(event);
        try {
            action.run();
        } finally {
            CURRENT_HURT_EVENT.remove();
        }
    }

    static void applyPendingDamageModifiers() {
        LivingHurtEvent hurtEvent = CURRENT_HURT_EVENT.get();
        if (hurtEvent == null) {
            return;
        }

        if (MainEvent.CommonEvent.hasDamageBoost) {
            hurtEvent.setAmount((float) (hurtEvent.getAmount() * MainEvent.CommonEvent.damageBoost));
            MainEvent.CommonEvent.damageBoost = 1;
            MainEvent.CommonEvent.hasDamageBoost = false;
        }

        if (MainEvent.CommonEvent.hasDamageNumber) {
            hurtEvent.setAmount((float) (hurtEvent.getAmount() + MainEvent.CommonEvent.damageNumber));
            MainEvent.CommonEvent.damageNumber = 0;
            MainEvent.CommonEvent.hasDamageNumber = false;
        }
    }
}
