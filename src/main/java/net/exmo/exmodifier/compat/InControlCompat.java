package net.exmo.exmodifier.compat;

import mcjty.incontrol.InControl;
import mcjty.incontrol.rules.RulesManager;
import mcjty.incontrol.rules.SpawnRule;
import mcjty.incontrol.rules.support.SpawnWhen;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.fml.ModList;

import java.util.List;

public class InControlCompat {
    public static boolean isLoad(){
        return ModList.get().isLoaded(InControl.MODID);

    }
    public static int getDifficulty(EntityJoinLevelEvent event){
        if(isLoad()){
            List<SpawnRule> filteredRules = RulesManager.getFilteredRules(event.getLevel(), SpawnWhen.ONJOIN);
            List<SpawnRule> list = filteredRules.stream().filter(rule -> rule.match(event)).toList();
        //    list.
        }
        return 0;
    }
}
