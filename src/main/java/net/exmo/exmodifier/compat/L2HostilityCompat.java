package net.exmo.exmodifier.compat;

import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import dev.xkmc.l2hostility.content.capability.player.PlayerDifficulty;
import dev.xkmc.l2hostility.init.L2Hostility;
import net.exmo.exmodifier.Exmodifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.ModList;

import java.util.concurrent.atomic.AtomicInteger;

public class L2HostilityCompat {
    public static boolean isLoad(){
        return ModList.get().isLoaded(L2Hostility.MODID);

    }
    public static int getDifficulty(LivingEntity livingEntity){
        if(isLoad()){
            if (livingEntity==null){
                Exmodifier.LOGGER.Logger.error("L2HostilityCompat getDifficulty LivingEntity is null");
                return 0;
            };
            AtomicInteger di = new AtomicInteger();
            LazyOptional<MobTraitCap> capability = livingEntity.getCapability(MobTraitCap.CAPABILITY);
            capability.ifPresent(e-> di.set(e.getLevel()));
            return di.get();
        }
        return 0;
    }
    public static int getPlayerDifficulty(Player player){
        if(isLoad()){
            AtomicInteger di = new AtomicInteger();
            var capability = player.getCapability(PlayerDifficulty.CAPABILITY);
            capability.ifPresent(e-> di.set(e.getLevel().level));
            return di.get();
        }
        return 0;
    }

}
