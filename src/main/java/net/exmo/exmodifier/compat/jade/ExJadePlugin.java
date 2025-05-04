package net.exmo.exmodifier.compat.jade;

import net.exmo.exmodifier.Exmodifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class ExJadePlugin implements IWailaPlugin {
    public static final ResourceLocation element_id = new ResourceLocation(Exmodifier.MODID, "element");
    @Override
    public void register(IWailaCommonRegistration registration) {
        //TODO register data providers
    }


    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(ExElementJadeProvider.INSTANCE, LivingEntity.class);
    }
}
