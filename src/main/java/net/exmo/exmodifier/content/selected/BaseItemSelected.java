package net.exmo.exmodifier.content.selected;

import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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

    private List<String> onlyTypes;
    private List<String> onlyTags;
    private List<String> onlyItems;

    public BaseItemSelected() {
    }
    public boolean contain(ItemStack stack){
        return containType(stack) && containTag(stack) && containItem(stack);
    }
    // Getter and Setter for onlyTypes
    public Optional<List<String>> getOnlyTypes() {
        return Optional.ofNullable(onlyTypes);
    }

    public T setOnlyTypes(List<String> onlyTypes) {
        this.onlyTypes = onlyTypes;
        return (T) this;
    }

    // Getter and Setter for onlyTags
    public Optional<List<String>> getOnlyTags() {
        return Optional.ofNullable(onlyTags);
    }

    public T setOnlyTags(List<String> onlyTags) {
        this.onlyTags = onlyTags;
        return (T) this;
    }

    // Getter and Setter for onlyItems
    public Optional<List<String>> getOnlyItems() {
        return Optional.ofNullable(onlyItems);
    }

    public T setOnlyItems(List<String> onlyItems) {
        this.onlyItems = onlyItems;
        return (T) this;
    }
    public  boolean containTag(ItemStack stack){
        if (getOnlyTags().isEmpty())return true;
        for (String tag : getOnlyTags().get() ){
            if (stack.is(ItemTags.create(new ResourceLocation(tag))))return true;
        }
        return false;
    }
    public  boolean containItem(ItemStack stack){
        if (getOnlyItems().isEmpty())return true;
        for (String item : getOnlyItems().get() ){
            if (stack.is(ItemTags.create(new ResourceLocation(item))))return true;
        }
        return false;
    }
    public  boolean containType(ItemStack stack){
        if (getOnlyTypes().isEmpty())return true;
        return ModifierEntry.containItemTypes(stack, getOnlyTypes().get().stream()
                .map(ModifierEntry::StringToType)
                .toList()
        );
    }
}
