package net.exmo.exmodifier.content.modifier;

import net.exmo.exmodifier.content.helper.ModifierEntryHelper;
import net.exmo.exmodifier.content.helper.register.ModifierCreateHelper;
import net.exmo.exmodifier.content.type.ExType;
import net.exmo.exmodifier.init.ExAttribute;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;

import java.util.Map;

public class APO extends ModifierEntryDataProvider{
    public APO(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, existingFileHelper,"exmodifier");
    }

    @Override
    public void add(Map<String, ModifierEntryDataBuilder> map) {
        ModifierEntryDataBuilder builder = new ModifierEntryDataBuilder(
                new ModifierCreateHelper("attack1", ExType.ATTACKABLE.get())
                .weight(5)
                        .IsAutoEquipment(true)
                .addModifierAttriGether().setModifierCreateHelper(Attributes.ATTACK_DAMAGE,0.2, AttributeModifier.Operation.ADDITION)
                        .finish_add().
                        finish());
        map.put("attack1",builder);

    }

    @Override
    public void gatherData(GatherDataEvent event) {

    }
}
