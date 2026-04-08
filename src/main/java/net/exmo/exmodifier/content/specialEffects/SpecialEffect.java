package net.exmo.exmodifier.content.specialEffects;

import net.exmo.exmodifier.events.LivingPlayerSwimEvent;
import net.exmo.exmodifier.events.LivingSwingEvent;
import net.exmo.exmodifier.util.MobEffectInstantBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.level.BlockEvent;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public  class SpecialEffect {
    private final String id;
    private final Consumer<LivingEntity> livingEntityConsumer;
    private List<MobEffectInstantBuilder> mobEffectInstantBuilders;

    public void onTick(TickEvent.PlayerTickEvent event) {

    }
    public void attackEntity(LivingHurtEvent event){

    }

    public void jump(LivingEvent.LivingJumpEvent event) {

    }
    public void projectileHit(ProjectileImpactEvent event) {

    }
    public void playerSwing(LivingSwingEvent event) {

    }
    public void playerCrit(CriticalHitEvent event) {

    }
    public void digger(BlockEvent.BreakEvent event) {

    }
    public void hurt(LivingHurtEvent event){

    }
    public void onDeath(LivingDeathEvent event) {

    }
    public void onKill(LivingDeathEvent event) {

    }
    public void onUseItem(LivingEntityUseItemEvent event) {

    }



    public SpecialEffect(String id, Consumer<LivingEntity> livingEntityConsumer) {
        this.id = id;
        this.livingEntityConsumer = livingEntityConsumer;
    }

    public String id() {
        return id;
    }

    public Consumer<LivingEntity> livingEntityConsumer() {
        return livingEntityConsumer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SpecialEffect) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.livingEntityConsumer, that.livingEntityConsumer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, livingEntityConsumer);
    }

    @Override
    public String toString() {
        return "SpecialEffect[" +
                "id=" + id + ", " +
                "livingEntityConsumer=" + livingEntityConsumer + ']';
    }


}