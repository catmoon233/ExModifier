package net.exmo.exmodifier.content.element;

import com.google.common.cache.Cache;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.exmo.exmodifier.Config;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.difficult.ExDifficultHelper;
import net.exmo.exmodifier.content.dynamicAttributes.DynamicAttribute;
import net.exmo.exmodifier.content.dynamicAttributes.DynamicAttributeRegister;
import net.exmo.exmodifier.content.helper.ExElementHelper;
import net.exmo.exmodifier.content.helper.entity.ExElementEntityHelper;
import net.exmo.exmodifier.content.modifier.MoConfig;
import net.exmo.exmodifier.events.ElementDamageEvent;
import net.exmo.exmodifier.events.OnElementRegisterEvent;
import net.exmo.exmodifier.init.ExAttribute;
import net.exmo.exmodifier.network.DamageNumberCompatMessage;
import net.exmo.exmodifier.util.ExConfigHandle;
import net.exmo.exmodifier.util.ItemSelector;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.PacketDistributor;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ExElementHandle {
    public static Map<ResourceLocation, ExElement> exElements = new java.util.HashMap<>();
    public static List<MoConfig> FoundElementConfigs = new ArrayList<>();
    public static final Path ElementConfigPath = FMLPaths.CONFIGDIR.get().resolve("exmo/elements/");
    public static Cache<DamageSource, List<ExElementInstant>> cache = com.google.common.cache.CacheBuilder.newBuilder().build();

    public static Optional<List<ExElementInstant>> getExElementInstant(DamageSource source) {
        return Optional.ofNullable(cache.getIfPresent(source));
    }

    enum ElementAttributeName {
        DAMAGE("damage"),
        ELEMENT_ATTRIBUTE("elementAttribute");


        ElementAttributeName(String name) {
            this.name = name;
        }
        String name;
    }
    record elementAttributeSer(String elementName,String modifier){
        public String gather(){
            return elementName+"::"+modifier;
        }
        public static elementAttributeSer fromString(String str){
            String[] split = str.split("::");
            return new elementAttributeSer(split[0],split[1]);
        }
    }
    public static void registryExElement(ExElement exElement) {
        exElements.put(exElement.getId(), exElement);
        for (ElementAttributeName elementAttributeName : ElementAttributeName.values()){
            DynamicAttributeRegister.registerDynamicAttribute(new DynamicAttribute(new elementAttributeSer(exElement.getId().toString(),elementAttributeName.name).gather(),new DynamicAttribute.range(0,Double.MAX_VALUE,0)));
        }
        Exmodifier.LOGGER.info("Registry ExElement: " + exElement.getId());
    }

    public static ExElement getExElement(ResourceLocation id) {
        return exElements.get(id);
    }

    public static ExElement getExElement(String id) {
        ExElement exElement = exElements.get(new ResourceLocation(id));
        return exElement;
    }

    public static Map<ResourceLocation, ExElement> getExElements() {
        return exElements;
    }


    public static void init() throws IOException {
        MinecraftForge.EVENT_BUS.post(new OnElementRegisterEvent());
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
            Exmodifier.LOGGER.info("No Element Config Found");
            return;
        }
        var elements = ExElement.EX_SERIALIZE.fromJson(moconfig.jsonObject);
        elements.forEach(ExElementHandle::registryExElement);


    }

    public static final Map<ItemSelector, DefaultItemElement> elementDefaultMap = new HashMap<>();
    public static final Map<EntityType<?>,DefaultEntityElement> elementDefaultMap2 = new HashMap<>();
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
        for (var read : DefaultItemElement.EX_SERIALIZE.fromJson(moconfig.jsonObject)){
            if (read.getItemSelector()!=null) elementDefaultMap.put(read.getItemSelector(),read);
        }
