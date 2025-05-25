package net.exmo.exmodifier.content.selected;

import net.exmo.exmodifier.content.type.ItemType;
import net.exmo.exmodifier.selector.BaseItemSelector;
import net.exmo.exmodifier.util.exSerialize.ExSerialize;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ModifierItemSelector<T> extends BaseItemSelector<T> {
    public static ExSerialize<Object> ExSer = ExSerialize.create(ModifierItemSelector::new)
        .addStringListField("onlyWashItems", 
            e -> ((ModifierItemSelector) e).OnlyWashItems,
            (modifierItemSelector, strings) -> ((ModifierItemSelector) modifierItemSelector).OnlyWashItems = strings)
        .marge(BaseItemSelector.ExSer); // 合并基类序列化配置

    protected List<String> OnlyWashItems = new ArrayList<>();

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

    @Override
    public Consumer<CompoundTag> getExtraNBT() {
        return e->{
            e.putString("OnlyWashItems", String.join(",", OnlyWashItems));
        };
    }
    @Override
    public Consumer<CompoundTag> disposeNBT() {
        return e->{
            OnlyWashItems.clear();
            if (e.contains("OnlyWashItems")) {
                OnlyWashItems.addAll(List.of(e.getString("OnlyWashItems").split(",")));
            }
        };
    }
}
