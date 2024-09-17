package net.exmo.exmodifier.content.modifier;

import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.config;
import net.exmo.exmodifier.content.suit.ExSuit;
import net.exmo.exmodifier.content.suit.ExSuitHandle;
import net.exmo.exmodifier.events.*;
import net.exmo.exmodifier.network.ExModifiervaV;
import net.exmo.exmodifier.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;


import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static net.exmo.exmodifier.util.ExConfigHandle.listFiles;
import static net.minecraft.world.item.ItemStack.ATTRIBUTE_MODIFIER_FORMAT;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModifierHandle {
    public static List<ModifierEntry> getEntrysFromItemStack(ItemStack stack) {
        List<ModifierEntry> modifierEntries = new ArrayList<>();

        for (ModifierEntry modifierAttriGether : modifierEntryMap.values().stream().filter(Objects::nonNull).toList()) {
            String id;
            for (int i = 0; true; i++) {
                id = stack.getOrCreateTag().getString("exmodifier_armor_modifier_applied"+i);
                if (id.isEmpty())break;
                if (id.equals(modifierAttriGether.getId())) {
                    modifierEntries.add(modifierAttriGether);
                }

            }
        }
        return modifierEntries;
    }
    //    public static void init() throws IOException {
//        modifierEntryMap = new HashMap<>();
//        readConfig();
//    }
    public static List<String> percentAtr = new ArrayList<>();

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

    @Mod.EventBusSubscriber
    public static   class CommonEvent{
        public static List<Component> generateEntryTooltip(ModifierEntry modifierEntry,Player player,ItemStack itemStack) {
            List<Component> tooltips = new ArrayList<>();
            String id = modifierEntry.getId();
            if (id.length() >= 2) {
                if (config.compact_tooltip) tooltips.add(Component.translatable("modifiler.entry." + id.substring(2)));
                else      tooltips.add(Component.translatable("modifiler.entry." + id.substring(2)).append(" : "));
                if (ExSuitHandle.LoadExSuit.entrySet().stream().anyMatch(e -> e.getValue().entry.contains(modifierEntry))){
                    ExModifiervaV.PlayerVariables pv = player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).orElse(new ExModifiervaV.PlayerVariables());
                    if (!config.compact_tooltip) tooltips.add(Component.translatable("modifiler.entry.suit"));

                    for (ExSuit suit : ExSuitHandle.LoadExSuit.values().stream().filter(exSuit -> exSuit.entry.contains(modifierEntry))
                            .toList()){
                        if (suit.visible) {
                            Integer integer = pv.SuitsNum.get(suit.id);
                            if (integer == null) integer = 0;
                            tooltips.add(Component.translatable("modifiler.entry.suit." + suit.id).append(Component.literal("§6(" + integer + "/" + suit.CountMaxLevelAndGet() + ")")));
                            if (!suit.LocalDescription.isEmpty())tooltips.add(Component.translatable(suit.LocalDescription));

                            //.append(Component.translatable("modifiler.entry.suit.color"))
                        }
                    };

                }
                for (ModifierAttriGether modifierAttriGether : modifierEntry.attriGether) {
                    AttributeModifier attributemodifier = modifierAttriGether.getModifier();
                    Attribute attribute = modifierAttriGether.getAttribute();
                    if (attribute == null)continue;
                    if (attributemodifier ==null)continue;
                    //    if (modifierAttriGether.slot==null)continue;
                    EquipmentSlot slot = modifierAttriGether.slot;
                    if (modifierAttriGether.IsAutoEquipmentSlot){
                        slot = ModifierEntry.TypeToEquipmentSlot(ModifierEntry.getType(itemStack));
                    }
                    if (!ItemAttrUtil.hasAttributeModifierCompoundTagNoAmount(itemStack, attribute, attributemodifier, modifierAttriGether.slot))continue;
                    //  Exmodifier.LOGGER.info(modifierAttriGether.getAttribute().getDescriptionId());
                    //   if (!itemStack.getAttributeModifiers(modifierAttriGether.slot).containsEntry(attribute, attributemodifier))continue;
                    double d0 = attributemodifier.getAmount();
                    boolean flag = false;
                    String percent = "";
                    double d1;
                    if (attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL  &&!percentAtr.contains(ForgeRegistries.ATTRIBUTES.getKey(attribute).toString())) {
                        if ((attribute).equals(Attributes.KNOCKBACK_RESISTANCE)) {
                            d1 = d0 * 10.0;
                        } else {
                            d1 = d0;
                        }
                    } else {
                        d1 = d0 * 100.0;
                    }
                    String amouta2 = "";
                    if (percentAtr.contains(ForgeRegistries.ATTRIBUTES.getKey(attribute).toString())){
                        percent = "%";
                        DecimalFormat df = new DecimalFormat("#.####");
                        amouta2 = df.format(attributemodifier.getAmount() * 100);
                        if (modifierAttriGether.attribute.getDescriptionId().length() >=4){
                            if (ForgeRegistries.ATTRIBUTES.getKey(attribute).toString().startsWith("twtp") ||ForgeRegistries.ATTRIBUTES.getKey(attribute).toString().startsWith("isfix") ) {
                                amouta2 = df.format(attributemodifier.getAmount()) ;
                            }
                        }
                    }

                    if (flag) {
                        tooltips.add((Component.literal(" ")).append(Component.translatable("attribute.modifier.equals." + attributemodifier.getOperation().toValue(), new Object[]{ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(attribute.getDescriptionId())})).withStyle(ChatFormatting.DARK_GREEN));
                    } else if (d0 > 0.0) {
                        if (percent.equals("%")) tooltips.add(Component.translatable("add").append(amouta2).append(percent).append(" ").append(Component.translatable(attribute.getDescriptionId())).withStyle(ChatFormatting.BLUE));
                        else tooltips.add((Component.translatable("attribute.modifier.plus." + attributemodifier.getOperation().toValue(), new Object[]{ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(attribute.getDescriptionId())})).withStyle(ChatFormatting.BLUE));
                    } else if (d0 < 0.0) {
                        d1 *= -1.0;
                        if (percent.equals("%")) tooltips.add(Component.translatable("subtract").append(amouta2).append(percent).append(" ").append(Component.translatable(attribute.getDescriptionId())).withStyle(ChatFormatting.RED));
                        else  tooltips.add((Component.translatable("attribute.modifier.take." + attributemodifier.getOperation().toValue(), new Object[]{ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(attribute.getDescriptionId())})).withStyle(ChatFormatting.RED));
                    }
                }
            }
            ExEntryTooltipEvent event = new ExEntryTooltipEvent(modifierEntry, player, itemStack, tooltips);
            MinecraftForge.EVENT_BUS.post(event);
            return event.getTooltip();
        }
        @SubscribeEvent
        public static void ItemTooltip(ItemTooltipEvent event) {


//            for (Component tooltip : event.getToolTip()){
//                Exmodifier.LOGGER.debug("tooltip: " + tooltip);
//            }
//            Exmodifier.LOGGER.debug("------------------------------------");

        }
        public static void RandomEntryCurios(ItemStack stack, WeightedUtil<String> weightedUtil,List<String> slots, int refreshments) {
            int numAddedModifiers = 0;

            List<ModifierAttriGether> finalAttriGethers = new ArrayList<>();
            Set<String> appliedModifiers = new HashSet<>();
            List<ModifierEntry> modifierEntries = new ArrayList<>();
            if (weightedUtil.weights.size()<refreshments)refreshments = weightedUtil.weights.size();
            while (numAddedModifiers < refreshments) {
                ModifierEntry modifierEntry = modifierEntryMap.get(weightedUtil.selectRandomKeyBasedOnWeights());
                if (modifierEntries.contains(modifierEntry))continue;
                if(modifierEntry.id!=null) Exmodifier.LOGGER.debug("add entry: " + modifierEntry.id);
                modifierEntries.add(modifierEntry);
                if (modifierEntry == null) {
                    Exmodifier.LOGGER.debug("modifierEntry is null");
                    return;
                }

                if (!appliedModifiers.contains(modifierEntry.id)) {
                    Exmodifier.LOGGER.debug("add entry start: " + modifierEntry.id);
                    appliedModifiers.add(modifierEntry.id);

                    stack.getOrCreateTag().putString("exmodifier_armor_modifier_applied" + numAddedModifiers, modifierEntry.id);
                    numAddedModifiers++;
                    Exmodifier.LOGGER.debug("add entry ing: " + modifierEntry.id);

                    List<ModifierAttriGether> attriGethers = selectModifierAttributes(modifierEntry,stack);
                    Exmodifier.LOGGER.debug("add entry end: " + modifierEntry.id +" "+attriGethers);
                    finalAttriGethers.addAll(attriGethers);
                }
            }

            applyModifiersCurios(stack, finalAttriGethers, slots);
        }
        public static void RandomEntry(ItemStack stack, WeightedUtil<String> weightedUtil, EquipmentSlot slot, int refreshments) {
            int numAddedModifiers = 0;

            List<ModifierAttriGether> finalAttriGethers = new ArrayList<>();
            Set<String> appliedModifiers = new HashSet<>();
            List<ModifierEntry> modifierEntries = new ArrayList<>();
            if (weightedUtil.weights.size()<refreshments)refreshments = weightedUtil.weights.size();

            while (numAddedModifiers < refreshments) {
                ModifierEntry modifierEntry = modifierEntryMap.get(weightedUtil.selectRandomKeyBasedOnWeights());
                if (modifierEntries.contains(modifierEntry))continue;
                Exmodifier.LOGGER.debug("add entry: " + modifierEntry.id);
                modifierEntries.add(modifierEntry);
                if (modifierEntry == null) {
                    Exmodifier.LOGGER.debug("modifierEntry is null");
                    return;
                }

                if (!appliedModifiers.contains(modifierEntry.id)) {
                    Exmodifier.LOGGER.debug("add entry start: " + modifierEntry.id);
                    appliedModifiers.add(modifierEntry.id);

                    stack.getOrCreateTag().putString("exmodifier_armor_modifier_applied" + numAddedModifiers, modifierEntry.id);
                    numAddedModifiers++;
                    Exmodifier.LOGGER.debug("add entry ing: " + modifierEntry.id);

                    List<ModifierAttriGether> attriGethers = selectModifierAttributes(modifierEntry,stack);
                    ExAddEntryAttrigethersEvent event = new ExAddEntryAttrigethersEvent(stack, weightedUtil, slot, refreshments, attriGethers,modifierEntry,modifierEntries);
                    MinecraftForge.EVENT_BUS.post(event);
                    finalAttriGethers.addAll(event.attriGether);
                }
            }

            applyModifiers(stack, finalAttriGethers, slot);
        }

        private static List<ModifierAttriGether> selectModifierAttributes(ModifierEntry modifierEntry,ItemStack stack) {
            List<ModifierAttriGether> attriGethers = new ArrayList<>();

            if (modifierEntry.RandomNum > 0) {
                int remainingRandoms = modifierEntry.RandomNum;
                Map<String,ModifierAttriGether> toRandom = new HashMap<>();
                for (int i = 0; i < modifierEntry.attriGether.size(); i++){
                    ModifierAttriGether attriGether = modifierEntry.attriGether.get(i);
                    if (attriGether.weight >0){
                        toRandom.put(String.valueOf(i),attriGether);
                    }
                }
                List<ModifierAttriGether> firstAdd = new ArrayList<>();
                for (int i = 0; i < modifierEntry.attriGether.size(); i++){
                    ModifierAttriGether attriGether = modifierEntry.attriGether.get(i);
                    if (attriGether.weight==0){
                        firstAdd.add(attriGether);
                    }
                }
                attriGethers.addAll(firstAdd);
                remainingRandoms -= firstAdd.size();
                Map<String,Float> wemap    = new HashMap<>();
                toRandom.forEach((key, value) -> wemap.put(key,value.weight));
                WeightedUtil<String> weightedUtil = new WeightedUtil<String>(wemap);
                while (remainingRandoms > 0) {
                    String selectedKey = (String) weightedUtil.selectRandomKeyBasedOnWeights();
                    ModifierAttriGether selectedAttriGether = toRandom.get(selectedKey);
                    if (!attriGethers.contains(selectedAttriGether)) {
                        if (selectedAttriGether != null)    {
                            if (selectedAttriGether.getAttribute()!=null){
                                ExAddEntryAttrigetherEvent event = new ExAddEntryAttrigetherEvent(modifierEntry, selectedAttriGether, stack);
                                MinecraftForge.EVENT_BUS.post(event);
                                attriGethers.add(event.selectedAttriGether);
                                Exmodifier.LOGGER.debug("add Random entry: " + selectedAttriGether.getAttribute().getDescriptionId());
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
        private static void applyModifiersCurios(ItemStack stack, List<ModifierAttriGether> attriGethers, List<String> CuriosSlots) {
            Map<String, Multimap<Attribute, AttributeModifier>> attriMap = new HashMap<>();
            for (String CuriosSlot : CuriosSlots) {
                attriMap.put(CuriosSlot, CuriosUtil.getAttributeModifiers(stack, CuriosSlot));
            }
            for (ModifierAttriGether attriGether : attriGethers) {
                attriGether.modifier = new AttributeModifier(UUID.nameUUIDFromBytes((attriGether.modifier.getName()+stack).getBytes()), attriGether.modifier.getName(), attriGether.modifier.getAmount(), attriGether.modifier.getOperation());

                if (ForgeRegistries.ATTRIBUTES.containsValue(attriGether.attribute)) {
                    for (String CuriosSlot : CuriosSlots) CuriosUtil.addAttributeModifierApi(stack,attriGether,CuriosSlot);
                    //   ItemAttrUtil.addItemAttributeModifier(stack, attriGether.attribute, attriGether.modifier, applicableSlot);
                } else {
                    Exmodifier.LOGGER.debug("attribute is not exists");
                }
            }
            attriMap.forEach((key, value)->{
                value.forEach((attribute, modifier) -> {
                    ExApplyEntryAttrigetherEvent event = new ExApplyEntryAttrigetherEvent(stack, new ModifierAttriGether(attribute,modifier), true, key);
                    MinecraftForge.EVENT_BUS.post(event);
                    CuriosUtil.addAttributeModifierApi(event.stack,event.attriGether,event.curiosSlot);
                });
            });
        }
        private static void applyModifiers(ItemStack stack, List<ModifierAttriGether> attriGethers, EquipmentSlot slot) {
            for (ModifierAttriGether attriGether : attriGethers) {
                EquipmentSlot applicableSlot = attriGether.IsAutoEquipmentSlot ? slot : attriGether.slot;

                if (ForgeRegistries.ATTRIBUTES.containsValue(attriGether.attribute)) {
                    attriGether.modifier = new AttributeModifier(UUID.nameUUIDFromBytes((attriGether.modifier.getName()+stack.getItem().getDescriptionId()).getBytes()), attriGether.modifier.getName(), attriGether.modifier.getAmount(), attriGether.modifier.getOperation());
                    ExApplyEntryAttrigetherEvent event = new ExApplyEntryAttrigetherEvent(stack, attriGether, applicableSlot);
                    MinecraftForge.EVENT_BUS.post(event);
                    ItemAttrUtil.addItemAttributeModifier(event.stack,event.attriGether.attribute, event.attriGether.modifier, event.slot);
                } else {
                    Exmodifier.LOGGER.debug("attribute is not exists");
                }
            }
        }
        public static void RandomEntryCurios(ItemStack stack, int rarity, int refreshnumber,String washItem) {
            if (stack.getTag() ==null||stack.getTag().getInt("exmodifier_armor_modifier_applied") >0)return;
            if (stack.getTag()!=null) {
                stack.getTag().remove("wash_item");
                stack.getTag().remove("modifier_refresh_rarity");
                stack.getTag().remove("modifier_refresh_add");
                stack.getTag().remove("modifier_refresh");

            }
            List<String> curiosType = CuriosUtil.getSlotsFromItemstack(stack);
            WeightedUtil<String> weightedUtil = new WeightedUtil<>(
                    modifierEntryMap.entrySet().stream()
                            .filter(e -> e.getValue().type == ModifierEntry.Type.CURIOS).filter(e -> curiosType.contains(e.getValue().curiosType)|| e.getValue().curiosType.equals("ALL"))
                            .filter(e -> (e.getValue().OnlyItems.isEmpty() ||e.getValue().OnlyItems.contains(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString())))
                            .filter(e -> !e.getValue().cantSelect)
                            .filter(e -> e.getValue().OnlyTags.isEmpty() ||e.getValue().containTag(stack))
                            .filter(e -> e.getValue().OnlyWashItems.isEmpty() ||e.getValue().OnlyWashItems.contains(washItem))
                            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().weight)));
            RandomEntryCurios(stack, weightedUtil, curiosType, refreshnumber);
            stack.getOrCreateTag().putInt("exmodifier_armor_modifier_applied",
                    stack.getOrCreateTag().getInt("exmodifier_armor_modifier_applied") + 1);

        }
        public static void RandomEntry(ItemStack stack, int rarity, int refreshnumber,String washItem) {
            if (stack.getTag() ==null||stack.getTag().getInt("exmodifier_armor_modifier_applied") >0)return;
            if (stack.getTag()!=null) {
                stack.getTag().remove("wash_item");
                stack.getTag().remove("modifier_refresh_rarity");
                stack.getTag().remove("modifier_refresh_add");
                stack.getTag().remove("modifier_refresh");

            }


            boolean over = false;
            Map<ModifierEntry.Type, EquipmentSlot> typeSlotMap =new  HashMap<>( Map.of(
                    ModifierEntry.Type.HELMET, EquipmentSlot.HEAD,
                    ModifierEntry.Type.CHESTPLATE, EquipmentSlot.CHEST,
                    ModifierEntry.Type.BOOTS, EquipmentSlot.FEET,
                    ModifierEntry.Type.LEGGINGS, EquipmentSlot.LEGS,
                    ModifierEntry.Type.ARMOR, EquipmentSlot.CHEST,  // For ARMOR type, we'll dynamically set the slot based on the item
                    ModifierEntry.Type.SHIELD, EquipmentSlot.OFFHAND,
                    ModifierEntry.Type.SWORD, EquipmentSlot.MAINHAND,
                    ModifierEntry.Type.ATTACKABLE, EquipmentSlot.MAINHAND,
                    ModifierEntry.Type.AXE, EquipmentSlot.MAINHAND
            ));

            for (Map.Entry<ModifierEntry.Type, EquipmentSlot> entry : typeSlotMap.entrySet()) {
                ModifierEntry.Type type = entry.getKey();
                EquipmentSlot slot = entry.getValue();

                if (!over && isValidForType(stack, type)) {
                    WeightedUtil<String> weightedUtil = new WeightedUtil<>(
                            modifierEntryMap.entrySet().stream()
                                    .filter(e -> e.getValue().type == type )
                                    .filter(e -> (e.getValue().OnlyItems.isEmpty() ||e.getValue().OnlyItems.contains(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString())))
                                    .filter(e -> !e.getValue().cantSelect)
                                    .filter(e -> e.getValue().OnlyTags.isEmpty() ||e.getValue().containTag(stack))
                                    .filter(e -> e.getValue().OnlyWashItems.isEmpty() ||e.getValue().OnlyWashItems.contains(washItem))
                                    .filter(e -> {
                                        boolean hasWashItem = materialsList.stream()
                                                .filter(m -> m.ItemId.equals(washItem))
                                                .findAny()
                                                .map(m -> !m.OnlyHasWashEntry)
                                                .orElse(true);

                                        return hasWashItem || e.getValue().OnlyWashItems.contains(washItem);
                                    })
                                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().weight))
                    );

                    if (!weightedUtil.weights.isEmpty()) {
                        weightedUtil.increaseWeightsByRarity(rarity);
                        if (entry.getKey() == ModifierEntry.Type.ARMOR) {  // ARMOR type, set the slot based on the item
                            slot = ((ArmorItem) stack.getItem()).getEquipmentSlot();
                        }
                        Exmodifier.LOGGER.debug("RandomEntry: " + type + " " + slot);
                        RandomEntry(stack, weightedUtil, slot, refreshnumber);
                        stack.getOrCreateTag().putInt("exmodifier_armor_modifier_applied",
                                stack.getOrCreateTag().getInt("exmodifier_armor_modifier_applied") + 1);
                        over = true;
                    }
                }
            }
        }

        private static boolean isValidForType(ItemStack stack, ModifierEntry.Type type) {

            if (type == ModifierEntry.Type.HELMET) return hasHelmetConfig && stack.getItem() instanceof ArmorItem && ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.HEAD;
            if (type == ModifierEntry.Type.CHESTPLATE) return hasChestConfig && stack.getItem() instanceof ArmorItem && ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST;
            if (type == ModifierEntry.Type.BOOTS) return hasBootsConfig && stack.getItem() instanceof ArmorItem && ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.FEET;
            if (type == ModifierEntry.Type.LEGGINGS) return hasLeggingsConfig && stack.getItem() instanceof ArmorItem && ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.LEGS;
            if (type == ModifierEntry.Type.ARMOR) return stack.getItem() instanceof ArmorItem;
            if (type == ModifierEntry.Type.SHIELD) return stack.getItem() instanceof ShieldItem;
            if (type == ModifierEntry.Type.SWORD) return hasSwordConfig && stack.getItem() instanceof SwordItem;
            if (type == ModifierEntry.Type.ATTACKABLE) return stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE).stream().mapToDouble(AttributeModifier::getAmount).sum() > 0;
            if (type == ModifierEntry.Type.AXE) return stack.getItem() instanceof AxeItem;

            return false;
        }
        public static void clearEntry(ItemStack stack){
            if (stack.getOrCreateTag().getInt("exmodifier_armor_modifier_applied")==0)return;
            ModifierEntry.Type type = ModifierEntry.getType(stack);
            List<String> curiosType = CuriosUtil.getSlotsFromItemstack(stack);
            List<ModifierEntry> hasAttriGether = getEntrysFromItemStack(stack);
            for (int i = 0; i < hasAttriGether.size(); i++)
            {
                ModifierEntry modifierAttriGether = hasAttriGether.get(i);
                for (ModifierAttriGether modifierAttriGether1 : modifierAttriGether.attriGether) {
                    EquipmentSlot slot = modifierAttriGether1.slot;
                    if (modifierAttriGether1.IsAutoEquipmentSlot){
                        slot = ModifierEntry.TypeToEquipmentSlot(ModifierEntry.getType(stack));
                    }
                    if (curiosType.isEmpty()) ItemAttrUtil.removeAttributeModifierNoAmout(stack, modifierAttriGether1.getAttribute(), modifierAttriGether1.getModifier(), slot);
                    else {
                        for (String curioType : curiosType)
                        {
                            if (ForgeRegistries.ATTRIBUTES.containsValue(modifierAttriGether1.getAttribute())&&ForgeRegistries.ATTRIBUTES.getKey(modifierAttriGether1.getAttribute())!=null) CuriosUtil.removeAttributeModifier(stack,ForgeRegistries.ATTRIBUTES.getKey(modifierAttriGether1.getAttribute()).toString(), modifierAttriGether1.getModifier().getAmount(),modifierAttriGether1.getModifier().getOperation().toValue(), curioType);
                        }
                    }
                    stack.getOrCreateTag().remove("exmodifier_armor_modifier_applied"+i);
                }

            }
//            for (ModifierEntry modifierAttriGether : modifierEntryMap.values()) {
//                String id =stack.getOrCreateTag().getString("exmodifier_armor_modifier_applied"+0);
//                for (int i = 1; !id.isEmpty(); i++) {
//                 //   Exmodifier.LOGGER.debug("Remove id" + id);
//                    if (id.equals(modifierAttriGether.getId())) {
//                        hasAttriGether.add(modifierAttriGether);
//                        for (ModifierAttriGether modifierAttriGether1 : modifierAttriGether.attriGether) {
//                            EquipmentSlot slot = modifierAttriGether1.slot;
//                            if (modifierAttriGether1.IsAutoEquipmentSlot){
//                                slot = ModifierEntry.TypeToEquipmentSlot(ModifierEntry.getType(stack));
//                            }
//                            ItemAttrUtil.removeAttributeModifierNoAmout(stack, modifierAttriGether1.getAttribute(), modifierAttriGether1.getModifier(), slot);
//                        }
//                        stack.getOrCreateTag().putString("exmodifier_armor_modifier_applied"+(i-1),"");
//                    }
//                    id = stack.getOrCreateTag().getString("exmodifier_armor_modifier_applied"+i);
//                }
//            }
        }

//        @SubscribeEvent
//        public static void atReload(AddReloadListenerEvent event) throws IOException {
//            modifierEntryMap = new HashMap<>();
//            readConfig();
//        }
// 已移至MainEvent
    }
    public static boolean hasHelmetConfig =false;
    public static boolean hasChestConfig = false;
    public static boolean hasLeggingsConfig = false;
    public static boolean hasBootsConfig = false;
    public static boolean hasSwordConfig = false;
    public static final Path WashingMaterialsConfigPath = FMLPaths.CONFIGDIR.get().resolve("exmo/WashingMaterials.json");
    // public static final Path ConfigPath = FMLPaths.MODSDIR.get().resolve("data/exmodifier/modifier");
    public static Path ConfigPath = FMLPaths.CONFIGDIR.get().resolve("exmo/modifier");
    public static List<MoConfig> Foundmoconfigs = new ArrayList<>();
    public static List<WashingMaterials> materialsList = new ArrayList<>();
    public static Map<String,ModifierEntry> modifierEntryMap = new HashMap<>();
    public static void RegisterModifierEntry(ModifierEntry modifierEntry){
        if (!hasBootsConfig)if (modifierEntry.type == ModifierEntry.Type.BOOTS)hasBootsConfig=true;
        if (!hasLeggingsConfig)if (modifierEntry.type == ModifierEntry.Type.LEGGINGS)hasLeggingsConfig=true;
        if (!hasChestConfig)if (modifierEntry.type == ModifierEntry.Type.CHESTPLATE)hasChestConfig=true;
        if (!hasHelmetConfig)if (modifierEntry.type == ModifierEntry.Type.HELMET)hasHelmetConfig=true;
        if (!hasSwordConfig)if (modifierEntry.type == ModifierEntry.Type.SWORD)hasSwordConfig=true;
        modifierEntryMap.put(modifierEntry.id,modifierEntry);
        Exmodifier.LOGGER.debug("RegisterModifierEntry: Type:" + modifierEntry.type + " Target:" + modifierEntry.id  );
    }

    public static void readConfig() {
        long startTime = System.nanoTime(); // 记录开始时间

        try {
            // 打印所有属性的日志
            ForgeRegistries.ATTRIBUTES.forEach(attribute ->
                    Exmodifier.LOGGER.debug("Attribute: " + ForgeRegistries.ATTRIBUTES.getKey(attribute))
            );

            // 读取洗涤材料配置
            if (Files.exists(WashingMaterialsConfigPath)) {
                materialsList = new ArrayList<>();
                MoConfig washingMaterialsConfig = new MoConfig(WashingMaterialsConfigPath);

                for (Map.Entry<String, JsonElement> entry : washingMaterialsConfig.readEntrys()) {
                    processWashingMaterialEntry(entry);
                }
            }

            // 读取其余配置文件
            Foundmoconfigs =  listFiles(ConfigPath);
            for (MoConfig moconfig : Foundmoconfigs) {
                processMoConfigEntries(moconfig);
            }
            long endTime = System.nanoTime(); // 记录结束时间
            long duration = endTime - startTime; // 计算持续时间
            Exmodifier.LOGGER.debug("ReadConfig Over Modifier time: " + duration / 1000000 + " ms");
            Exmodifier.LOGGER.debug("ReadConfig Over Modifier config count: " + Foundmoconfigs.size());
            Exmodifier.LOGGER.debug("ReadConfig Over Modifier modifier count: " + modifierEntryMap.size());
        } catch (IOException e) {
            Exmodifier.LOGGER.error("Error reading configuration files", e);
        } catch (Exception e) {
            Exmodifier.LOGGER.error("Unexpected error during configuration reading", e);
        }
    }

    // 处理洗涤材料条目
    private static void processWashingMaterialEntry(Map.Entry<String, JsonElement> entry) {
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
            if (    jsonObject.has("OnlyHasWashEntry")&&
                    jsonObject.get("OnlyHasWashEntry").getAsBoolean()){
                materials.OnlyHasWashEntry = true;

            }
            if (    jsonObject.has("MinRandomTime")
            ){
                materials.MinRandomTime = jsonObject.get("MinRandomTime").getAsInt();

            }
            if (    jsonObject.has("MaxRandomTime")
            ){
                materials.MinRandomTime = jsonObject.get("MaxRandomTime").getAsInt();

            }
            if (jsonObject.has("OnlyItems")){
                JsonArray OnlyItems = jsonObject.get("OnlyItems").getAsJsonArray();
                OnlyItems.forEach(item -> {
                    materials.OnlyItems.add(item.getAsString());
                });
            }
            if (jsonObject.has("OnlyTypes")){
                JsonArray OnlyItems = jsonObject.get("OnlyTypes").getAsJsonArray();
                OnlyItems.forEach(item -> {
                    materials.OnlyTypes.add(ModifierEntry.StringToType(item.getAsString()));
                });
            }
            if (jsonObject.has("OnlyTags")){
                JsonArray OnlyItems = jsonObject.get("OnlyTags").getAsJsonArray();
                OnlyItems.forEach(item -> {
                    materials.OnlyTags.add(item.getAsString());
                });
            }
            materialsList.add(materials);
            Exmodifier.LOGGER.debug("WashingMaterials: " + materials.ItemId + " additionEntry: " + materials.additionEntry + " rarity: " + materials.rarity);
        } catch (Exception e) {
            Exmodifier.LOGGER.error("Error processing WashingMaterial entry: " + entry.getKey(), e);
        }
    }

    // 处理 moconfig 条目
    private static void processMoConfigEntries(MoConfig moconfig) throws FileNotFoundException {
        List<ModifierEntry> entries = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : moconfig.readEntrys()) {
            try {
                processModifierEntry(moconfig, entry, entries);
            } catch (Exception e) {
                Exmodifier.LOGGER.error("Error processing modifier entry: " + entry.getKey(), e);
            }
        }

        WeightedUtil<String> weightedUtil = new WeightedUtil<String>(
                entries.stream().collect(Collectors.toMap(ModifierEntry::getId, ModifierEntry::getWeight))
        );

        entries.forEach(entry -> {
            RegisterModifierEntry(entry);
            Exmodifier.LOGGER.debug(entry.id + " 出现概率 " + weightedUtil.getProbability(entry.id) * 100 + "%");
        });
        ExEntryRegistryEvent event = new ExEntryRegistryEvent(entries);
        MinecraftForge.EVENT_BUS.post(event);

        Exmodifier.LOGGER.debug("ReadConfig Over: Type: " + moconfig.type + " Path: " + moconfig.configFile + " entries: " + entries.size());
    }

    // 处理单个 Modifier 条目
    private static void processModifierEntry(MoConfig moconfig, Map.Entry<String, JsonElement> entry, List<ModifierEntry> entries) {
        JsonElement itemElement = entry.getValue();
        if (!itemElement.isJsonObject()) {
            return;
        }

        JsonObject itemObject = itemElement.getAsJsonObject();
        ModifierEntry modifierEntry = new ModifierEntry();
        modifierEntry.type = itemObject.has("type") ? ModifierEntry.StringToType(itemObject.get("type").getAsString()) : moconfig.type;
        if (!moconfig.CuriosType.isEmpty())modifierEntry.curiosType = moconfig.CuriosType;
        if (modifierEntry.type.toString().toUpperCase().startsWith("CURIOS")){
            modifierEntry.isCuriosEntry = true;
            if (itemObject.has("curiosType"))
            {
                modifierEntry.curiosType = itemObject.get("curiosType").getAsString();
            }else {
                if (moconfig.CuriosType.isEmpty()) modifierEntry.curiosType = "ALL";
            }
        }
        modifierEntry.id = modifierEntry.type.toString().substring(0, 2) + entry.getKey();
        modifierEntry.isRandom = itemObject.has("isRandom") && itemObject.get("isRandom").getAsBoolean();
        modifierEntry.OnlyHasThisEntry = itemObject.has("OnlyHasThisEntry") && itemObject.get("OnlyHasThisEntry").getAsBoolean();
        modifierEntry.RandomNum = itemObject.has("RandomNum") ? itemObject.get("RandomNum").getAsInt() : 0;
        modifierEntry.weight = itemObject.has("weight") ? itemObject.get("weight").getAsFloat() : 1.0f;
        modifierEntry.cantSelect = itemObject.has("cantSelect") && itemObject.get("cantSelect").getAsBoolean();

        if (!modifierEntry.isRandom) modifierEntry.RandomNum = 0;
        Exmodifier.LOGGER.debug(modifierEntry.id + " weight " + modifierEntry.weight);
        if (itemObject.has("OnlyItems")){
            JsonArray OnlyItems = itemObject.get("OnlyItems").getAsJsonArray();
            OnlyItems.forEach(item -> {
                modifierEntry.OnlyItems.add(item.getAsString());
            });
        }
        if (itemObject.has("OnlyTags")){
            JsonArray OnlyItems = itemObject.get("OnlyTags").getAsJsonArray();
            OnlyItems.forEach(item -> {
                modifierEntry.OnlyTags.add(item.getAsString());
            });
        }
        if (itemObject.has("OnlyWashItems")){
            JsonArray OnlyItems = itemObject.get("OnlyWashItems").getAsJsonArray();
            OnlyItems.forEach(item -> {
                modifierEntry.OnlyWashItems.add(item.getAsString());
            });
        }
        if (itemObject.has("attrGethers")) {
            processAttrGethers(moconfig, modifierEntry, itemObject.getAsJsonObject("attrGethers"));
        }

        Exmodifier.LOGGER.debug("ReadConfig: Type: " + moconfig.type + " Path: " + moconfig.configFile + " id: " + entry.getKey() + " attrGethers: " + modifierEntry.attriGether.size());
        entries.add(modifierEntry);
    }

    // 处理 attrGethers
    private static void processAttrGethers(MoConfig moconfig, ModifierEntry modifierEntry, JsonObject attrGethers) {
        int index = 0;
        for (Map.Entry<String, JsonElement> attrGetherEntry : attrGethers.entrySet()) {
            try {
                processAttrGether(moconfig, modifierEntry, attrGetherEntry,index);
                index++;
            } catch (Exception e) {
                Exmodifier.LOGGER.error("Error processing attrGether: " + attrGetherEntry.getKey(), e);
            }
        }
    }

    // 处理单个 attrGether 条目
    private static void processAttrGether(MoConfig moconfig, ModifierEntry modifierEntry, Map.Entry<String, JsonElement> attrGetherEntry,int index) {
        JsonObject attrGetherObj = attrGetherEntry.getValue().getAsJsonObject();
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attrGetherEntry.getKey()));
        double attrValue = attrGetherObj.get("value").getAsDouble();


