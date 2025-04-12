package net.exmo.exmodifier.content.slot;

import net.exmo.exmodifier.content.selected.BaseItemSelected;
import net.exmo.exmodifier.selector.BaseItemSelector;
import net.exmo.exmodifier.util.SelectorClass;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public class UnLockSlotItem extends BaseItemSelected<UnLockSlotItem> implements SelectorClass<BaseItemSelector<UnLockSlotItem>> {

    public Item item;
    public int randomLevelSystemCount = 0;
    public int rarity =1;
    private double CostExp =0;
    private int NeedCount =1;
    private List<String> slots = new ArrayList<>();
    public BaseItemSelector<UnLockSlotItem> selector = new BaseItemSelector<UnLockSlotItem>();



    public UnLockSlotItem(Item item, int rarity) {
        this.item = item;
        this.rarity = rarity;
    }


    public double getCostExp() {
        return CostExp;
    }

    public UnLockSlotItem setCostExp(double costExp) {
        CostExp = costExp;
        return this;
    }

    public int getNeedCount() {
        return NeedCount;
    }

    public UnLockSlotItem setNeedCount(int needCount) {
        NeedCount = needCount;
        return this;
    }

    public List<String> getSlots() {
        return slots;
    }

    public UnLockSlotItem setSlots(List<String> slots) {
        this.slots = slots;
        return this;
    }

    @Override
    public BaseItemSelector<UnLockSlotItem> getModifierItemSelector() {
        return selector;
    }
}
