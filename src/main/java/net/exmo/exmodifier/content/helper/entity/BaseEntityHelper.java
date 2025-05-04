package net.exmo.exmodifier.content.helper.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

import javax.swing.text.html.parser.Entity;

public class BaseEntityHelper {
    public static final String EXMO_NBT = "exmo_nbt";
    public CompoundTag nbt;
    public final LivingEntity entity;

    public BaseEntityHelper(LivingEntity entity) {
        this.entity = entity;
        this.nbt = entity.getPersistentData();
    }
    public CompoundTag getMainNbt(){
        if (nbt==null)return new CompoundTag();
        return nbt.getCompound(EXMO_NBT);
    }
    public void createMainNbt(){
        nbt.put(EXMO_NBT,new CompoundTag());
    }
    public boolean ValidMainNbt(){
        if (nbt==null)return false;
        return nbt.contains(EXMO_NBT);
    }
}
