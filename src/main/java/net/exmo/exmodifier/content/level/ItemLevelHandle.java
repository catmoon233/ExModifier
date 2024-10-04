package net.exmo.exmodifier.content.level;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.event.parameter.EventParameter;
import net.exmo.exmodifier.content.modifier.MoConfig;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.events.ExItemUpEvent;
import net.exmo.exmodifier.events.ExLevelRegistryEvent;
import net.exmo.exmodifier.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.getEquipmentSlot;
import static net.exmo.exmodifier.content.modifier.ModifierHandle.materialsList;

public class ItemLevelHandle {
    @Mod.EventBusSubscriber
    public static class CommonEvent{
        @SubscribeEvent
        public static void AtUp(ExItemUpEvent event){
            ItemStack stack = event.stack;
            if (stack.getTag() == null)return;
            List<LevelAttriGether> attriGethers = event.LevelId.attriGethers;
            for (int i = 0; i < attriGethers.size(); i++){
                DynamicExpressionEvaluator dynamicExpressionEvaluator = new DynamicExpressionEvaluator();
                LevelAttriGether attriGether = attriGethers.get(i);
                UUID uuid = UUID.nameUUIDFromBytes((attriGether.getModifier().getName()+i+ stack).getBytes());
                EquipmentSlot slot = attriGether.slot;
                if (attriGether.IsAutoEquipmentSlot) {
                    if (stack.getItem() instanceof ArmorItem armorItem){
                        slot = armorItem.getEquipmentSlot();
                    }
                    else slot =ModifierEntry.TypeToEquipmentSlot(ModifierEntry.getType(stack));
                }                ItemAttrUtil.removeAttributeModifierNoAmout(stack, attriGether.getAttribute(), attriGether.getModifier(), slot);
                dynamicExpressionEvaluator.setVariable("level", event.nowLevel);

                double amout =  dynamicExpressionEvaluator.evaluate(attriGether.Expression);
                ItemAttrUtil.addItemAttributeModifier2(stack,attriGether.attribute,new AttributeModifier(uuid,attriGether.getModifier().getName(), amout, attriGether.modifier.getOperation()), slot);

            }
            if (event.entity instanceof  Player player){
              //  if (player.level().isClientSide) {
                    player.sendSystemMessage(Component.translatable("modifiler.level."+event.LevelId.id).append(" ").append(stack.getDisplayName()).append(" §r").append(Component.translatable("modifiler.level.up",event.beforeLevel,event.nowLevel,event.nowLevel-event.beforeLevel)));
                    //player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.player.levelup")), SoundSource.PLAYERS, 1, 1, false);

             //   }else {
                    player.level().playSound(null, new BlockPos((int) player.getX(), (int) player.getY(), (int) player.getZ()), ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.player.levelup")), SoundSource.PLAYERS, 1, 1);

             //   }
            }
        }
    }
    public static List<MoConfig> Foundlvconfigs = new ArrayList<>();
    public static final Path LEVEL_CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("exmo/ItemLevel");
    public static Map<String, ItemLevel> ItemLevels = new java.util.HashMap<>();
    public static void RegistryItemLevel(ItemLevel itemLevel){
        ItemLevels.put(itemLevel.getId(), itemLevel);
        Exmodifier.LOGGER.debug("Registry ItemLevel: " + itemLevel.id);
    }
    public static void contaiff(ItemStack stack, int rarity , int refreshnumber, ModifierEntry.Type type)  {
        Exmodifier.LOGGER.debug("ItemLevelRefresh: " + stack.getDescriptionId() + " " + type);
        WeightedUtil<String> weightedUtil = new WeightedUtil<>(
                ItemLevels.entrySet().stream()
                        .filter(e -> e.getValue().type == type )
//                                .filter(e -> (e.getValue().getOnlyItemIds().isEmpty() ||e.getValue().getOnlyItemIds().contains(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString())))
//                                .filter(e -> !e.getValue().cantSelect)
//                                .filter(e -> e.getValue().needFreshValue ==0 || e.getValue().needFreshValue <= refreshnumber)
//                                .filter(e -> e.getValue().getOnlyItemTags().isEmpty() ||e.getValue().containTag(stack))
//                                .filter(e -> e.getValue().OnlyWashItems.isEmpty() ||e.getValue().OnlyWashItems.contains(washItem))
//                                .filter(e -> {
//                                    boolean hasWashItem = materialsList.stream()
//                                            .filter(m -> m.ItemId.equals(washItem))
//                                            .findAny()
//                                            .map(m -> !m.OnlyHasWashEntry)
//                                            .orElse(true);
//
//                                    return hasWashItem || e.getValue().OnlyWashItems.contains(washItem);
//                                })
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getWeight()))
        );
        //RandomEntryCurios(stack, weightedUtil, curiosType, refreshnumber);
        if (!weightedUtil.weights.isEmpty()) {
            Exmodifier.LOGGER.debug("RandomLeelEntry: " + type);
            weightedUtil.increaseWeightsByRarity(rarity);
            ItemLevelRefresh2(stack, weightedUtil, refreshnumber);

            Exmodifier.LOGGER.debug("RandomLevel: " + type + " " + stack.getDescriptionId());
            stack.getTag().putInt("exmodifier_level_modifier_applied",
                    stack.getTag().getInt("exmodifier_level_modifier_applied") + 1);
        }

    }
    public static void ItemLevelRefresh(ItemStack stack,int rarity ,int refreshnumber,String washItem)  {
        if (stack.getTag() == null)return;
        if (  stack.getTag().getInt("exmodifier_level_modifier_applied") > 0) return;
        // List<String> curiosType = CuriosUtil.getSlotsFromItemstack(stack);
        boolean find = false;
        for (ModifierEntry.Type type : Arrays.stream(ModifierEntry.Type.values()).filter(e -> e != ModifierEntry.Type.UNKNOWN).filter(e -> e != ModifierEntry.Type.ALL).toList()) {
            if (ModifierEntry.containItemType(stack, type)) {
                contaiff(stack,rarity,refreshnumber,type);
                find = true;
                break;
            }

        }
        if (!find) {
            contaiff(stack,rarity,refreshnumber, ModifierEntry.Type.ALL);
            Exmodifier.LOGGER.debug("ItemLevelRefresh: No Type And refresh ALL TYPE");
        }
    }
    public static void ItemAddXpAuto(LivingEntity entity, List<EventParameter<?>> eventParameters,LivingEvent event1,String packname) {
        if (entity==null)return;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = entity.getItemBySlot(slot);
            ItemAddXp(entity,stack, eventParameters,event1,packname);
        }



    }
    public static Boolean pdEntitySequence(LivingEntity e,LivingEvent event,int entitySequence){
        if (event instanceof LivingHurtEvent lh){
            if (entitySequence==0){
                return lh.getEntity() == e;
            }
            if (entitySequence==1){
                return lh.getSource().getEntity() == e;
            }
        }
        return true ;
    }

    public static void ItemAddXp(LivingEntity entity, ItemStack stack, List<EventParameter<?>> params, LivingEvent event1,String packname){
        Exmodifier.LOGGER.debug("ItemLevelUp0: " + stack.serializeNBT());
        if (stack.getTag()==null)return;
        for (ItemLevel il : getItemLevels(stack)){
           // Exmodifier.LOGGER.debug("ItemLevelUp: " + il.id);
            if ( packname.equals (il.getUpEvent())) {
             //   Exmodifier.LOGGER.debug("ItemLevelUp1: " + il.id + " " + event1.getClass().getName());
                if (pdEntitySequence(entity, event1, il.getEntitySequence())) {
               //     Exmodifier.LOGGER.debug("ItemLevelUp2: " + il.id + " " + event1.getClass().getName());
                    ItemAddXpM(stack, params, il, entity);
                }
            }
        }

    }
    public static void ItemAddXpM(ItemStack stack,List<EventParameter<?>> params,ItemLevel itemLevel,LivingEntity entity) {
        if (stack.getTag() == null) return;
        DynamicExpressionEvaluator evaluator = new DynamicExpressionEvaluator();
        for (EventParameter<?> param : params) {
            evaluator.setVariable(param.getKey(), param.getDouble());
        }
        double addXp = evaluator.evaluate(itemLevel.getXpAddExpression());
        double level = getLevelItemLevel(stack, itemLevel.id);
        double xp = getLevelItemXp(stack, itemLevel.id);
        double needXp = getLevelItemNeedXpUp(stack, itemLevel.id);
        double _level = level;
        double finalXp = xp + addXp;
        if (finalXp > 0) {
            while (finalXp > 0) {
                if (finalXp >= needXp) {
                    level++;
                    finalXp -= needXp;
                    needXp = generateLevelNeedXp(itemLevel, (int) level);
                } else break;

            }
            setLevelItemLevel(stack, itemLevel.id, (int) level);
            setLevelItemXp(stack, itemLevel.id, (int) finalXp);
            setItemNeedXpUp(stack, itemLevel.id, needXp);

        }
        if (_level != level) {
            ExItemUpEvent event = new ExItemUpEvent(stack, (int) level,entity, (int) _level, (int) finalXp, itemLevel);
            MinecraftForge.EVENT_BUS.post(event);
        }
    }


    private static void setLevelItemLevel(ItemStack stack, String id, int level2) {
        if (stack.getTag()==null)return;
        stack.getTag().putDouble(id +"_level", level2);
    }

    private static void setLevelItemXp(ItemStack stack, String id, int xp2) {
        if (stack.getTag()==null)return;
        stack.getTag().putDouble(id +"_Xp", xp2);
    }

    public static double generateLevelNeedXp(ItemLevel itemLevel,int level)  {
        DynamicExpressionEvaluator evaluator = new DynamicExpressionEvaluator();
        evaluator.setVariable("level", level);
        evaluator.setVariable("lv", level);

        return evaluator.evaluate(itemLevel.getLevelExpression());
       // return 50;
    }
    public static List<ItemLevel> getItemLevels (ItemStack stack){
        List<ItemLevel> itemLevels = new ArrayList<>();
        if (stack.getTag() ==null)return itemLevels;

        for (int i = 0;true;i++){
            String id = stack.getTag().getString("exmodifier_level_modifier_applied"+i);
            if (id.isEmpty())break;
            itemLevels.add(ItemLevels.get(id));
        }
        return itemLevels;
    }
    public static List<Component> genItemLevelInfo(ItemStack stack){
        List<ItemLevel> itemLevels = getItemLevels(stack);
        List<Component> components = new ArrayList<>();
        for (ItemLevel itemLevel : itemLevels){
            components.addAll(getLevelItemLevelInfo(stack,itemLevel));
        }
        return components;
    }


    public static List<Component> getLevelItemLevelInfo(ItemStack stack, ItemLevel itemLevel) {
        List<Component> components = new ArrayList<>();
        int levelItemLevel = getLevelItemLevel(stack, itemLevel.id);
        if (Screen.hasShiftDown()) {
            // 添加物品等级信息
            components.add(Component.translatable("modifiler.level." + itemLevel.id)
                    .withStyle(ChatFormatting.GOLD)
                    .withStyle(ChatFormatting.UNDERLINE)
            ); // 使用颜色增强视觉效果

            // 显示当前等级与最大等级
            String levelInfo = String.format("%s %s %s",
                    " ",
                    levelItemLevel +
                            " / ",
                    getLevelItemMaxLevel(stack, itemLevel.id) +
                            " ");

            components.add(Component.translatable("modifiler.level")
                    .append(Component.literal(levelInfo))
                    .withStyle(ChatFormatting.AQUA)); // 另一个颜色强调

            // 显示当前经验与需经验
            String xpInfo = String.format("%s %s %s",
                    " ",
                    getLevelItemXp(stack, itemLevel.id) +
                            " / ",
                    getLevelItemNeedXpUp(stack, itemLevel.id) +
                            " ");

            components.add(Component.translatable("modifiler.xp")
                    .append(Component.literal(xpInfo))
                    .withStyle(ChatFormatting.GREEN));


        }else{
            components.add(Component.translatable("modifiler.level." + itemLevel.id)
                    .withStyle(ChatFormatting.GOLD).append(" ").append(levelItemLevel+""));
        }
            return components;
    }
    public static void addLevelSystemToItem(ItemStack stack,ItemLevel level,int index) {
        if (stack.getTag()==null)return;
        stack.getTag().putString("exmodifier_level_modifier_applied" + index, level.id);
        stack.getTag().putInt(level.id +"_level", level.getDefaultLevel());
        stack.getTag().putInt(level.id +"_MaxLevel", level.getMaxLevel());
        setItemNeedXpUp(stack,level.id, generateLevelNeedXp(level,level.getDefaultLevel()));
        stack.getTag().putDouble(level.id +"_Xp", 0);
    }
    public static void setItemNeedXpUp(ItemStack stack,String levelId,double xp){
        if (stack.getTag()==null)return;
        stack.getTag().putDouble(levelId +"_NeedXpUp",xp);
    }
    public static int getLevelItemXp(ItemStack stack,String levelId){
        if (stack.getTag()==null)return 0;
        return (int) stack.getTag().getDouble(levelId +"_Xp");
    }
    public static int getLevelItemNeedXpUp(ItemStack stack,String levelId){
        if (stack.getTag()==null)return 0;
        return (int) stack.getTag().getDouble(levelId +"_NeedXpUp");
    }
    public static int getLevelItemLevel(ItemStack stack,String levelId){
        if (stack.getTag()==null)return 0;
        return stack.getTag().getInt(levelId +"_level");
    }
    public static int getLevelItemMaxLevel(ItemStack stack,String levelId){
        if (stack.getTag()==null)return 0;
        return stack.getTag().getInt(levelId +"_MaxLevel");
    }
    private static void ItemLevelRefresh2(ItemStack stack, WeightedUtil<String> weightedUtil, int refreshnumber)  {
        int numAddedModifiers = 0;
        List<ItemLevel> foundItemLevels = new ArrayList<>();

        if (weightedUtil.weights.size()<refreshnumber)refreshnumber = weightedUtil.weights.size();
        while (numAddedModifiers < refreshnumber) {
            ItemLevel itemLevel = ItemLevels.get(weightedUtil.selectRandomKeyBasedOnWeights());
            if (foundItemLevels.contains(itemLevel))continue;
            Exmodifier.LOGGER.debug("add leelentry: " + itemLevel.id);
            foundItemLevels.add(itemLevel);
            addLevelSystemToItem(stack, itemLevel, numAddedModifiers);

            numAddedModifiers++;

        }
    }

    private static void processItemLevel(MoConfig moconfig, Map.Entry<String, JsonElement> entry, List<ItemLevel> entries) {
        JsonElement itemElement = entry.getValue();
        if (!itemElement.isJsonObject()) {
            return;
        }

        JsonObject itemObject = itemElement.getAsJsonObject();
        ItemLevel itemLevel = new ItemLevel();


        itemLevel.setType(itemObject.has("type") ? ModifierEntry.StringToType(itemObject.get("type").getAsString()) : moconfig.type);
        itemLevel.setId(itemLevel.getType().toString().substring(0,2)+entry.getKey());
        if (!moconfig.CuriosType.isEmpty())itemLevel.curiosType = moconfig.CuriosType;
        if (itemLevel.type.toString().toUpperCase().startsWith("CURIOS")){
            itemLevel.isCuriosEntry = true;
            if (itemObject.has("curiosType"))
            {
                itemLevel.curiosType = itemObject.get("curiosType").getAsString();
            }else {
                if (moconfig.CuriosType.isEmpty()) itemLevel.curiosType = "ALL";
            }
        }
        itemLevel.setDefaultLevel(itemObject.has("DefaultLevel") ? itemObject.get("DefaultLevel").getAsInt() : 0);
        itemLevel.setMaxLevel(itemObject.has("MaxLevel") ? itemObject.get("MaxLevel").getAsInt() : 0);
        if(itemObject.has("UpEvent")){
            itemLevel.setUpEvent(itemObject.get("UpEvent").getAsString());
        }
        itemLevel.setEntitySequence(itemObject.has("EntitySequence") ? itemObject.get("EntitySequence").getAsInt() : 0);
        if(itemObject.has("LevelExpression")){
            itemLevel.setLevelExpression(itemObject.get("LevelExpression").getAsString());
        }
        if (itemObject.has("XpAddValue")){
            itemLevel.setXpAddValue(itemObject.get("XpAddValue").getAsString());
        }
        if (itemObject.has("XpAddExpression")){
            itemLevel.setXpAddExpression(itemObject.get("XpAddExpression").getAsString());
        }
        itemLevel.needFreshValue = itemObject.has("needFreshValue") ? itemObject.get("needFreshValue").getAsFloat() : 0.0F;
        itemLevel.cantSelect = itemObject.has("cantSelect") && itemObject.get("cantSelect").getAsBoolean();
        if (itemObject.has("weight")){
            itemLevel.setWeight(itemObject.get("weight").getAsFloat());
        }
        if (itemObject.has("OnlyWashItems")){
            JsonArray OnlyItems = itemObject.get("OnlyWashItems").getAsJsonArray();
            OnlyItems.forEach(item -> {
                itemLevel.OnlyWashItems.add(item.getAsString());
            });
        }
        if (itemObject.has("OnlyItemIds")){
            itemLevel.setOnlyItemIds(new ArrayList<>());
            for (JsonElement itemId : itemObject.get("OnlyItemIds").getAsJsonArray()){
                itemLevel.getOnlyItemIds().add(itemId.getAsString());
            }
        }
        if (itemObject.has("OnlyItemTags")){
            itemLevel.setOnlyItemTags(new ArrayList<>());
            for (JsonElement itemTag : itemObject.get("OnlyItemTags").getAsJsonArray()){
                itemLevel.getOnlyItemTags().add(itemTag.getAsString());
            }
        }
        if (itemObject.has("attrGethers")) {
            processAttrGethers(moconfig, itemLevel, itemObject.getAsJsonObject("attrGethers"));
        }
    }
    public static void processAttrGethers(MoConfig moconfig, ItemLevel itemLevel, JsonObject attrGethers){
        int index = 0;
        for (Map.Entry<String, JsonElement> attrGetherEntry : attrGethers.entrySet()) {
            try {
                processAttrGether(moconfig, itemLevel, attrGetherEntry,index);
                index++;
            } catch (Exception e) {
                Exmodifier.LOGGER.error("Error processing attrGether: " + attrGetherEntry.getKey(), e);
            }
        }
    }
    public static void processAttrGether(MoConfig moconfig, ItemLevel itemLevel, Map.Entry<String, JsonElement> attrGetherEntry,int index) throws Exception {
        JsonObject attrGetherObj = attrGetherEntry.getValue().getAsJsonObject();
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attrGetherEntry.getKey()));
        double attrValue = attrGetherObj.has("value") ? attrGetherObj.get("value").getAsDouble() : 0;


