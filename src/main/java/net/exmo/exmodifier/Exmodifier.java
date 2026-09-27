package net.exmo.exmodifier;

import net.exmo.exmodifier.client.ExModifierClient;
import net.exmo.exmodifier.content.ExModifierContent;
import net.exmo.exmodifier.content.command.ExModifierCommands;
import net.exmo.exmodifier.content.special.SpecialEffects;
import net.exmo.exmodifier.core.attachment.ExModifierAttachments;
import net.exmo.exmodifier.core.component.ExModifierComponents;
import net.exmo.exmodifier.core.registry.ExModifierDatapack;
import net.exmo.exmodifier.core.runtime.AnvilBook;
import net.exmo.exmodifier.core.runtime.AnvilRefine;
import net.exmo.exmodifier.core.runtime.AnvilWash;
import net.exmo.exmodifier.core.runtime.AttributeCombat;
import net.exmo.exmodifier.core.runtime.CustomEffectsRuntime;
import net.exmo.exmodifier.core.runtime.ExModifierRuntime;
import net.exmo.exmodifier.core.runtime.ItemLevelRuntime;
import net.exmo.exmodifier.init.ExAttributes;
import net.exmo.exmodifier.util.TickCooldown;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(Exmodifier.MODID)
public final class Exmodifier {
    public static final String MODID = "exmodifier";

    public Exmodifier(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        ExAttributes.register(modBus);
        ExModifierContent.register(modBus);
        ExModifierComponents.register(modBus);
        ExModifierAttachments.register(modBus);
        modBus.addListener(ExModifierDatapack::register);
        NeoForge.EVENT_BUS.register(ExModifierRuntime.class);
        NeoForge.EVENT_BUS.register(AttributeCombat.class);
        NeoForge.EVENT_BUS.register(SpecialEffects.class);
        NeoForge.EVENT_BUS.register(ItemLevelRuntime.class);
        NeoForge.EVENT_BUS.register(AnvilBook.class);
        NeoForge.EVENT_BUS.register(AnvilRefine.class);
        NeoForge.EVENT_BUS.register(AnvilWash.class);
        NeoForge.EVENT_BUS.register(CustomEffectsRuntime.class);
        NeoForge.EVENT_BUS.register(TickCooldown.class);
        NeoForge.EVENT_BUS.addListener(Exmodifier::commands);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ExModifierClient.register(modBus);
        }
    }

    private static void commands(RegisterCommandsEvent event) {
        ExModifierCommands.register(event.getDispatcher());
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
