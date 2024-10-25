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
import net.minecraft.world.item.ItemStack;

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
    public void createModifierEntryNbt()
    {
        createNbt();
        if (ValidModifierEntry()) return;
        getMainNbt().put(MES,new ListTag());
    }

    public ModifierEntryHelper(ItemStack itemStack) {
        super(itemStack);
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
                    modifierEntries.add(new ModifierInstant(modifierEntry, level));
                }
                }
            }
            }
            return modifierEntries;


    }
    public void addModifierEntry(ModifierInstant modifierInstant,boolean addAttribute)
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

    }
    public void removeModifierEntry(ModifierInstant modifierInstant,boolean removeAttribute)
    {
        createNbt();
        if (!ValidMainNbt()) return;
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

