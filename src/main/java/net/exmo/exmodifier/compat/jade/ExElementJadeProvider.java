package net.exmo.exmodifier.compat.jade;

import net.exmo.exmodifier.content.element.ExElementEntityData;
import net.exmo.exmodifier.content.element.ExElementInstant;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum ExElementJadeProvider implements IEntityComponentProvider {
    INSTANCE;


    @Override
    public void appendTooltip(ITooltip iTooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {
        ExElementInstant orAskElement = ExElementEntityData.getOrAskElement(entityAccessor.getEntity().getUUID());
        if (orAskElement!=null){
            iTooltip.add(orAskElement.getDesc());
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ExJadePlugin.element_id;
    }
}
