package net.exmo.exmodifier.content.quality;

import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

import javax.print.attribute.Attribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemQuality {
    public int rarity; //稀有度
    public List<ModifierEntry> entries = new ArrayList<>();
    public boolean cantRemoveEntry = false;
    public String Id;
    public float weight ;
    private boolean isShowInTooltip = true;
    public List<String> OnlyItemIds = new ArrayList<>();
    public List<String> OnlyItemTags = new ArrayList<>();
    public List<String> OnlyWashItems = new ArrayList<>();
    public ModifierEntry.Type type;
    public String LocalDescription; //描述
    public List<String> items = new ArrayList<>();
    public Map<Attribute,Double> attributeLowerLimit = new HashMap<>();
    public boolean autoRefresh = false;
    public int refineNum = 0;
    public boolean isRandom = true;
    public List<String> materials = new ArrayList<>();
    public ItemQuality(int rarity, String id) {
        this.rarity = rarity;
        this.Id = id;
    }
    public  boolean containTag(ItemStack stack){
        for (String tag : getOnlyItemTags() ){
            if (stack.is(ItemTags.create(new ResourceLocation(tag))))return true;
        }
        return false;
    }
    public int getRarity() {
        return rarity;
    }

    public void setRarity(int rarity) {
        this.rarity = rarity;
    }

    public List<ModifierEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<ModifierEntry> entries) {
        this.entries = entries;
    }

    public boolean isCantRemoveEntry() {
        return cantRemoveEntry;
    }

    public void setCantRemoveEntry(boolean cantRemoveEntry) {
        this.cantRemoveEntry = cantRemoveEntry;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        this.Id = id;
    }

    public List<String> getOnlyItemIds() {
        return OnlyItemIds;
    }

    public void setOnlyItemIds(List<String> onlyItemIds) {
        OnlyItemIds = onlyItemIds;
    }

    public List<String> getOnlyItemTags() {
        return OnlyItemTags;
    }

    public void setOnlyItemTags(List<String> onlyItemTags) {
        OnlyItemTags = onlyItemTags;
    }

    public List<String> getOnlyWashItems() {
        return OnlyWashItems;
    }

    public void setOnlyWashItems(List<String> onlyWashItems) {
        OnlyWashItems = onlyWashItems;
    }

    public ModifierEntry.Type getType() {
        return type;
    }

    public void setType(ModifierEntry.Type type) {
        this.type = type;
    }

    public String getLocalDescription() {
        return LocalDescription;
    }

    public void setLocalDescription(String localDescription) {
        LocalDescription = localDescription;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public Map<Attribute, Double> getAttributeLowerLimit() {
        return attributeLowerLimit;
    }

    public void setAttributeLowerLimit(Map<Attribute, Double> attributeLowerLimit) {
        this.attributeLowerLimit = attributeLowerLimit;
    }

    public boolean isAutoRefresh() {
        return autoRefresh;
    }

    public void setAutoRefresh(boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
    }

    public int getRefineNum() {
        return refineNum;
    }

    public void setRefineNum(int refineNum) {
        this.refineNum = refineNum;
    }

    public boolean isRandom() {
        return isRandom;
    }

    public void setRandom(boolean random) {
        isRandom = random;
    }

    public List<String> getMaterials() {
        return materials;
    }

    public void setMaterials(List<String> materials) {
        this.materials = materials;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public boolean isShowInTooltip() {
        return isShowInTooltip;
    }

    public void setShowInTooltip(boolean showInTooltip) {
        isShowInTooltip = showInTooltip;
    }
}