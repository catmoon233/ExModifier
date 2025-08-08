package net.exmo.exmodifier.content.modifier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.exmo.exmodifier.Config;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.client.LanguageLoader;
import net.exmo.exmodifier.content.element.DefaultEntityElement;
import net.exmo.exmodifier.content.element.DefaultItemElement;
import net.exmo.exmodifier.content.element.ExElement;
import net.exmo.exmodifier.content.element.ExElementHandle;
import net.exmo.exmodifier.content.helper.ItemInfo;
import net.exmo.exmodifier.content.helper.ModifierEntryHelper;
import net.exmo.exmodifier.content.quality.ItemQualityHandle;
import net.exmo.exmodifier.content.resources.ZipHandle;
import net.exmo.exmodifier.content.selected.BaseItemSelected;
import net.exmo.exmodifier.content.suit.ExSuit;
import net.exmo.exmodifier.content.suit.ExSuitHandle;
import net.exmo.exmodifier.content.type.ExTypeHandle;
import net.exmo.exmodifier.content.type.ItemType;
import net.exmo.exmodifier.events.*;
import net.exmo.exmodifier.network.sync.defaultEntityElement.ClearDefaultItemElementMessage;
import net.exmo.exmodifier.network.sync.defaultEntityElement.SyncDefaultItemElementMessage;
import net.exmo.exmodifier.network.sync.defaultItemElement.ClearDefaultEntityElementMessage;
import net.exmo.exmodifier.network.sync.defaultItemElement.SyncDefaultEntityElementMessage;
import net.exmo.exmodifier.network.sync.element.ClearElementMessage;
import net.exmo.exmodifier.network.sync.element.SyncElementMessage;
import net.exmo.exmodifier.network.sync.lang.LangMessage;
import net.exmo.exmodifier.network.sync.modifier.ClearModifierEntryMessage;
import net.exmo.exmodifier.network.ExModifiervaV;
import net.exmo.exmodifier.network.sync.modifier.SyncModifierEntryMessage;
import net.exmo.exmodifier.network.sync.suit.ClearExSuitMessage;
import net.exmo.exmodifier.network.sync.suit.SyncExSuitMessage;
import net.exmo.exmodifier.util.*;
import net.exmo.exmodifier.util.gether.AttrGether;
import net.exmo.exmodifier.content.type.ExType;
import net.exmo.exmodifier.util.gether.AttrSimpleGether;
import net.minecraft.ChatFormatting;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;


import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static net.exmo.exmodifier.Exmodifier.*;
import static net.exmo.exmodifier.content.client.LanguageLoader.putLanguage;
import static net.exmo.exmodifier.content.level.ItemLevelHandle.*;

import static net.exmo.exmodifier.util.ExConfigHandle.*;
import static net.minecraft.world.item.ItemStack.ATTRIBUTE_MODIFIER_FORMAT;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModifierHandle {

    //    public static void init() throws IOException {
//        modifierEntryMap = new HashMap<>();
//        readConfig();
//    }
    public static List<String> percentAtr = new ArrayList<>();

//    public static void sendModifierEntryToServer(ModifierEntry modifierEntry) {
//
//        PACKET_HANDLER.sendToServer(new SyncModifierEntryMessage(modifierEntry));
//        // 处理服务器未初始化的情况
//        //LOGGER.Logger.error("Server is not initialized yet.");
//
//    }

    public static ModifierEntry findModifierEntry(String id) {
        return modifierEntryMap.get(id);
    }

    public static void sendModifierEntryToClient(ModifierEntry modifierEntry, ServerPlayer player) {
        PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new SyncModifierEntryMessage(modifierEntry));
    }

    public static void sendElementToClient(ExElement e, ServerPlayer player) {
        if (!Config.ELEMENT_SYSTEM.get())return;
        PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new SyncElementMessage(e));
    }

    public static void sendDefaultEntityElementToClient(DefaultEntityElement e, ServerPlayer player) {
        PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new SyncDefaultEntityElementMessage(e));
    }
    public static void sendLangMessageToClient(LangMessage.LangMessageHandler e, ServerPlayer player) {
        PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new LangMessage(e));
    }

    public static void sendDefaultItemElementToClient(DefaultItemElement e, ServerPlayer player) {
        PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new SyncDefaultItemElementMessage(e));
    }

    public static void sendExSuitToClient(ExSuit e, ServerPlayer player) {
        PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new SyncExSuitMessage(e));
    }


    public static void sendClearDataToClient(ServerPlayer player) {
        PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new ClearModifierEntryMessage());
        PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new ClearElementMessage());
        PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new ClearDefaultEntityElementMessage());
        PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new ClearDefaultItemElementMessage());
        PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new ClearExSuitMessage());

    }

    public static void sendModifierEntryToAllClient(ModifierEntry modifierEntry) {
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer == null) {
            //LOGGER.Logger.error("Server is not initialized yet.");
            return;
        }
        for (ServerPlayer player : currentServer.getPlayerList().getPlayers()) {
            PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new SyncModifierEntryMessage(modifierEntry));
        }
    }

    public static void sendClearDataToAllClient() {
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer == null) {
            //LOGGER.Logger.error("Server is not initialized yet.");
            return;
        }
        for (ServerPlayer player : currentServer.getPlayerList().getPlayers()) {
            sendClearDataToClient(player);
        }
    }

    static {
        ExModifierPercentAttr event = new ExModifierPercentAttr(List.of(
                "twtp:mianshan",
                "isfix:magic_restore",

//                "irons_spellbooks:fire_spell_power",
//                "irons_spellbooks:cast_time_reduction",
//                "irons_spellbooks:cooldown_reduction",
//                "irons_spellbooks:ice_spell_power",
//                "irons_spellbooks:lightning_spell_power",
//                "irons_spellbooks:holy_spell_power",
//                "irons_spellbooks:evocation_spell_power",
//                "irons_spellbooks:poison_spell_power",
                "twtp:alldamage",
                "twtp:axedamage"
//                "irons_spellbooks:spell_power",
//                "irons_spellbooks:blood_spell_power",
//                "irons_spellbooks:ender_spell_power"


        ));
        MinecraftForge.EVENT_BUS.post(event);
        percentAtr = event.attrs;
    }

    private static DecimalFormat df = new DecimalFormat("#.#####");

    public static List<ItemSelector> getItemSelector(ItemStack stack) {
        return itemsDefaultEntry.keySet().stream().filter(entry -> entry.compare(stack)).toList();
    }

    @Mod.EventBusSubscriber
    public static class CommonEvent {

        public static List<Component> GenSuitInfo(Player player, ModifierEntry modifierEntry) {
            if (player == null) return null;
            List<Component> tooltips = new ArrayList<>();
            var list = ExSuitHandle.LoadExSuit.entrySet().stream().filter(e -> modifierEntry.exsuits.contains(e.getKey())).map(Map.Entry::getValue).toList();
            if (!list.isEmpty()) {
                ExModifiervaV.PlayerVariables pv = player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).orElse(new ExModifiervaV.PlayerVariables());
                if (!Config.compact_tooltip) tooltips.add(Component.translatable("modifier.entry.suit"));

                for (ExSuit suit : list){
                    if (suit.visible) {
                        Integer integer = pv.SuitsNum.getOrDefault(suit.id,0);
                        tooltips.add(Component.translatable("modifier.entry.suit." + suit.id).append(Component.literal("§6(" + integer + "/" + suit.getMaxLevel() + ")")));
                        if (!suit.LocalDescription.isEmpty())
                            tooltips.addAll(TooltipUtil.sprit(Component.translatable(suit.LocalDescription)));

                        //.append(Component.translatable("modifier.entry.suit.color"))
                    }
                }


            }
            return tooltips;
        }

        public static List<Component> generateEntryTooltip(ModifierInstant modifierEntryInstant, Player player, ItemStack itemStack, boolean skinFold) {
            List<Component> tooltips = new ArrayList<>();
            ModifierEntry modifierEntry = modifierEntryInstant.getModifierEntry();
            int level = modifierEntryInstant.getLevel();
            String id = modifierEntry.getId();
            if (player == null) return tooltips;
            boolean foldFlag = !Config.entryFold || Screen.hasShiftDown();
            if (id.length() >= 2) {
                MutableComponent translatable = Component.translatable(modifierEntry.getDescriptionId());
                if (Config.entryUnderLine && foldFlag) {
                    translatable = Component.literal("§n").append(translatable);
                    //translatable.withStyle(ChatFormatting.UNDERLINE);
                }
                if (Config.compact_tooltip) {
                    if (level > 1)
                        translatable.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + level)).withStyle(ChatFormatting.GOLD);
                    tooltips.add(translatable);

                } else {
                    if (level > 1)
                        translatable.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + level)).withStyle(ChatFormatting.GOLD);
                    tooltips.add(translatable.append("§r:"));
                }
                if (!modifierEntry.localDescription.isEmpty())
                //        if (Screen.hasShiftDown())
                {
                    tooltips.addAll(TooltipUtil.sprit(Component.translatable(modifierEntry.localDescription)));

                }

                if (foldFlag || skinFold) {
                    tooltips.addAll(GenSuitInfo(player, modifierEntry));
                    for (ModifierAttriGether modifierAttriGether : modifierEntry.attriGether) {
                        ExAttributeModifier attributemodifier = modifierAttriGether.getModifier();
                        Attribute attribute = modifierAttriGether.getAttribute();
                        if (attribute == null) continue;
                        if (attributemodifier == null) continue;
                        //    if (modifierAttriGether.slot==null)continue;
//                    EquipmentSlot slot = modifierAttriGether.slot;
//                    if (modifierAttriGether.IsAutoEquipmentSlot){
//                        slot = ModifierEntry.TypeToEquipmentSlot(ModifierEntry.getType(itemStack));
//                    }
                        boolean isCurios = CuriosUtil.isCuriosItem2(itemStack);
                        List<AttrGether> attributeModifiersAffix = CuriosUtil.getAttributeModifiersAffix(itemStack);
                        boolean b = isCurios && attributeModifiersAffix.stream().noneMatch(attriGether -> attriGether.attributeModifier.getName().equals(attributemodifier.getName()));
                        if (
                                b &&
                                        !ItemAttrUtil.hasAttributeModifierCompoundTagNoAmount(itemStack, attribute, attributemodifier, modifierAttriGether.slot))
                            continue;
                        //  Exmodifier.LOGGER.info(modifierAttriGether.getAttribute().getDescriptionId());
                        //   if (!itemStack.getAttributeModifiers(modifierAttriGether.slot).containsEntry(attribute, attributemodifier))continue;
//                    attributemodifier = ItemAttrUtil.getAttributeModifierFromNamed(attributemodifier.getName(),itemStack);
//                    if (attributemodifier==null)continue;
                        double d0 = isCurios ? attributeModifiersAffix.stream().filter(attrGether -> attrGether.attributeModifier.getName().equals(attributemodifier.getName())).findFirst().get().attributeModifier.getAmount() : ItemAttrUtil.getAmountFromAttributeName(itemStack, attribute, attributemodifier.getName());
                        if (modifierAttriGether.Expression != null && !modifierAttriGether.Expression.isEmpty()) {
                            DynamicExpressionEvaluator evaluator = new DynamicExpressionEvaluator();
                            evaluator.setVariable("level", level);
                            d0 = evaluator.evaluate(modifierAttriGether.Expression);
                        }
                        boolean flag = false;
                        String percent = "";
                        double d1;
                        String attributeID = ExUtil.getAttributeID(attribute);
                        if (attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL && !percentAtr.contains(attributeID)) {
                            if ((attribute).equals(Attributes.KNOCKBACK_RESISTANCE)) {
                                d1 = d0 * 10.0;
                            } else {
                                d1 = d0;
                            }
                        } else {
                            d1 = d0 * 100.0;
                        }
                        String amouta2 = "";
                        if (percentAtr.contains(attributeID)) {
                            percent = "%";
                            DecimalFormat df = new DecimalFormat("#.####");
                            amouta2 = df.format(attributemodifier.getAmount() * 100);
                            if (modifierAttriGether.attribute.getDescriptionId().length() >= 4) {
                                if (attributeID.startsWith("twtp") || ExUtil.getAttributeID(attribute).toString().startsWith("isfix")) {
                                    amouta2 = df.format(attributemodifier.getAmount());
                                }
                            }
                        }

                        if (flag) {
                            tooltips.add((Component.literal(" ")).append(Component.translatable("attribute.modifier.equals." + attributemodifier.getOperation().toValue(), new Object[]{ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(attribute.getDescriptionId())})).withStyle(ChatFormatting.DARK_GREEN));
                        } else if (d0 > 0.0) {
                            if (percent.equals("%"))
                                tooltips.add(Component.translatable("add").append(amouta2).append(percent).append(" ").append(Component.translatable(attribute.getDescriptionId())).withStyle(ChatFormatting.BLUE));
                            else {
                                var component = Component.translatable(
                                        "attribute.modifier.plus." + attributemodifier.getOperation().toValue(),
                                        new Object[]{ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(attribute.getDescriptionId())}
                                );

                                if (Config.entryColor) {
                                    MutableComponent translatable1 = Component.translatable(modifierEntry.getDescriptionId());
                                    String string = translatable1.getString();

                                    // 新增颜色代码解析逻辑
                                    ChatFormatting byCode = getChatFormattingFromString(string);
                                    if (byCode != null && byCode.isColor()) {
                                        component.withStyle(byCode);
                                    }
                                    if (byCode == null) {
                                        component.withStyle(ChatFormatting.WHITE);
                                    }
                                }
                                tooltips.add(component);
                            }
                        } else if (d0 < 0.0) {
                            d1 *= -1.0;
                            if (percent.equals("%"))
                                tooltips.add(Component.translatable("subtract").append(amouta2).append(percent).append(" ").append(Component.translatable(attribute.getDescriptionId())).withStyle(ChatFormatting.RED));
                            else {
                                MutableComponent component = (Component.translatable("attribute.modifier.take." + attributemodifier.getOperation().toValue(), new Object[]{ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(attribute.getDescriptionId())})).withStyle(ChatFormatting.RED);
                                if (Config.entryColor) {
                                    MutableComponent translatable1 = Component.translatable(modifierEntry.getDescriptionId());
                                    String string = translatable1.getString();

                                    // 新增颜色代码解析逻辑
                                    ChatFormatting byCode = getChatFormattingFromString(string);
                                    if (byCode != null && byCode.isColor()) {
                                        component.withStyle(byCode);
                                    }
                                    if (byCode == null) {
                                        component.withStyle(ChatFormatting.WHITE);
                                    }
                                }
                                tooltips.add(component);
                            }
                        }
                    }
                }
            }

            ExEntryTooltipEvent event = new ExEntryTooltipEvent(modifierEntry, player, itemStack, tooltips);
            MinecraftForge.EVENT_BUS.post(event);
            return event.getTooltip();
        }
