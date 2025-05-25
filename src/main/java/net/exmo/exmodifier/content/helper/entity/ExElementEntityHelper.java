package net.exmo.exmodifier.content.helper.entity;

import net.exmo.exmodifier.content.element.ExElement;
import net.exmo.exmodifier.content.element.ExElementHandle;
import net.exmo.exmodifier.content.element.ExElementInstant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ExElementEntityHelper extends BaseEntityHelper{

    public final String  EEID = "elements";
    public ExElementEntityHelper(LivingEntity entity) {
        super(entity);
    }
    public static ExElementEntityHelper of (LivingEntity entity){
        return new ExElementEntityHelper(entity);
    }
    public boolean ValidElementEntry() {
        return ValidMainNbt() && getMainNbt().contains(EEID);
    }
    public ExElementEntityHelper createElementNbt() {
        if (ValidElementEntry()) return this;
        getMainNbt().put(EEID, new ListTag());

        return this;

    }
    public List<ExElementInstant> getExElementInstants(){
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
        List<ExElementInstant> elementInstants = getExElementInstants();
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
    public ExElementEntityHelper removeExElementEntryUnLock(ExElementInstant exElementInstant) {
        createMainNbt();
        if (!ValidMainNbt()) return this;
        ListTag elementsNbt = getElementsNbt();
        for (int i = 0; i < elementsNbt.size(); i++) {
            CompoundTag tag1 = elementsNbt.getCompound(i);
            if (tag1.getString(EEID).equals(exElementInstant.getElement().getExSerialize().toString())) {
                elementsNbt.remove(i);
                break;

            }
        }
        return this;
    }

    public ExElementEntityHelper addExElement(ExElementInstant exElementInstant, boolean gather){
        if (exElementInstant.getElement()==null||exElementInstant.getElement().getResId() == null) return this;
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
}
