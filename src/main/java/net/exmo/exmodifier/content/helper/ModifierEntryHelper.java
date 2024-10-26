package net.exmo.exmodifier.content.helper;

import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.content.modifier.ModifierInstant;
import net.exmo.exmodifier.util.CuriosUtil;
import net.exmo.exmodifier.util.ItemAttrUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.ArrayList;
import java.util.List;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.CommonEvent.*;

public class ModifierEntryHelper extends ExHelper {
    private static final String MES = "ModifierEntry";
    private static final String MEID = "EntryId";

    public boolean ValidModifierEntry()
    {
        return ValidMainNbt()&&getMainNbt().contains(MES);
    }
    public ModifierEntryHelper createModifierEntryNbt()
    {
        createNbt();
        if (ValidModifierEntry()) return this;
        getMainNbt().put(MES,new ListTag());
        return  this;

    }
    public static int getLivingEntityEntryLevel(String entryID, LivingEntity e){
        int level = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()){
            ItemStack itemBySlot = e.getItemBySlot(slot);
            if (itemBySlot.isEmpty())continue;
            if (!CuriosUtil.isCuriosItem(itemBySlot)) {
                ModifierEntryHelper modifierEntryHelper = new ModifierEntryHelper(itemBySlot);
               level+= modifierEntryHelper.getModifierEntryLevel(entryID);

            }
        }
        return level;
    }
    public int getModifierEntryLevel(String entryID){
        ListTag modifierEntriesNbt = getModifierEntriesNbt();
        for (int i = 0; i < modifierEntriesNbt.size(); i++){
            if (modifierEntriesNbt.getCompound(i).getString(MEID).equals(entryID)) return modifierEntriesNbt.getCompound(i).getInt("Level");
        }
        return 0;
    }
    public ModifierEntryHelper(ItemStack itemStack) {
        super(itemStack);
    }
    public int getModifierEntriesSize(){
        return getMainNbt().getList(MES,10).size();
    }

    public ListTag getModifierEntriesNbt()
    {
        return getMainNbt().getList(MES,10);
    }
    public List<ModifierInstant> getModifierEntries()
    {
        List<ModifierInstant> modifierEntries = new ArrayList<>();
        if (ValidMainNbt()) {
            CompoundTag tag = getMainNbt();
            if (tag.contains(MES)) {
                ListTag modifiersList = tag.getList(MES, 10);
                for (int i = 0; i < modifiersList.size(); i++){
                CompoundTag tag1 = modifiersList.getCompound(i);
                ModifierEntry modifierEntry = ModifierHandle.modifierEntryMap.get(tag1.getString(MEID));
                if (modifierEntry!=null)
                {
                    int level =1;
                    if (tag1.contains("Level"))level = tag1.getInt("Level");
                    tag1.remove("Level");
                    tag1.remove(MEID);
                    modifierEntries.add(new ModifierInstant(modifierEntry, level).setData(tag1));
                }
                }
            }
            }
            return modifierEntries;


    }
    public ModifierEntryHelper addModifierEntry(ModifierInstant modifierInstant,boolean addAttribute)
    {
        createNbt();
        if (!ValidMainNbt()) createMainNbt();
        createModifierEntryNbt();
        CompoundTag tag1 = new CompoundTag();
        tag1.putString(MEID,modifierInstant.getModifierEntry().id);
        tag1.putInt("Level",modifierInstant.getLevel());
        getModifierEntriesNbt().add(tag1);
        if (addAttribute){
            List<ModifierAttriGether> addTo = selectModifierAttributes(modifierInstant.getModifierEntry(),itemStack);
            if (CuriosUtil.isCuriosItem(this.itemStack))applyModifiersCurios(itemStack, addTo, CuriosUtil.getSlotsFromItemstack(itemStack));
            else applyModifiers(itemStack,addTo,getEquipmentSlot(itemStack));
        }
        return this;
    }
    public ModifierEntryHelper removeModifierEntry(ModifierInstant modifierInstant,boolean removeAttribute)
    {
        createNbt();
        if (!ValidMainNbt()) return this;
        ListTag modifiersList = getModifierEntriesNbt();
        for (int i = 0; i < modifiersList.size(); i++){
            CompoundTag tag1 = modifiersList.getCompound(i);
            if (tag1.getString(MEID).equals(modifierInstant.getModifierEntry().id))
            {
                modifiersList.remove(i);
                break;
            }
        }
        if (removeAttribute){
            for (ModifierAttriGether modifierAttriGether : modifierInstant.getModifierEntry().attriGether){
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    ItemAttrUtil.removeAttributeModifierNoAmout(itemStack, modifierAttriGether.attribute, modifierAttriGether.modifier, slot);
                }
            }
        }
        return this;
    }
    public static ModifierEntry getEntry(String entryName)
    {
        return ModifierHandle.modifierEntryMap.get(entryName);
    }
    public static List<ModifierAttriGether> getEntryAttriGether(ModifierEntry entry)
    {
        return entry.attriGether;
    }
    public static List<ModifierAttriGether> getEntryAttriGether(String entryID)
    {
        return getEntry(entryID).attriGether;
    }
}

