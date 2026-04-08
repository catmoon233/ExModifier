package net.exmo.exmodifier.content.element;

import com.google.gson.JsonObject;
import net.exmo.exmodifier.util.ExRegistryHelper;
import net.exmo.exmodifier.util.ItemSelector;
import net.exmo.exmodifier.util.exSerialize.ExSerialize;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DefaultItemElement {
    public static final ExSerialize<DefaultItemElement> EX_SERIALIZE = ExSerialize.create(
            ()-> new DefaultItemElement(null,new ArrayList<>()))
            .addJsonObjectField("itemSelector",defaultItemElement -> ItemSelector.EX_SERIALIZE.toSingleJson(defaultItemElement.itemSelector),DefaultItemElement::setItemSelectorString)
            .addJsonObjectList("entries", DefaultItemElement::getExElementInstantsString,DefaultItemElement::setExElementInstantsString);
            ;


    // 正确用法示例
    private List<JsonObject> getExElementInstantsString() {
        return ExRegistryHelper.toJsonObjects(exElementInstants, ExElementInstant.EX_SERIALIZE);
    }
    private void setExElementInstantsString(List<JsonObject> configs) {
        exElementInstants.clear();
        exElementInstants.addAll(ExRegistryHelper.fromJsonObjects(configs, ExElementInstant.EX_SERIALIZE));
    }
    private void setItemSelectorString(JsonObject s) {
        this.itemSelector = (ItemSelector.EX_SERIALIZE.fromJsonSingle(s));
    }

    private  ItemSelector itemSelector;

    public List<ExElementInstant> getExElementInstants() {
        return exElementInstants;
    }

    public DefaultItemElement setExElementInstants(List<ExElementInstant> exElementInstants) {
        this.exElementInstants = exElementInstants;
        return this;
    }

    public ItemSelector getItemSelector() {
        return itemSelector;
    }

    public DefaultItemElement setItemSelector(ItemSelector itemSelector) {
        this.itemSelector = itemSelector;
        return this;
    }

    private  List<ExElementInstant> exElementInstants;

    public DefaultItemElement(ItemSelector itemSelector, List<ExElementInstant> exElementInstants) {
        this.itemSelector = itemSelector;
        this.exElementInstants = exElementInstants;
    }

    public ItemSelector itemSelector() {
        return itemSelector;
    }

    public List<ExElementInstant> exElementInstants() {
        return exElementInstants;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DefaultItemElement) obj;
        return Objects.equals(this.itemSelector, that.itemSelector) &&
                Objects.equals(this.exElementInstants, that.exElementInstants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemSelector, exElementInstants);
    }

    @Override
    public String toString() {
        return "DefaultItemElement[" +
                "itemSelector=" + itemSelector + ", " +
                "exElementInstants=" + exElementInstants + ']';
    }

}
