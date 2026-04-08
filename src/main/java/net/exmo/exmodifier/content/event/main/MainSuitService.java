package net.exmo.exmodifier.content.event.main;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.helper.ModifierEntryHelper;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.suit.ExSuit;
import net.exmo.exmodifier.content.suit.ExSuitHandle;
import net.exmo.exmodifier.events.ExApplySuitAttrigetherEvent;
import net.exmo.exmodifier.events.ExApplySuitEffectEvent;
import net.exmo.exmodifier.events.ExSuitApplyOnChangeEvent;
import net.exmo.exmodifier.network.ExModifiervaV;
import net.exmo.exmodifier.util.EntityAttrUtil;
import net.exmo.exmodifier.util.gether.AttriGetherNormal;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.exmo.exmodifier.util.EntityAttrUtil.WearOrTake.TAKE;
import static net.exmo.exmodifier.util.EntityAttrUtil.WearOrTake.WEAR;

public final class MainSuitService {
    private MainSuitService() {
    }

    public static void applySuitEffect(Player player, ExSuit.Trigger trigger) {
        if (player == null || player.level().isClientSide) {
            return;
        }

        player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            List<MobEffectInstance> mobEffectsToAdd = new ArrayList<>();
            CommandSourceStack commandSourceStack = createCommandSourceStack(player);
            Map<String, Integer> suitsNum = capability.SuitsNum;
            boolean hasDirtySuits = false;

            Iterator<Map.Entry<String, Integer>> iterator = suitsNum.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Integer> entry = iterator.next();
                String suitId = entry.getKey();
                int suitLevel = entry.getValue();
                ExSuit suit = ExSuitHandle.INSTANCE.getAll().get(suitId);
                if (suit == null) {
                    iterator.remove();
                    hasDirtySuits = true;
                    continue;
                }

                for (int level = 1; level <= suitLevel; level++) {
                    if (suit.getTriggers().get(level) != trigger) {
                        continue;
                    }

                    MainSuitRuntimeContext.applyPendingDamageModifiers();
                    runSuitCommands(player, trigger, level, suit.getCommands().get(level), commandSourceStack);
                    collectSuitEffects(player, suit.getEffect().get(level), mobEffectsToAdd);
                }
            }

            if (hasDirtySuits) {
                capability.SuitsNum = suitsNum;
                capability.syncPlayerVariables(player);
            }

