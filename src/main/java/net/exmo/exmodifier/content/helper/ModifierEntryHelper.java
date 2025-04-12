package net.exmo.exmodifier.content.helper;

import com.google.common.collect.Multimap;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.modifier.ModifierAttriGether;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.content.modifier.ModifierInstant;

import net.exmo.exmodifier.content.type.ExType;
import net.exmo.exmodifier.content.type.ItemType;
import net.exmo.exmodifier.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.stream.Collectors;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.CommonEvent.*;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.modifierEntryMap;

public class ModifierEntryHelper extends ExHelper {
    public static final String MES = "ModifierEntry";
    public static final String MEID = "EntryID";
    public static final String SLOT = "slot";

    public static ModifierEntryHelper of(ItemStack itemStack){
        return new ModifierEntryHelper(itemStack);
    }

    public static void moveOldEntry(ItemStack itemStack){
        CompoundTag tag = itemStack.getTag();
        if (tag ==null)return;
        if (tag.contains("exmodifier_armor_modifier_applied")){
            ModifierEntryHelper modifierEntryHelper = new ModifierEntryHelper(itemStack);
            for (ModifierEntry modifierEntry : oldFunc.getEntrysFromItemStack_old(itemStack)){
                modifierEntryHelper.addModifierEntry(new ModifierInstant(modifierEntry,1),true,true);

            }
            oldFunc.clearEntry_old(itemStack);
            tag.remove("exmodifier_armor_modifier_applied");
        }
    }
//    public boolean containEntry(String id){
//
//    }
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
    public ModifierEntryHelper removeAllEntry(boolean removeAttribute){
        for (ModifierInstant modifierInstant : getModifierEntries()){
            removeModifierEntry(modifierInstant,removeAttribute);
        }
        getMainNbt().put(MES,new ListTag());
        return this;
    }
    public static int getLivingEntityEntryLevel(String entryID, LivingEntity e){
        int level = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()){
            ItemStack itemBySlot = e.getItemBySlot(slot);
            if (itemBySlot.isEmpty())continue;
            if (!CuriosUtil.isCuriosItem2(itemBySlot)) {
                ModifierEntryHelper modifierEntryHelper = new ModifierEntryHelper(itemBySlot);
               level+= modifierEntryHelper.getModifierEntryLevel(entryID);

            }
        }
        return level;
    }
    public static int getLivingEntitySubEntryLevel(String entryID, LivingEntity e){
        int level = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()){
            ItemStack itemBySlot = e.getItemBySlot(slot);
            if (itemBySlot.isEmpty())continue;
            if (!CuriosUtil.isCuriosItem2(itemBySlot)) {
                ModifierEntryHelper modifierEntryHelper = new ModifierEntryHelper(itemBySlot);
                level+= modifierEntryHelper.getSubModifierEntryLevel(entryID);

            }
        }
        return level;
    }
    public List<ModifierEntry> randomEntry(int rarity,String material,int refreshTime){


        Map<ItemType, EquipmentSlot[]> typeEquipmentSlotMap = typeSlotMap();
        WeightedUtil<String> weightedUtil = new WeightedUtil<>(new HashMap<>());


        for (Map.Entry<ItemType, EquipmentSlot[]> entry : typeEquipmentSlotMap.entrySet()) {
            ItemType type = entry.getKey();

            if (!isValidForType(itemStack, type)) {
                continue;
            }



            weightedUtil.merge(new WeightedUtil<>(
                    modifierEntryMap.entrySet().stream()
                            .filter(e -> {
                                if (e.getValue().weight==0)return false;
                                var modifier = e.getValue();
                                boolean hasWashItem =ModifierHandle.materialsList.stream()
                                        .anyMatch(m -> m.ItemId.equals(material) && !m.OnlyHasWashEntry);


                                List<String> onlyWashItems = modifier.getModifierItemSelector().getOnlyWashItems();
                                return modifier.types.stream().map(ItemType::name).toList().contains(type.name()) &&
                                        modifier.getModifierItemSelector().containItem(itemStack) &&
                                        !modifier.cantSelect &&
                                        (modifier.Slots.isEmpty()) &&
                                        (modifier.needFreshValue == 0 || modifier.needFreshValue <= rarity) &&
                                        (onlyWashItems.isEmpty() || onlyWashItems.contains(material) || hasWashItem);
                            })
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    e -> e.getValue().weight
                            ))
            ));
        }

        if (!weightedUtil.weights.isEmpty()) {
            weightedUtil.increaseWeightsByRarity(rarity);
            try {
                return ModifierSelector.selectEntriesOnly(
                        weightedUtil,refreshTime,
                        key -> modifierEntryMap.get(key)
                ).entries;
            } catch (Exception e) {
                // 处理异常
            }
        }
        return new ArrayList<>();
    }
    public  ModifierEntryHelper setModifierEntry(int index,ModifierInstant instant){
            ListTag modifierEntriesNbt = getModifierEntriesNbt();
            modifierEntriesNbt.set(index, instant.serializeNBT());
            getMainNbt().put(MES,modifierEntriesNbt);
            return this;
    }
    public ModifierEntryHelper setModifierEntryLevel(String entryID, int level){
        for (ModifierInstant modifierInstant : getModifierEntries()){
            if (modifierInstant.getModifierEntry().id.equals(entryID)){
                modifierInstant.setLevel(level);
                return this;
            }
        }
        return this;
    }
    public int getSubModifierEntryLevel(String entryID){
        ListTag modifierEntriesNbt = getModifierEntriesNbt();
        for (int i = 0; i < modifierEntriesNbt.size(); i++){
            if (modifierEntriesNbt.getCompound(i).getString(MEID).substring(2).equals(entryID.substring(2))) return modifierEntriesNbt.getCompound(i).getInt("Level");
        }
        return 0;
    }

    public int getModifierEntryLevel(String entryID){
        ListTag modifierEntriesNbt = getModifierEntriesNbt();
        for (int i = 0; i < modifierEntriesNbt.size(); i++){
            if (modifierEntriesNbt.getCompound(i).getString(MEID).equals(entryID)) return modifierEntriesNbt.getCompound(i).getInt("Level");
        }
        return 0;
    }
    public Optional<String> getModifierEntrySlot(String entryID){
        ListTag modifierEntriesNbt = getModifierEntriesNbt();
        for (int i = 0; i < modifierEntriesNbt.size(); i++){
            if (modifierEntriesNbt.getCompound(i).getString(MEID).equals(entryID)) return Optional.of(modifierEntriesNbt.getCompound(i).getString(SLOT));
        }
        return Optional.empty();
    }
    public ModifierEntryHelper setModifierEntrySlot(String entryID, String slot){
        ListTag modifierEntriesNbt = getModifierEntriesNbt();
        for (int i = 0; i < modifierEntriesNbt.size(); i++){
            if (modifierEntriesNbt.getCompound(i).getString(MEID).equals(entryID)){
                modifierEntriesNbt.getCompound(i).putString(SLOT,slot);
                return this;
            }
        }
        return this;
    }
    public ModifierEntryHelper(ItemStack itemStack) {
        super(itemStack);
    }
    public int getModifierEntriesSize(){
        CompoundTag mainNbt = getMainNbt();
        if (!ValidMainNbt()) return 0;
        return mainNbt.getList(MES,10).size();
    }

    public boolean gatherModifierInstant(ModifierInstant modifierInstant){
        List<ModifierInstant> modifierEntries = getModifierEntries();
        if (modifierEntries.stream().anyMatch(x->x.getModifierEntry().id.equals(modifierInstant.getModifierEntry().id))){
            int level = modifierInstant.getLevel()+ getModifierEntryLevel(modifierInstant.getModifierEntry().id);
            removeModifierEntryUnLock(modifierInstant,true);
            addModifierEntry(new ModifierInstant(modifierInstant.getModifierEntry(),level).setSlot(modifierInstant.getSlot().get()),true,false);
            return true;
        }
        return false;
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
                ModifierEntry modifierEntry = modifierEntryMap.get(tag1.getString(MEID));
                if (modifierEntry!=null)
                {
                    int level =1;
                    String slot = "";
                    if (tag1.contains("Level"))level = tag1.getInt("Level");
                    if (tag1.contains(SLOT))slot = tag1.getString(SLOT);
                    CompoundTag tag2 = tag1.copy();
                    tag2.remove("Level");
                    tag2.remove(SLOT);
                    tag2.remove(MEID);
                    modifierEntries.add(new ModifierInstant(modifierEntry, level)
                            .setData(tag2).setSlot(slot)
                    );
                }
                }
            }
            }
            return modifierEntries;


    }
    public List<ModifierEntry> getModifierEntriesB()
    {
        List<ModifierEntry> modifierEntries = new ArrayList<>();
        for (ModifierInstant modifierInstant : getModifierEntries()){
            modifierEntries.add(modifierInstant.getModifierEntry());
        }
        return modifierEntries;
    }

    public ModifierEntryHelper addModifierEntry(ModifierInstant modifierInstant,boolean addAttribute,boolean gather)
    {
        createNbt();
        if (!ValidMainNbt()) createMainNbt();
        createModifierEntryNbt();
        if (gather){if (gatherModifierInstant(modifierInstant))return this;};
        CompoundTag tag1 = new CompoundTag();
        tag1.putString(MEID,modifierInstant.getModifierEntry().id);
        if (modifierInstant.isItemQualityLock())tag1.putBoolean("ItemQualityLock",true);
        tag1.putInt("Level",modifierInstant.getLevel());
        if (modifierInstant.getSlot().isPresent())tag1.putString(SLOT,modifierInstant.getSlot().get());
        ListTag modifiersList = getModifierEntriesNbt();
        modifiersList.add(tag1);
        if (addAttribute){

            List<ModifierAttriGether> addTo = selectModifierAttributes(modifierInstant.getModifierEntry());

            if (CuriosUtil.isCuriosItem2(this.itemStack))applyModifiersCurios(itemStack, addTo, CuriosUtil.getSlotsFromItemstack(itemStack));
            else applyModifiers(itemStack,addTo,getEquipmentSlot(itemStack),modifierInstant);
        }
        return this;
    }
    public ModifierEntryHelper removeModifierEntryLevel(ModifierInstant modifierInstant, boolean removeAttribute){
        var old_modifier_level = getModifierEntryLevel(modifierInstant.getModifierEntry().id);
        if (old_modifier_level>modifierInstant.getLevel()){
            return this.setModifierEntryLevel(modifierInstant.getModifierEntry().id,old_modifier_level-modifierInstant.getLevel());
        }else return this.removeModifierEntry(modifierInstant,removeAttribute);

    }
    public ModifierEntryHelper removeModifierEntry(ModifierInstant modifierInstant, boolean removeAttribute) {
        createNbt();
        if (!ValidMainNbt()) return this;
        ListTag modifiersList = getModifierEntriesNbt();
        for (int i = modifiersList.size() - 1; i >= 0; i--) {
            CompoundTag tag1 = modifiersList.getCompound(i);
            if (tag1.getString(MEID).equals(modifierInstant.getModifierEntry().id)) {
                if (!tag1.getBoolean("CantRemove") || !tag1.getBoolean("itemQualityLock")) {
                    modifiersList.remove(i);
                }
            }
        }
        if (removeAttribute) {
            if (CuriosUtil.isCuriosItem2(itemStack)){
                for (ModifierAttriGether modifierAttriGether : modifierInstant.getModifierEntry().attriGether) {
                    if (modifierAttriGether.attribute!=null) CuriosUtil.removeAttributeModifierAffix(itemStack,ForgeRegistries.ATTRIBUTES.getKey(modifierAttriGether.attribute).toString(), modifierAttriGether.modifier.getName());
                }
            }
            for (ModifierAttriGether modifierAttriGether : modifierInstant.getModifierEntry().attriGether) {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    ItemAttrUtil.removeAttributeModifierNoAmout(itemStack, modifierAttriGether.attribute, modifierAttriGether.modifier, slot);
                }
            }
        }
        return this;
    }


    public ModifierEntryHelper removeModifierEntryUnLock(ModifierInstant modifierInstant,boolean removeAttribute)
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
        return modifierEntryMap.get(entryName);
    }
    public static List<ModifierAttriGether> getEntryAttriGether(ModifierEntry entry)
    {
        return entry.attriGether;
    }
    public static List<ModifierAttriGether> getEntryAttriGether(String entryID)
    {
        return getEntry(entryID).attriGether;
    }
    public static class oldFunc{
        @Deprecated(since = "0.033", forRemoval = true)
        public static int getItemStackEntryCount_old(ItemStack stack){
            if (stack.getTag()==null)return 0;
            for (int i = 0; true; i++) {
                if (stack.getTag().getString("exmodifier_armor_modifier_applied" + i).isEmpty()) {
                    return i;
                }

            }
        }
        @Deprecated(since = "0.033", forRemoval = true)
        public static List<ModifierEntry> getEntrysFromItemStack_old(ItemStack stack) {
            List<ModifierEntry> modifierEntries = new ArrayList<>();
            if (stack.getTag()==null)return modifierEntries;
            for (ModifierEntry modifierAttriGether : modifierEntryMap.values().stream().filter(Objects::nonNull).toList()) {
                String id;
                for (int i = 0; true; i++) {
                    id = stack.getTag().getString("exmodifier_armor_modifier_applied"+i);
                    if (id.isEmpty())break;
                    if (id.equals(modifierAttriGether.getId())) {
                        modifierEntries.add(modifierAttriGether);
                    }

                }
            }
            return modifierEntries;
        }
        @Deprecated(since = "0.033", forRemoval = true)
        public static void clearEntry_old(ItemStack stack){
            if (stack.getTag()==null)return;
            if (stack.getTag().getInt("exmodifier_armor_modifier_applied")==0)return;
          //  List<ItemType> types = ModifierEntry.getType(stack);
            List<String> curiosType = CuriosUtil.getSlotsFromItemstack(stack);
            List<ModifierEntry> hasAttriGether = getEntrysFromItemStack_old(stack);
            for (int i = 0; i < hasAttriGether.size(); i++)
            {
                ModifierEntry modifierAttriGether = hasAttriGether.get(i);
                for (ModifierAttriGether modifierAttriGether1 : modifierAttriGether.attriGether) {
                    EquipmentSlot slot = modifierAttriGether1.slot;
                    if (modifierAttriGether1.IsAutoEquipmentSlot){
                        List<ItemType> type = ModifierEntry.getType(stack);
                        if (!type.isEmpty()) slot = ModifierEntry.TypeToEquipmentSlot(type.get(0));
                    }
                    if (curiosType.isEmpty()) ItemAttrUtil.removeAttributeModifierNoAmout(stack, modifierAttriGether1.getAttribute(), modifierAttriGether1.getModifier(), slot);
                    else {
                        for (String curioType : curiosType)
                        {
                            if (ForgeRegistries.ATTRIBUTES.containsValue(modifierAttriGether1.getAttribute())&&ForgeRegistries.ATTRIBUTES.getKey(modifierAttriGether1.getAttribute())!=null) CuriosUtil.removeAttributeModifierAffix(stack,ForgeRegistries.ATTRIBUTES.getKey(modifierAttriGether1.getAttribute()).toString(), modifierAttriGether1.getModifier().getName());
                        }
                    }
                    stack.getOrCreateTag().remove("exmodifier_armor_modifier_applied"+i);
                }

            }
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()){
                Multimap<Attribute, AttributeModifier> attributeModifiers = stack.getAttributeModifiers(equipmentSlot);
                if (attributeModifiers.isEmpty())attributeModifiers.clear();
            }
        }
    }
}

