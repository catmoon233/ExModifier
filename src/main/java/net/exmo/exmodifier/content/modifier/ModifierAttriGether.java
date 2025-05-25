package net.exmo.exmodifier.content.modifier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.util.exSerialize.ExSerialize;
import net.exmo.exmodifier.util.gether.AttriGether;
import net.exmo.exmodifier.util.ExAttributeModifier;
import net.exmo.exmodifier.util.ExConfigHandle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.getEquipmentSlot;

public class ModifierAttriGether extends AttriGether {
    // 新增序列化配置
    public static ExSerialize<ModifierAttriGether> ExSer = ExSerialize.create(ModifierAttriGether::new)
        .addFloatField("weight", e -> e.weight, (e, v) -> e.weight = v)
        .addBooleanField("isRandom", e -> e.isRandom, (e, v) -> e.isRandom = v)
        .addBooleanField("hasUUID", e -> e.hasUUID, (e, v) -> e.hasUUID = v)
        .addDoubleField("minValue", e -> e.minValue, (e, v) -> e.minValue = v)
        .addDoubleField("maxValue", e -> e.maxValue, (e, v) -> e.maxValue = v)
        .addStringField("Expression", e -> e.Expression, (e, v) -> e.Expression = v)
        .addStringListField("OnlyItems", e -> e.OnlyItems, (e, v) -> e.OnlyItems = v)
        .addStringListField("OnlySlots", e -> e.OnlySlots, (e, v) -> e.OnlySlots = v)
        .addJsonObjectList("simpleWeight",
            e -> e.simpleWeight.entrySet().stream()
                .map(entry -> {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("key", entry.getKey());
                    obj.addProperty("value", entry.getValue());
                    return obj;
                }).collect(Collectors.toList()),
            (e, list) -> e.simpleWeight = list.stream()
                .collect(Collectors.toMap(
                    obj -> obj.get("key").getAsDouble(),
                    obj -> obj.get("value").getAsFloat()
                ))
        )
        .marge(
            AttriGether.ExSer // 假设父类已定义序列化配置
        );

    public float weight = 0;
    public boolean isRandom = false;
    public int reserveDouble = 3;
    public boolean hasUUID = false;
    public double minValue = 0;
    public double maxValue = 0;
    public Map<Double, Float> simpleWeight = new HashMap<>();
    public String Expression = "";
    public List<String> OnlyItems = new java.util.ArrayList<>();
    public List<String> OnlySlots = new java.util.ArrayList<>();

    public ModifierAttriGether() {
        super(null, null);
    }

//    public AttrGether toAttriGether() {
//        return new AttrGether(this.attribute, this.modifier);
//    }

    public ModifierAttriGether setExpression(String expression) {
        this.Expression = expression;
        return this;
    }

    public ModifierAttriGether copy() {
        ModifierAttriGether attriGether = new ModifierAttriGether(this.attribute, this.modifier, this.slot);
        attriGether.weight = this.weight;
        attriGether.isRandom = this.isRandom;
        attriGether.reserveDouble = this.reserveDouble;
        attriGether.hasUUID = this.hasUUID;
        attriGether.minValue = this.minValue;
        attriGether.maxValue = this.maxValue;
        attriGether.simpleWeight = this.simpleWeight;
        attriGether.Expression = this.Expression;
        attriGether.OnlyItems = this.OnlyItems;
        attriGether.OnlySlots = this.OnlySlots;
        return attriGether;
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("weight", weight);
        tag.putBoolean("isRandom", isRandom);
        tag.putInt("reserveDouble", reserveDouble);
        tag.putBoolean("hasUUID", hasUUID);
        tag.putBoolean("IsAutoEquipmentSlot", IsAutoEquipmentSlot);
        tag.putDouble("minValue", minValue);
        tag.putDouble("maxValue", maxValue);
        tag.putString("Expression", Expression);

        ListTag onlyItemsTag = new ListTag();
        for (String item : OnlyItems) {
            onlyItemsTag.add(StringTag.valueOf(item));
        }
        tag.put("OnlyItems", onlyItemsTag);

        ListTag onlySlotsTag = new ListTag();
        for (String slot : OnlySlots) {
            onlySlotsTag.add(StringTag.valueOf(slot));
        }
        tag.put("OnlySlots", onlySlotsTag);

        ListTag simpleWeightTag = new ListTag();
        for (Map.Entry<Double, Float> entry : simpleWeight.entrySet()) {
            CompoundTag weightEntry = new CompoundTag();
            weightEntry.putDouble("key", entry.getKey());
            weightEntry.putFloat("value", entry.getValue());
            simpleWeightTag.add(weightEntry);
        }
        tag.put("simpleWeight", simpleWeightTag);
        tag.merge(this.toNBT1());
        return tag;
    }

    public static ModifierAttriGether fromNBT(CompoundTag tag) {
        AttriGether attriGether1 = AttriGether.fromNBT1(tag);
        ModifierAttriGether attriGether = new ModifierAttriGether(attriGether1.attribute, attriGether1.modifier, attriGether1.slot);
        attriGether.weight = tag.getFloat("weight");
        attriGether.isRandom = tag.getBoolean("isRandom");
        attriGether.reserveDouble = tag.getInt("reserveDouble");
        attriGether.hasUUID = tag.getBoolean("hasUUID");
        attriGether.minValue = tag.getDouble("minValue");
        attriGether.maxValue = tag.getDouble("maxValue");
        attriGether.Expression = tag.getString("Expression");
        attriGether.IsAutoEquipmentSlot = tag.getBoolean("IsAutoEquipmentSlot");

        ListTag onlyItemsTag = tag.getList("OnlyItems", 8);
        for (int i = 0; i < onlyItemsTag.size(); i++) {
            attriGether.OnlyItems.add(onlyItemsTag.getString(i));
        }

        ListTag onlySlotsTag = tag.getList("OnlySlots", 8);
        for (int i = 0; i < onlySlotsTag.size(); i++) {
            attriGether.OnlySlots.add(onlySlotsTag.getString(i));
        }

        ListTag simpleWeightTag = tag.getList("simpleWeight", 10);
        for (int i = 0; i < simpleWeightTag.size(); i++) {
            CompoundTag weightEntry = simpleWeightTag.getCompound(i);
            double key = weightEntry.getDouble("key");
            float value = weightEntry.getFloat("value");
            attriGether.simpleWeight.put(key, value);
        }

        return attriGether;
    }

