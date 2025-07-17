package net.exmo.exmodifier.content.modifier;

import com.google.gson.JsonElement;
import net.exmo.exmodifier.util.AbstractReloadListener;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;


public class WashingMaterialsPreparableReloadListener extends AbstractReloadListener<WashingMaterials> {
    public WashingMaterialsPreparableReloadListener() {
        super("washing_materials",
            "loading washing_materials data...",
            json -> {
                ArrayList<WashingMaterials> list = new ArrayList<>();
                Set<Map.Entry<String, JsonElement>> entries = json.entrySet();
                for (Map.Entry<String, JsonElement> entry : entries) ModifierHandle.processWashingMaterialEntry(entry, list);
                return list;
            },
            (key, entry) -> ModifierHandle.registerWashingMaterials(entry)
        );
    }

    @Override
    public String getName() {
        return "WashingMaterialsPreparableReloadListener";
    }
}