            applyCollectedEffects(player, mobEffectsToAdd);
        });
    }

    public static boolean suitOperate(Player player, ItemStack wearStack, ItemStack takeStack) {
        if (player == null || player.level().isClientSide) {
            return false;
        }

        boolean changed = handleStack(player, wearStack, WEAR);
        if (handleStack(player, takeStack, TAKE)) {
            changed = true;
        }
        return changed;
    }

    public static void rebuildSuitState(Player player) {
        if (player == null || player.level().isClientSide) {
            return;
        }

        player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            if (capability.SuitsNum == null) {
                capability.SuitsNum = new HashMap<>();
            } else {
                capability.SuitsNum.clear();
            }
            capability.syncPlayerVariables(player);
        });

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack equipped = player.getItemBySlot(slot);
            if (!equipped.isEmpty()) {
                handleStack(player, equipped, WEAR);
            }
        }
    }

    private static CommandSourceStack createCommandSourceStack(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        return new CommandSourceStack(
                CommandSource.NULL,
                player.position(),
                player.getRotationVector(),
                serverLevel,
                4,
                player.getName().getString(),
                player.getDisplayName(),
                serverLevel.getServer(),
                player
        );
    }

    private static void runSuitCommands(
            Player player,
            ExSuit.Trigger trigger,
            int level,
            List<String> commands,
            CommandSourceStack commandSourceStack
    ) {
        if (commands == null || commands.isEmpty() || player.getServer() == null || commandSourceStack == null) {
            return;
        }

        for (String rawCommand : commands) {
            String command = rawCommand;
            if (trigger == ExSuit.Trigger.ATTACK || trigger == ExSuit.Trigger.PROJECTILE_HIT) {
                String hurtEntityUuid = player.getPersistentData().getString("hurtentity-uuid");
                if (!"null".equals(hurtEntityUuid)) {
                    command = command.replace("$(hurtentity)", hurtEntityUuid);
                }
            }

            command = command.replace("$(level)", Integer.toString(level));
            player.getServer().getCommands().performPrefixedCommand(commandSourceStack, command);
        }
    }

    private static void collectSuitEffects(Player player, List<MobEffectInstance> effects, List<MobEffectInstance> collector) {
        if (effects == null || effects.isEmpty()) {
            return;
        }

        for (MobEffectInstance mobEffectInstance : effects) {
            if (mobEffectInstance == null) {
                continue;
            }

            MobEffectInstance existingEffect = player.getEffect(mobEffectInstance.getEffect());
            if (existingEffect == null || existingEffect.getAmplifier() < mobEffectInstance.getAmplifier()) {
                collector.add(new MobEffectInstance(mobEffectInstance));
            }
        }
    }

    private static void applyCollectedEffects(Player player, List<MobEffectInstance> mobEffectsToAdd) {
        for (MobEffectInstance mobEffectInstance : mobEffectsToAdd) {
            ExApplySuitEffectEvent applySuitEffectEvent = new ExApplySuitEffectEvent(player, mobEffectInstance);
            MinecraftForge.EVENT_BUS.post(applySuitEffectEvent);
            if (!applySuitEffectEvent.isCanceled()) {
                player.addEffect(applySuitEffectEvent.mobEffectInstance);
            }
        }
    }

    private static boolean handleStack(Player player, ItemStack stack, EntityAttrUtil.WearOrTake effectType) {
        if (stack.isEmpty() && effectType == WEAR) {
            return false;
        }

        CompoundTag tag = stack.getTag();
        ModifierEntryHelper modifierEntryHelper = ModifierEntryHelper.of(stack);
        if (tag == null || modifierEntryHelper.getModifierEntriesSize() <= 0) {
            return false;
        }

        List<ModifierEntry> modifierEntries = modifierEntryHelper.getModifierEntriesB();
        if (modifierEntries.isEmpty()) {
            return false;
        }

        boolean changed = false;
        for (int i = 0; i < modifierEntries.size(); i++) {
            ModifierEntry modifierEntry = modifierEntries.get(i);
            String modifierId = modifierEntry.id;
            if (modifierId == null || modifierId.isEmpty()) {
                continue;
            }

            Set<String> foundSuitIds = new HashSet<>();
            for (ExSuit suit : ExSuitHandle.FindExSuitFromEntry(modifierId)) {
                if (!foundSuitIds.add(suit.id)) {
                    continue;
                }

                boolean excludeArmorInHand = "true".equals(suit.setting.getOrDefault("excludeArmorInHand", "false"))
                        && stack.getItem() instanceof ArmorItem;
                if (excludeArmorInHand) {
                    continue;
                }

                if (effectType == WEAR) {
                    ExSuitHandle.addSuitLevel(player, suit, 1);
                } else {
                    ExSuitHandle.RemoveSuitLevel(player, suit, 1);
                }
                changed = true;

                int suitLevel = ExSuitHandle.GetSuitLevel(player, suit);
                List<AttriGetherNormal> attriGethers = suit.attriGether.get(effectType == WEAR ? suitLevel : suitLevel + 1);
                if (attriGethers != null) {
                    for (AttriGetherNormal attrGether : attriGethers) {
                        if (!attrGether.getOnlyItems().isEmpty()) {
                            continue;
                        }

                        Exmodifier.LOGGER.debug("items : " + attrGether.getOnlyItems());
                        ExApplySuitAttrigetherEvent event = new ExApplySuitAttrigetherEvent(player, stack, effectType, attrGether);
                        try {
                            Exmodifier.LOGGER.debug("Apply Suit AttriGether: "
                                    + attrGether.attribute.getDescriptionId() + " "
                                    + attrGether.attributeModifier.getOperation() + " "
                                    + attrGether.attributeModifier.getAmount());
                        } catch (Exception e) {
                            Exmodifier.LOGGER.Logger.error("Read suit attri gather failed", e);
                        }
                        MinecraftForge.EVENT_BUS.post(event);
                        if (!event.isCanceled()) {
                            EntityAttrUtil.entityAddAttrTF(event.attriGether.attribute, event.attriGether.getModifier(), event.player, event.effectType);
                        }
                    }
                }

                MinecraftForge.EVENT_BUS.post(new ExSuitApplyOnChangeEvent(player, suit, i, effectType));
            }
        }
        return changed;
    }
}