    @Override
    public String toString() {
        return "ModifierAttriGether{" +
                "weight=" + weight +
                ", isRandom=" + isRandom +
                ", hasUUID=" + hasUUID +
                ", OnlyItems=" + OnlyItems +
                ", OnlySlots=" + OnlySlots +
                ", slot=" + slot +
                ", IsAutoEquipmentSlot=" + IsAutoEquipmentSlot +
                ", attribute=" + attribute +
                ", modifier=" + modifier +
                '}';
    }

    public List<String> getOnlyItems() {
        return OnlyItems;
    }

    public List<String> getOnlySlots() {
        return OnlySlots;
    }


    public ModifierAttriGether(Attribute attribute, ExAttributeModifier modifier, EquipmentSlot slot) {
        super(attribute, modifier, slot);
    }

    public ModifierAttriGether(Attribute attribute, ExAttributeModifier modifier) {
        super(attribute, modifier);
    }


    public static List<ModifierAttriGether> GenerateModifierAttriGethers(String autokey, JsonObject obj) {
        int index = 0;
        List<ModifierAttriGether> attriGethers = new java.util.ArrayList<>();
        for (Map.Entry<String, JsonElement> attrGetherEntry : obj.entrySet()) {
            try {

                attriGethers.add(processAttrGether(autokey, attrGetherEntry, index));
                index++;

            } catch (Exception e) {
                Exmodifier.LOGGER.Logger.error("Error processing attrGether: " + attrGetherEntry.getKey(), e);
            }
        }
        return attriGethers;
    }

    private static ModifierAttriGether processAttrGether(String autokey, Map.Entry<String, JsonElement> attrGetherEntry, int index) {
        JsonObject attrGetherObj = attrGetherEntry.getValue().getAsJsonObject();
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attrGetherEntry.getKey()));
        double attrValue = attrGetherObj.get("value").getAsDouble();


//        if (attrGetherObj.has("autoUUID")){
//            if (attrGetherObj.get("autoUUID").getAsBoolean()) {
//                uuid = ExConfigHandle.autoUUid(ExConfigHandle.autoUUID);
//                ExConfigHandle.autoUUID++;
//            }
//        }
        AttributeModifier.Operation operation = ExConfigHandle.getOperation(attrGetherObj.get("operation").getAsString());
        EquipmentSlot slot = getEquipmentSlot(attrGetherObj);
        String modifierName = (attrGetherObj.has("modifierName")) ? attrGetherObj.get("modifierName").getAsString() : autokey + index;
        ;
        boolean autoName = false;
        if (attrGetherObj.has("autoName")) {
            if (attrGetherObj.has("autoName")) {
                if (attrGetherObj.get("autoName").getAsBoolean()) {

                    modifierName = autokey + index;
                    autoName = true;
                }
            }
        }
//        UUID uuid = (attrGetherObj.has("uuid") && !attrGetherObj.get("uuid").getAsString().isEmpty()) ? UUID.fromString(attrGetherObj.get("uuid").getAsString()) : UUID.nameUUIDFromBytes(modifierName.getBytes());
//        if (attrGetherObj.has("autoUUID") && attrGetherObj.get("autoUUID").getAsBoolean())
//            uuid = UUID.nameUUIDFromBytes(modifierName.getBytes());
//        //UUID uuid = ExConfigHandle.generateUUIDFromString(modifierName);
//        Exmodifier.LOGGER.debug("uuid " + uuid);
        ExAttributeModifier modifier = new ExAttributeModifier( modifierName, attrValue, operation);
        ModifierAttriGether attrGether = new ModifierAttriGether(attribute, modifier, slot);
        attrGether.IsAutoEquipmentSlot = attrGetherObj.has("isAutoEquipmentSlot") && attrGetherObj.get("isAutoEquipmentSlot").getAsBoolean();
        attrGether.hasUUID = attrGetherObj.has("uuid");
        if (!attrGether.IsAutoEquipmentSlot) {
            if (attrGetherObj.has("slot")) {
                if (!attrGetherObj.get("slot").getAsString().equals("auto")) {
                    attrGether.slot = EquipmentSlot.valueOf(attrGetherObj.get("slot").getAsString());
                } else {
                    attrGether.IsAutoEquipmentSlot = true;
                }
            }
        }
        if (attrGetherObj.has("weight")) {
            attrGether.weight = attrGetherObj.get("weight").getAsFloat();
        }
        if (attrGetherObj.has("isRandom")) {
            attrGether.isRandom = attrGetherObj.get("isRandom").getAsBoolean();
        }

        Exmodifier.LOGGER.debug("Attribute: " + attribute + " key: " + attrGetherEntry.getKey());
        return attrGether;


    }

    public MutableComponent GenerateTooltip(boolean canSeeWeight) {
        if (canSeeWeight)
            return (generateTooltipBase().append(Component.translatable("exmodifier.tooltip.weight")).append(Component.literal("§9" + weight)));
        return generateTooltipBase();
    }

    public Float getWeight() {
        return weight;
    }
}
