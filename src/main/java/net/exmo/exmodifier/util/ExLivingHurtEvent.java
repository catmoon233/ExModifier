package net.exmo.exmodifier.util;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Cancelable
@Mod.EventBusSubscriber
public class ExLivingHurtEvent extends LivingEvent {
    public static float  amountA = 0 ;
    public static float  amountB = 0 ;
    public static float  MutiAmount = 1 ;
    public static float  TotalAmount = 1 ;
    private final DamageSource source;
    private final LivingHurtEvent livingHurtEvent;
    private float amount;
    public ExLivingHurtEvent(LivingEntity entity, DamageSource source, float amount, LivingHurtEvent livingHurtEvent)
    {
        super(entity);
        this.source = source;
        this.amount = amount;
        this.livingHurtEvent = livingHurtEvent;
    }

    public DamageSource getSource() { return source; }

    public float getAmount() { return amount; }

    public void setAmount(float amount) { this.amount = amount; }


    public ExLivingHurtEvent addAmount(float amount) {
        ExLivingHurtEvent.amountA += amount;
        return this;
    }
    public ExLivingHurtEvent addAmountB(float amount) {
        ExLivingHurtEvent.amountB += amount;
        return this;
    }

    public ExLivingHurtEvent addMutiAmount(float amount) {
        ExLivingHurtEvent.MutiAmount += amount;
        return this;
    }
    public static float getAmountA() {
        return ExLivingHurtEvent.amountA;
    }
    public static float getMutiAmountA() {
        return ExLivingHurtEvent.MutiAmount;
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void LivingHurtEvent(LivingHurtEvent event) {
        MinecraftForge.EVENT_BUS.post(new ExLivingHurtEvent(event.getEntity(), event.getSource(), event.getAmount(),  event));
        event.setAmount(Math.max(ExLivingHurtEvent.getAmountA()+event.getAmount(),0));
    }
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void LivingHurtEventA(LivingHurtEvent event) {
        event.setAmount( ExLivingHurtEvent.getMutiAmountA()*event.getAmount()*getTotalAmount()-ExLivingHurtEvent.getAmountB());

    }

    private static float getAmountB() {
        return ExLivingHurtEvent.amountB;
    }

    public static float getTotalAmount() {
        return TotalAmount;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void LivingHurtEventB(LivingHurtEvent event) {
        ExLivingHurtEvent.amountA = 0;
        ExLivingHurtEvent.MutiAmount = 1;
        ExLivingHurtEvent.TotalAmount = 1;
        ExLivingHurtEvent.amountB = 0;
        if (event.isCanceled()){
            event.setCanceled(true);

        }

    }

    public LivingHurtEvent getLivingHurtEvent() {
        return livingHurtEvent;
    }
}
