package net.exmo.exmodifier.content.modifier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModifierEntryDataBuilder {

    private final ModifierEntry entry;

    public ModifierEntryDataBuilder() {
        this.entry = new ModifierEntry();
    }

    public ModifierEntryDataBuilder(ModifierEntry entry) {
        this.entry = entry;
    }

    public ModifierEntryDataBuilder setWeight(float weight) {
        entry.weight = weight;
        return this;
    }

    public ModifierEntryDataBuilder setCantSelect(boolean cantSelect) {
        entry.cantSelect = cantSelect;
        return this;
    }

    public ModifierEntryDataBuilder setIsRandom(boolean isRandom) {
        entry.isRandom = isRandom;
        return this;
    }

    public ModifierEntryDataBuilder setOnlyHasThisEntry(boolean OnlyHasThisEntry) {
        entry.OnlyHasThisEntry = OnlyHasThisEntry;
        return this;
    }

    public ModifierEntryDataBuilder setLocalDescription(String localDescription) {
        entry.localDescription = localDescription;
        return this;
    }

    public ModifierEntryDataBuilder setMaxLevel(int maxLevel) {
        entry.maxLevel = maxLevel;
        return this;
    }

    public ModifierEntryDataBuilder setType(ModifierEntry.Type type) {
        entry.type = type;
        return this;
    }

    public ModifierEntryDataBuilder setSpecialTags(List<String> specialTags) {
        entry.specialTags.addAll(specialTags);
        return this;
    }

    public ModifierEntryDataBuilder setIsCuriosEntry(boolean isCuriosEntry) {
        entry.isCuriosEntry = isCuriosEntry;
        return this;
    }

    public ModifierEntryDataBuilder setNeedFreshValue(float needFreshValue) {
        entry.needFreshValue = needFreshValue;
        return this;
    }

    public ModifierEntryDataBuilder setCuriosType(String curiosType) {
        entry.curiosType = curiosType;
        return this;
    }

    public ModifierEntryDataBuilder setOnlyTags(List<String> onlyTags) {
        entry.OnlyTags.addAll(onlyTags);
        return this;
    }

    public ModifierEntryDataBuilder setOnlyItems(List<String> onlyItems) {
        entry.OnlyItems.addAll(onlyItems);
        return this;
    }

    public ModifierEntryDataBuilder setOnlyWashItems(List<String> onlyWashItems) {
        entry.OnlyWashItems.addAll(onlyWashItems);
        return this;
    }

    public ModifierEntryDataBuilder setCommands(List<String> commands) {
        entry.Commands.addAll(commands);
        return this;
    }

    public ModifierEntryDataBuilder setId(String id) {
        entry.id = id;
        return this;
    }

    public ModifierEntryDataBuilder setExpression(String expression) {
        entry.Expression = expression;
        return this;
    }

    public ModifierEntryDataBuilder setRandomNum(int randomNum) {
        entry.RandomNum = randomNum;
        return this;
    }

    public ModifierEntryDataBuilder setAttriGether(List<ModifierAttriGether> attriGether) {
        entry.attriGether.addAll(attriGether);
        return this;
    }

    public ModifierEntry build() {
        return entry;
    }
    public JsonElement toJson() {
        JsonObject json = new JsonObject();

        // Check and add non-default values
        if (entry.weight != 1.0f) json.addProperty("weight", entry.weight);
        if (entry.cantSelect) json.addProperty("cantSelect", entry.cantSelect);
        if (entry.isRandom != true) json.addProperty("isRandom", entry.isRandom);
        if (entry.OnlyHasThisEntry) json.addProperty("OnlyHasThisEntry", true);
        if (entry.localDescription != null && !entry.localDescription.isEmpty()) json.addProperty("localDescription", entry.localDescription);
        if (entry.maxLevel != 1) json.addProperty("maxLevel", entry.maxLevel);
        if (entry.type != ModifierEntry.Type.UNKNOWN) json.addProperty("type", entry.type.name());
        if (entry.curiosType != null && !entry.curiosType.isEmpty()) json.addProperty("curiosType", entry.curiosType);
        if (entry.isCuriosEntry) json.addProperty("isCuriosEntry", entry.isCuriosEntry);
        if (entry.needFreshValue != 0.0f) json.addProperty("needFreshValue", entry.needFreshValue);

        if (!entry.specialTags.isEmpty()) {
            JsonArray specialTagsArray = new JsonArray();
            for (String tag : entry.specialTags) {
                specialTagsArray.add(new JsonPrimitive(tag));
            }
            json.add("specialTags", specialTagsArray);
        }

        if (!entry.OnlyTags.isEmpty()) {
            JsonArray onlyTagsArray = new JsonArray();
            for (String tag : entry.OnlyTags) {
                onlyTagsArray.add(new JsonPrimitive(tag));
            }
            json.add("OnlyTags", onlyTagsArray);
        }

        if (!entry.OnlyItems.isEmpty()) {
            JsonArray onlyItemsArray = new JsonArray();
            for (String item : entry.OnlyItems) {
                onlyItemsArray.add(new JsonPrimitive(item));
            }
            json.add("OnlyItems", onlyItemsArray);
        }

        if (!entry.OnlyWashItems.isEmpty()) {
            JsonArray onlyWashItemsArray = new JsonArray();
            for (String item : entry.OnlyWashItems) {
                onlyWashItemsArray.add(new JsonPrimitive(item));
            }
            json.add("OnlyWashItems", onlyWashItemsArray);
        }

        if (!entry.Commands.isEmpty()) {
            JsonArray commandsArray = new JsonArray();
            for (String command : entry.Commands) {
                commandsArray.add(new JsonPrimitive(command));
            }
            json.add("Commands", commandsArray);
        }

        if (entry.id != null && !entry.id.isEmpty()) json.addProperty("id", entry.id.substring(2));
        if (entry.Expression != null && !entry.Expression.isEmpty()) json.addProperty("Expression", entry.Expression);
        if (entry.RandomNum != 0) json.addProperty("RandomNum", entry.RandomNum);

        if (!entry.attriGether.isEmpty()) {
            JsonArray attriGetherArray = new JsonArray();
            for (int i = 0; i < entry.attriGether.size(); i++) {
                ModifierAttriGether attriGether = entry.attriGether.get(i);
                JsonObject attriGetherJson = new JsonObject();
                attriGetherJson.addProperty("id",ForgeRegistries.ATTRIBUTES.getKey( attriGether.getAttribute()).toString());
                if (attriGether.weight != 0.0f) attriGetherJson.addProperty("weight", attriGether.weight);
                if (attriGether.getModifier().getAmount() != 0.0f) attriGetherJson.addProperty("value", attriGether.getModifier().getAmount());
                if (attriGether.slot != null && !attriGether.slot.equals(EquipmentSlot.MAINHAND)) attriGetherJson.addProperty("slot", attriGether.slot.name());
                if (attriGether.getModifier().getOperation() != null && !attriGether.getModifier().getOperation().equals(AttributeModifier.Operation.ADDITION)) attriGetherJson.addProperty("operation", attriGether.getModifier().getOperation().name());
                if (attriGether.minValue != 0.0) attriGetherJson.addProperty("minValue", attriGether.minValue);
                if (attriGether.maxValue != attriGether.minValue) attriGetherJson.addProperty("maxValue", attriGether.maxValue);
                if (attriGether.reserveDouble != 1) attriGetherJson.addProperty("reserveDouble", attriGether.reserveDouble);
                if (attriGether.Expression != null && !attriGether.Expression.isEmpty()) attriGetherJson.addProperty("ValueExpression", attriGether.Expression);
                if (attriGether.simpleWeight != null && !attriGether.simpleWeight.isEmpty()) {
                    JsonObject simpleWeightObj = new JsonObject();
                    for (Map.Entry<Double, Float> entry : attriGether.simpleWeight.entrySet()) {
                        simpleWeightObj.addProperty(entry.getKey().toString(), entry.getValue());
                    }
                    attriGetherJson.add("mayValues", new JsonArray());
                    attriGetherJson.add("mayValuesWeight", new JsonArray());
                    for (double value : attriGether.simpleWeight.keySet()) {
                        attriGetherJson.getAsJsonArray("mayValues").add(value);
                    }
                    for (float weight : attriGether.simpleWeight.values()) {
                        attriGetherJson.getAsJsonArray("mayValuesWeight").add(weight);
                    }
                }
                if (attriGether.IsAutoEquipmentSlot) attriGetherJson.addProperty("isAutoEquipmentSlot", true);
                if (attriGether.hasUUID) attriGetherJson.addProperty("hasUUID", attriGether.hasUUID);
                if (attriGether.isRandom) attriGetherJson.addProperty("isRandom", attriGether.isRandom);

                attriGetherJson.addProperty("modifierName", attriGether.getModifier().getName());
                if (attriGether.getModifier().getId() != null) attriGetherJson.addProperty("uuid", attriGether.getModifier().getId().toString());

                attriGetherJson.addProperty("attribute", attriGether.attribute.getDescriptionId());

                attriGetherArray.add(attriGetherJson);
            }
            json.add("attriGethers", attriGetherArray);
        } else {
            json.add("attriGethers", new JsonArray());
        }
        return json;
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("weight", entry.weight);
        tag.putBoolean("cantSelect", entry.cantSelect);
        tag.putBoolean("isRandom", entry.isRandom);
        tag.putBoolean("OnlyHasThisEntry", entry.OnlyHasThisEntry);
        tag.putString("localDescription", entry.localDescription);
        tag.putInt("maxLevel", entry.maxLevel);
        tag.putString("type", entry.type.name());
        tag.putString("curiosType", entry.curiosType);
        tag.putBoolean("isCuriosEntry", entry.isCuriosEntry);
        tag.putFloat("needFreshValue", entry.needFreshValue);

        ListTag specialTagsTag = new ListTag();
        for (String tagStr : entry.specialTags) {
            specialTagsTag.add(StringTag.valueOf(tagStr));
        }
        tag.put("specialTags", specialTagsTag);

        ListTag onlyTagsTag = new ListTag();
        for (String tagStr : entry.OnlyTags) {
            onlyTagsTag.add(StringTag.valueOf(tagStr));
        }
        tag.put("OnlyTags", onlyTagsTag);

        ListTag onlyItemsTag = new ListTag();
        for (String itemStr : entry.OnlyItems) {
            onlyItemsTag.add(StringTag.valueOf(itemStr));
        }
        tag.put("OnlyItems", onlyItemsTag);

        ListTag onlyWashItemsTag = new ListTag();
        for (String itemStr : entry.OnlyWashItems) {
            onlyWashItemsTag.add(StringTag.valueOf(itemStr));
        }
        tag.put("OnlyWashItems", onlyWashItemsTag);

        ListTag commandsTag = new ListTag();
        for (String commandStr : entry.Commands) {
            commandsTag.add(StringTag.valueOf(commandStr));
        }
        tag.put("Commands", commandsTag);

        tag.putString("id", entry.id);
        tag.putString("Expression", entry.Expression);
        tag.putInt("RandomNum", entry.RandomNum);

        ListTag attriGetherTag = new ListTag();
        for (ModifierAttriGether attriGether : entry.attriGether) {
            attriGetherTag.add(attriGether.toNBT());
        }
        tag.put("attriGether", attriGetherTag);

        return tag;
    }

    public static ModifierEntryDataBuilder fromNBT(CompoundTag tag) {
        ModifierEntryDataBuilder builder = new ModifierEntryDataBuilder();
        builder.setWeight(tag.getFloat("weight"));
        builder.setCantSelect(tag.getBoolean("cantSelect"));
        builder.setIsRandom(tag.getBoolean("isRandom"));
        builder.setOnlyHasThisEntry(tag.getBoolean("OnlyHasThisEntry"));
        builder.setLocalDescription(tag.getString("localDescription"));
        builder.setMaxLevel(tag.getInt("maxLevel"));
        builder.setType(ModifierEntry.StringToType(tag.getString("type")));
        builder.setCuriosType(tag.getString("curiosType"));
        builder.setIsCuriosEntry(tag.getBoolean("isCuriosEntry"));
        builder.setNeedFreshValue(tag.getFloat("needFreshValue"));

        ListTag specialTagsTag = tag.getList("specialTags", 8);
        for (int i = 0; i < specialTagsTag.size(); i++) {
            builder.setSpecialTags(List.of(specialTagsTag.getString(i)));
        }

        ListTag onlyTagsTag = tag.getList("OnlyTags", 8);
        for (int i = 0; i < onlyTagsTag.size(); i++) {
            builder.setOnlyTags(List.of(onlyTagsTag.getString(i)));
        }

        ListTag onlyItemsTag = tag.getList("OnlyItems", 8);
        for (int i = 0; i < onlyItemsTag.size(); i++) {
            builder.setOnlyItems(List.of(onlyItemsTag.getString(i)));
        }

        ListTag onlyWashItemsTag = tag.getList("OnlyWashItems", 8);
        for (int i = 0; i < onlyWashItemsTag.size(); i++) {
            builder.setOnlyWashItems(List.of(onlyWashItemsTag.getString(i)));
        }

        ListTag commandsTag = tag.getList("Commands", 8);
        for (int i = 0; i < commandsTag.size(); i++) {
            builder.setCommands(List.of(commandsTag.getString(i)));
        }

        builder.setId(tag.getString("id"));
        builder.setExpression(tag.getString("Expression"));
        builder.setRandomNum(tag.getInt("RandomNum"));

        ListTag attriGetherTag = tag.getList("attriGether", 10);
        for (int i = 0; i < attriGetherTag.size(); i++) {
            CompoundTag attriGetherTagCompound = attriGetherTag.getCompound(i);
            builder.setAttriGether(List.of(ModifierAttriGether.fromNBT(attriGetherTagCompound)));
        }

        return builder;
    }

}