//        @SubscribeEvent
//        public static void ItemTooltip(ItemTooltipEvent eventC) {
//
//

        /// /            for (Component tooltip : eventC.getToolTip()){
        /// /                Exmodifier.LOGGER.debug("tooltip: " + tooltip);
        /// /            }
        /// /            Exmodifier.LOGGER.debug("------------------------------------");
//
//        }
        public static void RandomEntryCurios(ItemStack stack, WeightedUtil<String> weightedUtil, List<String> slots, int refreshments) {
            int numAddedModifiers = 0;

            List<ModifierAttriGether> finalAttriGethers = new ArrayList<>();
            // Set<String> appliedModifiers = new HashSet<>();
            List<ModifierEntry> modifierEntries = new ArrayList<>();
            if (weightedUtil.weights.size() < refreshments) refreshments = weightedUtil.weights.size();
            while (numAddedModifiers < refreshments) {
                ModifierEntry modifierEntry = modifierEntryMap.get(weightedUtil.selectRandomKeyBasedOnWeights());
                if (modifierEntries.contains(modifierEntry)) continue;
                if (modifierEntry.id != null) LOGGER.debug("add entry: " + modifierEntry.id);
                modifierEntries.add(modifierEntry);
                if (modifierEntry == null) {
                    LOGGER.debug("exElement is null");
                    return;
                }
                weightedUtil.removeKey(modifierEntry.id);
                //        if (!appliedModifiers.contains(exElement.Id)) {
                LOGGER.debug("add entry start: " + modifierEntry.id);
                // appliedModifiers.add(exElement.Id);

//                    stack.getOrCreateTag().putString("exmodifier_armor_modifier_applied" + numAddedModifiers, exElement.Id);
                numAddedModifiers++;
                LOGGER.debug("add entry ing: " + modifierEntry.id);
                ModifierEntryHelper.of(stack).addModifierEntry(ModifierInstant.of(modifierEntry), true, true);

//                    List<ModifierAttriGether> attriGethers = selectModifierAttributes(exElement,stack);
//                    ExAddEntryAttrigethersEvent event = new ExAddEntryAttrigethersEvent(stack, weightedUtil, true, refreshments, attriGethers,exElement,modifierEntries);
//                    MinecraftForge.EVENT_BUS.post(event);
//
//                    LOGGER.debug("add entry end: " + exElement.Id +" "+attriGethers);
//                    finalAttriGethers.addAll(event.getAttriGether());
//                    if (exElement.OnlyHasThisEntry){
//                        finalAttriGethers = new ArrayList<>(event.getAttriGether());
//                        for (int i = numAddedModifiers ; i < refreshments; i++){
//                            stack.getOrCreateTag().putString("exmodifier_armor_modifier_applied" + i, "");
//                        }
//                        stack.getOrCreateTag().putString("exmodifier_armor_modifier_applied", exElement.Id);
//                        break;
//                    }
//             //   }
            }

            //    applyModifiersCurios(stack, finalAttriGethers, slots);
        }

        public static void RandomEntry(ItemStack stack, WeightedUtil<String> weightedUtil, EquipmentSlot[] slot, int refreshments) {
            int numAddedModifiers = 0;


            // Set<String> appliedModifiers = new HashSet<>();
            List<ModifierEntry> modifierEntries = new ArrayList<>();
            if (weightedUtil.weights.size() < refreshments) refreshments = weightedUtil.weights.size();

            while (numAddedModifiers < refreshments) {
                ModifierEntry modifierEntry = modifierEntryMap.get(weightedUtil.selectRandomKeyBasedOnWeights());
                if (modifierEntries.contains(modifierEntry)) continue;
                if (modifierEntry == null) {
                    LOGGER.debug("exElement is null");
                    continue;
                }
                LOGGER.debug("add entry: " + modifierEntry.id);

                modifierEntries.add(modifierEntry);
                List<ModifierAttriGether> finalAttriGethers = new ArrayList<>();

                // if (!appliedModifiers.contains(exElement.Id)) {
                LOGGER.debug("add entry start: " + modifierEntry.id);
                //  appliedModifiers.add(exElement.Id);
                weightedUtil.removeKey(modifierEntry.id);
                ItemInfo itemInfo = new ItemInfo(stack);

                int level = 1;
                if (modifierEntry.randomLevel!=null){
                    level = random.nextInt(modifierEntry.randomLevel.getFirst(), modifierEntry.randomLevel.getSecond());
                }
                ModifierInstant modifierInstant = new ModifierInstant(modifierEntry, level);
                itemInfo.getModifierEntryHelper().addModifierEntry(modifierInstant, false, true);
                //   stack.getOrCreateTag().putString("exmodifier_armor_modifier_applied" + numAddedModifiers, exElement.Id);
                numAddedModifiers++;
                LOGGER.debug("add entry ing: " + modifierEntry.id);

                List<ModifierAttriGether> attriGethers = selectModifierAttributes(modifierEntry);
                ExAddEntryAttrigethersEvent event = new ExAddEntryAttrigethersEvent(stack, weightedUtil, slot, refreshments, attriGethers, modifierEntry, modifierEntries);
                MinecraftForge.EVENT_BUS.post(event);
                finalAttriGethers.addAll(event.attriGether);
                if (modifierEntry.OnlyHasThisEntry) {
                    finalAttriGethers = new ArrayList<>(event.attriGether);
                    for (int i = numAddedModifiers; i < refreshments; i++) {
                        stack.getOrCreateTag().putString("exmodifier_armor_modifier_applied" + i, "");
                    }
                    stack.getOrCreateTag().putString("exmodifier_armor_modifier_applied", modifierEntry.id);
                    applyModifiers(stack, finalAttriGethers, slot, modifierInstant);
                    break;
                }
                //   }
                applyModifiers(stack, finalAttriGethers, slot, modifierInstant);
            }


        }

        public static void RandomEntry(ItemStack stack, int rarity, int refreshnumber, String washItem, int keepEntries) {
            CompoundTag tag = stack.getTag();
            if (tag == null || ModifierEntryHelper.of(stack).getModifierEntriesSize() > 0 && keepEntries == 0) return;

            tag.remove("wash_item");
            tag.remove("modifier_refresh_rarity");
            tag.remove("modifier_refresh_add");
            tag.remove("modifier_refresh");
            tag.remove("keepEntries");


            Map<ItemType, EquipmentSlot[]> typeEquipmentSlotMap = typeSlotMap();
            WeightedUtil<String> weightedUtil = new WeightedUtil<>(new HashMap<>());
            Set<EquipmentSlot> slotSet = new HashSet<>(); // 使用Set来自动去重
            var types = new ArrayList<>();

            for (Map.Entry<ItemType, EquipmentSlot[]> entry : typeEquipmentSlotMap.entrySet()) {
                ItemType type = entry.getKey();

                if (!isValidForType(stack, type)) {
                    continue;
                }
                types.add(type);
                slotSet.addAll(Arrays.asList(entry.getValue())); // 添加到Set中


                weightedUtil.merge(new WeightedUtil<>(
                        modifierEntryMap.entrySet().stream()
                                .filter(e -> {
                                    if (e.getValue().weight == 0) return false;
                                    var modifier = e.getValue();
                                    AtomicBoolean only = new AtomicBoolean(false);
                                    boolean hasWashItem = materialsList.stream()
                                            .anyMatch(m -> {
                                                if ( m.ItemId.equals(washItem)){
                                                    if (m.OnlyHasWashEntry) only.set(true);
                                                    return true;
                                                }
                                                return false;
                                            });
                                    boolean washPd = false;
                                    if (modifier.modifierItemSelector.getOnlyWashItems().isEmpty() ){
                                        if (!only.get()) {
                                            washPd = true;
                                        }
                                    }else {
                                        if (modifier.modifierItemSelector.getOnlyWashItems().contains(washItem)){
                                            washPd = true;
                                        }
                                    }


                                    return !ModifierEntryHelper.of(stack).getModifierEntriesB().contains(modifier) &&
                                            modifier.containItemType(type) &&
                                            modifier.hasDefaultTag() &&
                                            modifier.modifierItemSelector.containItem(stack) &&
                                            !modifier.cantSelect &&
                                            (modifier.Slots.isEmpty()) &&
                                            (modifier.needFreshValue == 0 || modifier.needFreshValue <= rarity) &&washPd;
                                            //(modifier.modifierItemSelector.getOnlyWashItems().isEmpty() || modifier.modifierItemSelector.getOnlyWashItems().contains(washItem) || hasWashItem);
                                })
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        e -> e.getValue().weight
                                ))
                ));
            }

            if (!weightedUtil.weights.isEmpty()) {
                weightedUtil.increaseWeightsByRarity(rarity);
                if (types.contains(ExType.ARMOR.get())) {  // ARMOR type, set the slot based on the item
                    Item item = stack.getItem();
                    if (item instanceof ArmorItem)
                        slotSet = new HashSet<>(List.of(((ArmorItem) item).getEquipmentSlot())); // 重新赋值为新的Set
                    else stack.getEquipmentSlot();
                }
                try {
                    RandomEntry(stack, weightedUtil, slotSet.toArray(new EquipmentSlot[0]), refreshnumber); // 将Set转换为数组
                } catch (Exception e) {
                    // 处理异常
                }
            }
        }


        public static Map<ItemType, EquipmentSlot[]> typeSlotMap() {
            var a = new HashMap<ItemType, EquipmentSlot[]>(Map.of(
                    ExType.HELMET.get(), new EquipmentSlot[]{EquipmentSlot.HEAD},
                    ExType.CHESTPLATE.get(), new EquipmentSlot[]{EquipmentSlot.CHEST},
                    ExType.BOOTS.get(), new EquipmentSlot[]{EquipmentSlot.FEET},
                    ExType.LEGGINGS.get(), new EquipmentSlot[]{EquipmentSlot.LEGS},
                    ExType.ARMOR.get(), new EquipmentSlot[]{EquipmentSlot.CHEST}, // For ARMOR type, dynamically set the slot based on the item
                    ExType.SHIELD.get(), new EquipmentSlot[]{EquipmentSlot.OFFHAND},
                    ExType.BOW.get(), new EquipmentSlot[]{EquipmentSlot.MAINHAND},
                    ExType.SWORD.get(), new EquipmentSlot[]{EquipmentSlot.MAINHAND},
                    ExType.ATTACKABLE.get(), new EquipmentSlot[]{EquipmentSlot.MAINHAND},
                    ExType.AXE.get(), new EquipmentSlot[]{EquipmentSlot.MAINHAND}
            ));
            a.put(ExType.CROSSBOW.get(), new EquipmentSlot[]{EquipmentSlot.MAINHAND});
            a.put(ExType.PICKAXE.get(), new EquipmentSlot[]{EquipmentSlot.MAINHAND});
            a.put(ExType.UNKNOWN.get(), new EquipmentSlot[]{EquipmentSlot.MAINHAND});

            for (var itemType : ExTypeHandle.itemTypes.values()) {
                a.put(itemType, itemType.getEquipmentSlot()); // 包装为数组
            }
            return a;
        }


