package net.exmo.exmodifier.content.adventure;

import net.exmo.exmodifier.content.helper.register.ModifierCreateHelper;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
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
    public static void registerModifierEntry(ExEntryRegistryEvent event) {
        List<ModifierEntry> entries = new ArrayList<>();
        new ModifierCreateHelper("attck1",ModifierEntry.Type.ATTACKABLE)
                .weight(5)
                .addModifierAttriGether().setModifierCreateHelper(ExAttribute.DIG_SPEED.get(),0.2, AttributeModifier.Operation.ADDITION).finish_add().
                addIntoListAndReload(entries,"armor1")

                .type(ModifierEntry.Type.ARMOR)
                .weight(5)
                .addModifierAttriGether().setModifierCreateHelper(ExAttribute.DODGE.get(),0.2, AttributeModifier.Operation.ADDITION).finish_add().
                addIntoListAndReload(entries,"armor2");
        for (int swim=0;swim<100;swim++){
            new ModifierCreateHelper("swim"+swim,ModifierEntry.Type.ATTACKABLE)
                    .weight(swim+1)
                    .addModifierAttriGether().setModifierCreateHelper(ForgeMod.SWIM_SPEED.get(),
                            101-swim, AttributeModifier.Operation.ADDITION).finish_add().
                    addIntoListAndReload(entries);
        }
        for (int atk=0;atk<100;atk++){
            new ModifierCreateHelper("atk"+atk,ModifierEntry.Type.ATTACKABLE)
                    .weight(atk+1)
                    .addModifierAttriGether().setModifierCreateHelper(Attributes.ATTACK_DAMAGE,
                            (101-atk)*2, AttributeModifier.Operation.ADDITION).finish_add().
                    addIntoListAndReload(entries);
        }
        for (int kui=0;kui<100;kui++){
            new ModifierCreateHelper("kui"+kui,ModifierEntry.Type.ATTACKABLE)
                    .weight(kui+1)
                    .addModifierAttriGether().setModifierCreateHelper(Attributes.ARMOR_TOUGHNESS,
                            (101-kui)*0.1, AttributeModifier.Operation.ADDITION).finish_add().
                    addIntoListAndReload(entries);
        }
        event.entries.addAll(entries);
    }
}
