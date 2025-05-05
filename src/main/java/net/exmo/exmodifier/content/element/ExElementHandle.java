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
import net.exmo.exmodifier.events.OnElementRegisterEvent;
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
        MinecraftForge.EVENT_BUS.post(new OnElementRegisterEvent());

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
        @SubscribeEvent
        public static void registerNormal(OnElementRegisterEvent elementRegisterEvent){
            elementRegisterEvent.registerElement(new ExElement(ResourceLocation.tryParse("exmodifier:normal")).setRestrain(Map.of()));
        }

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
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack itemStack = entity.getItemBySlot(slot);
                if (itemStack.isEmpty()) continue;

                ExElementHelper elementHelper = ExElementHelper.of(itemStack);
                if (!elementHelper.getElements().isEmpty()) {
                    for (ExElementInstant armorElement : elementHelper.getElements()) {
                        float elementResist = armorElement.getElement().getRestrain().getOrDefault(
                                attackElement.getElement().getId().toString(), 1f);
                        // 调整抗性系数为更平缓的增长
                        float resistance = (float) (Math.log1p(armorElement.getLevel() / 500.0) // 基数从1000改为500
                                * elementResist * 0.12f); // 系数从0.15降为0.12
                        totalResistance = Math.min(totalResistance + resistance, 0.75f); // 增加抗性上限75%
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
                List<ExElementInstant> elements = new ArrayList<>(List.of(new ExElementInstant(exElements.get(ResourceLocation.tryParse("exmodifier:normal")), 1)));
                if (mainHandItem.hasTag()) {
                    elements = ExElementHelper.of(mainHandItem).getElements();
                } else {
                    List<ExElementInstant> exElementInstants1 = ExElementEntityHelper.of(livingEntity).getExElementInstants();
                    if (!exElementInstants1.isEmpty()) {
                        elements = exElementInstants1;
                    };
                }
                if (!elements.isEmpty()) {
                    ExElementInstant exElementInstant = elements.get(0);
                    LivingEntity target = event.getEntity();

                    // 粒子效果逻辑保持不变
                    if (livingEntity.level() instanceof ServerLevel serverLevel) {
                        ParticleType<?> simpleParticleType = exElementInstant.getElement().getSimpleParticleType();
                        if (simpleParticleType != null) {
                            if (simpleParticleType instanceof SimpleParticleType simpleParticleType1) {
                                float particleScale = exElementInstant.getElement().getParticleScale();
                                serverLevel.sendParticles(simpleParticleType1, target.getX(), target.getY() + target.getEyeHeight() * 0.5, target.getZ(), 5, particleScale, particleScale, particleScale, 0.25);
                            }
                        }
                    }



                        // ====== 核心参数调整 ======
                        float conversion_rate = 120.0f;     // 攻击转化基数增大（值越大攻击收益越低）
                        float resistance_base = 300.0f;      // 防御抗性基数减小（值越小防御收益越低）
                        float dominance_power = 0.7f;        // 压制强度系数增强
                        float overkill_ratio = 0.25f;        // 碾压加成系数降低

                        // ====== 动态参数获取调整 ======
                        if (livingEntity.getAttributes().hasAttribute(ExAttribute.ELEMENT_CONVERSION_COEFFICIENT.get())) {
                            conversion_rate = Math.max(
                                    (float) livingEntity.getAttributeValue(ExAttribute.ELEMENT_CONVERSION_COEFFICIENT.get()) * 80.0f,
                                    0); // 增加转化率上限
                        }
                        if (target.getAttributes().hasAttribute(ExAttribute.ELEMENT_RESISTANCE_COEFFICIENT.get())) {
                            resistance_base = Math.max(
                                    (float) target.getAttributeValue(ExAttribute.ELEMENT_RESISTANCE_COEFFICIENT.get()) * 80.0f,
                                    0); // 增加抗性下限
                        }

                        ExElementEntityHelper exElementEntityHelper = ExElementEntityHelper.of(target);
                        List<ExElementInstant> exElementInstants = exElementEntityHelper.getExElementInstants();
                        if (!exElementInstants.isEmpty()) {
                            var el = exElementInstants.get(0);
                            int attackerLevel = exElementInstant.getLevel();
                            int targetLevel = el.getLevel();

                            float armorResistance = getArmorResistance(target, exElementInstant);

                            // ====== 增强压制效果 ======
                            Float baseMultiplier = exElementInstant.getElement().getRestrain().getOrDefault(el.getElement().getId().toString(), 1f);
                            // 调整对数基数从10000改为5000增强压制成长
                            float dominanceEffect = (float)Math.pow( Math.pow(
                                    baseMultiplier,
                                    dominance_power + (float) Math.log1p(attackerLevel / 5000.0)
                            ),1.35f);

                            // ====== 攻击因子调整 ======
                            float conversion_ratio = conversion_rate / 120.0f; // 基准调整
                            float attackBase = attackerLevel * conversion_ratio;
                            float attackFactor;

                            // 三阶段系数调整
                            if (attackBase <= 800) { // 提高第一阶段阈值
                                attackFactor = 1.0f + attackBase * 0.3f; // 系数从0.5降为0.3
                            } else if (attackBase <= 8000) {
                                float logGrowth = (float) Math.log1p(attackBase - 800) * 1.5f; // 系数从2.0降为1.5
                                attackFactor = 1.0f + 240.0f + logGrowth; // 基础值调整
                            } else {
                                float baseValue = 1.0f + 240.0f + (float) Math.log(7200) * 1.5f;
                                float additional = (float) Math.pow(attackBase - 8000, 0.5f); // 指数从0.55降为0.5
                                attackFactor = baseValue + additional;
                            }

                            // ====== 碾压加成合并优化 ======
                            if (attackerLevel > 50 * targetLevel) {
                                float overkill = (float) Math.log10(attackerLevel / (targetLevel + 1.0f));
                                float overkillBoost = overkill_ratio * (1.0f + conversion_ratio * 0.08f); // 转化率影响降低
                                attackFactor *= 1.0f + Math.min(overkill, 2.5f) * overkillBoost; // 最大加成层数降为2.5
                            }

                            // ====== 防御因子调整 ======
                            float defenseFactor = 1.0f + (float) Math.pow(
                                    (targetLevel * resistance_base) / 800.0f, // 分母从1000改为800
                                    0.75f // 指数从0.7提高到0.75
                            );

                            // ====== 最终倍率合成 ======
                            float finalMultiplier = dominanceEffect * attackFactor / defenseFactor;
                            finalMultiplier = Math.max(0.01f, finalMultiplier * (1.0f - armorResistance)); // 保底15%伤害

                            float amount = event.getAmount() * finalMultiplier;

                            // ====== 调试信息优化 ======
                            if (Config.element_debug) {
                                String analysis = String.format("""
                                                [平衡版元素分析]
                                                压制系统：
                                                ↳ 克制关系: %s → %s 系数: %s
                                                ↳ 基础倍率: %.2f → 增强指数: %.2f → 最终压制: %.2f
                                                攻击系统：
                                                ↳ 等级: %,d → 转化率: %.1f%% → 计算基数: %.1f
                                                ↳ 成长曲线: %s → 攻击因子: %.2f
                                                防御系统：
                                                ↳ 等级: %,d → 抗性系数: %.1f → 防御因子: %.2f
                                                ↳ 护甲减伤: %.1f%%
                                                最终倍率: %.2f × (%.2f / %.2f) × %.1f%% = %.2f
                                                最终伤害: %.1f → %.1f
                                                """,
                                        exElementInstant.getElement().getId(), el.getElement().getId(),baseMultiplier,
                                        baseMultiplier, dominance_power + Math.log1p(attackerLevel / 5000.0), dominanceEffect,
                                        attackerLevel, conversion_rate, attackBase,
                                        (attackBase <= 800) ? "线性" : (attackBase <= 8000) ? "对数" : "亚线性", attackFactor,
                                        targetLevel, resistance_base, defenseFactor,
                                        armorResistance * 100,
                                        dominanceEffect, attackFactor, defenseFactor, (1 - armorResistance) * 100, finalMultiplier,
                                        event.getAmount(), amount
                                );
                                livingEntity.sendSystemMessage(Component.literal(analysis));
                            }

                            event.setAmount(amount);
                        } else {
                            float armorResistance = getArmorResistance(target, exElementInstant);
                            event.setAmount(event.getAmount() * Math.max(0.15f, 1.0f - armorResistance));
                        }
                    }
                    cache.invalidate(event.getSource());
                }
            }
    }
}