//            public static List<ModifierAttriGether> selectModifierAttributes(ModifierEntry exElement, ItemStack stack) {
//                List<ModifierAttriGether> attriGethers = new ArrayList<>();
//
//                if (exElement.RandomNum > 0) {
//                    int remainingRandoms = exElement.RandomNum;
//                    Map<String, ModifierAttriGether> toRandom = new HashMap<>();
//                    Map<String, Float> weightMap = new HashMap<>();
//
//                    // Populate toRandom and weightMap in a single loop
//                    for (int i = 0; i < exElement.attriGether.size(); i++) {
//                        ModifierAttriGether attriGether = exElement.attriGether.get(i);
//                        if (attriGether.weight > 0) {
//                            String key = String.valueOf(i);
//                            toRandom.put(key, attriGether);
//                            weightMap.put(key, attriGether.weight);
//                        } else if (attriGether.weight == 0 && !attriGether.isRandom) {
//                            attriGethers.add(attriGether);
//                        }
//                    }
//
//                    remainingRandoms -= attriGethers.size();
//
//                    WeightedUtil<String> weightedUtil = new WeightedUtil<>(weightMap);
//
//                    while (remainingRandoms > 0) {
//                        String selectedKey = weightedUtil.selectRandomKeyBasedOnWeights();
//                        ModifierAttriGether selectedAttriGether = toRandom.get(selectedKey);
//
//                        if (selectedAttriGether != null && !attriGethers.contains(selectedAttriGether)) {
//                            ExAddEntryAttrigetherEvent event = new ExAddEntryAttrigetherEvent(exElement, selectedAttriGether, stack);
//                            MinecraftForge.EVENT_BUS.post(event);
//                            attriGethers.add(event.selectedAttriGether);
//                            Exmodifier.LOGGER.debug ("add Random entry: " + event.selectedAttriGether.getAttribute().getDescriptionId());
//                            remainingRandoms--;
//                        }
//                    }
//
//                } else {
//                    attriGethers.addAll(exElement.attriGether.stream()
//                            .filter(attriGether -> !attriGether.isRandom)
//                            .toList());
//                }
//
//                return attriGethers;
//            }
//
//


        public static List<ModifierAttriGether> selectModifierAttributes(ModifierEntry modifierEntry) {
            List<ModifierAttriGether> attriGethers = new ArrayList<>();

            if (modifierEntry.RandomNum > 0) {
                int remainingRandoms = modifierEntry.RandomNum;
                Map<String, ModifierAttriGether> toRandom = new HashMap<>();
                for (int i = 0; i < modifierEntry.attriGether.size(); i++) {
                    ModifierAttriGether attriGether = modifierEntry.attriGether.get(i);
                    if (attriGether.weight > 0) {
                        toRandom.put(String.valueOf(i), attriGether);
                    }
                }
                List<ModifierAttriGether> firstAdd = new ArrayList<>();
                for (int i = 0; i < modifierEntry.attriGether.size(); i++) {
                    ModifierAttriGether attriGether = modifierEntry.attriGether.get(i);
                    if (attriGether.weight == 0) {
                        firstAdd.add(attriGether);
                    }
                }
                attriGethers.addAll(firstAdd);
                remainingRandoms -= firstAdd.size();
                Map<String, Float> wemap = new HashMap<>();
                toRandom.forEach((key, value) -> wemap.put(key, value.weight));
                WeightedUtil<String> weightedUtil = new WeightedUtil<String>(wemap);
                while (remainingRandoms > 0) {
                    String selectedKey = (String) weightedUtil.selectRandomKeyBasedOnWeights();
                    ModifierAttriGether selectedAttriGether = toRandom.get(selectedKey);
                    if (!attriGethers.contains(selectedAttriGether)) {
                        if (selectedAttriGether != null) {
                            if (selectedAttriGether.getAttribute() != null) {
                                ExAddEntryAttrigetherEvent event = new ExAddEntryAttrigetherEvent(modifierEntry, selectedAttriGether);
                                MinecraftForge.EVENT_BUS.post(event);
                                attriGethers.add(event.selectedAttriGether);
                                LOGGER.debug("add Random entry: " + event.selectedAttriGether.getAttribute().getDescriptionId());
                            }
                        }

                        remainingRandoms--;
                    }

                }

            } else {
                attriGethers.addAll(modifierEntry.attriGether.stream()
                        .filter(attriGether -> !attriGether.isRandom)
                        .toList());
            }

            return attriGethers;
        }

        //        public static void AddEntryToItem (ItemStack itemStack,String e){
