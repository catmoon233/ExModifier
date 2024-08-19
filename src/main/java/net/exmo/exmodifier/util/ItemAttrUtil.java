package net.exmo.exmodifier.util;
import net.exmo.exmodifier.Exmodifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.*;

import java.util.Optional;




public  class ItemAttrUtil {
    @SubscribeEvent
    public static void addbaseattributes(ItemAttributeModifierEvent event) {
        List removelist = new ArrayList<>();
        attmap.forEach((id,attrgroup ) -> {
            ItemStack itemStack = event.getItemStack();

            //         if (itemStack != null) {
            // 使用Objects.equals实现null安全比较
            String itemKey = ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString();
            //   System.out.println("1: " +itemKey +" 2:"+id);
            if (Objects.equals(id, itemKey)) {
                EquipmentSlot slot = attrgroup.getSlot();
                if (slot != null) {
                    // 对EquipmentSlot使用equals进行比较，这里假设EquipmentSlot类正确实现了equals方法
                    //   System.out.println("1: " +slot +" 2:"+itemStack.getEquipmentSlot());
                    if (slot == event.getSlotType()) {
                        event.addModifier(attrgroup.attr, attrgroup.attributeModifier);
                        //  removelist.add(id);
                        //  }
                    }
                }
            }
        });
        for (int i = 0; i < removelist.size(); i++){
            attmap.remove(removelist.get(i));
        }
    }

    public static class attrgroup {
        private EquipmentSlot slot;
        private Attribute attr;
        private AttributeModifier attributeModifier;

        // 添加构造函数
        public  attrgroup(EquipmentSlot slot, Attribute attr, AttributeModifier attributeModifier) {
            if (slot == null || attr == null || attributeModifier == null) {
                throw new IllegalArgumentException("All arguments must be non-null");
            }
            this.slot = slot;
            this.attr = attr;
            this.attributeModifier = attributeModifier;
        }

        // 提供getter方法以获取成员变量的值（如果需要访问）
        public EquipmentSlot getSlot() {
            return slot;
        }

        public Attribute getAttr() {
            return attr;
        }

        public AttributeModifier getAttributeModifier() {
            return attributeModifier;
        }
    }
    public EquipmentSlot getEquipmentSlot(String s) {
        return EquipmentSlot.byName(s);
    }
    public static String getattrstringamount(ItemStack itemStack,EquipmentSlot equipmentSlot, Attribute pAttribute){
        Map<Attribute,String> zh = new HashMap<>();
        List<Double>amount = new ArrayList<>();
        zh.put(Attributes.ATTACK_DAMAGE,"攻击伤害");
        //    List<String> textlist = new ArrayList<>();
        final String[] disattrname = new String[1];
        zh.forEach((k1,v1)->{
            if (k1==pAttribute){
                disattrname[0] = v1;
            }
        });

        itemStack.getAttributeModifiers(equipmentSlot).forEach((k, v) -> {
            if (k==pAttribute){
                amount.add(v.getAmount());
            }
        });
        double finalamount=0;
        for(int i = 0; i < amount.size(); i++){
            finalamount +=amount.get(i);
        }
        String finalstring = disattrname[0] + ":" + (finalamount+1);
        return finalstring;
    }

