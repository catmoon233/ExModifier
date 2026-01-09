package net.exmo.exmodifier.content.modifier;

import net.exmo.exmodifier.content.type.ItemType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WashingMaterials {
    public String ItemId;
    public Item item;
    public int randomLevelSystemCount = 0;
    public int rarity;

    public void setKeepEntries(int keepEntries) {
        this.keepEntries = keepEntries;
    }

    private int keepEntries = 0;
    public List<ItemType> OnlyTypes = new ArrayList<>();
    public double CostExp =0;
    public int NeedCount =1;
    public  boolean containTag(ItemStack stack){
        for (String tag : OnlyTags ){
            if (stack.is(ItemTags.create(new ResourceLocation(tag))))return true;
        }
        return false;
    }
    public boolean OnlyHasWashEntry = false;
    public int MinRandomTime;
    public int MaxRandomTime;
    public int additionEntry;
    public List<String> OnlyTags;
    public List<String> OnlyItems;

    public WashingMaterials(String itemId, int additionEntry, Item itemId1, int rarity) {
        this.ItemId = itemId;
        this.additionEntry = additionEntry;
        this.rarity = rarity;
        this.item = itemId1;
    }

    public WashingMaterials(String itemId, Item item, int rarity, int minRandomTime, int maxRandomTime) {
        ItemId = itemId;
        this.item = item;
        this.rarity = rarity;
        MinRandomTime = minRandomTime;
        MaxRandomTime = maxRandomTime;
    }

    public int getKeepEntries() {
        return keepEntries;
    }

    public List<Component> getTooltip() {
        List<Component> list = new ArrayList<>();
        list.add(Component.empty());
        list.add(Component.translatable("exmodifier.washing_materials.description.title").withStyle(ChatFormatting.GOLD));
        list.add(Component.translatable("exmodifier.washing_materials.description.rarity", rarity).withStyle(ChatFormatting.LIGHT_PURPLE));
        list.add(Component.translatable("exmodifier.washing_materials.description.cost_exp", CostExp).withStyle(ChatFormatting.GREEN));
        list.add(Component.translatable("exmodifier.washing_materials.description.need_count", NeedCount).withStyle(ChatFormatting.AQUA));
        if (MinRandomTime > 0 || MaxRandomTime > 0) {
            list.add(Component.translatable("exmodifier.washing_materials.description.min_random_time", MinRandomTime).withStyle(ChatFormatting.YELLOW));
            list.add(Component.translatable("exmodifier.washing_materials.description.max_random_time", MaxRandomTime).withStyle(ChatFormatting.RED));
        }
        if (additionEntry > 0) {
            list.add(Component.translatable("exmodifier.washing_materials.description.addition_entry", additionEntry).withStyle(ChatFormatting.GOLD));
        }
        if (keepEntries > 0) {
            list.add(Component.translatable("exmodifier.washing_materials.description.keep_entries", keepEntries).withStyle(ChatFormatting.GRAY));
        }
        if (OnlyTypes != null && !OnlyTypes.isEmpty()) {
            StringBuilder typesStr = new StringBuilder();
            for (int i = 0; i < OnlyTypes.size(); i++) {
                if (i > 0) typesStr.append(", ");
                typesStr.append(OnlyTypes.get(i).toString());
            }
            list.add(Component.translatable("exmodifier.washing_materials.description.only_types", typesStr.toString()).withStyle(ChatFormatting.BLUE));
        }
        if (OnlyTags != null && !OnlyTags.isEmpty()) {
            StringBuilder tagsStr = new StringBuilder();
            for (int i = 0; i < OnlyTags.size(); i++) {
                if (i > 0) tagsStr.append(", ");
                tagsStr.append(OnlyTags.get(i));
            }
            list.add(Component.translatable("exmodifier.washing_materials.description.only_tags", tagsStr.toString()).withStyle(ChatFormatting.DARK_PURPLE));
        }
        if (OnlyItems != null && !OnlyItems.isEmpty()) {
            StringBuilder itemsStr = new StringBuilder();
            for (int i = 0; i < OnlyItems.size(); i++) {
                if (i > 0) itemsStr.append(", ");
                itemsStr.append(OnlyItems.get(i));
            }
            list.add(Component.translatable("exmodifier.washing_materials.description.only_items", itemsStr.toString()).withStyle(ChatFormatting.GOLD));
        }
        if (OnlyHasWashEntry) {
            list.add(Component.translatable("exmodifier.washing_materials.description.only_has_wash_entry").withStyle(ChatFormatting.DARK_RED));
        }
        return list;
    }
}