//
//            List<ModifierEntry> modifierEntries = ModifierHandle.getEntrysFromItemStack(itemStack);
//
//            ModifierHandle.CommonEvent.clearEntry(itemStack);
//
//            ModifierEntry modifier = ModifierHandle.modifierEntryMap.get(e);
//            if (!modifierEntries.contains(modifier)) modifierEntries.add(modifier);
//            boolean over = false;
//            Map<String, Float> weightedUtilmap = new HashMap<>();
//            for (ModifierEntry exElement : modifierEntries.stream().filter(Objects::nonNull).toList()) {
//                Exmodifier.LOGGER.info(exElement.getId());
//                weightedUtilmap.put(exElement.getId(), 1.0f);
//            }
//
//            Map<ExType, EquipmentSlot> typeSlotMap =new  HashMap<>( Map.of(
//                    ExType.HELMET, EquipmentSlot.HEAD,
//                    ExType.CHESTPLATE, EquipmentSlot.CHEST,
//                    ExType.BOOTS, EquipmentSlot.FEET,
//                    ExType.LEGGINGS, EquipmentSlot.LEGS,
//                    ExType.ARMOR, EquipmentSlot.CHEST,  // For ARMOR type, we'll dynamically set the slot based on the item
//                    ExType.SHIELD, EquipmentSlot.OFFHAND,
//                    ExType.BOW, EquipmentSlot.MAINHAND,
//                    ExType.SWORD, EquipmentSlot.MAINHAND,
//                    ExType.ATTACKABLE, EquipmentSlot.MAINHAND,
//                    ExType.AXE, EquipmentSlot.MAINHAND
//            ));
//
//            for (Map.Entry<ExType, EquipmentSlot> entry : typeSlotMap.entrySet()) {
//                ExType type = entry.getKey();
//                EquipmentSlot slot = entry.getValue();
//                if (!over && isValidForType(itemStack, type)) {
//
//
//                    if (entry.getKey() == ExType.ARMOR) {  // ARMOR type, set the slot based on the item
//                        slot = ((ArmorItem) itemStack.getItem()).getEquipmentSlot();
//                    }
//                    LOGGER.debug("RandomEntry: " + type + " " + slot);
//                    RandomEntry(itemStack, new WeightedUtil<String>(weightedUtilmap), slot, weightedUtilmap.size());
//                    itemStack.getOrCreateTag().putInt("exmodifier_armor_modifier_applied",
//                            itemStack.getOrCreateTag().getInt("exmodifier_armor_modifier_applied") + 1);
//                    over = true;
//                }
//            }
//        }
        public static void applyModifiersCurios(ItemStack stack, List<ModifierAttriGether> attriGethers, List<String> CuriosSlots, ModifierInstant oldHelper) {
//            Map<String, Multimap<Attribute, AttributeModifier>> attriMap = new HashMap<>();
//            for (String CuriosSlot : CuriosSlots) {
//                attriMap.put(CuriosSlot, CuriosUtil.getAttributeModifiersAffix(stack, CuriosSlot));
//            }
//            List<CuriosUtil.slotInfo> attriList = CuriosUtil.getCurioAttributeModifiers(stack);
            for (ModifierAttriGether attriGether : attriGethers) {
                attriGether.modifier = new ExAttributeModifier(attriGether.modifier.getName(), attriGether.modifier.getAmount(), attriGether.modifier.getOperation());

                if (ForgeRegistries.ATTRIBUTES.containsValue(attriGether.attribute)) {
                    ExApplyEntryAttrigetherEvent event = new ExApplyEntryAttrigetherEvent(stack, new ModifierAttriGether(attriGether.attribute, attriGether.modifier), true, null).setOldInstant(oldHelper);
                    MinecraftForge.EVENT_BUS.post(event);
                    CuriosUtil.addAttributeModifierAffix(stack, new AttrSimpleGether(event.attriGether.attribute, event.attriGether.getModifier()));

//                    for (String CuriosSlot : CuriosSlots) CuriosUtil.addAttributeModifierApi(stack,attriGether,CuriosSlot);
                    //   ItemAttrUtil.addItemAttributeModifier(stack, attriGether.attribute, attriGether.modifier, applicableSlot);
                } else {
                    LOGGER.debug("attribute is not exists");
                }
            }
//            attriMap.forEach((key, value)->{
//                value.forEach((attribute, modifier) -> {
//                    ExApplyEntryAttrigetherEvent event = new ExApplyEntryAttrigetherEvent(stack, new ModifierAttriGether(attribute,modifier), true, key);
//                    MinecraftForge.EVENT_BUS.post(event);
//
//                    //CuriosUtil.addAttributeModifierApi(event.stack,event.attriGether,event.curiosSlot);
//                });
//            });
//            for (CuriosUtil.slotInfo slotInfo : attriList){
//                CuriosApi.getCuriosHelper().addSlotModifier(stack,slotInfo.identifie,slotInfo.name,slotInfo.uuid,slotInfo.amount,slotInfo.operation,slotInfo.slot);
//            }
        }

        public static void applyModifiers(ItemStack stack, List<ModifierAttriGether> attriGethers, EquipmentSlot[] slot, ModifierInstant modifierInstant, ModifierInstant oldHelper) {
            for (ModifierAttriGether attriGether : attriGethers) {
                EquipmentSlot[] applicableSlot = attriGether.IsAutoEquipmentSlot ? slot : new EquipmentSlot[]{attriGether.slot};

                if (ForgeRegistries.ATTRIBUTES.containsValue(attriGether.attribute)) {
                    attriGether.modifier = ExAttributeModifier.fromModifier(new AttributeModifier(UUID.nameUUIDFromBytes((attriGether.modifier.getName() + stack.getItem().getDescriptionId()).getBytes()), attriGether.modifier.getName(), attriGether.modifier.getAmount(), attriGether.modifier.getOperation()));
                    ExApplyEntryAttrigetherEvent event = new ExApplyEntryAttrigetherEvent(stack, attriGether, applicableSlot, modifierInstant).setOldInstant(oldHelper);
                    MinecraftForge.EVENT_BUS.post(event);
                    ItemAttrUtil.addItemAttributeModifier(event.stack, event.attriGether.attribute, event.attriGether.modifier, event.slot);
                } else {
                    LOGGER.debug("attribute is not exists");
                }
            }
        }

        public static void applyModifiers(ItemStack stack, List<ModifierAttriGether> attriGethers, EquipmentSlot[] slot, ModifierInstant modifierInstant) {
            applyModifiers(stack, attriGethers, slot, modifierInstant, null);
        }

        public static void RandomEntryCurios(ItemStack stack, int rarity, int refreshnumber, String washItem) {
            if (stack.getTag() != null && ModifierEntryHelper.of(stack).getModifierEntriesSize() > 0) return;
            if (stack.getTag() != null) {
                stack.getTag().remove("wash_item");
                stack.getTag().remove("modifier_refresh_rarity");
                stack.getTag().remove("modifier_refresh_add");
                stack.getTag().remove("modifier_refresh");

            }
            List<String> curiosType = CuriosUtil.getSlotsFromItemstack(stack);
            WeightedUtil<String> weightedUtil = new WeightedUtil<>(
                    modifierEntryMap.entrySet().stream()
                            .filter(e -> {
                                var modifier = e.getValue();
                                AtomicBoolean only = new AtomicBoolean(false);
                                boolean hasWashItem = materialsList.stream()
                                        .anyMatch(m -> {
                                           if ( m.ItemId.equals(washItem)){
                                                 if (m.OnlyHasWashEntry) only.set(true);
                                               return true;
                                           }
                                           return false;
                                        });
                               boolean washPd = false;
                                if (modifier.modifierItemSelector.getOnlyWashItems().isEmpty() ){
                                    if (!only.get()) {
                                        washPd = true;
                                    }
                                    }else {
                                    if (modifier.modifierItemSelector.getOnlyWashItems().contains(washItem)){
                                        washPd = true;
                                    }
                                }


                                return modifier.types.contains(ExType.CURIOS.get()) &&
                                        (curiosType.contains(modifier.curiosType) || "ALL".equals(modifier.curiosType)) &&
                                        modifier.modifierItemSelector.containItem(stack) &&
                                        modifier.hasDefaultTag() &&
                                        !modifier.cantSelect &&
                                        (modifier.Slots.isEmpty()) &&
                                        (modifier.needFreshValue == 0 || modifier.needFreshValue <= rarity) &&
                                        washPd;
                                      //  (modifier.modifierItemSelector.getOnlyWashItems().isEmpty() || modifier.modifierItemSelector.getOnlyWashItems().contains(washItem));
                            })
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    e -> e.getValue().weight + 1
                            ))
            );

            RandomEntryCurios(stack, weightedUtil, curiosType, refreshnumber);
