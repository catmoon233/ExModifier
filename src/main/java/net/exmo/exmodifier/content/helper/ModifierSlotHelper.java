package net.exmo.exmodifier.content.helper;

import net.exmo.exmodifier.content.slot.ModifierSlot;
import net.exmo.exmodifier.content.slot.ModifierSlotHandle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ModifierSlotHelper extends ExHelper {
    public ModifierSlotHelper(ItemStack itemStack) {
        super(itemStack);
    }
    public static ModifierSlotHelper of (ItemStack itemStack) {
        return new ModifierSlotHelper(itemStack);
    }
    public static final String STL = "SlotList";
    public static final String SID = "id";
    public boolean validList(){
        return ValidMainNbt() && getMainNbt().contains(STL);
    }
    public ListTag getList() {
        if (validList()){
            return getMainNbt().getList(STL,10);
        }
        return new ListTag();
    }

    public List<ModifierSlot> getModifierSlotList() {
        if (validList()){
            var tag = getMainNbt();
            var list = tag.getList(STL, 10);
            var relist = new ArrayList<ModifierSlot>();
            for (int i = 0; i < list.size(); i++){
                var slot = ModifierSlotHandle.getSlot(ResourceLocation.tryParse(list.getCompound(i).getString(SID)));
                if (slot!=null)relist.add(slot);
            }
            return relist;
        }
        return new ArrayList<>();
    }
    public List<String> getSlotListId() {
        if (validList()){
            var tag = getMainNbt();
            var list = tag.getList(STL, 10);
            var relist = new ArrayList<String>();
            for (int i = 0; i < list.size(); i++){
                relist.add(list.getCompound(i).getString(SID));
            }
            return relist;
        }
        return new ArrayList<>();
    }
    public boolean containSlot(ModifierSlot slot){
        for (int i = 0; i < getList().size(); i++) {
            if (getList().getCompound(i).getString(SID).equals(ModifierSlotHandle.getKey(slot).toString())){
                return true;
            }
        }
        return false;
    }
    public boolean containSlot(String slot){
        for (int i = 0; i < getList().size(); i++) {
            if (getList().getCompound(i).getString(SID).equals(slot)){
                return true;
            }
        }
        return false;
    }
    public ModifierSlotHelper addSlot(ModifierSlot slot){
        if (!ValidMainNbt())createMainNbt();
        if (!validList()){
            createNbt();
        }
        var ls = getList();
        String id = ModifierSlotHandle.getKey(slot).toString();
        if (!containSlot(id)){
            CompoundTag e = new CompoundTag();

            e.putString(SID,id);
            ls.add(e);
            getMainNbt().put(STL,ls);
        }
        return this;
    }
}
