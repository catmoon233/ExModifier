package net.exmo.exmodifier.content.helper;

import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierInstant;
import net.exmo.exmodifier.content.quality.ItemQuality;
import net.exmo.exmodifier.content.quality.ItemQualityHandle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ItemQualityHelper extends ExHelper{
    public ItemQualityHelper(ItemStack itemStack) {
        super(itemStack);
    }
    public final static String IQT = "ItemQuality";
    public final static String IQID = "id";

    public static ItemQualityHelper of(ItemStack itemStack){
        return new ItemQualityHelper(itemStack);
    }

    public int getQualityEntriesSize(){
        return getMainNbt().getList(IQT,10).size();
    }
    public boolean ValidModifierEntry()
    {
        return ValidMainNbt()&&getMainNbt().contains(IQT);
    }
    public ListTag getQualityEntriesNbt()
    {
        return getMainNbt().getList(IQT,10);
    }
    public ItemQualityHelper createQualityNbt()
    {
        createNbt();
        if (ValidModifierEntry()) return this;
        getMainNbt().put(IQT,new ListTag());

        return  this;

    }

    public ItemQualityHelper addQualityEntry(ItemQuality itemQuality,boolean addEntries,boolean addAttribute)
    {
        createNbt();
        if (!ValidMainNbt()) createMainNbt();
        createQualityNbt();
        CompoundTag tag1 = new CompoundTag();
        tag1.putString(IQID,itemQuality.Id);
        ListTag modifiersList = getQualityEntriesNbt();
        modifiersList.add(tag1);
        if (addEntries) {
            for (int i = 0; i < itemQuality.entries.size(); i++) {
                ModifierEntryHelper.of(this.itemStack).addModifierEntry(ModifierInstant.of(itemQuality.entries.get(i)).setItemQualityLock(itemQuality.cantRemoveEntry), addAttribute,true);
            }
        }
        return this;
    }
    public ItemQualityHelper removeQualityEntry(ItemQuality itemQuality,boolean removeEntries,boolean removeAttribute)
    {
        ListTag modifiersList = getQualityEntriesNbt();
        for (int i = 0; i < modifiersList.size(); i++) {
            CompoundTag tag = modifiersList.getCompound(i);
            if (tag.getString(IQID).equals(itemQuality.Id))
            {
                modifiersList.remove(i);
                if (removeEntries) {
                    for (ModifierEntry modifierEntry : itemQuality.entries) {
                        ModifierEntryHelper.of(this.itemStack).removeModifierEntryUnLock(ModifierInstant.of(modifierEntry), removeAttribute);
                    }

                }
                break;
            }
       }
        return this;
    }
    public  List<MutableComponent> getQualityEntriesTooltip()
    {
        List<MutableComponent> list = new java.util.ArrayList<>();
        ItemQualityHelper itemQualityHelper = ItemQualityHelper.of(itemStack);
        for (ItemQuality itemQuality : itemQualityHelper.getQualityEntries()) {
            if (itemQuality ==null)continue;
            list.add(Component.translatable("exmodifier.quality."+itemQuality.Id));
            if (!itemQuality.LocalDescription.isEmpty()) list.add(Component.translatable(itemQuality.LocalDescription));
        }
        return list;
    }

public List<ItemQuality> getQualityEntries()
{
    List<ItemQuality> list = new java.util.ArrayList<>();
    ListTag qualityEntriesNbt = getQualityEntriesNbt();
    for (int i = 0; i < qualityEntriesNbt.size(); i++){
        list.add(ItemQualityHandle.itemQualityMap.get(qualityEntriesNbt.getCompound(i).getString(IQID)));
    }
    return list;
}
}