//            stack.getOrCreateTag().putInt("exmodifier_armor_modifier_applied",
//                    stack.getOrCreateTag().getInt("exmodifier_armor_modifier_applied") + 1);

        }


        public static boolean isValidForType(ItemStack stack, ItemType type) {

            return type.compare(stack);
//            if (type == ExType.HELMET) return hasHelmetConfig && stack.getItem() instanceof ArmorItem && ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.HEAD;
//            if (type == ExType.CHESTPLATE) return hasChestConfig && stack.getItem() instanceof ArmorItem && ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST;
//            if (type == ExType.BOOTS) return hasBootsConfig && stack.getItem() instanceof ArmorItem && ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.FEET;
//            if (type == ExType.LEGGINGS) return hasLeggingsConfig && stack.getItem() instanceof ArmorItem && ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.LEGS;
//            if (type == ExType.ARMOR) return stack.getItem() instanceof ArmorItem;
//            if (type == ExType.BOW) return stack.getItem() instanceof BowItem || stack.getUseAnimation() == UseAnim.BOW;
//            if (type == ExType.CROSSBOW) return stack.getItem() instanceof CrossbowItem;
//
//            if (type == ExType.SHIELD) return stack.getUseAnimation() == UseAnim.BLOCK;
//            if (type == ExType.SWORD) return hasSwordConfig && stack.getItem() instanceof SwordItem;
//            if (type == ExType.ATTACKABLE) return stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE).stream().mapToDouble(AttributeModifier::getAmount).sum() > 0;
//            if (type == ExType.AXE) return stack.getItem() instanceof AxeItem;
//            if (type == ExType.UNKNOWN) return !stack.getTags().filter(e -> RefreshContainTagHandle.refreshContainTag.contains(e.toString())).toList().isEmpty() || RefreshContainItemHandle.refreshContainItem.contains(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString()) ;
//
//            return false;
        }


