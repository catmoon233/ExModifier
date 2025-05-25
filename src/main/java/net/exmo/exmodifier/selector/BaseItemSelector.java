package net.exmo.exmodifier.selector;

import com.google.gson.Gson;
import com.mojang.datafixers.types.Func;
import com.mojang.serialization.Codec;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.util.ExUtil;
import net.exmo.exmodifier.util.exSerialize.ExSerialize;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.Tags;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;



public class BaseItemSelector<T> {
    public static ExSerialize<Object> ExSer = ExSerialize.create(BaseItemSelector::new)
        .addStringListField("onlyTags", e -> ((BaseItemSelector) e).OnlyTags, (s, v) -> ((BaseItemSelector) s).OnlyTags = v)
        .addStringListField("onlyItems", e -> ((BaseItemSelector) e).OnlyItems, (s, v) -> ((BaseItemSelector) s).OnlyItems = v)
        .addStringListField("unlessItemTags", e -> ((BaseItemSelector) e).UnlessItemTags, (s, v) -> ((BaseItemSelector) s).UnlessItemTags = v)
        .addStringListField("unlessItemIds", e -> ((BaseItemSelector) e).UnlessItemIds, (s, v) -> ((BaseItemSelector) s).UnlessItemIds = v)
        .addStringListField("onlySlots", e -> ((BaseItemSelector) e).OnlySlots, (s, v) -> ((BaseItemSelector) s).OnlySlots = v);

    protected static final Codec<BaseItemSelector<?>> CODEC = Codec.unit(BaseItemSelector::new);



    public T setUnlessItemTags(List<String> unlessItemTags) {
        UnlessItemTags = unlessItemTags;
        return (T) this;
    }

    public T setOnlyTags(List<String> onlyTags) {
        OnlyTags = onlyTags;
        return (T) this;
    }

    public T setOnlyItems(List<String> onlyItems) {
        OnlyItems = onlyItems;
        return (T) this;
    }

    public T setUnlessItemIds(List<String> unlessItemIds) {
        UnlessItemIds = unlessItemIds;
        return (T) this;
    }

    public T setOnlySlots(List<String> onlySlots) {
        OnlySlots = onlySlots;
        return (T) this;
    }

    protected List<String>  onlyTypes;
    protected  List<String> UnlessItemTags = new ArrayList<>();
    protected  List<String> UnlessItemIds = new ArrayList<>();
    protected  List<String> OnlySlots = new ArrayList<>();
    protected  List<String> OnlyTags = new ArrayList<>();
    protected  List<String> OnlyItems = new ArrayList<>();

    public T addOnlyTag(String tag) {
        OnlyTags.add(tag);
        return (T) this;
    }

    public T addOnlyItem(String item) {
        OnlyItems.add(item);
        return (T) this;
    }

    public T addUnlessItemTag(String tag) {
        UnlessItemTags.add(tag);
        return (T) this;
    }

    public T addUnlessItemId(String id) {
        UnlessItemIds.add(id);
        return (T) this;
    }

    public T addOnlySlot(String slot) {
        OnlySlots.add(slot);
        return (T) this;
    }

    // 批量添加元素的方法
    public T addOnlyTags(List<String> tags) {
        OnlyTags.addAll(tags);
        return (T) this;
    }

    public T addOnlyItems(List<String> items) {
        OnlyItems.addAll(items);
        return (T) this;
    }

    public T addUnlessItemTags(List<String> tags) {
        UnlessItemTags.addAll(tags);
        return (T) this;
    }

    public T addUnlessItemIds(List<String> ids) {
        UnlessItemIds.addAll(ids);
        return (T) this;
    }

    public T addOnlySlots(List<String> slots) {
        OnlySlots.addAll(slots);
        return (T) this;
    }


    public List<String> getOnlyTags() {
        return OnlyTags;
    }

    public List<String> getOnlyItems() {
        return OnlyItems;
    }

    public List<String> getUnlessItemTags() {
        return UnlessItemTags;
    }

    public List<String> getUnlessItemIds() {
        return UnlessItemIds;
    }

