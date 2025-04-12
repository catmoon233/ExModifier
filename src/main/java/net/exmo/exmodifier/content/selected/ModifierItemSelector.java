package net.exmo.exmodifier.content.selected;

import net.exmo.exmodifier.content.type.ItemType;
import net.exmo.exmodifier.selector.BaseItemSelector;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModifierItemSelector<T> extends BaseItemSelector<T> {
    protected  List<String> OnlyWashItems = new ArrayList<>();

    // 添加单个元素的方法
    public T addOnlyWashItem(String item) {
        OnlyWashItems.add(item);
        return (T) this;
    }

    // 批量添加元素的方法
    public T addOnlyWashItems(List<String> items) {
        OnlyWashItems.addAll(items);
        return (T) this;
    }

    // Optional 化的 getter 方法
    public List<String> getOnlyWashItems() {
        return OnlyWashItems;
    }







}
