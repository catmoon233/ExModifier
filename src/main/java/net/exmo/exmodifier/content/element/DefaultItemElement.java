package net.exmo.exmodifier.content.element;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.exmo.exmodifier.util.ItemSelector;
import net.exmo.exmodifier.util.exSerialize.ExSerialize;
import net.minecraft.nbt.TagParser;
import net.minecraftforge.registries.ForgeRegistries;

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
        return  exElementInstants.stream().map(ExElementInstant.EX_SERIALIZE::toSingleJson).toList();
    }
    private void setExElementInstantsString(List<JsonObject> configs) {
        configs.forEach(config -> {
            // 解析每个配置对象中的 entries 数组
            var instants = ExElementInstant.EX_SERIALIZE.fromJsonSingle(
                    config
            );
            exElementInstants.add(instants);
        });
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