//        @SubscribeEvent
//        public static void atReload(AddReloadListenerEvent eventC) throws IOException {
//            modifierEntryMap = new HashMap<>();
//            readConfig();
//        }
// 已移至MainEvent
    }

    public static @Nullable ChatFormatting getChatFormattingFromString(String string) {
        char colorCode = '-';
        Set<Character> excludedFormats = new HashSet<>(Arrays.asList('k', 'l', 'm', 'n', 'o', 'r'));

        for (int i = 0; i < string.length() - 1; i++) {
            if (string.charAt(i) == '§') {
                char codeChar = Character.toLowerCase(string.charAt(i + 1));

                // 检查是否是有效颜色且不在排除列表中
                if (!excludedFormats.contains(codeChar)) {
                    boolean isColor = (codeChar >= '0' && codeChar <= '9')
                            || (codeChar >= 'a' && codeChar <= 'f');

                    if (isColor) {
                        colorCode = codeChar;
                        break; // 找到第一个有效颜色后立即停止搜索
                    }
                }
            }
        }

        ChatFormatting byCode = ChatFormatting.getByCode(colorCode);
        return byCode;
    }

    public static boolean hasHelmetConfig = false;
    public static boolean hasChestConfig = false;
    public static boolean hasLeggingsConfig = false;
    public static boolean hasBootsConfig = false;
    public static boolean hasSwordConfig = false;
    public static final Path WashingMaterialsConfigPath = FMLPaths.CONFIGDIR.get().resolve("exmo/WashingMaterials.json");
    public static final Path ItemsDefaultEntryConfigPath = FMLPaths.CONFIGDIR.get().resolve("exmo/ItemsDefaultEntry.json");
    // public static final Path ConfigPath = FMLPaths.MODSDIR.get().resolve("data/exmodifier/modifier");
    public static Path ConfigPath = FMLPaths.CONFIGDIR.get().resolve("exmo/modifier");
    public static List<MoConfig> Foundmoconfigs = new ArrayList<>();
    public static List<WashingMaterials> materialsList = new ArrayList<>();
    public static Map<String, ModifierEntry> modifierEntryMap = new HashMap<>();
    public static Map<String, List<String>> EEMatchQueue = new HashMap<>();
    public static List<String> cantWashItemIds = new ArrayList<>();
    public static Map<ItemSelector, List<ModifierInstant>> itemsDefaultEntry = new HashMap<>();
    public static List<String> onlyCanRefreshPointEntryItemIds = new ArrayList<>();

    public static void registerWashingMaterials(WashingMaterials washingMaterials) {
        if (materialsList.stream().anyMatch(e ->e.ItemId.contains(washingMaterials.ItemId))){
            LOGGER.info("WashingMaterials:" + washingMaterials.ItemId + "already exits");
        }else{
            materialsList.add(washingMaterials);
            LOGGER.info("RegisterWashingMaterials:" + washingMaterials.ItemId);
        }
    }
    public static void RegisterModifierEntry(ModifierEntry modifierEntry) {
        var type = modifierEntry.types;
        if (!hasBootsConfig) if (type.contains(ExType.BOOTS.get())) hasBootsConfig = true;
        if (!hasLeggingsConfig) if (type.contains(ExType.LEGGINGS.get())) hasLeggingsConfig = true;
        if (!hasChestConfig) if (type.contains(ExType.CHESTPLATE.get())) hasChestConfig = true;
        if (!hasHelmetConfig) if (type.contains(ExType.HELMET.get())) hasHelmetConfig = true;
        if (!hasSwordConfig) if (type.contains(ExType.SWORD.get())) hasSwordConfig = true;
        String id = modifierEntry.id;
        modifierEntryMap.put(id, modifierEntry);
        BaseItemSelected.IDS.put(StringToIntConverter.stringToInt(id), modifierEntry);
        LOGGER.debug("RegisterModifierEntry: Type:" + type + " Target:" + id);
    }

    public static void EEMatchQueueHandle() {
        EEMatchQueue.forEach((k, v) -> {
                ModifierEntry modifierEntry1 = modifierEntryMap.get(k);
                modifierEntry1.exsuits.addAll(v);
        });
        EEMatchQueue = new HashMap<>();
    }

    public static void readConfig() {


        long startTime = System.nanoTime(); // 记录开始时间

        try {
            // 打印所有属性的日志
//            ForgeRegistries.ATTRIBUTES.forEach(attribute ->
//                    LOGGER.debug("Attribute: " + ExUtil.getAttributeID(attribute))
//            );

            // 读取洗涤材料配置
            if (Files.exists(WashingMaterialsConfigPath)) {

                MoConfig washingMaterialsConfig = new MoConfig(WashingMaterialsConfigPath);

                List<WashingMaterials> washingMaterials = new ArrayList<>();
                for (Map.Entry<String, JsonElement> entry : washingMaterialsConfig.readEntrys()) {
                    processWashingMaterialEntry(entry,washingMaterials);

                }
                washingMaterials.forEach(ModifierHandle::registerWashingMaterials);
            }
            // 读取物品默认条目配置
            if (Files.exists(ItemsDefaultEntryConfigPath)) {

                MoConfig washingMaterialsConfig = new MoConfig(ItemsDefaultEntryConfigPath);

                for (Map.Entry<String, JsonElement> entry : washingMaterialsConfig.readEntrys()) {
                    processItemsDefaultEntryEntry(entry);
                }

            }
            MinecraftForge.EVENT_BUS.post(new ExItemDefaultEntry());
            // 读取其余配置文件
            Foundmoconfigs = listFiles(ConfigPath);
            for (MoConfig moconfig : Foundmoconfigs) {
                processEntryMoConfigEntries(moconfig);
            }

            // 读取升级配置
            Foundlvconfigs = listFiles(LEVEL_CONFIG_PATH);
            for (MoConfig moconfig : Foundlvconfigs) {
                processLevelMoConfigEntries(moconfig);
            }
            long endTime = System.nanoTime(); // 记录结束时间
            long duration = endTime - startTime; // 计算持续时间
            LOGGER.debug("ReadConfig Over Modifier time: " + duration / 1000000 + " ms");
            LOGGER.debug("ReadConfig Over Modifier config count: " + Foundmoconfigs.size());
            LOGGER.debug("ReadConfig Over Modifier modifier count: " + modifierEntryMap.size());
        } catch (IOException e) {
            LOGGER.error("Error reading configuration files", e);
        } catch (Exception e) {
            LOGGER.error("Unexpected error during configuration reading", e);
        }
    }

    public static void readConfigFromZipFile(ZipFile zipFile, ZipHandle.ZipFunction zipFunction) {
        long startTime = System.nanoTime(); // 记录开始时间
        try {
            // 读取自定义类型配置
            ExTypeHandle.FoundTypeConfigs = listFilesFromZipFile(zipFile, ExTypeHandle.ConfigPath.getFileName());
            for (MoConfig moconfig : ExTypeHandle.FoundTypeConfigs) {
                ExTypeHandle.processItemTypes(moconfig);

            }
            // 读取洗涤材料配置
            String WashingConfigFilePath = WashingMaterialsConfigPath.getFileName().toString();
            ZipEntry wash = zipFile.getEntry(WashingConfigFilePath);
            if (wash != null) {
                MoConfig washingMaterialsConfig = new MoConfig(Path.of(zipFile.getName(), WashingConfigFilePath), zipFile.getInputStream(wash));

                List<WashingMaterials> list = new ArrayList<>();
                for (Map.Entry<String, JsonElement> entry : washingMaterialsConfig.readEntrys()) {
                    processWashingMaterialEntry(entry,list);
                }
                list.forEach(ModifierHandle::registerWashingMaterials);
            }
            Foundmoconfigs = listFilesFromZipFile(zipFile, ConfigPath.getFileName());
            for (MoConfig moconfig : Foundmoconfigs) {
                processEntryMoConfigEntries(moconfig);
            }
            // 读取物品默认条目配置
            MinecraftForge.EVENT_BUS.post(new ExItemDefaultEntry());
            // 读取其余配置文件

            String ItemsDefaultEntryFilePath = ItemsDefaultEntryConfigPath.getFileName().toString();
            ZipEntry item = zipFile.getEntry(ItemsDefaultEntryFilePath);
            if (item != null) {
                MoConfig washingMaterialsConfig = new MoConfig(Path.of(zipFile.getName(), ItemsDefaultEntryFilePath), zipFile.getInputStream(item));
                Set<Map.Entry<String, JsonElement>> entries = washingMaterialsConfig.readEntrys();
                zipFunction.defaultEntry().add(() -> {
                    for (Map.Entry<String, JsonElement> entry : entries) {
                        processItemsDefaultEntryEntry(entry);
                    }
                });
            }


            // 读取元素
            String ElementPath = ExElementHandle.ElementConfigPath.getFileName().toString();
            ZipEntry element = zipFile.getEntry(ElementPath);
            if (element != null) {

                MoConfig ElementConfig = new MoConfig(Path.of(zipFile.getName(), ElementPath), zipFile.getInputStream(element));
                ExElementHandle.processMoConfigEntries(ElementConfig);

            }


            // 读取升级配置
            Foundlvconfigs = listFilesFromZipFile(zipFile, LEVEL_CONFIG_PATH.getFileName());
            for (MoConfig moconfig : Foundlvconfigs) {
                processLevelMoConfigEntries(moconfig);
            }
            Foundlvconfigs = listFilesFromZipFile(zipFile, ExSuitHandle.ConfigPath.getFileName());
            List<MoConfig> foundlvconfigs = Foundlvconfigs;
            zipFunction.suit().add(
                    () -> {
                        // 读取套装配置
                        try {



                            for (MoConfig moconfig : foundlvconfigs) {
                            ExSuitHandle.processMoConfigEntries(moconfig);
                        }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );


            // 读取物品品质配置
            Foundlvconfigs = listFilesFromZipFile(zipFile, ItemQualityHandle.ItemsQualityConfigPath.getFileName());
            for (MoConfig moconfig : Foundlvconfigs) {
                ItemQualityHandle.processMoConfigEntries(moconfig);
            }
            //读取默认品质
            Foundlvconfigs = listFilesFromZipFile(zipFile, ItemQualityHandle.ItemsDefaultQualityConfigPath.getFileName());
            for (MoConfig moconfig : Foundlvconfigs) {
                ItemQualityHandle.processMoConfigEntries2(moconfig);
            }
            //读取默认元素
            Foundlvconfigs = listFilesFromZipFile(zipFile, ExElementHandle.DefaultElementConfigPath.getFileName());
            List<MoConfig> foundlvconfigs1 = Foundlvconfigs;
            zipFunction.elementDefault().add(
                    () -> {

                        for (MoConfig moconfig : foundlvconfigs1) {
                                try {
                                    ExElementHandle.processMoConfigEntries2(moconfig);
                                } catch (FileNotFoundException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                        );



            //读取默认元素
            Foundlvconfigs = listFilesFromZipFile(zipFile, ExElementHandle.DefaultEntityElementConfigPath.getFileName());
            List<MoConfig> foundlvconfigs2 = Foundlvconfigs;
            zipFunction.elementDefault().add(
                    () -> {
                        try {

                            for (MoConfig moconfig : foundlvconfigs2) {
                                ExElementHandle.processMoConfigEntries3(moconfig);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

            );


            for (MoConfig moconfig : listFilesFromZipFile(zipFile, LanguageLoader.LANGUAGES_FILE_PATH.getFileName())) {
                String string = moconfig.configFile.getFileName().toString();
                String languageCode = string.substring(0, string.length() - 5);
                Map<String, String> collect = moconfig.jsonObject.asMap().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getAsString()));
                putLanguage(languageCode, collect);
            }


            long endTime = System.nanoTime(); // 记录结束时间
            long duration = endTime - startTime; // 计算持续时间
            LOGGER.debug("ReadConfig Over Modifier time: " + duration / 1000000 + " ms");
            LOGGER.debug("ReadConfig Over Modifier config count: " + Foundmoconfigs.size());
            LOGGER.debug("ReadConfig Over Modifier modifier count: " + modifierEntryMap.size());
        } catch (IOException e) {
            LOGGER.error("Error reading configuration files", e);
        } catch (Exception e) {
            LOGGER.error("Unexpected error during configuration reading", e);
        }
    }

    private static void processItemsDefaultEntryEntry(Map.Entry<String, JsonElement> entry) {
        if (!entry.getValue().isJsonObject()) {
            return;
        }
        try {
            JsonObject jsonObject = entry.getValue().getAsJsonObject();
            List<Pair<String, Integer>> entries = new ArrayList<>();
            for (JsonElement item : jsonObject.get("entries").getAsJsonArray()) {
                JsonObject asJsonObject = item.getAsJsonObject();
                int level = asJsonObject.has("level") ? asJsonObject.get("level").getAsInt() : 1;
                entries.add(new Pair<>(asJsonObject.get("id").getAsString(), level));
            }

            List<ModifierInstant> modifierEntries = new ArrayList<>();
            for (var entryA : entries) {
                ModifierEntry modifierEntry = ModifierHandle.modifierEntryMap.get(entryA.getA());
                if  (modifierEntry == null) continue;
                modifierEntries.add(modifierEntry.toInstant(entryA.getB()));
            }
            ItemSelector itemSelector = null;
            if (jsonObject.has("itemSelector")) {
                itemSelector = ItemSelector.EX_SERIALIZE.fromJsonSingle(jsonObject.get("itemSelector").getAsJsonObject());
            }
            if (itemSelector != null) itemsDefaultEntry.put(itemSelector, modifierEntries);
            LOGGER.debug("Add ItemsDefaultEntry:" + entry.getKey() + " To ModifierEntry:" + modifierEntries);

        } catch (Exception e) {
            LOGGER.error("Error reading ItemsDefaultEntry config file", e);
        }
    }

    // 处理洗涤材料条目
    public static void processWashingMaterialEntry(Map.Entry<String, JsonElement> entry,List<WashingMaterials> materialsList1) {
        if (!entry.getValue().isJsonObject()) {
            return;
        }
        try {
            JsonObject jsonObject = entry.getValue().getAsJsonObject();
            WashingMaterials materials = new WashingMaterials(
                    entry.getKey(),
                    jsonObject.get("additionEntry").getAsInt(),
                    ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry.getKey())),
                    jsonObject.get("rarity").getAsInt()
            );
            if (jsonObject.has("OnlyHasWashEntry") &&
                    jsonObject.get("OnlyHasWashEntry").getAsBoolean()) {
                materials.OnlyHasWashEntry = true;

            }
            if (jsonObject.has("MinRandomTime")
            ) {
                materials.MinRandomTime = jsonObject.get("MinRandomTime").getAsInt();

            }
            if (jsonObject.has("randomLevelSystemCount")
            ) {
                materials.randomLevelSystemCount = jsonObject.get("randomLevelSystemCount").getAsInt();

            }
            if (jsonObject.has("keepEntries")
            ) {
                materials.setKeepEntries(jsonObject.get("keepEntries").getAsInt());

            }
            if (jsonObject.has("CostExp")
            ) {
                materials.CostExp = jsonObject.get("CostExp").getAsDouble();

            }
            if (jsonObject.has("NeedCount")
            ) {
                materials.NeedCount = jsonObject.get("NeedCount").getAsInt();

            }
            if (jsonObject.has("OnlyItems")) {
                JsonArray OnlyItems = jsonObject.get("OnlyItems").getAsJsonArray();
                OnlyItems.forEach(item -> {
                    materials.OnlyItems.add(item.getAsString());
                });
            }

            if (jsonObject.has("OnlyTypes")) {
                JsonArray OnlyItems = jsonObject.get("OnlyTypes").getAsJsonArray();
                OnlyItems.forEach(item -> {
                    materials.OnlyTypes.add(ExTypeHandle.itemTypes.get(item.getAsString()));
                });
            }
            if (jsonObject.has("OnlyTags")) {
                JsonArray OnlyItems = jsonObject.get("OnlyTags").getAsJsonArray();
                OnlyItems.forEach(item -> {
                    materials.OnlyTags.add(item.getAsString());
                });
            }
            materialsList1.add(materials);
            LOGGER.debug("WashingMaterials: " + materials.ItemId + " additionEntry: " + materials.additionEntry + " rarity: " + materials.rarity);
        } catch (Exception e) {
            LOGGER.error("Error processing WashingMaterial entry: " + entry.getKey(), e);
        }
    }

    // 处理 moconfig 条目
    private static void processEntryMoConfigEntries(MoConfig moconfig) throws FileNotFoundException {
        List<ModifierEntry> entries = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : moconfig.readEntrys()) {
            try {
                processModifierEntry(moconfig, entry.getKey(), entry.getValue(), entries);
            } catch (Exception e) {
                LOGGER.error("Error processing modifier entry: " + entry.getKey(), e);
            }
        }
        ExEntryRegistryEvent event = new ExEntryRegistryEvent(entries);
        MinecraftForge.EVENT_BUS.post(event);
        WeightedUtil<String> weightedUtil = new WeightedUtil<String>(
                event.entries.stream().collect(Collectors.toMap(ModifierEntry::getId, ModifierEntry::getWeight))
        );

        event.entries.forEach(entry -> {
            RegisterModifierEntry(entry);
            LOGGER.debug(entry.id + " 出现概率 " + weightedUtil.getProbability(entry.id) * 100 + "%");
        });

        LOGGER.debug("ReadConfig Over: Type: " + moconfig.type + " Path: " + moconfig.configFile + " entries: " + entries.size());
    }

    //LevelRead
    public static void processModifierEntry(String read, List<ModifierEntry> entries) {
        try {
            // 使用 Gson 解析 JSON 字符串
            JsonObject jsonObject = JsonParser.parseString(read).getAsJsonObject();
            // 创建 MoConfig 对象并设置属性
            MoConfig moconfig = new MoConfig(Path.of(""));
            moconfig.jsonObject = jsonObject;
            if (jsonObject.has("isList") && jsonObject.get("isList").getAsBoolean()){
                if (moconfig.readSetting("type")!=null){
                    moconfig.type = ModifierEntry.StringToType(moconfig.readSetting("type").getAsString());
                }
                JsonElement group = moconfig.readSetting("group");
                if (group!=null){
                    moconfig.group = group.getAsString();
                }else moconfig.group = "exmodifier_tab";

                if (moconfig.type == ExType.CURIOS.get() &&moconfig.readSetting("type").getAsString().length()>6)moconfig.CuriosType=moconfig.readSetting("type").getAsString().substring(7);

                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    try {
                        processModifierEntry(moconfig, entry.getKey(), entry.getValue(), entries);
                    } catch (Exception e) {
                        LOGGER.error("Error processing modifier entry: " + entry.getKey(), e);
                    }
                }
                return;
            }
            moconfig.type = ModifierEntry.StringToType(jsonObject.get("type").getAsString());
            moconfig.group =jsonObject.get("group").getAsString();
            moconfig.autoTypeId = jsonObject.get("autoTypeId").getAsBoolean();
            // moconfig.CuriosType = jsonObject.get("CuriosType").getAsString();

            // 获取 modifier 入口项的集合


            // 遍历每个 modifier 入口项
            processModifierEntry(moconfig, jsonObject.get("id").getAsString(), jsonObject, entries);

        } catch (Exception e) {
            LOGGER.error("Error processing modifier entry", e);
        }
    }


    // 处理单个 Modifier 条目
    private static void processModifierEntry(MoConfig moconfig, String key, JsonElement itemElement, List<ModifierEntry> entries) {

        if (!itemElement.isJsonObject()) {
            return;
        }

        JsonObject itemObject = itemElement.getAsJsonObject();
        ModifierEntry modifierEntry = new ModifierEntry();
        //exElement.type = itemObject.has("type") ? ModifierEntry.StringToType(itemObject.get("type").getAsString()) : moconfig.type;
        if (!moconfig.CuriosType.isEmpty()) modifierEntry.curiosType = moconfig.CuriosType;

        List<ItemType> types = new ArrayList<>();

        if (itemObject.has("types")) {
            JsonArray typesArray = itemObject.get("types").getAsJsonArray();
            for (JsonElement typeElement : typesArray) {

                types.add(ModifierEntry.StringToType(typeElement.getAsString()));
            }
        } else {
            if (itemObject.has("type")) {
                types.add(ModifierEntry.StringToType(itemObject.get("type").getAsString()));
            } else {
                types.add(moconfig.type);

            }
        }
        if (itemObject.has("displayNameInItemName")) {
            modifierEntry.displayNameInItemName = itemObject.get("displayNameInItemName").getAsBoolean();
        }
        if (itemObject.has("randomLevel")){
            JsonArray randomLevel = itemObject.get("randomLevel").getAsJsonArray();
            modifierEntry.randomLevel =  new com.mojang.datafixers.util.Pair<>(randomLevel.get(0).getAsInt(),randomLevel.get(1).getAsInt());
        }
        if (types.contains(ExType.CURIOS.get())) {
            modifierEntry.isCuriosEntry = true;
            if (itemObject.has("curiosType")) {
                modifierEntry.curiosType = itemObject.get("curiosType").getAsString();
            } else {
                if (moconfig.CuriosType.isEmpty()) modifierEntry.curiosType = "ALL";
            }
        }
        StringBuilder affString = new StringBuilder();
        for (ItemType type : types) {
            affString.append(type.name(), 0, 2);

        }
        modifierEntry.types = types;
        boolean autoTypeId =itemObject.has("autoTypeId") ? itemObject.get("autoTypeId").getAsBoolean() : moconfig.autoTypeId;
        if (autoTypeId) modifierEntry.autoId = true;
        modifierEntry.id = autoTypeId ?  affString + key : key;
        modifierEntry.isRandom = itemObject.has("isRandom") && itemObject.get("isRandom").getAsBoolean();
        modifierEntry.OnlyHasThisEntry = itemObject.has("OnlyHasThisEntry") && itemObject.get("OnlyHasThisEntry").getAsBoolean();

        modifierEntry.RandomNum = itemObject.has("RandomNum") ? itemObject.get("RandomNum").getAsInt() : 0;
        modifierEntry.maxLevel = itemObject.has("maxLevel") ? itemObject.get("maxLevel").getAsInt() : 1;
        modifierEntry.iconSize = itemObject.has("iconSize") ? itemObject.get("iconSize").getAsInt() : 0;
        modifierEntry.weight = itemObject.has("weight") ? itemObject.get("weight").getAsFloat() : 1.0f;
        modifierEntry.needFreshValue = itemObject.has("needFreshValue") ? itemObject.get("needFreshValue").getAsFloat() : 0.0F;
        modifierEntry.cantSelect = itemObject.has("cantSelect") && itemObject.get("cantSelect").getAsBoolean();
        modifierEntry.localDescription = itemObject.has("localDescription") ? itemObject.get("localDescription").getAsString() : "";
        modifierEntry.icon = itemObject.has("icon") ? itemObject.get("icon").getAsString() : "";
        modifierEntry.source = itemObject.has("source") ? itemObject.get("source").getAsString() : moconfig.getSource();
        if (itemObject.has("group")){
            modifierEntry.group  = itemObject.get("group").getAsString();
        }else {
            if (!moconfig.group.isEmpty()) modifierEntry.group = moconfig.group;
            else modifierEntry.group = "exmodifier_tab";
        }
            if (itemObject.has("exsuit")) {
            List<String> exss = new ArrayList<>();
            for (JsonElement exsuit : itemObject.get("exsuit").getAsJsonArray()) {
                exss.add(exsuit.getAsString());
            }
            if (ModifierHandle.EEMatchQueue.containsKey(modifierEntry.id)) {
                ModifierHandle.EEMatchQueue.get(modifierEntry.id).addAll(exss);
            }else ModifierHandle.EEMatchQueue.put(modifierEntry.id, exss);
        }
        if (!modifierEntry.isRandom) modifierEntry.RandomNum = 0;
        LOGGER.debug(modifierEntry.id + " weight " + modifierEntry.weight);
        if (itemObject.has("OnlyItems")) {
            JsonArray OnlyItems = itemObject.get("OnlyItems").getAsJsonArray();
            OnlyItems.forEach(item -> {
                modifierEntry.modifierItemSelector.addOnlyWashItem(item.getAsString());
            });
        }
        if (itemObject.has("entityTypes")) {
            JsonArray OnlyItems = itemObject.get("entityTypes").getAsJsonArray();
            OnlyItems.forEach(item -> {
                modifierEntry.entityTypes.add(item.getAsString());
            });
        }

        modifierEntry.Slots = new ArrayList<>();
        if (itemObject.has("slots")) {
            JsonArray OnlyItems = itemObject.get("slots").getAsJsonArray();
            OnlyItems.forEach(item -> {
                modifierEntry.Slots.add(item.getAsString());
            });
        }
        if (itemObject.has("specialTags")) {
            JsonArray OnlyItems = itemObject.get("specialTags").getAsJsonArray();
            OnlyItems.forEach(item -> {

                if (item.isJsonObject()){
                    JsonObject asJsonObject = item.getAsJsonObject();
                    String asString = asJsonObject.get("id").getAsString();
                    modifierEntry.specialTags.add(asString);
                    modifierEntry.specialTagSetting.put(asString,asJsonObject);
                }else {
                    modifierEntry.specialTags.add(item.getAsString());
                }
            });
        }
        if (itemObject.has("tags")) {
            JsonArray OnlyItems = itemObject.get("tags").getAsJsonArray();
            OnlyItems.forEach(item -> {
                modifierEntry.tags.add(ExUtil.createOrGetModifierTagKey(new ResourceLocation(item.getAsString())));
            });
        }
        modifierEntry.tags.add(ModifierEntry.defaultTag);
        if (itemObject.has("OnlyTags")) {
            JsonArray OnlyItems = itemObject.get("OnlyTags").getAsJsonArray();
            OnlyItems.forEach(item -> {
                modifierEntry.modifierItemSelector.addOnlyTag(item.getAsString());
            });
        }
        if (itemObject.has("OnlyWashItems")) {
            JsonArray OnlyItems = itemObject.get("OnlyWashItems").getAsJsonArray();
            OnlyItems.forEach(item -> {
                modifierEntry.modifierItemSelector.addOnlyWashItem(item.getAsString());
            });
        }
        if (itemObject.has("UnlessItemIds")) {
            for (JsonElement itemId : itemObject.get("UnlessItemIds").getAsJsonArray()) {
                modifierEntry.modifierItemSelector.addUnlessItemId(itemId.getAsString());
            }
        }
        if (itemObject.has("UnlessItemTags")) {
            for (JsonElement itemId : itemObject.get("UnlessItemTags").getAsJsonArray()) {
                modifierEntry.modifierItemSelector.addUnlessItemTag(itemId.getAsString());
            }
        }
        if (itemObject.has("attrGethers")) {
            processAttrGethers(moconfig, modifierEntry, itemObject.get("attrGethers"));
        }
        if (itemObject.has("attriGethers")) {
            processAttriGethers(moconfig, modifierEntry, itemObject.get("attriGethers"));
        }

        LOGGER.debug("ReadConfig: Type: " + moconfig.type + " Path: " + moconfig.configFile + " Id: " + key + " attrGethers: " + modifierEntry.attriGether.size());
        entries.add(modifierEntry);
    }

    // 处理 attrGethers

    public static void processAttrGethers(MoConfig moconfig, ModifierEntry modifierEntry, JsonElement attrGethers) {

        // Fallback to the original behavior if it's still an object
        JsonObject attrGethersObject = attrGethers.getAsJsonObject();
        int index = 0;
        for (Map.Entry<String, JsonElement> attrGetherEntry : attrGethersObject.entrySet()) {
            try {
                processAttrGether(moconfig, modifierEntry, attrGetherEntry.getKey(), attrGetherEntry.getValue(), index);
                index++;
            } catch (Exception e) {
                LOGGER.error("Error processing attrGether: " + attrGetherEntry.getKey(), e);
            }
        }

    }

    public static void processAttriGethers(MoConfig moconfig, ModifierEntry modifierEntry, JsonElement attrGethers) {

        JsonArray attrGethersArray = attrGethers.getAsJsonArray();
        int index = 0;
        for (JsonElement attrGetherElement : attrGethersArray) {
            try {
                processAttrGether(moconfig, modifierEntry, attrGetherElement.getAsJsonObject().get("id").getAsString(), attrGetherElement, index);
                index++;
            } catch (Exception e) {
                LOGGER.error("Error processing attrGether at index " + index, e);
            }
        }
    }

    // 处理单个 attrGether 条目
    private static void processAttrGether(MoConfig moconfig, ModifierEntry modifierEntry, String key, JsonElement attrGetherEntry, int index) {
        JsonObject attrGetherObj = attrGetherEntry.getAsJsonObject();
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(key));
        double attrValue = attrGetherObj.has("value") ? attrGetherObj.get("value").getAsDouble() : 0;


//        if (attrGetherObj.has("autoUUID")){
//            if (attrGetherObj.get("autoUUID").getAsBoolean()) {
//                uuid = ExConfigHandle.autoUUid(ExConfigHandle.autoUUID);
//                ExConfigHandle.autoUUID++;
//            }
//        }

        AttributeModifier.Operation operation = attrGetherObj.has("operation") ? ExConfigHandle.getOperation(attrGetherObj.get("operation").getAsString()) : AttributeModifier.Operation.ADDITION;
        EquipmentSlot slot = getEquipmentSlot(attrGetherObj);
        String modifierName = (attrGetherObj.has("modifierName")) ? attrGetherObj.get("modifierName").getAsString() : modifierEntry.id + index;
        ;
        if (attrGetherObj.has("id")) {
            attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attrGetherObj.get("id").getAsString()));
        }
        if (attrGetherObj.has("autoName")) {
            if (attrGetherObj.has("autoName")) {
                if (attrGetherObj.get("autoName").getAsBoolean()) {
                    modifierName = modifierEntry.id + index;

                }
            }
        }
