package net.exmo.exmodifier.compat;

import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.exmo.exmodifier.events.ExCanRefineEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class SlashBladeCompat {
    @SubscribeEvent
    public static void onRefine(ExCanRefineEvent event){
        if (ModList.get().isLoaded("slashblade")){
            if (event.originalItemStack.getItem() instanceof ItemSlashBlade itemSlashBlade){
                if (event.targetItemStack.getItem() instanceof ItemSlashBlade itemSlashBlade1){
                    event.canRefine = (event.originalItemStack.getTag().getCompound("bladeState").getString("translationKey").equals(event.targetItemStack.getTag().getCompound("bladeState").getString("translationKey")));
                }
            }
        }
    }
}
