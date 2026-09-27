package net.exmo.exmodifier.api;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.exmo.exmodifier.core.component.AppliedModifier;
import net.exmo.exmodifier.core.component.AppliedModifiers;
import net.exmo.exmodifier.core.data.AttributeSpec;
import net.exmo.exmodifier.core.data.ElementDefinition;
import net.exmo.exmodifier.core.data.LevelExpression;
import net.exmo.exmodifier.core.data.ModifierEntryDefinition;
import net.exmo.exmodifier.core.registry.ExModifierCatalog;
import net.exmo.exmodifier.core.runtime.ElementCombat;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ExModifierApiTestHarness {
    public static void main(String[] args) {
        codecRoundTrip();
        componentPersistence();
        attributeQuery();
        expression();
        elementOverlayAndCombat();
        System.out.println("ExModifier API tests passed");
    }

    private static void codecRoundTrip() {
        AppliedModifiers original = sample();
        JsonElement json = AppliedModifiers.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        AppliedModifiers decoded = AppliedModifiers.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        check(decoded.values().size() == 1, "codec keeps one applied modifier");
        check(decoded.values().getFirst().entryId().equals(id("sharp")), "codec keeps entry id");
        check(decoded.values().getFirst().level() == 3, "codec keeps level");
    }

    private static void componentPersistence() {
        AppliedModifiers original = sample();
        Tag tag = AppliedModifiers.CODEC.encodeStart(NbtOps.INSTANCE, original).getOrThrow();
        AppliedModifiers decoded = AppliedModifiers.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();
        check(!decoded.isEmpty(), "data component payload survives NBT roundtrip");
        check(decoded.values().getFirst().locked(), "data component keeps locked flag");
    }

    private static void attributeQuery() {
        ModifierEntryDefinition definition = new ModifierEntryDefinition(
                List.of(), List.of(), 10.0F, 5, true, false,
                List.of(new AttributeSpec(ResourceLocation.parse("minecraft:generic.attack_damage"), 1.0D, 0.5D,
                        AttributeModifier.Operation.ADD_VALUE, Optional.empty())),
                List.of(), List.of(), Optional.empty());
        ExModifierCatalog.bind(new ExModifierCatalog(
                Map.of(id("sharp"), definition), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of()));
        List<AttributeContribution> contributions = ExModifierApi.attributesOn(sample());
        check(!contributions.isEmpty(), "item with modifier component queries non-empty attributes");
        check(Math.abs(contributions.getFirst().amount() - 2.0D) < 1e-6, "level 3 uses amount + per-level");
        check(!ExModifierApi.modifiersOn(sample()).isEmpty(), "item with modifier component queries non-empty entries");
        ExModifierCatalog.bind(ExModifierCatalog.EMPTY);
    }

    private static void expression() {
        check(Math.abs(LevelExpression.evaluate("2 * level + 1", 3) - 7.0D) < 1e-6, "expression evaluates level");
    }

    private static void elementOverlayAndCombat() {
        ResourceLocation fire = ResourceLocation.parse("irons_spellbooks:fire");
        ResourceLocation ender = ResourceLocation.parse("irons_spellbooks:ender");
        ResourceLocation evocation = ResourceLocation.parse("irons_spellbooks:evocation");
        ElementDefinition datapackFire = new ElementDefinition(0.2F, 0.1F, 0xFFAA00,
                Map.of(ender, 1.5F), List.of(ResourceLocation.parse("irons_spellbooks:fire_magic")), Optional.of(fire));
        ExModifierCatalog catalog = new ExModifierCatalog(
                Map.of(), Map.of(), Map.of(fire, datapackFire), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of());
        catalog = catalog.overlayElements(Map.of(
                fire, new ElementDefinition(0.99F, 0.0F, 1),
                ender, new ElementDefinition(0.10F, 0.05F, 0xFF55FF,
                        Map.of(), List.of(ResourceLocation.parse("irons_spellbooks:ender_magic")), Optional.of(ender)),
                evocation, new ElementDefinition(0.10F, 0.05F, 0xFFFFFF,
                        Map.of(), List.of(ResourceLocation.parse("irons_spellbooks:evocation_magic")), Optional.of(evocation))
        ));
        check(catalog.element(fire).orElseThrow().damageBonus() == 0.2F, "datapack element wins over ISS overlay");
        check(catalog.element(ender).isPresent(), "ISS ender school becomes an element");
        check(catalog.element(evocation).isPresent(), "ISS evocation school becomes an element");
        ExModifierCatalog.bind(catalog);
        float multiplier = ElementCombat.multiplier(catalog, Map.of(fire, 1), Map.of(ender, 1));
        check(multiplier > 1.0F, "fire restrained against ender increases damage");
        ExModifierCatalog.bind(ExModifierCatalog.EMPTY);
    }

    private static AppliedModifiers sample() {
        return new AppliedModifiers(List.of(new AppliedModifier(id("sharp"), 3, Optional.empty(), true)));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("exmodifier", path);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
