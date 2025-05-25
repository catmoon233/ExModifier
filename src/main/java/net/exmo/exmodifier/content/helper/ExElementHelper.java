package net.exmo.exmodifier.content.helper;

import net.exmo.exmodifier.content.element.ExElement;
import net.exmo.exmodifier.content.element.ExElementHandle;
import net.exmo.exmodifier.content.element.ExElementInstant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ExElementHelper extends ExHelper{
    public final String  EEID = "elements";
    public ExElementHelper(ItemStack itemStack) {
        super(itemStack);
    }
    public static ExElementHelper of(ItemStack itemStack) {
        return new ExElementHelper(itemStack);
    }
    public boolean ValidElementEntry() {
        return ValidMainNbt() && getMainNbt().contains(EEID);
    }
    public ExElementHelper createElementNbt() {
        createNbt();
        if (ValidElementEntry()) return this;
        getMainNbt().put(EEID, new ListTag());

        return this;

    }
    public List<ExElementInstant> getElements() {
        List<ExElementInstant> elementInstants = new ArrayList<>();
        if (ValidMainNbt()) {
            CompoundTag tag = getMainNbt();
            if (tag.contains(EEID)) {
                ListTag elementList = tag.getList(EEID, 10);
                for (int i = 0; i < elementList.size(); i++) {
                    CompoundTag tag1 = elementList.getCompound(i);
                    ExElement exElement = ExElementHandle.getExElement(tag1.getString(EEID));
                    if (exElement != null) {
                        int level = 1;
                        if (tag1.contains("Level")) level = tag1.getInt("Level");
                        CompoundTag tag2 = tag1.copy();
                        tag2.remove("Level");

                        elementInstants.add(ExElementInstant.of(exElement, level)
                                .setData(tag2));
                    }
                }
            }
        }
        return elementInstants;


    }
    public boolean gatherElement(ExElementInstant exElementInstant) {
        List<ExElementInstant> elementInstants = getElements();
        AtomicInteger level = new AtomicInteger(exElementInstant.getLevel());

        elementInstants.forEach(x -> {
            if (x.getElement().getResId().equals(exElementInstant.getElement().getResId())) {
                level.set(exElementInstant.getLevel() + x.getLevel());
            }
        });

        if (level.get() == exElementInstant.getLevel()) return false;

        removeExElementEntryUnLock(exElementInstant);
        addExElement(new ExElementInstant(exElementInstant.getElement(), level.get()), false);

        return level.get() != exElementInstant.getLevel();
    }

    public ListTag getElementsNbt() {
        return getMainNbt().getList(EEID, 10);
    }
    public ExElementHelper removeExElementEntryUnLock(ExElementInstant exElementInstant) {
        createNbt();
        if (!ValidMainNbt()) return this;
        ListTag elementsNbt = getElementsNbt();
        for (int i = 0; i < elementsNbt.size(); i++) {
            CompoundTag tag1 = elementsNbt.getCompound(i);
            if (tag1.getString(EEID).equals(exElementInstant.getElement().getResId().toString())) {
                elementsNbt.remove(i);
                break;

            }
        }
        return this;
    }

    public ExElementHelper addExElement(ExElementInstant exElementInstant, boolean gather){
        if (exElementInstant.getElement()==null)return this;
        createNbt();
        if (!ValidMainNbt()) createMainNbt();
        createElementNbt();
        if (gather) {
            if (gatherElement(exElementInstant)) return this;
        }
        CompoundTag tag1 = new CompoundTag();
        tag1.putString(EEID, exElementInstant.getElement().getResId().toString());
        tag1.putInt("Level", exElementInstant.getLevel());
        ListTag modifiersList = getElementsNbt();
        modifiersList.add(tag1);
        return this;
    }

    public int getElementEntriesSize() {
        CompoundTag mainNbt = getMainNbt();
        if (!ValidMainNbt()) return 0;
        return mainNbt.getList(EEID, 10).size();
    }
}
