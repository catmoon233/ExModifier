package net.exmo.exmodifier.core.runtime;

import net.exmo.exmodifier.api.ExModifierApi;
import net.exmo.exmodifier.core.data.ElementDefinition;
import net.exmo.exmodifier.core.registry.ExModifierCatalog;
import net.exmo.exmodifier.events.ElementDamageEvent;
import net.exmo.exmodifier.init.ExAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Applies item/entity element restrain plus ISS school damage-type mapping. */
public final class ElementCombat {
    private ElementCombat() {}

    public static void apply(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            Map<ResourceLocation, Integer> attack = attackElements(event.getSource(), attacker, ExModifierCatalog.current());
            if (!attack.isEmpty()) {
                NeoForge.EVENT_BUS.post(new ElementDamageEvent(event.getEntity(), event.getSource(), event.getAmount(), attack));
            }
            float multiplier = multiplier(event.getSource(), attacker, event.getEntity());
            if (Math.abs(multiplier - 1.0F) > 1e-4F) {
                event.setAmount(Math.max(0.01F, event.getAmount() * multiplier));
            }
        }
    }

    public static float multiplier(DamageSource source, LivingEntity attacker, LivingEntity victim) {
        ExModifierCatalog catalog = ExModifierCatalog.current();
        Map<ResourceLocation, Integer> attack = attackElements(source, attacker, catalog);
        if (attack.isEmpty()) return 1.0F;
        double conversion = ExAttributes.value(attacker, ExAttributes.ELEMENT_CONVERSION_COEFFICIENT, 1.0D);
        double resistance = ExAttributes.value(victim, ExAttributes.ELEMENT_RESISTANCE_COEFFICIENT, 6.0D);
        return multiplier(catalog, attack, defenseElements(victim), conversion, resistance);
    }

    public static float multiplier(ExModifierCatalog catalog, Map<ResourceLocation, Integer> attack, Map<ResourceLocation, Integer> defense) {
        return multiplier(catalog, attack, defense, 1.0D, 6.0D);
    }

    public static float multiplier(ExModifierCatalog catalog, Map<ResourceLocation, Integer> attack, Map<ResourceLocation, Integer> defense,
                                   double conversion, double resistance) {
        float bonus = 0.0F;
        float restrainProduct = 1.0F;
        float armorResist = 0.0F;
        for (Map.Entry<ResourceLocation, Integer> attacking : attack.entrySet()) {
            ElementDefinition definition = catalog.element(attacking.getKey()).orElse(null);
            if (definition == null) continue;
            int amount = Math.max(1, attacking.getValue());
            bonus += definition.damageBonus() * amount;
            for (Map.Entry<ResourceLocation, Integer> defending : defense.entrySet()) {
                float restrain = definition.restrainAgainst(defending.getKey());
                restrainProduct *= 1.0F + (restrain - 1.0F) * 0.25F;
                ElementDefinition defenseDef = catalog.element(defending.getKey()).orElse(null);
                if (defenseDef != null) {
                    armorResist += (float) (Math.log1p(Math.max(1, defending.getValue()) / 500.0D) * defenseDef.damageResist() * 0.12F);
                }
            }
        }
        double convertedBonus = Math.max(0.0D, conversion) * bonus;
        double resistScale = Math.max(0.0D, resistance) / 6.0D;
        return Math.max(0.01F, (float) ((1.0D + convertedBonus) * restrainProduct * (1.0D - Math.min(0.75F, armorResist * resistScale))));
    }

    public static Map<ResourceLocation, Integer> attackElements(DamageSource source, LivingEntity attacker, ExModifierCatalog catalog) {
        LinkedHashMap<ResourceLocation, Integer> amounts = new LinkedHashMap<>();
        mergeInts(amounts, ExModifierApi.elementsOn(attacker.getMainHandItem()));
        mergeFloats(amounts, ExModifierApi.elementsOn(attacker));
        ResourceLocation mapped = catalog.elementForDamage(source);
        if (mapped != null) amounts.merge(mapped, 1, Integer::sum);
        return amounts;
    }

    public static Map<ResourceLocation, Integer> defenseElements(LivingEntity victim) {
        LinkedHashMap<ResourceLocation, Integer> amounts = new LinkedHashMap<>();
        mergeFloats(amounts, ExModifierApi.elementsOn(victim));
        for (ItemStack stack : ExModifierApi.equipped(victim)) mergeInts(amounts, ExModifierApi.elementsOn(stack));
        return amounts;
    }

    private static void mergeInts(Map<ResourceLocation, Integer> into, Map<ResourceLocation, Integer> extra) {
        extra.forEach((id, amount) -> into.merge(id, amount, Integer::sum));
    }

    private static void mergeFloats(Map<ResourceLocation, Integer> into, Map<ResourceLocation, Float> extra) {
        extra.forEach((id, amount) -> into.merge(id, Math.max(1, Math.round(amount)), Integer::sum));
    }

    public static List<ResourceLocation> snapshot(LivingEntity entity) {
        List<ResourceLocation> ids = new ArrayList<>(ExModifierApi.elementsOn(entity).keySet());
        for (ItemStack stack : ExModifierApi.equipped(entity)) ids.addAll(ExModifierApi.elementsOn(stack).keySet());
        return List.copyOf(ids);
    }
}
