package net.exmo.exmodifier.content.modifier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.ArrayList;
import java.util.List;

import static net.exmo.exmodifier.content.helper.ModifierEntryHelper.MEID;
import static net.exmo.exmodifier.content.modifier.ModifierHandle.modifierEntryMap;

public class ModifierEntryContainer {
    public List<ModifierInstant> entries;
    public static final String tagName = "MEC";

    public ModifierEntryContainer(List<ModifierInstant> entries) {
        this.entries = entries;
    }

    public  CompoundTag serializeNBT(){
       CompoundTag tag = new CompoundTag();
        ListTag listTag = tag.getList(tagName, 10);
        for (ModifierInstant entry : entries){
            listTag.add(entry.serializeNBT());
        }
        return tag;
    }
    public static ModifierEntryContainer deserializeNBT(CompoundTag tag ) {
        List<ModifierInstant> modifierEntries = new ArrayList<>();
        if (tag.contains(tagName)) {
            ListTag listTag = tag.getList(tagName, 10);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag tag1 = listTag.getCompound(i);
                ModifierEntry modifierEntry = modifierEntryMap.get(tag1.getString(MEID));
                if (modifierEntry!=null)
                {
                    int level =1;
                    if (tag1.contains("Level"))level = tag1.getInt("Level");
                    CompoundTag tag2 = tag1.copy();
                    tag2.remove("Level");
                    tag2.remove(MEID);
                    modifierEntries.add(new ModifierInstant(modifierEntry, level)
                            .setData(tag2)
                    );
                }

            }
        }
        return new ModifierEntryContainer(modifierEntries);
    }
}