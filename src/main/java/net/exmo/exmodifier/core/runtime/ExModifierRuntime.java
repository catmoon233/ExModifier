package net.exmo.exmodifier.core.runtime;

import net.exmo.exmodifier.Config;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.api.AttributeContribution;
import net.exmo.exmodifier.api.ExModifierApi;
import net.exmo.exmodifier.core.component.AppliedModifiers;
import net.exmo.exmodifier.core.component.ItemElements;
import net.exmo.exmodifier.core.component.RefineState;
import net.exmo.exmodifier.core.registry.ExModifierCatalog;
import net.exmo.exmodifier.init.ExAttributes;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class ExModifierRuntime {
    private ExModifierRuntime() {}

    @SubscribeEvent
    public static void catalog(ServerAboutToStartEvent event) {
        ExModifierCatalog.bind(event.getServer().registryAccess());
    }

    @SubscribeEvent
    public static void tags(OnDatapackSyncEvent event) {
        ExModifierCatalog.bind(event.getPlayerList().getServer().registryAccess());
    }

    @SubscribeEvent
    public static void attributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        double boost = 0.0D;
        for (AttributeContribution contribution : ExModifierApi.attributesOn(stack)) {
            if (contribution.attribute().equals(ExAttributes.ALL_ATTRIBUTE_BOOST.getId())) {
                boost += contribution.amount();
            }
        }
        for (AttributeContribution contribution : ExModifierApi.attributesOn(stack)) {
            Holder<Attribute> holder = BuiltInRegistries.ATTRIBUTE.getHolder(contribution.attribute()).orElse(null);
            if (holder == null) continue;
            ResourceLocation id = Exmodifier.id("entry/" + contribution.sourceEntry().getNamespace() + "/" + contribution.sourceEntry().getPath());
            double amount = contribution.amount();
            if (boost != 0.0D && !contribution.attribute().equals(ExAttributes.ALL_ATTRIBUTE_BOOST.getId())
                    && !contribution.attribute().equals(Attributes.ATTACK_SPEED.unwrapKey().map(key -> key.location()).orElse(null))) {
                amount *= (boost + 1.0D);
            }
            event.addModifier(holder, new AttributeModifier(id, amount, contribution.operation()), EquipmentSlotGroup.ANY);
        }
        RefineState refine = ExModifierApi.refineState(stack);
        if (Config.refineSystem && refine.count() > 0) {
            ResourceLocation attributeId = ResourceLocation.tryParse(Config.refineAttribute);
            Holder<Attribute> damage = attributeId == null ? null : BuiltInRegistries.ATTRIBUTE.getHolder(attributeId).orElse(null);
            if (damage != null) {
                event.addModifier(damage, new AttributeModifier(Exmodifier.id("refine"), ModifierService.refineBonusPerStar(stack) * refine.count(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.MAINHAND);
            }
        }
    }

    @SubscribeEvent
    public static void incoming(LivingIncomingDamageEvent event) {
        if (event.isCanceled()) return;
        if (Config.elementSystem) ElementCombat.apply(event);
    }

    @SubscribeEvent
    public static void join(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof LivingEntity living) {
            DefaultElementIndex.applyEntity(living);
        }
    }

    @SubscribeEvent
    public static void equipment(LivingEquipmentChangeEvent event) {
        if (event.getEntity() != null && !event.getEntity().level().isClientSide() && !creative(event.getEntity())) {
            initializeObtained(event.getTo());
        }
        SuitRuntime.equipment(event);
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        SuitRuntime.tick(event);
    }

    @SubscribeEvent
    public static void crafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack result = event.getCrafting();
        if (result.isEmpty()) return;
        AppliedModifiers merged = AppliedModifiers.EMPTY;
        ItemElements elements = ItemElements.EMPTY;
        for (int i = 0; i < event.getInventory().getContainerSize(); i++) {
            ItemStack ingredient = event.getInventory().getItem(i);
            if (ingredient.isEmpty()) continue;
            for (var modifier : ExModifierApi.appliedModifiers(ingredient).values()) {
                merged = merged.with(modifier);
            }
            for (var entry : ExModifierApi.elementsOn(ingredient).entrySet()) {
                elements = elements.with(entry.getKey(), entry.getValue());
            }
        }
        if (!merged.isEmpty()) ExModifierApi.setModifiers(result, merged);
        if (!elements.amounts().isEmpty()) ExModifierApi.setElements(result, elements);
        if (event.getEntity() != null && !creative(event.getEntity())) {
            initializeObtained(result);
        }
    }

    /** Defaults apply when survival gear is obtained, not when a tooltip or creative tab only displays it. */
    public static void initializeObtained(ItemStack stack) {
        if (!isObtainableGear(stack)) return;
        DefaultElementIndex.applyItem(stack);
        DefaultEntryIndex.applyItem(stack);
        QualityRuntime.rollIfAbsent(stack);
    }

    private static boolean creative(LivingEntity entity) {
        return entity instanceof Player player && player.getAbilities().instabuild;
    }

    private static boolean isObtainableGear(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        var item = stack.getItem();
        if (item instanceof ArmorItem || item instanceof SwordItem || item instanceof DiggerItem
                || item instanceof ProjectileWeaponItem || item instanceof TridentItem) {
            return true;
        }
        var types = ExModifierCatalog.current().itemTypes().keySet();
        return !types.isEmpty() && ItemTypeMatcher.matchesAnyType(stack, types);
    }
}
