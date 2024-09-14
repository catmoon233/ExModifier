package net.exmo.exmodifier.events;

import net.exmo.exmodifier.content.suit.ExSuit;
import net.exmo.exmodifier.util.EntityAttrUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class ExSuitApplyOnChangeEvent extends PlayerEvent {
    public ExSuit exSuit;
    public int level;
    public EntityAttrUtil.WearOrTake wearOrTake;
    public ExSuitApplyOnChangeEvent(Player entity, ExSuit exSuit, int level, EntityAttrUtil.WearOrTake wearOrTake) {
        super(entity);
        this.exSuit = exSuit;
        this.level = level;
        this.wearOrTake = wearOrTake;
    }
}