//        if (attrGetherObj.has("autoUUID")){
//            if (attrGetherObj.get("autoUUID").getAsBoolean()) {
//                uuid = ExConfigHandle.autoUUid(ExConfigHandle.autoUUID);
//                ExConfigHandle.autoUUID++;
//            }
//        }

        AttributeModifier.Operation operation = attrGetherObj.has("operation") ? ExConfigHandle.getOperation(attrGetherObj.get("operation").getAsString()) : AttributeModifier.Operation.ADDITION;
        EquipmentSlot slot = getEquipmentSlot(attrGetherObj);
        String modifierName = (attrGetherObj.has("modifierName")) ? attrGetherObj.get("modifierName").getAsString() :itemLevel.id + index;;
        if (attrGetherObj.has("id")){
            attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attrGetherObj.get("id").getAsString()));
        }
        if (attrGetherObj.has("autoName")){
            if (attrGetherObj.has("autoName")) {
                if (attrGetherObj.get("autoName").getAsBoolean()) {
                    modifierName = itemLevel.id + index;

                }
            }
        }
        UUID uuid = (attrGetherObj.has("uuid") && !attrGetherObj.get("uuid").getAsString().isEmpty()) ? UUID.fromString(attrGetherObj.get("uuid").getAsString()) : UUID.nameUUIDFromBytes(modifierName.getBytes());
        if(attrGetherObj.has("autoUUID") && attrGetherObj.get("autoUUID").getAsBoolean()) uuid = UUID.nameUUIDFromBytes(modifierName.getBytes());
        //UUID uuid = ExConfigHandle.generateUUIDFromString(modifierName);
        Exmodifier.LOGGER.debug("uuid "+uuid);

        AttributeModifier modifier = new AttributeModifier(uuid, modifierName, attrValue, operation);
        LevelAttriGether attrGether = new LevelAttriGether(attribute, modifier, slot);
        if (attrGetherObj.has("minValue")){
            attrGether.minValue = attrGetherObj.get("minValue").getAsDouble();
        }
        attrGether.maxValue = attrGetherObj.has("maxValue") ? attrGetherObj.get("maxValue").getAsDouble() : attrGether.minValue;
        attrGether.reserveDouble = attrGetherObj.has("reserveDouble") ? attrGetherObj.get("reserveDouble").getAsInt() : 0;
        Map<Double, Float> simpleWeight = new HashMap<>();
        List<Double> mayValues = new ArrayList<>();
        List<Float>  mayValuesKey = new ArrayList<>();
        if (attrGetherObj.has("mayValues")){
            JsonArray mayValuesArray = attrGetherObj.get("mayValues").getAsJsonArray();
            mayValuesArray.forEach(mayValue -> {
                mayValues.add(mayValue.getAsDouble());
            });
        }
        if (attrGetherObj.has("mayValuesWeight")){
            JsonArray mayValuesKeyArray = attrGetherObj.get("mayValuesWeight").getAsJsonArray();
            mayValuesKeyArray.forEach(mayValueKey -> {
                mayValuesKey.add(mayValueKey.getAsFloat());
            });
        }
        for (int i = 0; i < mayValues.size(); i++) {

            Float value = mayValuesKey.get(i);
            if (value == null) value = 0f;
            simpleWeight.put(mayValues.get(i), value);
        }
        if (!simpleWeight.isEmpty()) attrGether.simpleWeight = simpleWeight;
        attrGether.IsAutoEquipmentSlot = attrGetherObj.has("isAutoEquipmentSlot") && attrGetherObj.get("isAutoEquipmentSlot").getAsBoolean();
        attrGether.hasUUID = attrGetherObj.has("uuid");
        if (!attrGether.IsAutoEquipmentSlot){
            if (attrGetherObj.has("slot")) {
                if (!attrGetherObj.get("slot").getAsString().equals("auto")) {
                    attrGether.slot = EquipmentSlot.valueOf(attrGetherObj.get("slot").getAsString());
                }else {
                    attrGether.IsAutoEquipmentSlot = true;
                }
            }
        }
        if (attrGetherObj.has("weight")) {
            attrGether.weight = attrGetherObj.get("weight").getAsFloat();
        }
        if (attrGetherObj.has("isRandom")) {
            attrGether.isRandom = attrGetherObj.get("isRandom").getAsBoolean();
        }
        if (attrGetherObj.has("Expression")) {
            attrGether.Expression = attrGetherObj.get("Expression").getAsString();
        }

        Exmodifier.LOGGER.debug("Attribute: " + attribute + " key: " + attrGetherEntry.getKey());
        itemLevel.attriGethers.add(attrGether);
        RegistryItemLevel(itemLevel);
    }
    public static void processLevelMoConfigEntries(MoConfig moconfig) throws FileNotFoundException {
        List<ItemLevel> entries = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : moconfig.readEntrys()) {
            try {
                processItemLevel(moconfig, entry, entries);
            } catch (Exception e) {
                Exmodifier.LOGGER.error("Error processing Item Level: " + entry.getKey(), e);
            }
        }

        WeightedUtil<String> weightedUtil = new WeightedUtil<String>(
                entries.stream().collect(Collectors.toMap(ItemLevel::getId, ItemLevel::getWeight))
        );

        entries.forEach(entry -> {
            RegistryItemLevel(entry);
            Exmodifier.LOGGER.debug(entry.id + " 出现概率 " + weightedUtil.getProbability(entry.id) * 100 + "%");
        });
        ExLevelRegistryEvent event = new ExLevelRegistryEvent(entries);
        MinecraftForge.EVENT_BUS.post(event);

        Exmodifier.LOGGER.debug("ReadConfig Over: Type: " + moconfig.type + " Path: " + moconfig.configFile + " entries: " + entries.size());
    }


}