package net.exmo.exmodifier.content.helper;

import net.exmo.exmodifier.content.event.parameter.EventParameter;
import net.exmo.exmodifier.content.level.ItemLevel;
import net.exmo.exmodifier.content.level.ItemLevelHandle;
import net.exmo.exmodifier.content.level.ItemLevelInstant;
import net.exmo.exmodifier.events.ExItemUpEvent;
import net.exmo.exmodifier.util.AttriGether;
import net.exmo.exmodifier.util.DynamicExpressionEvaluator;
import net.exmo.exmodifier.util.ItemAttrUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;

import static net.exmo.exmodifier.content.level.ItemLevelHandle.ItemLevels;
import static net.exmo.exmodifier.content.level.ItemLevelHandle.generateLevelNeedXp;

public class ItemLevelHelper extends ExHelper {
    public CompoundTag nbt;
    public static final String ITEM_LEVEL = "itemLevel";
    public static final String ITEM_LEVEL_ID = "itemLevelId";

    public ItemLevelHelper(ItemStack itemStack) {
        super(itemStack);
    }
    public static ItemLevelHelper of (ItemStack itemStack)
    {
        return new ItemLevelHelper(itemStack);
    }
    public boolean ValidItemLevelNbt()
    {
        return ValidMainNbt()&&getMainNbt().contains(ITEM_LEVEL);
    }
    public ItemLevelHelper createModifierEntryNbt()
    {
        createNbt();
        if (ValidItemLevelNbt()) return this;
        getMainNbt().put(ITEM_LEVEL,new ListTag());
        return  this;

    }
    public ItemLevelHelper removeItemLevelHelper(ItemLevelInstant itemLevelInstant, boolean removeAttribute) {
        ListTag modifiersList = getItemLevelNbt();
        List<Integer> indicesToRemove = new ArrayList<>();
        for (int i = 0; i < modifiersList.size(); i++) {
            if (modifiersList.getCompound(i).getString(ITEM_LEVEL_ID).equals(itemLevelInstant.getItemLevel().id)) {
                indicesToRemove.add(i);
            }
        }
        // 从后向前删除，避免索引越界
        for (int index : indicesToRemove) {
            modifiersList.remove(index);
        }
        return this;
    }

