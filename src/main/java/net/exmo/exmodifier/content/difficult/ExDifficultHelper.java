package net.exmo.exmodifier.content.difficult;

import net.exmo.exmodifier.Config;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.compat.L2HostilityCompat;
import net.exmo.exmodifier.content.element.ExElementEntityData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ExDifficultHelper {
    private static final double BALANCE_FACTOR = 5.0;    // 提升平衡系数
    private static final float BASE_HEALTH = 5.0f;      // 降低基准血量
    private static final float BASE_ATTACK = 2.0f;       // 调整基准攻击力
    private static final int HIGH_LEVEL_THRESHOLD = 20;  // 高等级阈值
    private static final double HIGH_LEVEL_DECAY = 0.85; // 衰减系数优化
    private static final double NEAR_DISTANCE = 64;
    private static final double TIER1_FACTOR = 12.0;    // 低阶段增幅加强
    private static final double TIER2_FACTOR = 1.2;     // 中阶段增幅
    private static final double TIER3_FACTOR = 0.8;     // 高阶段衰减
    public static int getEntityLevel(LivingEntity entity,boolean debug) {
        if (entity.level().isClientSide) {
            return 1;
        }
        ServerLevel level = ((ServerLevel) entity.level());
        // 1. 获取属性值（攻击力缩小十倍）
        double health = entity.getAttribute(Attributes.MAX_HEALTH).getValue();

        ExElementEntityData.defAttribute defAttribute = ExElementEntityData.defaultEntityAttributes.get(entity.getType());
        double baseHealth =entity instanceof Player ? entity.getMaxHealth() : defAttribute !=null ? defAttribute.MaxHealth() :  entity.getMaxHealth();
        double originalAttack = entity.getAttribute(Attributes.ATTACK_DAMAGE) !=null ? entity.getAttributeValue(Attributes.ATTACK_DAMAGE) : 1;
        double baseAttack =entity instanceof Player ? entity.getAttributeValue(Attributes.ATTACK_DAMAGE) : defAttribute !=null ? defAttribute.AttackDamage() : entity.getAttribute(Attributes.ATTACK_DAMAGE) !=null ? entity.getAttributeValue(Attributes.ATTACK_DAMAGE) : 1;
        double adjustedAttack  = Math.pow(originalAttack, 1.15);
        MinecraftServer server = level.getServer();
        if (Config.element_summon_debug && debug) {
            server.getPlayerList().broadcastSystemMessage(Component.literal("[DEBUG] 基础血量：%.1f | 基础攻击 %.1f  | 血量: %.1f | 原始攻击: %.1f | 调整后攻击: %.1f ".formatted(
                    baseHealth,baseAttack,health, originalAttack, adjustedAttack)),false);
        }

        // 2. 计算标准化系数

        float healthFactor = (float) Math.max(0.1f, health / baseHealth);
        float attackFactor = Math.max(0.1f, (float) (adjustedAttack / baseAttack));
        double product = healthFactor * attackFactor;

        if (Config.element_summon_debug && debug) {
            server.getPlayerList().broadcastSystemMessage(Component.literal("[DEBUG] 标准化系数 | 血量系数: %.1f | 攻击系数: %.1f | 乘积: %.1f".formatted(
                    healthFactor, attackFactor, product)),false);
        }

        // 3. 分段成长系统
        double rawLevel;
        String stage = "";
        if (product <= 10_000) {
            rawLevel = Math.pow(product, 0.7) * TIER1_FACTOR;
            stage = "常规阶段";
        } else if (product <= 1_000_000) {
            rawLevel = Math.pow(product, 0.5) * TIER2_FACTOR;
            stage = "精英阶段";
        } else {
            rawLevel = Math.pow(product, 0.35) * TIER3_FACTOR;
            stage = "世界BOSS阶段";
        }

        // 4. 调试输出成长计算
        if (Config.element_summon_debug && debug) {
            server.getPlayerList().broadcastSystemMessage(Component.literal("[DEBUG] 成长计算 | 阶段: %s | 基底值: %.1f | 增幅系数: %.1f".formatted(
                    stage, Math.pow(product, getExponent(product)), getFactor(product))),false);
        }

        // 5. 1000级后衰减系统
        double preDecayLevel = rawLevel;
        if (rawLevel > 1000) {
            double over = rawLevel - 1000;
            rawLevel = 1000 + over * 0.6;
        }

        // 6. 最终处理并输出调试信息
        int finalLevel = (int) Math.max(1, Math.min(50_000, Math.round(rawLevel)));

        if (Config.element_summon_debug && debug) {
            server.getPlayerList().broadcastSystemMessage(Component.literal("[DEBUG] 最终计算 | 衰减前等级: %.1f | 衰减后等级: %.1f | 最终等级: %d".formatted(
                    preDecayLevel, rawLevel, finalLevel)),false);
            server.sendSystemMessage(Component.literal("----------------------------------------"));
        }

        return finalLevel;
    }

    // 辅助方法用于调试输出
    private static double getExponent(double product) {
        return product <= 10_000 ? 0.7 : product <= 1_000_000 ? 0.5 : 0.35;
    }

    private static double getFactor(double product) {
        return product <= 10_000 ? TIER1_FACTOR : product <= 1_000_000 ? TIER2_FACTOR : TIER3_FACTOR;
    }
    public static double getElementLevelMulti(LivingEntity livingEntity) {
        // 获取基础怪物等级
        int modBoost = L2HostilityCompat.getDifficulty(livingEntity);
        int mobLevel = modBoost != 0 ? modBoost : getEntityLevel(livingEntity,false);

        Level level = livingEntity.level();

        // 计算玩家平均等级
        int playerCount = 0;
        int totalPlayerLevel = 0;

        if (level instanceof ServerLevel serverLevel) {
            List<ServerPlayer> nearbyPlayers = serverLevel.getPlayers(p ->
                    p.getOnPos().getCenter().distanceTo(livingEntity.getOnPos().getCenter()) < NEAR_DISTANCE
            );

            playerCount = nearbyPlayers.size();
            totalPlayerLevel = nearbyPlayers.stream()
                    .mapToInt(L2HostilityCompat::getPlayerDifficulty)
                    .sum();
        }

        // 安全计算玩家平均等级
        int playerBoost = 0;
        if (playerCount > 0) {
            playerBoost = Math.round((float) totalPlayerLevel / playerCount); // 使用浮点除法
        }

        // 计算等级差异系数
        float difference;
        if (playerBoost > mobLevel) {
            float gap = playerBoost - mobLevel;
            difference = 1.0f + gap / (mobLevel + 10.0f); // 正增益
        } else {
            float gap = mobLevel - playerBoost;
            difference = 1.0f / (1.0f + gap / (mobLevel + 10.0f)); // 负增益衰减
        }

        // 难度系数（保证最低1.0）
        double pow = Math.max(1.0, Math.pow(1.2, level.getDifficulty().getId() + 1));

        // 最终等级计算
        double finalMulti = pow * difference;
        return finalMulti;


    }
    public static int getElementLevel(LivingEntity livingEntity){
        int modBoost = livingEntity instanceof ServerPlayer serverPlayer ? L2HostilityCompat.getPlayerDifficulty(serverPlayer) : L2HostilityCompat.getDifficulty(livingEntity) ;
        int mobLevel = modBoost !=0 ? modBoost : getEntityLevel(livingEntity,true) ;
        int finalLevel = (int) Math.round(mobLevel * getElementLevelMulti(livingEntity));

        // 设置合理范围（50%-200%）
        return Math.max(
                mobLevel / 2,
                Math.min(finalLevel, mobLevel * 2)
        );
    }

}