    public List<String> getOnlySlots() {
        return OnlySlots;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
    public JsonObject toJsonObject() {
        Gson gson = new Gson();
        return gson.fromJson(toJson(), JsonObject.class);
    }
    public BaseItemSelector fromJsonObject(JsonObject jsonObject) {
        Gson gson = new Gson();
        return gson.fromJson(gson.toJson(jsonObject), BaseItemSelector.class);
    }

    public Consumer<CompoundTag> getExtraNBT(){
        return (e)->{

        };
    }
    public Consumer<CompoundTag> disposeNBT(){
        return (e)->{

        };
    }
    public CompoundTag serializeNBT() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("OnlyTags", String.join(",", OnlyTags));
        compoundTag.putString("OnlyItems", String.join(",", OnlyItems));
        compoundTag.putString("UnlessItemTags", String.join(",", UnlessItemTags));
        compoundTag.putString("UnlessItemIds", String.join(",", UnlessItemIds));
        compoundTag.putString("OnlySlots", String.join(",", OnlySlots));
        getExtraNBT().accept(compoundTag);
        return compoundTag;
    }
    public static BaseItemSelector<?> deserializeNBT(CompoundTag compoundTag) {
        BaseItemSelector<?> baseItemSelector = new BaseItemSelector<>();
        baseItemSelector.OnlyTags.addAll(List.of(compoundTag.getString("OnlyTags").split(",")));
        baseItemSelector.OnlyItems.addAll(List.of(compoundTag.getString("OnlyItems").split(",")));
        baseItemSelector.UnlessItemTags.addAll(List.of(compoundTag.getString("UnlessItemTags").split(",")));
        baseItemSelector.UnlessItemIds.addAll(List.of(compoundTag.getString("UnlessItemIds").split(",")));
        baseItemSelector.OnlySlots.addAll(List.of(compoundTag.getString("OnlySlots").split(",")));
        baseItemSelector.disposeNBT().accept(compoundTag);
        return baseItemSelector;
    }
    public boolean containItem(ItemStack stack) {
        String itemId = ExUtil.getItemID(stack);

        // 检查 OnlyItems 条件
        if (!checkOnlyItems(itemId)) {
            return false;
        }

        // 检查 UnlessItemIds 条件
        if (isBlockedByUnlessItemIds(itemId)) {
            return false;
        }

        // 检查 OnlyTags 条件
        if (!checkOnlyTags(stack)) {
            return false;
        }

        // 检查 UnlessItemTags 条件
        if (isBlockedByUnlessItemTags(stack)) {
            return false;
        }

        return true;
    }

    // 检查 OnlyItems 条件
    private boolean checkOnlyItems(String itemId) {
        if (OnlyItems.isEmpty()) {
            return true;
        }
        return OnlyItems.contains(itemId);
    }

    // 检查 UnlessItemIds 条件
    private boolean isBlockedByUnlessItemIds(String itemId) {
        if (UnlessItemIds.isEmpty()) {
            return false;
        }
        return UnlessItemIds.contains(itemId);
    }

    // 检查 OnlyTags 条件
    private boolean checkOnlyTags(ItemStack stack) {
        if (OnlyTags.isEmpty()) {
            return true;
        }
        return containTag(stack);
    }

    // 检查 UnlessItemTags 条件
    private boolean isBlockedByUnlessItemTags(ItemStack stack) {
        if (UnlessItemTags.isEmpty()) {
            return false;
        }
        return !unContainTag(stack);
    }

    public  boolean containTag(ItemStack stack){
        if (OnlyTags.isEmpty())return true;
        for (String tag : OnlyTags ){
            if (stack.is(ItemTags.create(new ResourceLocation(tag))))return true;
        }
        return false;
    }
    public boolean unContainTag(ItemStack stack){
        List<String> unlessItemTags = getUnlessItemTags();
        if (unlessItemTags.isEmpty())return true;
        for (String tag : unlessItemTags){
            if (stack.is(ItemTags.create(new ResourceLocation(tag))))return false;
        }
        return true;
    }
    private boolean checkTags(ItemStack stack) {
        for (var s : stack.getTags().toList()) {
            if (OnlyTags.contains(s.location().toString())) {
                return true;
            }
        }
        return false;
    }
}
