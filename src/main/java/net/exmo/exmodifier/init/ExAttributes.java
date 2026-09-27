package net.exmo.exmodifier.init;

import net.exmo.exmodifier.Exmodifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ExAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, Exmodifier.MODID);

    public static final DeferredHolder<Attribute, Attribute> ARROW_BASE_DAMAGE = ranged("arrow_base_damage", 0, 0, 1.0E8);
    public static final DeferredHolder<Attribute, Attribute> DURABILITY = ranged("durability", 1, 0, 1.0E8);
    public static final DeferredHolder<Attribute, Attribute> DIG_SPEED = ranged("dig_speed", 1, 0, 1.0E8);
    public static final DeferredHolder<Attribute, Attribute> DODGE = ranged("dodge", 1, 0, 1.0E7);
    public static final DeferredHolder<Attribute, Attribute> MAX_INJURY_FREE = ranged("max_injury_free", 1.85, 0, 1.0E7);
    public static final DeferredHolder<Attribute, Attribute> MAX_DODGE = ranged("max_dodge", 1.85, 0, 1.0E7);
    public static final DeferredHolder<Attribute, Attribute> HIT_RATE = ranged("hit_rate", 1, 0, 1.0E7);
    public static final DeferredHolder<Attribute, Attribute> PERCENT_HEAL = ranged("percent_heal", 1, 0, 1.0E7);
    public static final DeferredHolder<Attribute, Attribute> INJURY_FREE = ranged("injury_free", 1, -1.0E5, 1.0E7);
    public static final DeferredHolder<Attribute, Attribute> BEHIND_DAMAGE = ranged("behind_damage", 1, -1.0E8, 1.0E8);
    public static final DeferredHolder<Attribute, Attribute> FIREWORK_DAMAGE = ranged("firework_damage", 1, 0, 1.0E8);
    public static final DeferredHolder<Attribute, Attribute> DEFENSE = ranged("defense", 0, -1.0E5, 1.0E7);
    public static final DeferredHolder<Attribute, Attribute> DIRECT_PROTECTION = ranged("direct_protection", 0, -1.0E5, 1.0E7);
    public static final DeferredHolder<Attribute, Attribute> MAGIC_PROTECTION = ranged("magic_protection", 0, -1.0E5, 1.0E7);
    public static final DeferredHolder<Attribute, Attribute> ELEMENT_CONVERSION_COEFFICIENT = ranged("element_conversion_coefficient", 1, 0, 1.0E8);
    public static final DeferredHolder<Attribute, Attribute> ELEMENT_RESISTANCE_COEFFICIENT = ranged("element_resistance_coefficient", 6, 0, 1.0E8);
    public static final DeferredHolder<Attribute, Attribute> ALL_ATTRIBUTE_BOOST = ranged("all_attribute_boost", 1, 0, 1.0E8);
    public static final DeferredHolder<Attribute, Attribute> FALL_PROTECT = ranged("fall_protect", 1, 0, 2);

    private ExAttributes() {}

    public static void register(IEventBus bus) {
        ATTRIBUTES.register(bus);
        bus.addListener(ExAttributes::addAttributes);
    }

    private static DeferredHolder<Attribute, Attribute> ranged(String name, double base, double min, double max) {
        return ATTRIBUTES.register(name, () -> new RangedAttribute("attribute." + Exmodifier.MODID + "." + name, base, min, max).setSyncable(true));
    }

    private static void addAttributes(EntityAttributeModificationEvent event) {
        event.getTypes().forEach(type -> ATTRIBUTES.getEntries().forEach(holder -> event.add(type, holder)));
    }

    public static double value(LivingEntity entity, DeferredHolder<Attribute, Attribute> holder, double fallback) {
        if (entity == null || !entity.getAttributes().hasAttribute(holder)) return fallback;
        return entity.getAttributeValue(holder);
    }
}
