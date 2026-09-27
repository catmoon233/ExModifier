package net.exmo.exmodifier.core.runtime;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.api.ExModifierApi;
import net.exmo.exmodifier.content.special.SpecialEffects;
import net.exmo.exmodifier.core.data.AttributeSpec;
import net.exmo.exmodifier.core.data.MobEffectSpec;
import net.exmo.exmodifier.core.data.SuitDefinition;
import net.exmo.exmodifier.core.data.SuitLevel;
import net.exmo.exmodifier.core.registry.ExModifierCatalog;
import net.exmo.exmodifier.events.ExAfterArmorChange;
import net.exmo.exmodifier.events.ExApplySuitEffectEvent;
import net.exmo.exmodifier.events.ExSuitApplyOnChangeEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;

public final class SuitRuntime {
    private SuitRuntime() {}

    public static void equipment(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            apply(player);
            NeoForge.EVENT_BUS.post(new ExAfterArmorChange(player, event.getSlot(), event.getFrom(), event.getTo()));
            NeoForge.EVENT_BUS.post(new ExSuitApplyOnChangeEvent(player));
        }
    }

    public static void tick(PlayerTickEvent.Post event) {
        if (event.getEntity().tickCount % 20 == 0) apply(event.getEntity());
    }

    public static void apply(LivingEntity entity) {
        Map<ResourceLocation, Integer> pieces = ExModifierApi.countSuitPieces(entity);
        ExModifierCatalog catalog = ExModifierCatalog.current();
        catalog.suits().forEach((id, definition) -> applySuit(entity, id, definition, pieces.getOrDefault(id, 0)));
    }

    public static void onTrigger(LivingEntity entity, String trigger) {
        if (entity == null || trigger == null || entity.level().isClientSide()) return;
        ResourceLocation triggerId = parseTrigger(trigger);
        Map<ResourceLocation, Integer> pieces = ExModifierApi.countSuitPieces(entity);
        ExModifierCatalog.current().suits().forEach((id, definition) -> {
            int count = pieces.getOrDefault(id, 0);
            ExApplySuitEffectEvent applyEvent = new ExApplySuitEffectEvent(entity, id, trigger);
            NeoForge.EVENT_BUS.post(applyEvent);
            if (applyEvent.canceled()) return;
            for (SuitLevel level : definition.levels()) {
                if (count < level.pieces()) continue;
                if (!matchesTrigger(level.trigger(), triggerId)) continue;
                applyEffects(entity, level);
                runCommands(entity, level);
                for (ResourceLocation effect : level.specialEffects()) {
                    if (SpecialEffects.has(entity, effect)) continue;
                }
            }
        });
    }

    private static void applySuit(LivingEntity entity, ResourceLocation suitId, SuitDefinition definition, int pieces) {
        for (int index = 0; index < definition.levels().size(); index++) {
            SuitLevel level = definition.levels().get(index);
            boolean active = pieces >= level.pieces();
            for (int attr = 0; attr < level.attributes().size(); attr++) {
                AttributeSpec spec = level.attributes().get(attr);
                Holder<Attribute> holder = BuiltInRegistries.ATTRIBUTE.getHolder(spec.attribute()).orElse(null);
                if (holder == null) continue;
                AttributeInstance instance = entity.getAttribute(holder);
                if (instance == null) continue;
                ResourceLocation modifierId = Exmodifier.id("suit/" + suitId.getNamespace() + "/" + suitId.getPath() + "/" + index + "/" + attr);
                instance.removeModifier(modifierId);
                if (active) {
                    instance.addTransientModifier(new AttributeModifier(modifierId, spec.amountAt(1), spec.operation()));
                }
            }
            if (active && matchesTrigger(level.trigger(), Exmodifier.id("tick"))) {
                applyEffects(entity, level);
            }
        }
    }

    private static void applyEffects(LivingEntity entity, SuitLevel level) {
        for (MobEffectSpec spec : level.effects()) {
            Holder<MobEffect> holder = BuiltInRegistries.MOB_EFFECT.getHolder(spec.effect()).orElse(null);
            if (holder == null) continue;
            entity.addEffect(new MobEffectInstance(holder, spec.duration(), spec.amplifier(), spec.ambient(), spec.visible()));
        }
    }

    private static void runCommands(LivingEntity entity, SuitLevel level) {
        if (level.commands().isEmpty()) return;
        MinecraftServer server = entity.getServer();
        if (server == null) return;
        for (String command : level.commands()) {
            if (command == null || command.isBlank()) continue;
            String resolved = command.replace("@s", entity.getScoreboardName()).replace("@p", entity.getScoreboardName());
            if (entity instanceof ServerPlayer player) {
                server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(2).withSuppressedOutput(), resolved);
            }
        }
    }

    private static ResourceLocation parseTrigger(String trigger) {
        String raw = trigger.toLowerCase();
        ResourceLocation parsed = ResourceLocation.tryParse(raw.contains(":") ? raw : "exmodifier:" + raw);
        return parsed == null ? Exmodifier.id("tick") : parsed;
    }

    private static boolean matchesTrigger(ResourceLocation configured, ResourceLocation fired) {
        if (configured == null || fired == null) return false;
        if (configured.equals(fired)) return true;
        return configured.getPath().equalsIgnoreCase(fired.getPath());
    }
}