    public static void addItemAttributeModifier(ItemStack itemStack, Attribute pAttribute, AttributeModifier pModifier, EquipmentSlot pSlot) {
        if (!ForgeRegistries.ATTRIBUTES.containsValue(pAttribute)){
            Exmodifier.LOGGER.error("Attribute " + pAttribute + " does not exist");
            return;
        }
        Exmodifier.LOGGER.debug("add attribute: "+pAttribute+" "+pAttribute.getDescriptionId());
        itemStack.getOrCreateTag();
        if (!itemStack.getTag().contains("ExAttributeModifiers", 9)) {
            itemStack.getTag().put("ExAttributeModifiers", new ListTag());
        }
        ListTag listtag = itemStack.getTag().getList("ExAttributeModifiers", Tag.TAG_COMPOUND);
        if (!listtag.isEmpty()) {
            for (int i = 0; i < listtag.size(); i++) {
                CompoundTag compoundTag = listtag.getCompound(i);
                if (
                        compoundTag.getString("AttributeName").equals(ForgeRegistries.ATTRIBUTES.getKey(pAttribute).toString()) &&
                                compoundTag.getString("Name").equals(pModifier.getName()) &&
                                compoundTag.getInt("Operation") == pModifier.getOperation().toValue()
                ) {
                    double amount = compoundTag.getDouble("Amount");

                }
            }
        }
        listtag.add(getAttributeModifierCompoundTag(pAttribute, pModifier, pSlot));
    }
    public static boolean hasDifferentAttributeValue(ItemStack itemStack, Attribute pAttribute, AttributeModifier pModifier, EquipmentSlot pSlot) {
        if (itemStack.getTag() != null && itemStack.getTag().contains("ExAttributeModifiers", 9)) {
            ListTag listtag = itemStack.getTag().getList("ExAttributeModifiers", Tag.TAG_COMPOUND);
            for (int i = 0; i < listtag.size(); i++) {
                CompoundTag compoundTag = listtag.getCompound(i);
                if (
                        compoundTag.contains("AttributeName") &&
                                compoundTag.getString("AttributeName").equals(ForgeRegistries.ATTRIBUTES.getKey(pAttribute).toString()) &&
                                compoundTag.contains("Name") &&
                                compoundTag.getString("Name").equals(pModifier.getName()) &&
                                compoundTag.contains("Operation") &&
                                compoundTag.getInt("Operation") == pModifier.getOperation().toValue() &&
                                (!compoundTag.contains("Slot") || (pSlot == null || compoundTag.contains("Slot") && compoundTag.getString("Slot").equals(pSlot.getName()))) &&
                                compoundTag.contains("Amount") && // Assuming the value is stored in a field named "Amount"
                                compoundTag.getDouble("Amount") != pModifier.getAmount()
                ) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasAttributeModifierCompoundTag(ItemStack itemStack, Attribute pAttribute, AttributeModifier pModifier, EquipmentSlot pSlot) {
        if (itemStack.getTag() != null && itemStack.getTag().contains("ExAttributeModifiers", 9)) {
            ListTag listtag = itemStack.getTag().getList("ExAttributeModifiers", Tag.TAG_COMPOUND);
            for (int i = 0; i < listtag.size(); i++) {
                CompoundTag compoundTag = listtag.getCompound(i);
                if (
                        compoundTag.contains("AttributeName") &&
                                compoundTag.getString("AttributeName").equals(ForgeRegistries.ATTRIBUTES.getKey(pAttribute).toString()) &&
                                compoundTag.contains("Name") &&
                                compoundTag.getString("Name").equals(pModifier.getName()) &&
                                compoundTag.contains("Operation") &&
                                compoundTag.getInt("Operation") == pModifier.getOperation().toValue() &&
                                (!compoundTag.contains("Slot") || (pSlot == null || compoundTag.contains("Slot") && compoundTag.getString("Slot").equals(pSlot.getName())))
                ) {
                    return true;
                }
            }
        }
        return false;
    }
    public static boolean hasAttributeModifierByUUID(ItemStack itemStack, Attribute pAttribute, UUID pModifierUUID, EquipmentSlot pSlot) {
        if (itemStack.getTag() != null && itemStack.getTag().contains("ExAttributeModifiers", 9)) {
            ListTag listtag = itemStack.getTag().getList("ExAttributeModifiers", Tag.TAG_COMPOUND);
            for (int i = 0; i < listtag.size(); i++) {
                CompoundTag compoundTag = listtag.getCompound(i);
                if (
                        compoundTag.contains("AttributeName") &&
                                compoundTag.getString("AttributeName").equals(ForgeRegistries.ATTRIBUTES.getKey(pAttribute).toString()) &&
                                compoundTag.contains("UUIDMost") && // Assuming "UUIDMost" and "UUIDLeast" are used to store the UUID
                                compoundTag.getLong("UUIDMost") == pModifierUUID.getMostSignificantBits() &&
                                compoundTag.contains("UUIDLeast") &&
                                compoundTag.getLong("UUIDLeast") == pModifierUUID.getLeastSignificantBits() &&
                                (!compoundTag.contains("Slot") || (pSlot == null || compoundTag.contains("Slot") && compoundTag.getString("Slot").equals(pSlot.getName())))
                ) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void removeAttributeModifier(ItemStack itemStack, Attribute pAttribute, AttributeModifier pModifier, EquipmentSlot pSlot) {
        if (!ForgeRegistries.ATTRIBUTES.containsValue(pAttribute))return;
        if (itemStack.getTag() != null && itemStack.getTag().contains("ExAttributeModifiers", 9)) {
            ListTag listtag = itemStack.getTag().getList("ExAttributeModifiers", Tag.TAG_COMPOUND);
            for (int i = 0; i < listtag.size(); ) { // 注意这里使用i而不是i++
                CompoundTag compoundTag = listtag.getCompound(i);
                if (
                        compoundTag.contains("AttributeName") &&
                                compoundTag.getString("AttributeName").equals(ForgeRegistries.ATTRIBUTES.getKey(pAttribute).toString()) &&
                                compoundTag.contains("Name") &&
                                compoundTag.getString("Name").equals(pModifier.getName()) &&
                                compoundTag.contains("Operation") &&
                                compoundTag.getInt("Operation") == pModifier.getOperation().toValue() &&
                                (!compoundTag.contains("Slot") || (pSlot == null || compoundTag.contains("Slot") && compoundTag.getString("Slot").equals(pSlot.getName())))
                ) {
                    listtag.remove(i);
                } else {
                    i++;
                }
            }
        }
    }
    public static void addItemAttributeModifier2(ItemStack itemStack, Attribute pAttribute, AttributeModifier pModifier, EquipmentSlot pSlot) {
        try {
            if (hasDifferentAttributeValue(itemStack, pAttribute, pModifier, pSlot)) {
                AttributeModifier currentModifier = getAttributeModifierFromCompoundTag(getAttributeModifierCompoundTag(pAttribute, pModifier, pSlot));

                if (currentModifier != null) {
                    double oldAmount = currentModifier.getAmount();
                    // 保证数值增加的逻辑安全，防止溢出
                    double updatedAmount = Math.min(oldAmount + pModifier.getAmount(), Double.MAX_VALUE);

                    AttributeModifier updatedModifier = new AttributeModifier(pModifier.getName(), updatedAmount, pModifier.getOperation());
                    removeAttributeModifier(itemStack, pAttribute, currentModifier, pSlot);
                    addItemAttributeModifier(itemStack, pAttribute, updatedModifier, pSlot);
                } else {
                    // 如果getAttributeModifierFromCompoundTag返回null，这里可以记录日志或处理异常
                    System.err.println("Failed to retrieve the current attribute modifier.");
                }
            } else {
                addItemAttributeModifier(itemStack, pAttribute, pModifier, pSlot);
            }
        } catch (Exception e) {
            // 添加了基本的异常处理，但在实际应用中，应更详细地处理不同类型的异常并恢复到安全状态
            System.err.println("An error occurred while adding or updating an item attribute modifier: " + e.getMessage());
        }
    }


    public static Map<String,attrgroup> attmap = new HashMap<>();
    public static  void putattinattmap(String name,attrgroup attgroup)//安排属性
    {
        attmap.put(name,attgroup);
    }
    public static CompoundTag getAttributeModifierCompoundTag(Attribute attribute, AttributeModifier modifier, EquipmentSlot slot) {

        CompoundTag compoundtag = modifier.save();
        compoundtag.putString("AttributeName", ForgeRegistries.ATTRIBUTES.getKey(attribute).toString());
        if (slot != null) {
            compoundtag.putString("Slot", slot.getName());
        }
        return compoundtag;
    }
    public static AttributeModifier getAttributeModifierFromCompoundTag(CompoundTag compoundTag) {
        if (compoundTag.contains("AttributeName") && compoundTag.contains("Name") && compoundTag.contains("Operation")) {
            ResourceLocation attributeId = new ResourceLocation(compoundTag.getString("AttributeName"));
            String modifierName = compoundTag.getString("Name");
            AttributeModifier.Operation operation = AttributeModifier.Operation.fromValue(compoundTag.getInt("Operation"));

            // 获取Amount和其他可能的参数，这里假设Amount是double类型
            double amount = compoundTag.getDouble("Amount");

            // 根据具体实现，AttributeModifier可能还需要UUID或其他额外信息
            UUID uuid = null;
            if (compoundTag.contains("UUIDMost") && compoundTag.contains("UUIDLeast")) {
                long most = compoundTag.getLong("UUIDMost");
                long least = compoundTag.getLong("UUIDLeast");
                uuid = new UUID(most, least);
            }

            return new AttributeModifier(uuid, modifierName, amount, operation);
        }
        return null; // 如果缺少必要信息，则返回null
    }
    public static void EntityAddAttr(LivingEntity livingEntity,UUID uuid,String name,double amount,Attribute attribute,AttributeModifier.Operation operation){
        if (!(Objects.requireNonNull(livingEntity.getAttribute(attribute))
                .hasModifier((new AttributeModifier(uuid, name ,amount, operation)))))
            Objects.requireNonNull(livingEntity.getAttribute(attribute))
                    .addPermanentModifier((new AttributeModifier(uuid, name, amount, operation)));
    }



}