    public ListTag getItemLevelNbt()
    {
        return getMainNbt().getList(ITEM_LEVEL,10);
    }
    public static void moveOldLevel(ItemStack stack){
        CompoundTag tag = stack.getTag();
        if (tag==null)return;
        if (tag.contains("exmodifier_level_modifier_applied")){
            List<ItemLevel> itemLevels = oldFunc.getItemLevels(stack);
            for (ItemLevel itemLevel : itemLevels) {
                double xp = oldFunc.getLevelItemXp(stack, itemLevel.id);
                int level = oldFunc.getLevelItemLevel(stack, itemLevel.id);
                double needXp = oldFunc.getLevelItemNeedXpUp(stack, itemLevel.id);
                int maxLevel = oldFunc.getLevelItemMaxLevel(stack, itemLevel.id);
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    for (AttriGether attrGather : itemLevel.attriGethers) {
                        ItemAttrUtil.removeAttributeModifierNoAmout(stack, attrGather.attribute, attrGather.getModifier(), slot);
                    }
                }
                ItemLevelHelper.of(stack).addItemLevelHelper(new ItemLevelInstant(itemLevel,level,maxLevel,xp,needXp));
                List<String> keysToRemove = new ArrayList<>();
                for (String key : tag.getAllKeys()) {
                    if (key.contains(itemLevel.id)) {
                        keysToRemove.add(key);
                    }
                }

                for (String key : keysToRemove) {
                    tag.remove(key);
                }
            }
            tag.remove("exmodifier_level_modifier_applied");

        }
    }
    public ItemLevelHelper addItemLevelHelper(ItemLevelInstant itemLevelInstant)
    {
        createNbt();
        if (!ValidMainNbt()) createMainNbt();
        createModifierEntryNbt();
        CompoundTag tag1 = new CompoundTag();
        tag1.putString(ITEM_LEVEL_ID,itemLevelInstant.getItemLevel().id);
        tag1.putInt("Level",itemLevelInstant.getLevel());
        tag1.putDouble("Xp",itemLevelInstant.xp);
        tag1.putDouble("NeedXp",itemLevelInstant.needXp);
        tag1.putInt("MaxLevel",itemLevelInstant.getMaxLevel());
        ListTag modifiersList = getItemLevelNbt();
        modifiersList.add(tag1);
        return this;
    }
    public int getItemLevelsSize()
    {
        return getItemLevelNbt().size();
    }
    public List<ItemLevel> getItemLevels()
    {
        List<ItemLevel> list = new java.util.ArrayList<>();
        for (int i = 0; i < getItemLevelNbt().size(); i++){
            list.add(ItemLevels.get(getItemLevelNbt().getCompound(i).getString(ITEM_LEVEL)));
        }
        return list;
    }
    public List<ItemLevelInstant> getItemLevelInstants()
    {
        List<ItemLevelInstant> list = new java.util.ArrayList<>();
        for (int i = 0; i < getItemLevelNbt().size(); i++) {
            CompoundTag tag = getItemLevelNbt().getCompound(i);
            int level = tag.getInt("Level");
            int maxLevel = tag.getInt("MaxLevel");
            double xp = tag.getDouble("Xp");
            double needXp = tag.getDouble("NeedXp");
            String itemLevelId = tag.getString(ITEM_LEVEL_ID);
            list.add(new ItemLevelInstant(ItemLevels.get(itemLevelId),level,maxLevel,xp,needXp));
        }
        return list;
    }
    public ItemLevelHelper setXp(String itemLevelId,int xp)
    {
        ListTag itemLevelNbt = getItemLevelNbt();
        for (int i = 0; i < itemLevelNbt.size(); i++){
            if (itemLevelNbt.getCompound(i).getString(ITEM_LEVEL_ID).equals(itemLevelId)) {
                itemLevelNbt.getCompound(i).putDouble("Xp",xp);
                return this;};
        }
        return this;
    }
    public double getXp(String itemLevelId)
    {
        for (int i = 0; i < getItemLevelNbt().size(); i++){
            if (getItemLevelNbt().getCompound(i).getString(ITEM_LEVEL_ID).equals(itemLevelId)) return getItemLevelNbt().getCompound(i).getDouble("Xp");
        }
        return 0;
    }
    public ItemLevelHelper setNeedXp(String itemLevelId,double needXp)
    {
        ListTag itemLevelNbt = getItemLevelNbt();
        for (int i = 0; i < itemLevelNbt.size(); i++){
            if (itemLevelNbt.getCompound(i).getString(ITEM_LEVEL_ID).equals(itemLevelId)){
                itemLevelNbt.getCompound(i).putDouble("NeedXp",needXp);
                return this;
            }
        }
        return this;
    }
    public double getNeedXp(String itemLevelId){
        for (int i = 0; i < getItemLevelNbt().size(); i++){
            if (getItemLevelNbt().getCompound(i).getString(ITEM_LEVEL_ID).equals(itemLevelId)) return getItemLevelNbt().getCompound(i).getDouble("NeedXp");
        }
        return 0;
    }
    public ItemLevelHelper setLevel(String itemLevelId,int level)
    {
        ListTag itemLevelNbt = getItemLevelNbt();
        for (int i = 0; i < itemLevelNbt.size(); i++){
            if (itemLevelNbt.getCompound(i).getString(ITEM_LEVEL_ID).equals(itemLevelId)){
                itemLevelNbt.getCompound(i).putInt("Level",level);
                return this;
            }
        }
        return this;
    }
    public int getLevel(String itemLevelId){
        for (int i = 0; i < getItemLevelNbt().size(); i++){
            if (getItemLevelNbt().getCompound(i).getString(ITEM_LEVEL_ID).equals(itemLevelId)) return getItemLevelNbt().getCompound(i).getInt("Level");
        }
        return 0;
    }
    public ItemLevelInstant getItemLevelInstant(String itemLevelId){
        for (int i = 0; i < getItemLevelInstants().size(); i++){
            if (getItemLevelInstants().get(i).itemLevel.id.equals(itemLevelId)) return getItemLevelInstants().get(i);
        }
        return null ;
    }
    public ItemLevelHelper setMaxLevel(String itemLevelId,int maxLevel)
    {
        ListTag itemLevelNbt = getItemLevelNbt();
        for (int i = 0; i < itemLevelNbt.size(); i++){
            if (itemLevelNbt.getCompound(i).getString(ITEM_LEVEL_ID).equals(itemLevelId)){
                itemLevelNbt.getCompound(i).putInt("MaxLevel",maxLevel);
                return this;
            }
        }
        return this;
    }
    public int getMaxLevel(String itemLevelId){
        for (int i = 0; i < getItemLevelNbt().size(); i++){
            if (getItemLevelNbt().getCompound(i).getString(ITEM_LEVEL_ID).equals(itemLevelId)) return getItemLevelNbt().getCompound(i).getInt("MaxLevel");
        }
        return 0;
    }
    public  void ItemAddXpM(ItemStack stack, List<EventParameter<?>> params, ItemLevelInstant itemLevelInstant, LivingEntity entity) {
        if (stack.getTag() == null) return;

        DynamicExpressionEvaluator evaluator = new DynamicExpressionEvaluator();
        for (EventParameter<?> param : params) {
            evaluator.setVariable(param.getKey(), param.getDouble());
        }
        double addXp = evaluator.evaluate(itemLevelInstant.itemLevel.getXpAddExpression());
        double level = itemLevelInstant.level;
        double xp = itemLevelInstant.xp;
        double needXp = itemLevelInstant.needXp;
        double _level = level;
        double finalXp = xp + addXp;
        if (finalXp > 0) {
            while (finalXp > 0) {
                if (finalXp >= needXp) {
                    if (level<itemLevelInstant.maxLevel) {
                        level++;
                    }
                    finalXp -= needXp;
                    needXp = generateLevelNeedXp(itemLevelInstant.itemLevel, (int) level);
                } else break;

            }
            setLevel( itemLevelInstant.itemLevel.id, (int) level);
            setXp( itemLevelInstant.itemLevel.id, (int) finalXp);
            setNeedXp( itemLevelInstant.itemLevel.id, needXp);

        }
        if (_level != level) {
            ExItemUpEvent event = new ExItemUpEvent(stack, (int) level,entity, (int) _level, (int) finalXp, itemLevelInstant);
            MinecraftForge.EVENT_BUS.post(event);
        }
    }
    public static class oldFunc{
            private static void setLevelItemLevel(ItemStack stack, String id, int level2) {
        if (stack.getTag()==null)return;
        stack.getTag().putDouble(id +"_level", level2);
    }

    private static void setLevelItemXp(ItemStack stack, String id, int xp2) {
        if (stack.getTag()==null)return;
        stack.getTag().putDouble(id +"_Xp", xp2);
    }
    public static List<ItemLevel> getItemLevels (ItemStack stack){
        List<ItemLevel> itemLevels = new ArrayList<>();
        if (stack.getTag() ==null)return itemLevels;

        for (int i = 0;true;i++){
            String id = stack.getTag().getString("exmodifier_level_modifier_applied"+i);
            if (id.isEmpty())break;
            itemLevels.add(ItemLevels.get(id));
        }
        return itemLevels;
    }
            public static void setItemNeedXpUp(ItemStack stack,String levelId,double xp){
        if (stack.getTag()==null)return;
        stack.getTag().putDouble(levelId +"_NeedXpUp",xp);
    }
    public static double getLevelItemXp(ItemStack stack,String levelId){
        if (stack.getTag()==null)return 0;
        return  stack.getTag().getDouble(levelId +"_Xp");
    }
    public static double getLevelItemNeedXpUp(ItemStack stack,String levelId){
        if (stack.getTag()==null)return 0;
        return stack.getTag().getDouble(levelId +"_NeedXpUp");
    }
    public static int getLevelItemLevel(ItemStack stack,String levelId){
        if (stack.getTag()==null)return 0;
        return stack.getTag().getInt(levelId +"_level");
    }
    public static int getLevelItemMaxLevel(ItemStack stack,String levelId){
        if (stack.getTag()==null)return 0;
        return stack.getTag().getInt(levelId +"_MaxLevel");
   }
    }
}