//        if (attrGetherObj.has("autoUUID")){
//            if (attrGetherObj.get("autoUUID").getAsBoolean()) {
//                uuid = ExConfigHandle.autoUUid(ExConfigHandle.autoUUID);
//                ExConfigHandle.autoUUID++;
//            }
//        }
        AttributeModifier.Operation operation = ExConfigHandle.getOperation(attrGetherObj.get("operation").getAsString());
        EquipmentSlot slot = getEquipmentSlot(attrGetherObj);
        String modifierName = (attrGetherObj.has("modifierName")) ? attrGetherObj.get("modifierName").getAsString() :modifierEntry.id + index;;

        if (attrGetherObj.has("autoName")){
            if (attrGetherObj.has("autoName")) {
                if (attrGetherObj.get("autoName").getAsBoolean()) {
                    modifierName = modifierEntry.id + index;

                }
            }
        }
        UUID uuid = (attrGetherObj.has("uuid") && !attrGetherObj.get("uuid").getAsString().isEmpty()) ? UUID.fromString(attrGetherObj.get("uuid").getAsString()) : UUID.nameUUIDFromBytes(modifierName.getBytes());
        if(attrGetherObj.has("autoUUID") && attrGetherObj.get("autoUUID").getAsBoolean()) uuid = UUID.nameUUIDFromBytes(modifierName.getBytes());
        //UUID uuid = ExConfigHandle.generateUUIDFromString(modifierName);
        Exmodifier.LOGGER.debug("uuid "+uuid);
        AttributeModifier modifier = new AttributeModifier(uuid, modifierName, attrValue, operation);
        ModifierAttriGether attrGether = new ModifierAttriGether(attribute, modifier, slot);
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

        Exmodifier.LOGGER.debug("Attribute: " + attribute + " key: " + attrGetherEntry.getKey());
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
    public static UUID getUUID(JsonObject attrGetherObj,ModifierEntry modifierEntry) {
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
