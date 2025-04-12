package net.exmo.exmodifier.content.slot;

import net.exmo.exmodifier.content.selected.BaseItemSelected;
import net.exmo.exmodifier.selector.BaseItemSelector;
import net.exmo.exmodifier.util.SelectorClass;

import java.util.Optional;

public class ModifierSlot extends BaseItemSelected<ModifierSlot> implements SelectorClass<BaseItemSelector<ModifierSlot>> {
    private String LocalDescription;
    private float weight =1;
    private int id;
    public BaseItemSelector<ModifierSlot> baseItemSelector = new BaseItemSelector<>();
//    private BaseItemSelected<ModifierSlot> baseItemSelected = new BaseItemSelected<>();
//
//
//
//    public void setBaseItemSelected(BaseItemSelected<ModifierSlot> baseItemSelected) {
//        this.baseItemSelected = baseItemSelected;
//    }
//    public Optional<BaseItemSelected<ModifierSlot>> getBaseItemSelected() {
//        return Optional.ofNullable(baseItemSelected);
//    }
    public void setLocalDescription(String localDescription) {
        LocalDescription = localDescription;
    }
    public String getLocalDescription() {
        return LocalDescription;
    }

    public float getWeight() {
        return weight;
    }

    public ModifierSlot setWeight(int weight) {
        this.weight = weight;
        return this;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public BaseItemSelector<ModifierSlot> getModifierItemSelector() {
        return baseItemSelector;
    }
}
