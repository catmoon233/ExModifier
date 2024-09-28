package net.exmo.exmodifier.content.level;

import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemLevel
{
    public String id ;
    public ModifierEntry.Type type;
    public boolean isCuriosEntry;

    public String getLevelExpression() {
        return LevelExpression;
    }

    public void setLevelExpression(String levelExpression) {
        LevelExpression = levelExpression;
    }

    public String LevelExpression;
    public List<String> getOnlyWashItems() {
        return OnlyWashItems;

    }

    public void setOnlyWashItems(List<String> onlyWashItems) {
        OnlyWashItems = onlyWashItems;
    }

    public List<String> OnlyWashItems = new ArrayList<>();

    public String getCuriosType() {
        return curiosType;
    }

    @Override
    public String toString() {
        return "ItemLevel{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", OnlyWashItems=" + OnlyWashItems +
                ", curiosType='" + curiosType + '\'' +
                ", needFreshValue=" + needFreshValue +
                ", cantSelect=" + cantSelect +
                ", weight=" + weight +
                ", OnlyItemIds=" + OnlyItemIds +
                ", OnlyItemTags=" + OnlyItemTags +
                ", MaxLevel=" + MaxLevel +
                ", DefaultLevel=" + DefaultLevel +
                ", XpAddExpression='" + XpAddExpression + '\'' +
                ", attriGethers=" + attriGethers +
                ", UpEvent='" + UpEvent + '\'' +
                ", XpAddValue='" + XpAddValue + '\'' +
                '}';
    }

    public void setCuriosType(String curiosType) {
        this.curiosType = curiosType;
    }

    public String curiosType;

    public float getNeedFreshValue() {
        return needFreshValue =0;
    }
    public  boolean containTag(ItemStack stack){
        for (String tag : getOnlyItemTags() ){
            if (stack.is(ItemTags.create(new ResourceLocation(tag))))return true;
        }
        return false;
    }
    public void setNeedFreshValue(float needFreshValue) {
        this.needFreshValue = needFreshValue;
    }

    public float needFreshValue = 0;

    public boolean isCantSelect() {
        return cantSelect;
    }

    public void setCantSelect(boolean cantSelect) {
        this.cantSelect = cantSelect;
    }

    public boolean cantSelect = false;
    private float weight =0;
    public List<String> OnlyItemIds = new ArrayList<>();
    public List<String> OnlyItemTags = new ArrayList<>();
    public int MaxLevel;
    public int DefaultLevel;
    private String XpAddExpression;
    public List<LevelAttriGether> attriGethers = new ArrayList<>();
    public String UpEvent ;
    public String XpAddValue ;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ModifierEntry.Type getType() {
        return type;
    }

    public void setType(ModifierEntry.Type type) {
        this.type = type;
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

    public int getMaxLevel() {
        return MaxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        MaxLevel = maxLevel;
    }

    public int getDefaultLevel() {
        return DefaultLevel;
    }

    public void setDefaultLevel(int defaultLevel) {
        DefaultLevel = defaultLevel;
    }

    public List<LevelAttriGether> getAttriGethers() {
        return attriGethers;
    }

    public void setAttriGethers(List<LevelAttriGether> attriGethers) {
        this.attriGethers = attriGethers;
    }

    public String getUpEvent() {
        return UpEvent;
    }

    public void setUpEvent(String upEvent) {
        UpEvent = upEvent;
    }

    public String getXpAddValue() {
        return XpAddValue;
    }

    public void setXpAddValue(String xpAddValue) {
        XpAddValue = xpAddValue;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public String getXpAddExpression() {
        return XpAddExpression;
    }

    public void setXpAddExpression(String xpAddExpression) {
        XpAddExpression = xpAddExpression;
    }
}