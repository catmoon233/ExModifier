package net.exmo.exmodifier.core.runtime;

import net.exmo.exmodifier.Config;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.api.AttributeContribution;
import net.exmo.exmodifier.api.ExModifierApi;
import net.exmo.exmodifier.core.component.ExModifierComponents;
import net.exmo.exmodifier.core.component.ItemLevelState;
import net.exmo.exmodifier.core.data.AttributeSpec;
import net.exmo.exmodifier.core.data.ItemLevelDefinition;
import net.exmo.exmodifier.core.registry.ExModifierCatalog;
import net.exmo.exmodifier.events.ExItemUpEvent;
import net.exmo.exmodifier.util.WeightedRandom;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ItemLevelRuntime {
    private ItemLevelRuntime() {}

    @SubscribeEvent
    public static void attributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        for (AttributeContribution contribution : attributesOn(stack)) {
            Holder<Attribute> holder = BuiltInRegistries.ATTRIBUTE.getHolder(contribution.attribute()).orElse(null);
            if (holder == null || Math.abs(contribution.amount()) < 1.0e-6) continue;
            ResourceLocation id = Exmodifier.id("level/" + contribution.sourceEntry().getNamespace() + "/" + contribution.sourceEntry().getPath());
            event.addModifier(holder, new AttributeModifier(id, contribution.amount(), contribution.operation()), EquipmentSlotGroup.ANY);
        }
    }

    @SubscribeEvent
    public static void kill(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity killer) {
            addXp(killer, "KILL", Math.max(1.0D, event.getEntity().getMaxHealth()));
        }
    }

    @SubscribeEvent
    public static void attack(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            addXp(attacker, "ATTACK", event.getAmount());
        }
        addXp(event.getEntity(), "ON_HURT", event.getAmount());
    }

    public static void applyIfAbsent(ItemStack stack) {
        if (Config.addLevelSystemCount <= 0) return;
        applyTable(stack);
    }

    /** Explicit attach used by commands. Automatic rolls stay behind {@link Config#addLevelSystemCount}. */
    public static void applyTable(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        if (stack.get(ExModifierComponents.ITEM_LEVEL.get()) != null) return;
        Optional<ResourceLocation> picked = pick(stack);
        if (picked.isEmpty()) return;
        ItemLevelDefinition definition = ExModifierCatalog.current().itemLevel(picked.get()).orElse(null);
        if (definition == null) return;
        stack.set(ExModifierComponents.ITEM_LEVEL.get(), new ItemLevelState(picked.get(), definition.defaultLevel(), 0));
    }

    public static Optional<ResourceLocation> pick(ItemStack stack) {
        Map<ResourceLocation, Float> weights = new HashMap<>();
        ExModifierCatalog.current().itemLevels().forEach((id, definition) -> {
            if (definition.weight() <= 0.0F) return;
            if (!ItemTypeMatcher.matchesAnyType(stack, definition.itemTypes()) && !definition.itemTypes().isEmpty()) return;
            if (!ItemTypeMatcher.matches(stack, definition.onlyItems(), definition.onlyTags())) return;
            weights.put(id, definition.weight());
        });
        RandomSource random = RandomSource.create(BuiltInRegistries.ITEM.getKey(stack.getItem()).hashCode() * 17L + 7);
        return WeightedRandom.pick(random, weights);
    }

    public static void addXp(LivingEntity entity, String eventName, double amount) {
        if (entity == null || entity.level().isClientSide()) return;
        for (ItemStack stack : ExModifierApi.equipped(entity)) {
            addXp(entity, stack, eventName, amount);
        }
    }

    public static void addXp(LivingEntity entity, ItemStack stack, String eventName, double amount) {
        applyIfAbsent(stack);
        ItemLevelState state = ExModifierApi.itemLevel(stack).orElse(null);
        if (state == null) return;
        ItemLevelDefinition definition = ExModifierCatalog.current().itemLevel(state.tableId()).orElse(null);
        if (definition == null) return;
        if (!definition.upEvent().equalsIgnoreCase(eventName)) return;
        int gain = definition.xpGain(state.level(), amount);
        if (gain <= 0) return;
        int xp = state.xp() + gain;
        int level = state.level();
        int before = level;
        while (level < definition.maxLevel()) {
            int need = definition.xpNeeded(level);
            if (xp < need) break;
            xp -= need;
            level++;
        }
        if (level >= definition.maxLevel()) {
            level = definition.maxLevel();
            xp = Math.min(xp, definition.xpNeeded(level) - 1);
        }
        ItemLevelState next = state.withXp(xp, level);
        stack.set(ExModifierComponents.ITEM_LEVEL.get(), next);
        if (level != before) {
            NeoForge.EVENT_BUS.post(new ExItemUpEvent(stack, entity, state, next));
            if (entity instanceof Player player) {
                player.displayClientMessage(Component.translatable("tooltip.exmodifier.level.up", stack.getHoverName(), before, level), false);
                player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    public static List<AttributeContribution> attributesOn(ItemStack stack) {
        Optional<ItemLevelState> state = ExModifierApi.itemLevel(stack);
        if (state.isEmpty()) return List.of();
        ItemLevelDefinition definition = ExModifierCatalog.current().itemLevel(state.get().tableId()).orElse(null);
        if (definition == null) return List.of();
        List<AttributeContribution> contributions = new ArrayList<>();
        for (AttributeSpec spec : definition.attributes()) {
            contributions.add(new AttributeContribution(spec.attribute(), spec.amountAt(state.get().level()), spec.operation(), state.get().tableId()));
        }
        return contributions;
    }
}
