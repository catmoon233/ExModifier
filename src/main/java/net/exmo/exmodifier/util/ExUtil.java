package net.exmo.exmodifier.util;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public class ExUtil {
    static HashMap<Item, String> _IS = new HashMap<>();

    public static String getItemID(Item item) {
        if (_IS.containsKey(item)) {
            return _IS.get(item);
        } else {
            String id = ForgeRegistries.ITEMS.getKey(item).toString();
            _IS.put(item, id);
            return id;
        }

    }

    public static String getItemID(RegistryObject<Item> registryObject) {
        return registryObject.getKey().toString();

    }
    public static String getItemID(ItemStack stack) {
        return getItemID(stack.getItem());

    }

    static Map<Attribute,String> _AS = new HashMap<>();
    public static String getAttributeID(Attribute attribute){
        if (_AS.containsKey(attribute)){
            return _AS.get(attribute);
        }else
        {
            String string = ForgeRegistries.ATTRIBUTES.getKey(attribute).toString();
            _AS.put(attribute, string);
            return string;
        }

    }

    public static String getAttributeID(RegistryObject<Attribute> attribute){
        return getAttributeID(attribute.get());
    }
}
