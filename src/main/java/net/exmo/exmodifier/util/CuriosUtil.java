package net.exmo.exmodifier.util;

import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CuriosUtil {
    public static List<String> getSlotsFromItemstack(ItemStack itemStack) {
        if (CuriosUtil.isCuriosItem(itemStack)) {
            Set<String> curioTags = CuriosApi.getCuriosHelper().getCurioTags(itemStack.getItem());
            return new ArrayList<>(curioTags);
        }
        return new ArrayList<>();
        }

    public static boolean isCuriosItem(ItemStack itemStack){
        return CuriosApi.getCuriosHelper().getCurio(itemStack).isPresent();
    }
    public static void addAttributeModifier(ItemStack itemStack, AttriGether attriGether, String slot){
        CuriosUtil.addAttributeModifier(itemStack, ForgeRegistries.ATTRIBUTES.getKey(attriGether.attribute).toString(), attriGether.modifier.getAmount(), attriGether.modifier.getOperation().toValue(), slot);
    }
    public static void addAttributeModifierApi(ItemStack itemStack, AttriGether attriGether, String slot){
        if (attriGether.attribute!=null)
        {
            {
                CuriosApi.getCuriosHelper().addModifier(itemStack, attriGether.attribute,attriGether.modifier.getName(), attriGether.modifier.getId(), attriGether.modifier.getAmount(), attriGether.modifier.getOperation(),slot);
            }
        }
    }

    public static Multimap<Attribute, AttributeModifier> getAttributeModifiers(ItemStack itemStack, String slot) {
      return   CuriosApi.getCuriosHelper().getAttributeModifiers(slot, itemStack);
    }
    /**
     * 从物品的NBT标签中删除特定的属性修饰符。
     *
     * @param itemStack 要修改的物品堆栈
     * @param attributeName 属性的名字
     * @param amount 属性修饰符的数值
     * @param operation 属性修饰符的操作类型
     * @param slot 属性修饰符适用的槽位
     */
    public static void removeAttributeModifier(ItemStack itemStack, String attributeName, double amount, int operation, String slot) {
        CompoundTag itemTag = itemStack.getOrCreateTag();

        // 检查是否已经有CurioAttributeModifiers标签
        if (itemTag.contains("CurioAttributeModifiers", 9)) {
            ListTag modifiersList = itemTag.getList("CurioAttributeModifiers", 10);

            // 创建一个新的列表来保存未匹配的修饰符
            ListTag newModifiersList = new ListTag();

            // 遍历所有的修饰符
            for (int i = 0; i < modifiersList.size(); ++i) {
                CompoundTag modifierTag = modifiersList.getCompound(i);

                // 如果找到匹配的修饰符，则跳过它
                if (slot.equals(modifierTag.getString("Slot")) &&
                        attributeName.equals(modifierTag.getString("AttributeName")) &&
                        modifierTag.getDouble("Amount") == amount &&
                        modifierTag.getInt("Operation") == operation) {
                    continue;
                }

                // 否则，将修饰符添加到新列表中
                newModifiersList.add(modifierTag);
            }

            // 替换旧的修饰符列表
            itemTag.put("CurioAttributeModifiers", newModifiersList);
        }

        itemStack.setTag(itemTag);
    }

    /**
     * 向物品的NBT标签中添加属性修饰符。
     *
     * @param itemStack 要修改的物品堆栈
     * @param attributeName 属性的名字
     * @param amount 属性修饰符的数值
     * @param operation 属性修饰符的操作类型
     * @param slot 属性修饰符适用的槽位
     */
    public static void addAttributeModifier(ItemStack itemStack, String attributeName, double amount, int operation, String slot) {
        CompoundTag itemTag = itemStack.getOrCreateTag();

        // 检查是否已经有CurioAttributeModifiers标签
        if (!itemTag.contains("CurioAttributeModifiers", 9)) {
            itemTag.put("CurioAttributeModifiers", new ListTag());
        }

        ListTag modifiersList = itemTag.getList("CurioAttributeModifiers", 10);
        boolean found = false;

        // 查找已存在的修饰符
        for (int i = 0; i < modifiersList.size(); ++i) {
            CompoundTag modifierTag = modifiersList.getCompound(i);
            if (slot.equals(modifierTag.getString("Slot")) && attributeName.equals(modifierTag.getString("AttributeName"))) {
                modifierTag.putDouble("Amount", amount);
                modifierTag.putInt("Operation", operation);
                found = true;
                break;
            }
        }

        // 如果没有找到，则添加新的修饰符
        if (!found) {
            CompoundTag newModifier = new CompoundTag();
            newModifier.putString("Slot", slot);
            newModifier.putString("AttributeName", attributeName);
            newModifier.putString("Name", attributeName); // 名称通常与属性名相同
            newModifier.putDouble("Amount", amount);
            newModifier.putInt("Operation", operation);
            modifiersList.add(newModifier);
        }

        itemTag.put("CurioAttributeModifiers", modifiersList);
        itemStack.setTag(itemTag);
    }
}
