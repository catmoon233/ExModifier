package net.exmo.exmodifier.content.selected;

import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
public abstract class BaseItemSelected<T> {
    public static Map<Integer,Object> IDS ;

    public static Object getValue(int id){
        return IDS.get(id);
    }
    public static int getId(Object o){
        for (int i = 0; i < IDS.size(); i++) {
            if (IDS.get(i) == o)return i;
        }
        return -1;
    }


}
