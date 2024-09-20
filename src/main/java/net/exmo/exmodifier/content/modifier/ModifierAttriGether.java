package net.exmo.exmodifier.content.modifier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.util.AttriGether;
import net.exmo.exmodifier.util.ExConfigHandle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.getEquipmentSlot;

public class ModifierAttriGether extends AttriGether {

    public float weight = 0;
    public boolean isRandom = false;
    public int reserveDouble = 1;
    public boolean hasUUID =false;
    public double minValue = 0;
    public double maxValue = 0;
    public Map<Double, Float> simpleWeight = new HashMap<>();

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

    public List<String> OnlyItems = new java.util.ArrayList<>();
    public  List<String> OnlySlots = new java.util.ArrayList<>();


    public ModifierAttriGether(Attribute attribute, AttributeModifier modifier, EquipmentSlot slot) {
        super(attribute, modifier,slot);
    }

    public ModifierAttriGether(Attribute attribute, AttributeModifier modifier) {
        super(attribute, modifier);
    }

    public boolean isHasUUID() {
        return !this.modifier.getId().toString().isEmpty();
    }
    public static List<ModifierAttriGether> GenerateModifierAttriGethers(String autokey,JsonObject obj) {
        int index = 0;
        List<ModifierAttriGether> attriGethers = new java.util.ArrayList<>();
        for (Map.Entry<String, JsonElement> attrGetherEntry : obj.entrySet()) {
            try {

                attriGethers.add(processAttrGether(autokey, attrGetherEntry,index));
                index++;

            } catch (Exception e) {
                Exmodifier.LOGGER.error("Error processing attrGether: " + attrGetherEntry.getKey(), e);
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
        String modifierName = (attrGetherObj.has("modifierName")) ? attrGetherObj.get("modifierName").getAsString() :autokey + index;;

        if (attrGetherObj.has("autoName")){
            if (attrGetherObj.has("autoName")) {
                if (attrGetherObj.get("autoName").getAsBoolean()) {
                    modifierName = autokey + index;

                }
            }
        }
        UUID uuid = (attrGetherObj.has("uuid") && !attrGetherObj.get("uuid").getAsString().isEmpty()) ? UUID.fromString(attrGetherObj.get("uuid").getAsString()) : UUID.nameUUIDFromBytes(modifierName.getBytes());
        if(attrGetherObj.has("autoUUID") && attrGetherObj.get("autoUUID").getAsBoolean()) uuid = UUID.nameUUIDFromBytes(modifierName.getBytes());
        //UUID uuid = ExConfigHandle.generateUUIDFromString(modifierName);
        Exmodifier.LOGGER.debug("uuid "+uuid);
        AttributeModifier modifier = new AttributeModifier(uuid, modifierName, attrValue, operation);
        ModifierAttriGether attrGether = new ModifierAttriGether(attribute, modifier, slot);
        attrGether.IsAutoEquipmentSlot = attrGetherObj.has("isAutoEquipmentSlot") && attrGetherObj.get("isAutoEquipmentSlot").getAsBoolean();
        attrGether.hasUUID = attrGetherObj.has("uuid");
        if (!attrGether.IsAutoEquipmentSlot){
            if (attrGetherObj.has("slot")) {
                if (!attrGetherObj.get("slot").getAsString().equals("auto")) {
                    attrGether.slot = EquipmentSlot.valueOf(attrGetherObj.get("slot").getAsString());
                }else {
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
}
