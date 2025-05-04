package net.exmo.exmodifier.content.element;

import com.google.common.cache.Cache;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.exmo.exmodifier.Config;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.compat.DamageNumberCompatData;
import net.exmo.exmodifier.content.helper.ExElementHelper;
import net.exmo.exmodifier.content.helper.entity.ExElementEntityHelper;
import net.exmo.exmodifier.content.modifier.MoConfig;
import net.exmo.exmodifier.events.ElementDamageEvent;
import net.exmo.exmodifier.init.ExAttribute;
import net.exmo.exmodifier.network.DamageNumberColorCompatMessage;
import net.exmo.exmodifier.network.DamageNumberCompatMessage;
import net.exmo.exmodifier.util.ExConfigHandle;
import net.exmo.exmodifier.util.ItemSelector;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.PacketDistributor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ExElementHandle {
    public static Map<ResourceLocation, ExElement> exElements = new java.util.HashMap<>();
    public static List<MoConfig> FoundElementConfigs = new ArrayList<>();
    public static final Path ElementConfigPath = FMLPaths.CONFIGDIR.get().resolve("exmo/elements/");
    public static Cache<DamageSource, List<ExElementInstant>> cache = com.google.common.cache.CacheBuilder.newBuilder().build();

    public static Optional<List<ExElementInstant>> getExElementInstant(DamageSource source) {
        return Optional.ofNullable(cache.getIfPresent(source));
    }

    public static void registryExElement(ExElement exElement) {
        exElements.put(exElement.getId(), exElement);
        Exmodifier.LOGGER.info("Registry ExElement: " + exElement.getId());
    }

    public static ExElement getExElement(ResourceLocation id) {
        return exElements.get(id);
    }

    public static ExElement getExElement(String id) {
        return exElements.get(new ResourceLocation(id));
    }

    public static Map<ResourceLocation, ExElement> getExElements() {
        return exElements;
    }


    public static void init() throws IOException {
        if (Files.exists(ElementConfigPath)) {
            long startTime = System.nanoTime(); // 记录开始时间

            FoundElementConfigs = ExConfigHandle.listFiles(ElementConfigPath);
            for (MoConfig moconfig : FoundElementConfigs) {
                processMoConfigEntries(moconfig);
            }

            long endTime = System.nanoTime(); // 记录结束时间
            long duration = endTime - startTime; // 计算持续时间
            Exmodifier.LOGGER.debug("ReadConfig Quality Over time: " + duration / 1000000 + " ms");


        }
    }

    public static void processMoConfigEntries(MoConfig moconfig) throws FileNotFoundException {
        if (moconfig.readEntrys().isEmpty()) {
            Exmodifier.LOGGER.info("No Suit Config Found");
            return;
        }
        var elements = ExElement.EX_SERIALIZE.fromJson(moconfig.jsonObject);
        elements.forEach(ExElementHandle::registryExElement);

    }

    public static final Map<ItemSelector, List<ExElementInstant>> elementDefaultMap = new HashMap<>();
    public static final Map<EntityType<?>, List<ExElementInstant>> elementDefaultMap2 = new HashMap<>();
    public static final List<MoConfig> FoundDefaultElementConfigs = new ArrayList<>();
    public static final List<MoConfig> FoundEntityDefaultElementConfigs = new ArrayList<>();
    public static final Path DefaultElementConfigPath = FMLPaths.CONFIGDIR.get().resolve("exmo/default_elements/");
    public static final Path DefaultEntityElementConfigPath = FMLPaths.CONFIGDIR.get().resolve("exmo/default_entity_elements/");

    public static void init2() throws IOException {
        if (Files.exists(DefaultElementConfigPath)) {

            FoundDefaultElementConfigs.addAll(ExConfigHandle.listFiles(DefaultElementConfigPath));
            for (MoConfig config : FoundDefaultElementConfigs) {
                processMoConfigEntries2(config);
            }
        }
    }

    public static void init3() throws IOException {
        if (Files.exists(DefaultEntityElementConfigPath)) {

            FoundEntityDefaultElementConfigs.addAll(ExConfigHandle.listFiles(DefaultEntityElementConfigPath));
            for (MoConfig config : FoundEntityDefaultElementConfigs) {
                processMoConfigEntries3(config);
            }
        }
    }

    public static void processMoConfigEntries2(MoConfig moconfig) throws FileNotFoundException {
        if (moconfig.readEntrys().isEmpty()) {
            Exmodifier.LOGGER.info("No Default Element Config Found: " + moconfig.configFile);
            return;
        }
        for (Map.Entry<String, JsonElement> entry : moconfig.readEntrys()) {
            processElementDefaultConfigEntry(entry);
        }
    }

    public static void processMoConfigEntries3(MoConfig moconfig) throws FileNotFoundException {
        if (moconfig.readEntrys().isEmpty()) {
            Exmodifier.LOGGER.info("No Entity Default Element Config Found: " + moconfig.configFile);
            return;
        }
        for (Map.Entry<String, JsonElement> entry : moconfig.readEntrys()) {
            processEntityElementDefaultConfigEntry(entry);
        }
    }

    private static void processEntityElementDefaultConfigEntry(Map.Entry<String, JsonElement> entry) {
        if (!entry.getValue().isJsonObject()) {
            Exmodifier.LOGGER.Logger.error("Invalid config entry format: " + entry.getKey());
            return;
        }

        try {
            JsonObject jsonObject = entry.getValue().getAsJsonObject();


            // 解析entries
            List<ExElementInstant> elements = new ArrayList<>();
            if (jsonObject.has("entries") && jsonObject.get("entries").isJsonArray()) {
                for (JsonElement elementEntry : jsonObject.get("entries").getAsJsonArray()) {
                    if (!elementEntry.isJsonObject()) continue;

                    JsonObject entryObj = elementEntry.getAsJsonObject();
                    String elementId = entryObj.get("id").getAsString();
                    int level = entryObj.has("level") ? entryObj.get("level").getAsInt() : 1;

                    ExElement element = ExElementHandle.getExElement(elementId);
                    if (element != null) {
                        elements.add(new ExElementInstant(element, level));
                    } else {
                        Exmodifier.LOGGER.Logger.error("Unknown element ID: " + elementId);
                    }
                }
            }

            Optional<EntityType<?>> entityType = EntityType.byString(entry.getKey());
            if (jsonObject.has("entityType")) {
                entityType = EntityType.byString(jsonObject.get("entityType").getAsString());
            }
            // 注册到map
            if (entityType.isPresent() && !elements.isEmpty()) {
                elementDefaultMap2.put(entityType.get(), elements);
                Exmodifier.LOGGER.Logger.debug("Added default entity elements for selector: {}", entityType);
            }
        } catch (Exception e) {
            Exmodifier.LOGGER.error("Error processing entity element default entry: " + entry.getKey(), e);
        }
    }

    private static void processElementDefaultConfigEntry(Map.Entry<String, JsonElement> entry) {
        if (!entry.getValue().isJsonObject()) {
            Exmodifier.LOGGER.Logger.error("Invalid config entry format: " + entry.getKey());
            return;
        }

        try {
            JsonObject jsonObject = entry.getValue().getAsJsonObject();

            // 解析itemSelector
            ItemSelector itemSelector = null;
            if (jsonObject.has("itemSelector")) {
                itemSelector = ItemSelector.EX_SERIALIZE.fromJsonSingleObject(jsonObject.get("itemSelector").getAsJsonObject(), "itemSelector");
            }

            // 解析entries
            List<ExElementInstant> elements = new ArrayList<>();
            if (jsonObject.has("entries") && jsonObject.get("entries").isJsonArray()) {
                for (JsonElement elementEntry : jsonObject.get("entries").getAsJsonArray()) {
                    if (!elementEntry.isJsonObject()) continue;

                    JsonObject entryObj = elementEntry.getAsJsonObject();
                    String elementId = entryObj.get("id").getAsString();
                    int level = entryObj.has("level") ? entryObj.get("level").getAsInt() : 1;

                    ExElement element = ExElementHandle.getExElement(elementId);
                    if (element != null) {
                        elements.add(new ExElementInstant(element, level));
                    } else {
                        Exmodifier.LOGGER.Logger.error("Unknown element ID: " + elementId);
                    }
                }
            }

            // 注册到map
            if (itemSelector != null && !elements.isEmpty()) {
                elementDefaultMap.put(itemSelector, elements);
                Exmodifier.LOGGER.Logger.debug("Added default elements for selector: {}", itemSelector);
            }
        } catch (Exception e) {
            Exmodifier.LOGGER.error("Error processing element default entry: " + entry.getKey(), e);
        }
    }

    public static List<ExElementInstant> getDefaultElements(ItemStack stack) {
        return elementDefaultMap.entrySet().stream().filter(entry -> entry.getKey().compare(stack)).flatMap(entry -> entry.getValue().stream()).collect(Collectors.toList());
    }

    public static List<ItemSelector> getItemSelector(ItemStack stack) {
        return elementDefaultMap.keySet().stream().filter(entry -> entry.compare(stack)).toList();
    }

    @Mod.EventBusSubscriber
    public static class CommonEvent {

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onHurt(LivingDamageEvent event) {
            if (event.getSource().is(DamageTypes.GENERIC_KILL)) return;
            Entity entity = event.getSource().getEntity();
            if (entity instanceof LivingEntity livingEntity) {
                ItemStack mainHandItem = livingEntity.getMainHandItem();
                if (mainHandItem.hasTag()) {
                    List<ExElementInstant> elements = ExElementHelper.of(mainHandItem).getElements();
                    if (!elements.isEmpty()) {
                        cache.put(event.getSource(), elements);

                        if (livingEntity instanceof ServerPlayer serverPlayer) {
                            ExElementInstant exElementInstant = elements.get(0);
                            Exmodifier.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new DamageNumberCompatMessage(exElementInstant.getElement().getColor()));
                        }
                        MinecraftForge.EVENT_BUS.post(new ElementDamageEvent(event.getEntity(), event.getSource(), event.getAmount(), elements));
                    } else if (livingEntity instanceof ServerPlayer serverPlayer)
                        Exmodifier.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new DamageNumberCompatMessage(-1));
                } else {
                    if (livingEntity instanceof ServerPlayer serverPlayer)
                        Exmodifier.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new DamageNumberCompatMessage(-1));
                }
            }
        }

        public static float getArmorResistance(LivingEntity entity, ExElementInstant attackElement) {
            float totalResistance = 0.0f;

            // 遍历所有装备槽
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack itemStack = entity.getItemBySlot(slot);
                if (itemStack.isEmpty()) continue;


                // 2. 元素特性抗性
                ExElementHelper elementHelper = ExElementHelper.of(itemStack);
                if (!elementHelper.getElements().isEmpty()) {
                    for (ExElementInstant armorElement : elementHelper.getElements()) {
                        // 计算元素克制关系
                        float elementResist = armorElement.getElement().getRestrain().getOrDefault(attackElement.getElement().getId().toString(), 1f);

                        // 抗性公式：等级对数增长 + 基础抗性
                        float resistance = (float) (Math.log1p(armorElement.getLevel() / 1000.0) // 平滑增长
                                * elementResist * 0.15f // 系数控制
                        );
                        totalResistance += resistance;
                    }
                }
            }
            return totalResistance;
        }

        @SubscribeEvent(priority = EventPriority.LOW)
        public static void onHurtOver(LivingDamageEvent event) {
            if (event.getSource().is(DamageTypes.GENERIC_KILL)) return;
            Entity entity = event.getSource().getEntity();
            if (entity instanceof LivingEntity livingEntity) {
                ItemStack mainHandItem = livingEntity.getMainHandItem();
                List<ExElementInstant> elements;
                if (mainHandItem.hasTag()) {
                    elements = ExElementHelper.of(mainHandItem).getElements();
                } else {
                    List<ExElementInstant> exElementInstants1 = ExElementEntityHelper.of(livingEntity).getExElementInstants();
                    if (!exElementInstants1.isEmpty()) {
                        elements = exElementInstants1;
                    } else return;
                }
                if (!elements.isEmpty()) {
                    // if (livingEntity instanceof ServerPlayer serverPlayer) {
                    ExElementInstant exElementInstant = elements.get(0);

                    LivingEntity target = event.getEntity();
                    if (livingEntity.level() instanceof ServerLevel serverLevel) {
                        ParticleType<?> simpleParticleType = exElementInstant.getElement().getSimpleParticleType();
                        if (simpleParticleType != null) {
                            if (simpleParticleType instanceof SimpleParticleType simpleParticleType1) {
                                float particleScale = exElementInstant.getElement().getParticleScale();
                                serverLevel.sendParticles(simpleParticleType1, target.getX(), target.getY() + target.getEyeHeight() * 0.5, target.getZ(), 5, particleScale, particleScale, particleScale, 0.25);
                            }
                        }
                    }

                    // ====== 核心参数配置 ======
                    float conversion_rate = 80.0f;     // 攻击转化基数
                    float resistance_rate = 5000.0f;    // 防御抗性基数（值越大防御收益越低）
                    float dominance_base = 1.8f;        // 压制基准指数
                    float overkill_threshold = 50.0f;   // 碾压判定阈值（攻击/防御比值）

                    // ====== 动态参数获取 ======
                    if (livingEntity.getAttributes().hasAttribute(ExAttribute.ELEMENT_CONVERSION_COEFFICIENT.get())) {
                        conversion_rate = (float) livingEntity.getAttributeValue(ExAttribute.ELEMENT_CONVERSION_COEFFICIENT.get()) * 100;
                    }
                    if (target.getAttributes().hasAttribute(ExAttribute.ELEMENT_RESISTANCE_COEFFICIENT.get())) {
                        resistance_rate = (float) target.getAttributeValue(ExAttribute.ELEMENT_RESISTANCE_COEFFICIENT.get()) * 100;
                    }

                    ExElementEntityHelper exElementEntityHelper = ExElementEntityHelper.of(target);
                    List<ExElementInstant> exElementInstants = exElementEntityHelper.getExElementInstants();
                    if (!exElementInstants.isEmpty()) {
                        var el = exElementInstants.get(0);
                        int attackerLevel = exElementInstant.getLevel();
                        int targetLevel = el.getLevel();
                        // ====== 新增：护甲抗性计算 ======
                        float armorResistance = getArmorResistance(target, exElementInstant);
                        // ====== 公式计算 ======
                        // 1. 动态压制系数（随攻击强度对数增长）
                        Float baseMultiplier = exElementInstant.getElement().getRestrain().getOrDefault(el.getElement().getId().toString(), 1f);


                        float dominanceEffect = (float) Math.pow(baseMultiplier, dominance_base + Math.log1p(attackerLevel / 10000.0) // 对数增长控制
                        );

                        //2. 攻击因子（非线性增长 + 转化率放大）
                        float attackBase = attackerLevel * conversion_rate / 100.0f; // 将转化率作为放大系数（如80→0.8倍）
                        float attackFactor;

                        // 核心增长公式：对数平滑 + 转化率指数增强
                        if (attackBase <= 1000) {
                            // 低区间：快速线性增长
                            attackFactor = 1.0f + (float) Math.log1p(attackBase) * 0.5f;
                        } else {
                            // 高区间：亚线性增长防爆炸
                            attackFactor = 1.0f + (float) (Math.pow(attackBase, 0.7) / 50.0f);
                        }

                        // 碾压加成（独立乘区）
                        if (attackerLevel > overkill_threshold * targetLevel) {
                            float overkillRatio = (float) Math.log10(attackerLevel / (targetLevel + 1.0f));
                            attackFactor *= 1.0f + overkillRatio * 0.3f; // 每10倍攻击强度增加30%（更可控）
                        }
                        if (attackerLevel > overkill_threshold * targetLevel) {
                            float overkillRatio = (float) Math.log10(attackerLevel / (targetLevel + 1.0f));
                            attackFactor *= 1.0f + overkillRatio * 0.5f; // 每10倍攻击强度增加50%
                        }

                        // 3. 防御因子（亚线性增长）
                        float defenseFactor = 1.0f + (float) Math.pow(targetLevel / resistance_rate, 0.6f);

                        // 4. 最终倍率合成
                        float finalMultiplier = dominanceEffect * attackFactor / defenseFactor;
                        //finalMultiplier = Math.max(0.1f, finalMultiplier); //下限
                        // 应用抗性（示例：乘法叠加）
                        finalMultiplier *= (1.0f - armorResistance);
                        // ====== 伤害计算 ======
                        float amount = event.getAmount() * finalMultiplier;

                        // ====== 详细输出 ======
                        if (Config.element_debug) {
                            String analysis = String.format("""
                                    [元素伤害分析]
                                    基础压制值: %,f
                                    攻击方强度: %,d → 转化效率: %.1f
                                    防御方强度: %,d → 抗性效率: %.4f
                                    压制增强: %.2f^(%.2f) → %.2f
                                    攻击因子: %.2f %s
                                    防御因子: %.2f
                                    最终倍率: %.2f × (%.2f / %.2f) = %.2f
                                    最终伤害: %.1f × %.2f = %.1f
                                    """, baseMultiplier, attackerLevel, conversion_rate, targetLevel, resistance_rate, baseMultiplier, dominance_base + Math.log1p(attackerLevel / 10000.0), dominanceEffect, attackFactor, (attackerLevel > overkill_threshold * targetLevel) ? "[碾压激活]" : "", defenseFactor, dominanceEffect, attackFactor, defenseFactor, finalMultiplier, event.getAmount(), finalMultiplier, amount);
                            livingEntity.sendSystemMessage(Component.literal(analysis));
                        }

                        event.setAmount(amount);
                        //     }
                    } else {
                        float armorResistance = getArmorResistance(target, exElementInstant);
                        event.setAmount(event.getAmount() *Math.max (0,1.0f - armorResistance));
                    }
                }
            }
            cache.invalidate(event.getSource());
        }
    }
}
