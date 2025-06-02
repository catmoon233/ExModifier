package net.exmo.exmodifier.util.gether;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.ArrayList;
import java.util.List;

import static net.exmo.exmodifier.Exmodifier.GSON;

public class AttriGetherNormal extends AttrGether {


    public String genJson(){
        return GSON.toJson(this);
    }
    public static  AttriGetherNormal fromJson(String json){
        return GSON.fromJson(json,AttriGetherNormal.class);
    }
    public boolean hasUUID;
    private List<String> OnlyItems = new ArrayList<>();
    private List<String> OnlySlots = new ArrayList<>();


    public AttriGetherNormal(Attribute attribute, AttributeModifier attributeModifier) {
        super(attribute, attributeModifier);
    }

    public boolean isHasUUID() {
        return hasUUID;
    }

    public AttriGetherNormal setHasUUID(boolean hasUUID) {
        this.hasUUID = hasUUID;
        return this;
    }

    public List<String> getOnlyItems() {
        return OnlyItems;
    }

    public AttriGetherNormal setOnlyItems(List<String> onlyItems) {
        OnlyItems = onlyItems;
        return this;
    }

    public List<String> getOnlySlots() {
        return OnlySlots;
    }

    public AttriGetherNormal setOnlySlots(List<String> onlySlots) {
        OnlySlots = onlySlots;
        return this;
    }

    // 添加toNbt方法
    public CompoundTag toNbt() {
        CompoundTag tag = exSerialize.toNbt(this);
        tag.putBoolean("hasUUID", this.hasUUID);
        
        // 序列化OnlyItems列表
        ListTag onlyItemsTag = new ListTag();
        this.OnlyItems.forEach(item -> onlyItemsTag.add(StringTag.valueOf(item)));
        tag.put("OnlyItems", onlyItemsTag);
        
        // 序列化OnlySlots列表
        ListTag onlySlotsTag = new ListTag();
        this.OnlySlots.forEach(slot -> onlySlotsTag.add(StringTag.valueOf(slot)));
        tag.put("OnlySlots", onlySlotsTag);
        
        return tag;
    }

    // 添加fromNbt静态方法
    public static AttriGetherNormal fromNbt(CompoundTag tag) {
        AttriGetherNormal obj = (AttriGetherNormal) AttrGether.exSerialize.fromNbt(tag);
        obj.hasUUID = tag.getBoolean("hasUUID");
        
        // 反序列化OnlyItems列表
        ListTag onlyItemsTag = tag.getList("OnlyItems", 8);
        obj.OnlyItems = new ArrayList<>();
        for (int i = 0; i < onlyItemsTag.size(); i++) {
            obj.OnlyItems.add(onlyItemsTag.getString(i));
        }
        
        // 反序列化OnlySlots列表
        ListTag onlySlotsTag = tag.getList("OnlySlots", 8);
        obj.OnlySlots = new ArrayList<>();
        for (int i = 0; i < onlySlotsTag.size(); i++) {
            obj.OnlySlots.add(onlySlotsTag.getString(i));
        }
        
        return obj;
    }
}
