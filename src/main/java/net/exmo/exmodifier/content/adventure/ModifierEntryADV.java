package net.exmo.exmodifier.content.adventure;

import net.exmo.exmodifier.content.helper.register.ModifierCreateHelper;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.type.ExType;
import net.exmo.exmodifier.events.ExEntryRegistryEvent;
import net.exmo.exmodifier.init.ExAttribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class ModifierEntryADV {

    @SubscribeEvent
    public static void registerModifierEntry(ExEntryRegistryEvent event) {}
}