//        for (Map.Entry<String, JsonElement> entry : moconfig.readEntrys()) {
//            processElementDefaultConfigEntry(entry);
//        }
    }

    public static void processMoConfigEntries3(MoConfig moconfig) throws FileNotFoundException {
        if (moconfig.readEntrys().isEmpty()) {
            Exmodifier.LOGGER.info("No Entity Default Element Config Found: " + moconfig.configFile);
            return;
        }
        for (var read : DefaultEntityElement.SERIALIZER.fromJson(moconfig.jsonObject)){
            if (!read.getEntityTypeString().isEmpty()) elementDefaultMap2.put(read.getEntityType(),read);
        }
//        for (Map.Entry<String, JsonElement> entry : moconfig.readEntrys()) {
//            processEntityElementDefaultConfigEntry(entry);
//        }
    }

    private static void processEntityElementDefaultConfigEntry(Map.Entry<String, JsonElement> entry) {
//        if (!entry.getValue().isJsonObject()) {
//            Exmodifier.LOGGER.Logger.error("Invalid config entry format: " + entry.getKey());
//            return;
//        }
//
//        try {
//            JsonObject jsonObject = entry.getValue().getAsJsonObject();
//
//
//            // 解析entries
//            List<ExElementInstant> elements = new ArrayList<>();
//            if (jsonObject.has("entries") && jsonObject.get("entries").isJsonArray()) {
//                for (JsonElement elementEntry : jsonObject.get("entries").getAsJsonArray()) {
//                    if (!elementEntry.isJsonObject()) continue;
//
//                    JsonObject entryObj = elementEntry.getAsJsonObject();
//                    String elementId = entryObj.get("id").getAsString();
//                    int level = entryObj.has("level") ? entryObj.get("level").getAsInt() : 1;
//
//                    ExElement element = ExElementHandle.getExElement(elementId);
//                    if (element != null) {
//                        elements.add(new ExElementInstant(element, level));
//                    } else {
//                        Exmodifier.LOGGER.Logger.error("Unknown element ID: " + elementId);
//                    }
//                }
//            }
//
//            Optional<EntityType<?>> entityType = EntityType.byString(entry.getKey());
//            if (jsonObject.has("entityType")) {
//                entityType = EntityType.byString(jsonObject.get("entityType").getAsString());
//            }
//            // 注册到map
//            if (entityType.isPresent() && !elements.isEmpty()) {
//                elementDefaultMap2.put(entityType.get(), elements);
//                Exmodifier.LOGGER.Logger.debug("Added default entity elements for selector: {}", entityType);
//            }
//        } catch (Exception e) {
//            Exmodifier.LOGGER.error("Error processing entity element default entry: " + entry.getKey(), e);
//        }
    }

    private static void processElementDefaultConfigEntry(Map.Entry<String, JsonElement> entry) {
//        if (!entry.getValue().isJsonObject()) {
//            Exmodifier.LOGGER.Logger.error("Invalid config entry format: " + entry.getKey());
//            return;
//        }
//
//        try {
//            JsonObject jsonObject = entry.getValue().getAsJsonObject();
//
//            // 解析itemSelector
//            ItemSelector itemSelector = null;
//            if (jsonObject.has("itemSelector")) {
//                itemSelector = ItemSelector.EX_SERIALIZE.fromJsonSingleObject(jsonObject.get("itemSelector").getAsJsonObject(), "itemSelector");
//            }
//
//            // 解析entries
//            List<ExElementInstant> elements = new ArrayList<>();
//            if (jsonObject.has("entries") && jsonObject.get("entries").isJsonArray()) {
//                for (JsonElement elementEntry : jsonObject.get("entries").getAsJsonArray()) {
//                    if (!elementEntry.isJsonObject()) continue;
//
//                    JsonObject entryObj = elementEntry.getAsJsonObject();
//                    String elementId = entryObj.get("id").getAsString();
//                    int level = entryObj.has("level") ? entryObj.get("level").getAsInt() : 1;
//
//                    ExElement element = ExElementHandle.getExElement(elementId);
//                    if (element != null) {
//                        elements.add(new ExElementInstant(element, level));
//                    } else {
//                        Exmodifier.LOGGER.Logger.error("Unknown element ID: " + elementId);
//                    }
//                }
//            }
//
//            // 注册到map
//            if (itemSelector != null && !elements.isEmpty()) {
//                elementDefaultMap.put(itemSelector, elements);
//                Exmodifier.LOGGER.Logger.debug("Added default elements for selector: {}", itemSelector);
//            }
//        } catch (Exception e) {
//            Exmodifier.LOGGER.error("Error processing element default entry: " + entry.getKey(), e);
//        }
    }

    public static List<ExElementInstant> getDefaultElements(ItemStack stack) {
        return elementDefaultMap.entrySet().stream().filter(entry -> entry.getKey().compare(stack)).flatMap(entry -> entry.getValue().getExElementInstants().stream()).collect(Collectors.toList());
    }

    public static List<ItemSelector> getItemSelector(ItemStack stack) {
        return elementDefaultMap.keySet().stream().filter(entry ->entry!=null && entry.compare(stack)).toList();
    }

    @Mod.EventBusSubscriber
    public static class CommonEvent {
        @SubscribeEvent
        public static void registerNormal(OnElementRegisterEvent elementRegisterEvent){
            ResourceLocation id = ResourceLocation.tryParse("exmodifier:normal");
            if (getExElement(id)==null) elementRegisterEvent.registerElement(new ExElement(id).setColor(Color.WHITE.getRGB()).setRestrain(new HashMap<>()));
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
        // 新建元素伤害计算模块
        public static class ElementDamageCalculator {

            // 旧版计算方法
            public static CalculationResult calculateLegacyDamage(
                    float dominanceEffect,
                    float attackFactor,
                    float defenseFactor,
                    float armorResistance
            ) {
                float baseMultiplier = dominanceEffect * attackFactor / defenseFactor;
                float finalMultiplier = baseMultiplier * (1 - armorResistance);
                finalMultiplier = Math.max(0.01f, finalMultiplier);

                return new CalculationResult(
                        finalMultiplier,
                        attackFactor,
                        defenseFactor,
                        attackFactor / defenseFactor,
                        "旧版公式：压制系数×(攻击因子/防御因子)",
                        dominanceEffect,
                        armorResistance,
                        "LEGACY"
                );
            }

            // 新版分段计算方法
            public static CalculationResult calculateNewDamage(
                    float attackFactor,
                    float defenseFactor,
                    float dominanceEffect,
                    float armorResistance
            ) {
                float attackIntensity = attackFactor;
                float defenseIntensity = defenseFactor;
                float ratio = attackIntensity / defenseIntensity;
                String formulaUsed = "";
                float baseMultiplier;

                // 根据比值选择计算公式
                if (ratio > 0.8f && ratio < 1.25f) {
                    baseMultiplier = dominanceEffect * ratio;
                    formulaUsed = "新版-平衡区间：压制系数×(攻击/防御)";
                } else if (ratio >= 0.6667f && ratio <= 0.8f) {
                    baseMultiplier = dominanceEffect * ((float) Math.pow(ratio, 2) + 0.16f);
                    formulaUsed = "新版-弱势区间：压制系数×[(攻击/防御)^2+0.16]";
                } else if (ratio >= 1.25f && ratio <= 1.5f) {
                    baseMultiplier = dominanceEffect * ((float) Math.sqrt(ratio) + 0.132f);
                    formulaUsed = "新版-优势区间：压制系数×[√(攻击/防御)+0.132]";
                } else if (ratio < 0.6667f) {
                    baseMultiplier = dominanceEffect * (0.15f * ratio + 0.5f);
                    formulaUsed = "新版-绝对弱势：压制系数×(0.15*(攻击/防御)+0.5)";
                } else {
                    baseMultiplier = dominanceEffect * 0.15f * (10f - 1f / ratio);
                    formulaUsed = "新版-绝对优势：压制系数×0.15×(10-1/(攻击/防御))";
                }

                float finalMultiplier = baseMultiplier * (1 - armorResistance);
                finalMultiplier = Math.max(0.01f, finalMultiplier);

                return new CalculationResult(
                        finalMultiplier,
                        attackFactor,
                        defenseFactor,
                        ratio,
                        formulaUsed,
                        dominanceEffect,
                        armorResistance,
                        "NEW"
                );
            }

            // 统一结果封装类
            public record CalculationResult(float finalMultiplier, float attackIntensity, float defenseIntensity,
                                                        float ratio, String formulaDesc, float dominanceEffect,
                                                        float armorResistance, String version) {
            }
        }
       static final float LEVEL_FACTOR = 5000.0f; // 等级影响系数
       static final float POWER_SCALE = 0.85f;    // 基础强度缩放
       static final float MIN_EFFECT = 0.1f;      // 最小压制效果
        public static  float calculateDominanceEffect(Float baseMultiplier, float dominancePower, int attackerLevel) {

            // 等级带来的增益系数（0~1之间）
            float levelBonus = (float) Math.log1p(attackerLevel / LEVEL_FACTOR);

            // 基础压制系数调整（当基础倍数<1时进行平滑处理）
            float adjustedBase = baseMultiplier < 1.0f ?
                    (float) Math.pow(baseMultiplier, 0.6f) :  // 减缓衰减速度
                    baseMultiplier;

            // 动态指数计算
            float exponent = dominancePower * (1.0f + levelBonus * 0.3f); // 等级影响上限30%

            // 核心压制效果
            float rawEffect = (float) Math.pow(adjustedBase, exponent);

            // 效果增强曲线（保持正向增长的敏感性）
            float scaledEffect = (float) Math.pow(rawEffect, 1.15f);

            // 当基础倍数<1时的补偿机制
            if (baseMultiplier < 1.0f) {
                // 补偿公式：0.5*(1 + x) 当x接近0时补偿到0.5，x=1时补偿到1.0
                float compensation = 0.5f * (1.0f + scaledEffect);
                scaledEffect = Math.max(scaledEffect, compensation);
            }

            // 最终效果修正
            return (float) Math.pow(Math.max(MIN_EFFECT, scaledEffect * POWER_SCALE),1.2f);
        }
        @SubscribeEvent(priority = EventPriority.LOW)
        public static void onHurtOver(LivingDamageEvent event) {
            if (event.getSource().is(DamageTypes.GENERIC_KILL)) return;
            Entity entity = event.getSource().getEntity();
            if (entity instanceof LivingEntity livingEntity) {
                ItemStack mainHandItem = livingEntity.getMainHandItem();
                List<ExElementInstant> elements = new ArrayList<>();
                if (mainHandItem.hasTag()) {
                    elements = ExElementHelper.of(mainHandItem).getElements();
                } else {
                    List<ExElementInstant> exElementInstants1 = ExElementEntityHelper.of(livingEntity).getExElementInstants();
                    if (!exElementInstants1.isEmpty()) {
                        elements = exElementInstants1;
                    };
                }
                if (elements.isEmpty()) elements =List.of(new ExElementInstant(exElements.get(ResourceLocation.tryParse("exmodifier:normal")), ExDifficultHelper.getElementLevel(livingEntity)));
                ExElementInstant exElementInstant = elements.get(0);
                LivingEntity target = event.getEntity();
                ExElementEntityHelper exElementEntityHelper = ExElementEntityHelper.of(target);
                List<ExElementInstant> exElementInstants = exElementEntityHelper.getExElementInstants();
                if (exElementInstants.isEmpty())return;
                // 粒子效果逻辑保持不变
                if (livingEntity.level() instanceof ServerLevel serverLevel) {
                    if (exElementInstant.getElement() != null) {

                        if (exElementInstant.getElement().getSimpleParticleType() != null) {

                            ParticleType<?> simpleParticleType = exElementInstant.getElement().getSimpleParticleType();
                            if (simpleParticleType != null) {
                                if (simpleParticleType instanceof SimpleParticleType simpleParticleType1) {
                                    float particleScale = exElementInstant.getElement().getParticleScale();
                                    serverLevel.sendParticles(simpleParticleType1, target.getX(), target.getY() + target.getEyeHeight() * 0.5, target.getZ(), 5, particleScale, particleScale, particleScale, 0.25);
                                }
                            }
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


                if (!exElementInstants.isEmpty()) {
                    var el = exElementInstants.get(0);
                    int attackerLevel = exElementInstant.getLevel();
                    int targetLevel = el.getLevel();

                    float armorResistance = getArmorResistance(target, exElementInstant);

                    // 压制效果计算保持不变...
                    Map<String, Float> restrain = exElementInstant.getElement().getRestrain();
                    if (el.getElement()==null) return;
                    if (restrain==null) restrain = new HashMap<>();
                    Float baseMultiplier = restrain .getOrDefault(el.getElement().getId().toString(), 1f);
//                    float dominanceEffect = (float) Math.pow(Math.pow(
//                            baseMultiplier,
//                            dominance_power + (float) Math.log1p(attackerLevel / 5000.0)
//                    ), 1.35f);

                    float dominanceEffect = calculateDominanceEffect(baseMultiplier, dominance_power, attackerLevel);
                    // 攻击因子计算保持不变...
                    float conversion_ratio = conversion_rate / 120.0f;
                    float attackBase = attackerLevel * conversion_ratio;
                    float attackFactor;
                    if (attackBase <= 800) {
                        attackFactor = 1.0f + attackBase * 0.3f;
                    } else if (attackBase <= 8000) {
                        float logGrowth = (float) Math.log1p(attackBase - 800) * 1.5f;
                        attackFactor = 1.0f + 240.0f + logGrowth;
                    } else {
                        float baseValue = 1.0f + 240.0f + (float) Math.log(7200) * 1.5f;
                        float additional = (float) Math.pow(attackBase - 8000, 0.5f);
                        attackFactor = baseValue + additional;
                    }

                    if (attackerLevel > 50 * targetLevel) {
                        float overkill = (float) Math.log10(attackerLevel / (targetLevel + 1.0f));
                        float overkillBoost = overkill_ratio * (1.0f + conversion_ratio * 0.08f);
                        attackFactor *= 1.0f + Math.min(overkill, 2.5f) * overkillBoost;
                    }

                    // 防御因子计算保持不变...
                    float defenseFactor = 1.0f + (float) Math.pow(
                            (targetLevel * resistance_base) / 800.0f,
                            0.75f
                    );

                    // 选择计算版本（此处可通过配置切换）
                    ElementDamageCalculator.CalculationResult result = Config.useNewDamageFormula ?
                            ElementDamageCalculator.calculateNewDamage(attackFactor, defenseFactor, dominanceEffect, armorResistance) :
                            ElementDamageCalculator.calculateLegacyDamage(dominanceEffect, attackFactor, defenseFactor, armorResistance);

                    float amount = event.getAmount() * result.finalMultiplier;

                    // 调试信息增强
                    if (Config.element_debug) {
                        String analysis = String.format("""
                                [元素伤害分析][%s]
                                压制系统：
                                ↳ 克制关系: %s → %s 基础系数: %.2f
                                ↳ 最终压制效果: %.2f
                                强度系统：
                                ↳ 攻击强度: %.1f | 防御强度: %.1f → 强度比: %.2f
                                ↳ 计算公式: %s
                                抗性系统：
                                ↳ 护甲减伤: %.1f%%
                                最终计算：
                                ↳ 基础倍率: %.2f
                                ↳ 护甲减伤系数: (1 - %.1f%%) → %.2f
                                ↳ 最终倍率: %.2f × %.2f = %.2f
                                最终伤害: %.1f → %.1f
                                """,
                                result.version,
                                exElementInstant.getElement().getId(), el.getElement().getId(), baseMultiplier,
                                result.dominanceEffect,
                                result.attackIntensity, result.defenseIntensity, result.ratio,
                                result.formulaDesc,
                                result.armorResistance * 100,
                                result.dominanceEffect, 
                                result.armorResistance * 100, // 修正减伤系数显示值
                                (1 - result.armorResistance), // 新增实际使用的减伤系数
                                result.dominanceEffect, (1 - result.armorResistance), result.finalMultiplier, 
                                event.getAmount(), amount
                        );
                        livingEntity.sendSystemMessage(Component.literal(analysis));
                    }

                    event.setAmount(amount);
                } else {
                    float armorResistance = getArmorResistance(target, exElementInstant);
                    event.setAmount(event.getAmount() * Math.max(0.15f, 1.0f - armorResistance));
                }
                cache.invalidate(event.getSource());
            }
        }
    }
}
