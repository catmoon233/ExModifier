package net.exmo.exmodifier.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class AttriGetherPlus extends AttriGether {
    public List<String> OnlyItems= new ArrayList<>();
    public List<String> OnlyTags= new ArrayList<>();

    public AttriGetherPlus(Attribute attribute, AttributeModifier modifier, EquipmentSlot slot, List<String> tags, List<String> items) {
        super(attribute, modifier, slot);
        OnlyItems.addAll(items);
        OnlyTags.addAll(tags);
    }

    public AttriGetherPlus(Attribute attribute, AttributeModifier modifier, EquipmentSlot slot) {
        super(attribute, modifier, slot);
    }

    public AttriGetherPlus(Attribute attribute, AttributeModifier modifier) {
        super(attribute, modifier);
    }

    public AttriGetherPlus(Attribute attribute, AttributeModifier modifier, boolean isAutoEquipmentSlot) {
        super(attribute, modifier, isAutoEquipmentSlot);
    }

    public CompoundTag serializeNBT() {
        CompoundTag compound = new CompoundTag();

        // Serialize superclass properties
        compound.putString("AttributeName", this.getAttribute().getDescriptionId());
        compound.put("AttributeModifier", this.getModifier().save());

        if (this.slot != null) {
            compound.putString("Slot", this.slot.getName());
        }

        ListTag itemsTag = new ListTag();
        for (String item : OnlyItems) {
            itemsTag.add(StringTag.valueOf(item));
        }
        compound.put("OnlyItems", itemsTag);

        ListTag tagsTag = new ListTag();
        for (String tag : OnlyTags) {
            tagsTag.add(StringTag.valueOf(tag));
        }
        compound.put("OnlyTags", tagsTag);

        return compound;
    }
    public static Attribute getAttribute(String attribute) {
        for (Attribute attr : ForgeRegistries.ATTRIBUTES){
            if (ForgeRegistries.ATTRIBUTES.getKey(attr).toString().equals(attribute))return attr;
        }
        return null;
    }
    public static AttriGetherPlus deserializeNBT(CompoundTag nbt) {
        Attribute attribute = getAttribute(nbt.getString("AttributeName")) ;
        AttributeModifier modifier = AttributeModifier.load(nbt.getCompound("AttributeModifier"));
        EquipmentSlot slot = nbt.contains("Slot") ? EquipmentSlot.byName(nbt.getString("Slot")) : null;

        List<String> items = new ArrayList<>();
        ListTag itemsTag = nbt.getList("OnlyItems", 8); // 8 is the tag type for strings
        for (int i = 0; i < itemsTag.size(); i++) {
            items.add(itemsTag.getString(i));
        }

        List<String> tags = new ArrayList<>();
        ListTag tagsTag = nbt.getList("OnlyTags", 8); // 8 is the tag type for strings
        for (int i = 0; i < tagsTag.size(); i++) {
            tags.add(tagsTag.getString(i));
        }

        AttriGetherPlus instance = new AttriGetherPlus(attribute, modifier, slot, tags, items);

        return instance;
    }
}