//        UUID uuid = null;
//        if (attrGetherObj.has("uuid") && !attrGetherObj.get("uuid").getAsString().isEmpty()) {
//            UUID.fromString(attrGetherObj.get("uuid").getAsString());
//        } else {
//            UUID.nameUUIDFromBytes(modifierName.getBytes());
//
//
//        }
//        if (attrGetherObj.has("autoUUID") && attrGetherObj.get("autoUUID").getAsBoolean())
//            uuid = UUID.nameUUIDFromBytes(modifierName.getBytes());
//        //UUID uuid = ExConfigHandle.generateUUIDFromString(modifierName);
//        LOGGER.debug("uuid " + uuid);

        ExAttributeModifier modifier = new ExAttributeModifier(modifierName, attrValue, operation);
        ModifierAttriGether attrGether = new ModifierAttriGether(attribute, modifier, slot);
        if (attrGetherObj.has("minValue")) {
            attrGether.minValue = attrGetherObj.get("minValue").getAsDouble();
        }
        attrGether.Expression = attrGetherObj.has("ValueExpression") ? attrGetherObj.get("ValueExpression").getAsString() : "";
        attrGether.maxValue = attrGetherObj.has("maxValue") ? attrGetherObj.get("maxValue").getAsDouble() : attrGether.minValue;
        attrGether.reserveDouble = attrGetherObj.has("reserveDouble") ? attrGetherObj.get("reserveDouble").getAsInt() : 3;
        Map<Double, Float> simpleWeight = new HashMap<>();
        List<Double> mayValues = new ArrayList<>();
        List<Float> mayValuesKey = new ArrayList<>();
        if (attrGetherObj.has("mayValues")) {
            JsonArray mayValuesArray = attrGetherObj.get("mayValues").getAsJsonArray();
            mayValuesArray.forEach(mayValue -> {
                mayValues.add(mayValue.getAsDouble());
            });
        }
        if (attrGetherObj.has("mayValuesWeight")) {
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
        if (!attrGether.IsAutoEquipmentSlot) {
            if (attrGetherObj.has("slot")) {
                if (!attrGetherObj.get("slot").getAsString().equals("auto")) {
                    attrGether.slot = EquipmentSlot.valueOf(attrGetherObj.get("slot").getAsString());
                } else {
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

        LOGGER.debug("Attribute: " + attribute + " key: " + key);
        modifierEntry.attriGether.add(attrGether);


    }

    // 获取UUID
    public static UUID getUUID(JsonObject attrGetherObj) {
        if (attrGetherObj.has("autoUUID") && attrGetherObj.get("autoUUID").getAsBoolean()) {
            return ExConfigHandle.autoUUid(ExConfigHandle.autoUUID);
        }
        if (attrGetherObj.has("uuid") && !attrGetherObj.get("uuid").getAsString().isEmpty()) {
            return UUID.fromString(attrGetherObj.get("uuid").getAsString());
        }
        return ExConfigHandle.autoUUid(ExConfigHandle.autoUUID);
    }

    public static UUID getUUID(JsonObject attrGetherObj, ModifierEntry modifierEntry) {
        return UUID.nameUUIDFromBytes(modifierEntry.id.getBytes());
    }

    public static UUID getUUID(JsonObject attrGetherObj, ExSuit exSuit) {
        return UUID.nameUUIDFromBytes(exSuit.id.getBytes());
    }

    // 获取装备槽位
    public static EquipmentSlot getEquipmentSlot(JsonObject attrGetherObj) {
        if (attrGetherObj.has("slot")) {
            String slotStr = attrGetherObj.get("slot").getAsString();
            if (!slotStr.equals("auto")) {
                return ExConfigHandle.getEquipmentSlot(slotStr);
            }
        }
        return null;
    }
}
