package net.exmo.exmodifier.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public record ItemSelector(Item item, List<String> itemId, List<CompoundTag> containNBT,
                           CompareType type, List<TagKey<Item>> containTag,BiConsumer<ItemStack, AtomicBoolean> customCompare ){


    public enum CompareType {
        EMPTY,
        TAG,
        ID,
        ITEM,
        NBT,
        NBT_AND_ITEM,
        NBT_AND_ID,
        CUSTOM
    }

    /**
     * 构造一个新的ItemSelector。
     *
     * @param item       要比较的物品实例
     * @param itemId     物品ID字符串（如果适用）
     * @param containNBT 包含的NBT标签列表（如果适用）
     * @param type       比较类型
     * @param containTag 包含的标签列表（如果适用）
     */
    public ItemSelector(Item item, String itemId, List<CompoundTag> containNBT, CompareType type, List<TagKey<Item>> containTag, BiConsumer<ItemStack, AtomicBoolean> customCompare) {

        this(item,Collections.singletonList(itemId), containNBT, type, containTag,customCompare);
    }
    public ItemSelector(Item item, List<String> itemId, List<CompoundTag> containNBT, CompareType type, List<TagKey<Item>> containTag, BiConsumer<ItemStack, AtomicBoolean> customCompare) {
        this.item = item;
        this.itemId = itemId;
        this.containNBT = containNBT != null ? containNBT : List.of();
        this.type = type;
        this.containTag = containTag != null ? containTag : List.of();
        this.customCompare = customCompare;
    }
    public ItemSelector(Item item, String itemId, List<CompoundTag> containNBT, CompareType type, List<TagKey<Item>> containTag) {
        this(
                item,
                itemId,
                containNBT,
                type,
                containTag,
                (e,b) ->{
                    b.set(false);
                }
        );

    }
    public ItemSelector( BiConsumer<ItemStack, AtomicBoolean> customCompare) {
        this(null, List.of(), List.of(), CompareType.CUSTOM, List.of(),customCompare);

    }

    public ItemSelector() {
        this(null, null, List.of(), CompareType.EMPTY, List.of());
    }


    //    public static ItemSelector formItemStack(ItemStack stack){
//
//    }
    public static ItemSelector fromItem(Item item) {
        return new ItemSelector(item, null, null, CompareType.ITEM, null);
    }

    /**
     * 比较给定的ItemStack是否符合选择器的标准。
     *
     * @param stack 要比较的ItemStack
     * @return 如果ItemStack符合选择器的标准，则返回true；否则返回false
     */
    public boolean compare(ItemStack stack) {
        if (stack.isEmpty()) return false;

        return switch (type) {
            case TAG -> checkTags(stack);
            case CUSTOM -> {
                AtomicBoolean u = new AtomicBoolean(false);
                 customCompare.accept(stack, u);
                 yield u.get();
            }
            case ID -> itemId.contains(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
            case ITEM -> Objects.equals(stack.getItem(), item);
            case NBT -> checkNBT(stack);
            case NBT_AND_ITEM -> Objects.equals(stack.getItem(), item) && checkNBT(stack);
            case NBT_AND_ID ->
                    itemId.contains(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString()) && checkNBT(stack);
            default -> false;
        };
    }
    public boolean compareItemSelector(ItemSelector itemSelector){
        if (itemSelector.item != null && itemSelector.item != item) return false;
        if (itemSelector.itemId !=null && !itemSelector.itemId.equals(itemId)) return false;
        if (itemSelector.containNBT !=null && !itemSelector.containNBT.equals(containNBT)) return false;
        if (itemSelector.containTag !=null && !itemSelector.containTag.equals(containTag)) return false;

        return true;
    }

    private boolean checkTags(ItemStack stack) {
        for (TagKey<Item> tag : containTag) {
            if (stack.is(tag)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkNBT(ItemStack stack) {
        CompoundTag stackTag = stack.getTag();
        if (stackTag == null) return containNBT.isEmpty();

        for (CompoundTag nbt : containNBT) {
            // 检查所有键是否存在
            boolean allKeysPresent = nbt.getAllKeys().stream().allMatch(stackTag::contains);
            // 检查所有键值对是否相等
            boolean allValuesEqual = allKeysPresent && stackTag.equals(nbt);
            if (allValuesEqual) {
                return true;
            }
        }
        return false;
    }

//    /**
//     * 将ItemSelector对象序列化为NBT数据。
//     *
//     * @return 表示ItemSelector的CompoundTag。
//     */
//    public CompoundTag toNBT() {
//        CompoundTag tag = new CompoundTag();
//
//        // 序列化Item或ItemId
//        if (item != null) {
//            tag.putString("ItemId", ForgeRegistries.ITEMS.getKey(item).toString());
//        } else if (itemId != null && !itemId.isEmpty()) {
//            tag.putString("ItemId", itemId);
//        }
//
//        // 序列化CompareType
//        tag.putString("CompareType", type.name());
//
//        // 序列化ContainNBT
//        ListTag nbtList = new ListTag();
//        nbtList.addAll(containNBT);
//        tag.put("ContainNBT", nbtList);
//
//        // 序列化ContainTag
//        ListTag tagList = new ListTag();
//        for (TagKey<Item> tagKey : containTag) {
//            tagList.add(StringTag.valueOf(tagKey.location().toString()));
//        }
//        tag.put("ContainTag", tagList);
//
//        if (customCompare != null) {
//
//        }
//
//        return tag;
//    }

    /**
     * 从NBT数据反序列化出ItemSelector对象。
     *
     * @param tag 包含ItemSelector信息的CompoundTag。
     * @return 反序列化的ItemSelector对象。
     */
    public static ItemSelector fromNBT(CompoundTag tag) {
        Item item = null;
        String itemId = tag.getString("ItemId");
        if (!itemId.isEmpty()) {
            item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
        }

        CompareType type = CompareType.valueOf(tag.getString("CompareType"));

        List<CompoundTag> containNBT = new java.util.ArrayList<>();
        if (tag.contains("ContainNBT", Tag.TAG_LIST)) {
            ListTag nbtList = tag.getList("ContainNBT", Tag.TAG_COMPOUND);
            for (Tag nbtTag : nbtList) {
                if (nbtTag instanceof CompoundTag compoundTag) {
                    containNBT.add(compoundTag);
                }
            }
        }

        List<TagKey<Item>> containTag = new java.util.ArrayList<>();
        if (tag.contains("ContainTag", Tag.TAG_LIST)) {
            ListTag tagList = tag.getList("ContainTag", Tag.TAG_STRING);
            for (Tag stringTag : tagList) {
                if (stringTag instanceof StringTag stringTagInstance) {
                    containTag.add(TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), new ResourceLocation(stringTagInstance.getAsString())));
                }
            }
        }

        return new ItemSelector(item, itemId, containNBT, type, containTag);
    }
}