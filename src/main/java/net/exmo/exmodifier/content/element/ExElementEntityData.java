package net.exmo.exmodifier.content.element;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.difficult.ExDifficultHelper;
import net.exmo.exmodifier.content.helper.entity.ExElementEntityHelper;
import net.exmo.exmodifier.network.AskSyncEntityElementMessage;
import net.exmo.exmodifier.network.SyncEntityElementMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;

@Mod.EventBusSubscriber
public class ExElementEntityData {
    public static final Map<UUID, ExElementInstant> ELEMENT_ENTITY_DATA = new java.util.HashMap<>();

    public static Map<EntityType<?>,defAttribute> defaultEntityAttributes = new HashMap<>();
    public record defAttribute(double MaxHealth, double AttackDamage){}
    public static ExElementInstant getOrAskElement(UUID uuid) {
        ExElementInstant exElementInstant = null;
        if (ELEMENT_ENTITY_DATA.containsKey(uuid)) {
            exElementInstant = ELEMENT_ENTITY_DATA.get(uuid);
        }
        if (exElementInstant == null) {
            Exmodifier.PACKET_HANDLER.sendToServer(new AskSyncEntityElementMessage(uuid));
        }
        return exElementInstant;
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void recordEntityAttribute(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            if (livingEntity instanceof Player) return;
            defaultEntityAttributes.put(
                    livingEntity.getType(),
                    new defAttribute(livingEntity.getMaxHealth(), livingEntity.getAttribute(Attributes.ATTACK_DAMAGE)!=null?livingEntity.getAttribute(Attributes.ATTACK_DAMAGE).getValue():1)
            );
        }

    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAdd(EntityJoinLevelEvent event) {
        Exmodifier.queueServerWork(1, () -> {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            if (livingEntity instanceof Player) return;
            if (livingEntity.level() instanceof ServerLevel serverLevel) {
                Map<ResourceLocation, ExElement> exElements =new HashMap<>(ExElementHandle.exElements);
                exElements.remove(new ResourceLocation("exmodifier:normal"));
                List<ResourceLocation> list = exElements.keySet().stream().toList();
                if (list.isEmpty()) return;
                ExElementEntityHelper exElementEntityHelper = ExElementEntityHelper.of(livingEntity);
                if (exElementEntityHelper.ValidElementEntry())return;
                List<ExElementInstant> exElementInstant;
                if (ExElementHandle.elementDefaultMap2.containsKey(livingEntity.getType())) {
                    exElementInstant = new ArrayList<>(ExElementHandle.elementDefaultMap2.get(livingEntity.getType())
                            .exElementInstants().stream()
                            .map(exElementInstant1 -> exElementInstant1.multiply((float) ExDifficultHelper.getElementLevelMulti(livingEntity)))
                            .toList());  // 添加ArrayList包装
                } else {
                    exElementInstant = new ArrayList<>();
                }
                if (exElementInstant.isEmpty()) {
                    ExElement randomExElement = exElements.get(list.get(serverLevel.getRandom().nextInt(exElements.size())));
                    exElementInstant.add(new ExElementInstant(randomExElement, ExDifficultHelper.getElementLevel(livingEntity)));
                }
                for (ExElementInstant exElementInstant1 : exElementInstant) {

                    exElementEntityHelper.addExElement(exElementInstant1, true);
                }
                serverLevel.getPlayers(
                        player -> {
                            for (ExElementInstant exElementInstant1 : exElementInstant) {
                                Exmodifier.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new SyncEntityElementMessage(livingEntity.getUUID(), exElementInstant1));
                            }
                            return true;
                        }
                );
            }
        }
        defaultEntityAttributes.remove(event.getEntity().getType());
        });
    }
}
