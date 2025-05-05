package net.exmo.exmodifier.content.modifier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.exmo.exmodifier.content.type.ExType;
import net.exmo.exmodifier.content.type.ExTypeHandle;
import net.exmo.exmodifier.content.type.ItemType;
import net.exmo.exmodifier.util.ExUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;


import java.util.ArrayList;
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

    public ModifierEntryDataBuilder setIcon(String icon) {
        entry.icon = icon;
        return this;
    }

    public ModifierEntryDataBuilder setOnlyHasThisEntry(boolean OnlyHasThisEntry) {
        entry.OnlyHasThisEntry = OnlyHasThisEntry;
        return this;
    }

    public ModifierEntryDataBuilder setTags(List<TagKey<ModifierEntry>> tags) {
        entry.tags = tags;
        return this;
    }

    public ModifierEntryDataBuilder addTag(TagKey<ModifierEntry> tag) {
        entry.tags.add(tag);
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

    public ModifierEntryDataBuilder setType(List<ItemType> type) {
        entry.types = type;
        return this;
    }

    public ModifierEntryDataBuilder setSlots(List<String> slots) {
        entry.Slots = slots;
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

    public ModifierEntryDataBuilder setDisplayNameInItemName(boolean displayNameInItemName) {
        entry.displayNameInItemName = displayNameInItemName;
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
        entry.getModifierItemSelector().setOnlyTags(onlyTags);
        return this;
    }

    public ModifierEntryDataBuilder setOnlyItems(List<String> onlyItems) {
        entry.getModifierItemSelector().setOnlyItems(onlyItems);
        return this;
    }

    public ModifierEntryDataBuilder setOnlyWashItems(List<String> onlyWashItems) {
        entry.getModifierItemSelector().setOnlyItems(onlyWashItems);
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

        // Check and add non-default itemTypes
        if (entry.weight != 1.0f) json.addProperty("weight", entry.weight);
        if (entry.cantSelect) json.addProperty("cantSelect", true);
        if (!entry.isRandom) json.addProperty("isRandom", false);
        if (entry.OnlyHasThisEntry) json.addProperty("OnlyHasThisEntry", true);
        if (entry.localDescription != null && !entry.localDescription.isEmpty())
            json.addProperty("localDescription", entry.localDescription);
        if (entry.maxLevel != 1) json.addProperty("maxLevel", entry.maxLevel);
        if (entry.curiosType != null && !entry.curiosType.isEmpty()) json.addProperty("curiosType", entry.curiosType);
        if (entry.isCuriosEntry) json.addProperty("isCuriosEntry", true);
        if (entry.displayNameInItemName) json.addProperty("displayNameInItemName", true);
        if (entry.needFreshValue != 0.0f) json.addProperty("needFreshValue", entry.needFreshValue);

        if (!entry.specialTags.isEmpty()) {
            JsonArray specialTagsArray = new JsonArray();
            for (String tag : entry.specialTags) {
                specialTagsArray.add(new JsonPrimitive(tag));
            }
            json.add("specialTags", specialTagsArray);
        }
        if (!entry.tags.isEmpty()) {
            JsonArray tagsArray = new JsonArray();
            for (var tag : entry.tags) {
                String string = tag.location().toString();
                tagsArray.add(new JsonPrimitive(string));
            }
            json.add("tags", tagsArray);
        }
        if (!entry.types.contains(ExType.UNKNOWN.get())) {
            JsonArray typesArray = new JsonArray();
            for (var tag : entry.types) {
                typesArray.add(new JsonPrimitive(tag.name()));
            }
            json.add("types", typesArray);
            //  json.addProperty("type", entry.type.name());
        }

        List<String> onlyTags = entry.getModifierItemSelector().getOnlyTags();
        if (!onlyTags.isEmpty()) {
            JsonArray onlyTagsArray = new JsonArray();
            for (String tag : onlyTags) {
                onlyTagsArray.add(new JsonPrimitive(tag));
            }
            json.add("OnlyTags", onlyTagsArray);
        }

        List<String> onlyItems = entry.getModifierItemSelector().getOnlyItems();
        if (!onlyItems.isEmpty()) {
            JsonArray onlyItemsArray = new JsonArray();
            for (String item : onlyItems) {
                onlyItemsArray.add(new JsonPrimitive(item));
            }
            json.add("OnlyItems", onlyItemsArray);
        }

        List<String> onlyWashItems = entry.getModifierItemSelector().getOnlyWashItems();
        if (!onlyWashItems.isEmpty()) {
            JsonArray onlyWashItemsArray = new JsonArray();
            for (String item : onlyWashItems) {
                onlyWashItemsArray.add(new JsonPrimitive(item));
            }
            json.add("OnlyWashItems", onlyWashItemsArray);
        }


        if (entry.id != null && !entry.id.isEmpty()) json.addProperty("id", entry.id.substring(2));
        if (entry.Expression != null && !entry.Expression.isEmpty()) json.addProperty("Expression", entry.Expression);
        if (entry.icon != null && !entry.icon.isEmpty()) json.addProperty("icon", entry.icon);
        if (entry.RandomNum != 0) json.addProperty("RandomNum", entry.RandomNum);

        if (!entry.attriGether.isEmpty()) {
            JsonArray attriGetherArray = new JsonArray();
            for (int i = 0; i < entry.attriGether.size(); i++) {
                ModifierAttriGether attriGether = entry.attriGether.get(i);
                JsonObject attriGetherJson = new JsonObject();
                attriGetherJson.addProperty("id", ForgeRegistries.ATTRIBUTES.getKey(attriGether.getAttribute()).toString());
                if (attriGether.weight != 0.0f) attriGetherJson.addProperty("weight", attriGether.weight);
                if (attriGether.getModifier().getAmount() != 0.0f)
                    attriGetherJson.addProperty("value", attriGether.getModifier().getAmount());
                if (attriGether.slot != null && !attriGether.slot.equals(EquipmentSlot.MAINHAND))
                    attriGetherJson.addProperty("slot", attriGether.slot.name());
                if (attriGether.getModifier().getOperation() != null && !attriGether.getModifier().getOperation().equals(AttributeModifier.Operation.ADDITION))
                    attriGetherJson.addProperty("operation", attriGether.getModifier().getOperation().name());
                if (attriGether.minValue != 0.0) attriGetherJson.addProperty("minValue", attriGether.minValue);
                if (attriGether.maxValue != attriGether.minValue)
                    attriGetherJson.addProperty("maxValue", attriGether.maxValue);
                if (attriGether.reserveDouble != 1)
                    attriGetherJson.addProperty("reserveDouble", attriGether.reserveDouble);
                if (attriGether.Expression != null && !attriGether.Expression.isEmpty())
                    attriGetherJson.addProperty("ValueExpression", attriGether.Expression);
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
//                if (attriGether.getModifier().getId() != null)
//                    attriGetherJson.addProperty("uuid", attriGether.getModifier().getId().toString());

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
        tag.putString("icon", entry.icon);
        tag.putInt("maxLevel", entry.maxLevel);
        //tag.putString("type", entry.type.name());
        tag.putString("curiosType", entry.curiosType);
        tag.putBoolean("isCuriosEntry", entry.isCuriosEntry);
        tag.putBoolean("displayNameInItemName", entry.displayNameInItemName);
        tag.putFloat("needFreshValue", entry.needFreshValue);

        ListTag specialTagsTag = new ListTag();
        for (String tagStr : entry.specialTags) {
            specialTagsTag.add(StringTag.valueOf(tagStr));
        }
        tag.put("specialTags", specialTagsTag);
        ListTag slotsTags = new ListTag();
        for (String tagStr : entry.Slots) {
            slotsTags.add(StringTag.valueOf(tagStr));
        }
        tag.put("slotsTags", slotsTags);

        ListTag tags = new ListTag();
        for (var tagStr : entry.tags) {
            tags.add(StringTag.valueOf(tagStr.location().toString()));
        }
        tag.put("tags", tags);

        ListTag onlyTagsTag = new ListTag();
        for (String tagStr : entry.getModifierItemSelector().getOnlyTags()) {
            onlyTagsTag.add(StringTag.valueOf(tagStr));
        }
        tag.put("OnlyTags", onlyTagsTag);

        ListTag typesTag = new ListTag();
        for (var tagStr : entry.types) {
            typesTag.add(StringTag.valueOf(tagStr.name()));
        }
        tag.put("types", typesTag);

        ListTag onlyItemsTag = new ListTag();
        for (String itemStr : entry.getModifierItemSelector().getOnlyItems()) {
            onlyItemsTag.add(StringTag.valueOf(itemStr));
        }
        tag.put("OnlyItems", onlyItemsTag);

        ListTag onlyWashItemsTag = new ListTag();
        for (String itemStr : entry.getModifierItemSelector().getOnlyWashItems()) {
            onlyWashItemsTag.add(StringTag.valueOf(itemStr));
        }
        tag.put("OnlyWashItems", onlyWashItemsTag);


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
        // builder.setType(ModifierEntry.StringToType(tag.getString("type")));
        builder.setCuriosType(tag.getString("curiosType"));
        builder.setIsCuriosEntry(tag.getBoolean("isCuriosEntry"));
        builder.setNeedFreshValue(tag.getFloat("needFreshValue"));
        builder.setDisplayNameInItemName(tag.getBoolean("displayNameInItemName"));
        builder.setIcon(tag.getString("icon"));

        ListTag specialTagsTag = tag.getList("specialTags", 8);
        List<String> specialTags = new ArrayList<>();
        for (int i = 0; i < specialTagsTag.size(); i++) {
            specialTags.add(specialTagsTag.getString(i));
        }
        builder.setSpecialTags(specialTags);
        ListTag typesTag = tag.getList("types", 8);
        List<ItemType> types = new ArrayList<>();
        for (int i = 0; i < typesTag.size(); i++) {
            types.add(ExTypeHandle.itemTypes.get(typesTag.getString(i)));
        }
        builder.setType(types);
        ListTag tags = tag.getList("tags", 8);
        for (net.minecraft.nbt.Tag value : tags) {
            builder.addTag(ExUtil.createOrGetModifierTagKey(ResourceLocation.tryParse(value.getAsString())));
        }

        ListTag onlyTagsTag = tag.getList("OnlyTags", 8);
        List<String> onlyTags = new ArrayList<>();
        for (int i = 0; i < onlyTagsTag.size(); i++) {
            onlyTags.add(onlyTagsTag.getString(i));
        }
        builder.setOnlyTags(onlyTags);

        ListTag onlyItemsTag = tag.getList("OnlyItems", 8);
        List<String> onlyItems = new ArrayList<>();
        for (int i = 0; i < onlyItemsTag.size(); i++) {
            onlyItems.add(onlyItemsTag.getString(i));
        }
        builder.setOnlyItems(onlyItems);

        ListTag slotsTags = tag.getList("slotsTags", 8);
        List<String> slots = new ArrayList<>();
        for (int i = 0; i < slotsTags.size(); i++) {
            slots.add(slotsTags.getString(i));
        }
        builder.setSlots(slots);

        ListTag onlyWashItemsTag = tag.getList("OnlyWashItems", 8);
        List<String> onlyWashItems = new ArrayList<>();
        for (int i = 0; i < onlyWashItemsTag.size(); i++) {
            onlyWashItems.add(onlyWashItemsTag.getString(i));
        }
        builder.setOnlyWashItems(onlyWashItems);


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