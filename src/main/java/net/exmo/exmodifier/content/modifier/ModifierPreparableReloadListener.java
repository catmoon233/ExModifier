package net.exmo.exmodifier.content.modifier;

import net.exmo.exmodifier.util.AbstractReloadListener;

import java.util.*;


public class ModifierPreparableReloadListener extends AbstractReloadListener<ModifierEntry> {
    public ModifierPreparableReloadListener() {
        super("modifier_entries",
            "loading modifier data...",
            json -> {
                ArrayList<ModifierEntry> list = new ArrayList<>();
                ModifierHandle.processModifierEntry(json.toString(), list);
                return list;
            },
            (key, entry) -> ModifierHandle.registerModifierEntry(entry)
        );
    }

    @Override
    public String getName() {
        return "ModifierPreparableReloadListener";
    }
}