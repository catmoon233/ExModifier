package net.exmo.exmodifier.content.modifier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.util.ItemAttrUtil;
import net.exmo.exmodifier.util.WeightedUtil;
import net.exmo.exmodifier.util.exconfigHandle;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.jline.utils.Log;
import org.spongepowered.asm.mixin.Final;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.minecraft.world.item.ItemStack.ATTRIBUTE_MODIFIER_FORMAT;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModifierHandle {
    public static List<ModifierEntry> getEntrysFromItemStack(ItemStack stack) {
        List<ModifierEntry> modifierEntries = new ArrayList<>();

        for (ModifierEntry modifierAttriGether : modifierEntryMap.values()) {
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
    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) throws IOException {
        modifierEntryMap = new HashMap<>();
        readConfig();
    }
    public static List<String> percentAtr = new ArrayList<>();
    static {
        percentAtr = List.of(
                "twtp.mianshang",
                "apotheosis:crit_chance",
                "apotheosis:crit_damage",
                "irons_spellbooks:fire_spell_power",
                "irons_spellbooks:cast_time_reduction",
                "irons_spellbooks:cooldown_reduction",
                "irons_spellbooks:ice_spell_power",
                "irons_spellbooks:lightning_spell_power",
                "irons_spellbooks:holy_spell_power",
                "irons_spellbooks:evocation_spell_power",
                "irons_spellbooks:poison_spell_power",
                "irons_spellbooks:spell_power",
                "irons_spellbooks:blood_spell_power",
                "irons_spellbooks:ender_spell_power"


                );
    }
    @Mod.EventBusSubscriber
    public static   class CommonEvent{
        public static List<Component> generateEntryTooltip(ModifierEntry modifierEntry,Player player) {
            List<Component> tooltips = new ArrayList<>();
            String id = modifierEntry.getId();
            if (id.length() >= 2) {
                tooltips.add(new TranslatableComponent("modifiler.entry." + id.substring(2)).append(" : "));
                for (ModifierAttriGether modifierAttriGether : modifierEntry.attriGether) {
                    AttributeModifier attributemodifier = modifierAttriGether.getModifier();
                    Attribute attribute = modifierAttriGether.getAttribute();
                    if (attribute == null)continue;
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
                        DecimalFormat df = new DecimalFormat("#.##");
                        amouta2 = df.format(attributemodifier.getAmount()*100);
                    }

                    if (flag) {
                        tooltips.add((new TextComponent(" ")).append(new TranslatableComponent("attribute.modifier.equals." + attributemodifier.getOperation().toValue(), new Object[]{ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslatableComponent(attribute.getDescriptionId())})).withStyle(ChatFormatting.DARK_GREEN));
                    } else if (d0 > 0.0) {
                        if (percent.equals("%")) tooltips.add(new TranslatableComponent("add").append("").append(amouta2).append(percent).append(" ").append(new TranslatableComponent(attribute.getDescriptionId())).withStyle(ChatFormatting.BLUE));
                        else tooltips.add((new TranslatableComponent("attribute.modifier.plus." + attributemodifier.getOperation().toValue(), new Object[]{ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslatableComponent(attribute.getDescriptionId())})).withStyle(ChatFormatting.BLUE));
                    } else if (d0 < 0.0) {
                        d1 *= -1.0;
                        if (percent.equals("%")) tooltips.add(new TranslatableComponent("subtract").append("").append(amouta2).append(percent).append(" ").append(new TranslatableComponent(attribute.getDescriptionId())).withStyle(ChatFormatting.RED));
                         else  tooltips.add((new TranslatableComponent("attribute.modifier.take." + attributemodifier.getOperation().toValue(), new Object[]{ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslatableComponent(attribute.getDescriptionId())})).withStyle(ChatFormatting.RED));
                    }
                }
            }
            return tooltips;
        }
        @SubscribeEvent
        public static void ItemTooltip(ItemTooltipEvent event) {

            ItemStack stack = event.getItemStack();
//            for (Component tooltip : event.getToolTip()){
//                Exmodifier.LOGGER.debug("tooltip: " + tooltip);
//            }
//            Exmodifier.LOGGER.debug("------------------------------------");
            if (stack.getOrCreateTag().getInt("exmodifier_armor_modifier_applied") > 0) {
                event.getToolTip().add(new TranslatableComponent("null"));
                if (stack.getOrCreateTag().getString("exmodifier_armor_modifier_applied0").equals("UNKNOWN")) {
                    event.getToolTip().add(new TranslatableComponent("modifiler.entry.UNKNOWN"));
                }else {
                    for (ModifierEntry modifierEntry : getEntrysFromItemStack(stack)) {
                        event.getToolTip().addAll(generateEntryTooltip(modifierEntry, event.getPlayer()));
                        event.getToolTip().add(new TranslatableComponent("null"));
                    }
                }
//                String applied0 = stack.getOrCreateTag().getString("exmodifier_armor_modifier_applied0");
//                String applied1 = stack.getOrCreateTag().getString("exmodifier_armor_modifier_applied1");
//
//                if (applied0.equals("UNKNOWN")) {
//                    event.getToolTip().set(0, new TranslatableComponent("modifiler.entry.UNKNOWN")
//                            .append("\u00a7r ")
//                            .append(event.getToolTip().get(0)));
//                } else {
//                    // 确保字符串长度足够长
//                    if (applied0.length() >= 2 && applied1.length() >= 2) {
//                        event.getToolTip().set(0, new TranslatableComponent("modifiler.entry." + applied0.substring(2))
//                                .append("\u00a7r ")
//                                .append(new TranslatableComponent("modifiler.entry." + applied1.substring(2)))
//                                .append(" \u00a7r")
//                                .append(event.getToolTip().get(0)));
//                    } else {
//                        // 如果字符串太短，则提供默认行为或其他逻辑
//                        // 可以选择直接使用原始字符串
//                        event.getToolTip().set(0, new TranslatableComponent("modifiler.entry." + applied0)
//                                .append("\u00a7r ")
//                                .append(new TranslatableComponent("modifiler.entry." + applied1))
//                                .append(" \u00a7r")
//                                .append(event.getToolTip().get(0)));
//                    }
//                }
            }
        }
        public static void RandomEntry(ItemStack stack, WeightedUtil weightedUtil, EquipmentSlot slot, int refreshments) {
            int numAddedModifiers = 0;

            List<ModifierAttriGether> finalAttriGethers = new ArrayList<>();
            Set<String> appliedModifiers = new HashSet<>();
            List<ModifierEntry> modifierEntries = new ArrayList<>();

            while (numAddedModifiers < refreshments) {
                ModifierEntry modifierEntry = modifierEntryMap.get(weightedUtil.selectRandomKeyBasedOnWeights());

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

                    List<ModifierAttriGether> attriGethers = selectModifierAttributes(modifierEntry);
                    finalAttriGethers.addAll(attriGethers);
                }
            }

            applyModifiers(stack, finalAttriGethers, slot);
        }

        private static List<ModifierAttriGether> selectModifierAttributes(ModifierEntry modifierEntry) {
            List<ModifierAttriGether> attriGethers = new ArrayList<>();

            if (modifierEntry.isRandom && modifierEntry.RandomNum > 0) {
                int remainingRandoms = modifierEntry.RandomNum;

                List<ModifierAttriGether> weightedAttriGethers = modifierEntry.attriGether.stream()
                        .filter(attriGether -> attriGether.weight > 0)
                        .collect(Collectors.toList());

                WeightedUtil weightedUtil1 = new WeightedUtil(weightedAttriGethers.stream()
                        .collect(Collectors.toMap(attriGether -> attriGether.getModifier().getId().toString(), attriGether -> attriGether.weight)));

                while (remainingRandoms > 0) {
                    ModifierAttriGether randomAttriGether = weightedAttriGethers.stream()
                            .filter(attriGether -> attriGether.getModifier().getId().toString().equals(weightedUtil1.selectRandomKeyBasedOnWeights()))
                            .findFirst().orElse(null);

                    if (randomAttriGether != null && !attriGethers.contains(randomAttriGether)) {
                        attriGethers.add(randomAttriGether);
                        remainingRandoms--;
                    }
                }
            } else {
                attriGethers.addAll(modifierEntry.attriGether.stream()
                        .filter(attriGether -> !attriGether.isRandom)
                        .collect(Collectors.toList()));
            }

            return attriGethers;
        }

        private static void applyModifiers(ItemStack stack, List<ModifierAttriGether> attriGethers, EquipmentSlot slot) {
            for (ModifierAttriGether attriGether : attriGethers) {
                EquipmentSlot applicableSlot = attriGether.IsAutoEquipmentSlot ? slot : attriGether.slot;

                if (ForgeRegistries.ATTRIBUTES.containsValue(attriGether.attribute)) {
                    ItemAttrUtil.addItemAttributeModifier2(stack, attriGether.attribute, attriGether.modifier, applicableSlot);
                } else {
                    Exmodifier.LOGGER.debug("attribute is not exists");
                }
            }
        }

        public static void RandomEntry(ItemStack stack, int rarity, int refreshnumber) {
            if (stack.getOrCreateTag().getInt("exmodifier_armor_modifier_applied") > 0) {
                return;
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
                    WeightedUtil weightedUtil = new WeightedUtil(
                            modifierEntryMap.entrySet().stream()
                                    .filter(e -> e.getValue().type == type)
                                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().weight))
                    );

                    if (!weightedUtil.weights.isEmpty()) {
                        weightedUtil.increaseWeightsByRarity(rarity);
                        if (entry.getKey() == ModifierEntry.Type.ARMOR) {  // ARMOR type, set the slot based on the item
                            slot = ((ArmorItem) stack.getItem()).getSlot();
                        }
                        RandomEntry(stack, weightedUtil, slot, refreshnumber);
                        stack.getOrCreateTag().putInt("exmodifier_armor_modifier_applied",
                                stack.getOrCreateTag().getInt("exmodifier_armor_modifier_applied") + 1);
                        over = true;
                    }
                }
            }
        }

        private static boolean isValidForType(ItemStack stack, ModifierEntry.Type type) {
            if (type == ModifierEntry.Type.HELMET) return hasHelmetConfig && stack.getItem() instanceof ArmorItem && ((ArmorItem) stack.getItem()).getSlot() == EquipmentSlot.HEAD;
            if (type == ModifierEntry.Type.CHESTPLATE) return hasChestConfig && stack.getItem() instanceof ArmorItem && ((ArmorItem) stack.getItem()).getSlot() == EquipmentSlot.CHEST;
            if (type == ModifierEntry.Type.BOOTS) return hasBootsConfig && stack.getItem() instanceof ArmorItem && ((ArmorItem) stack.getItem()).getSlot() == EquipmentSlot.FEET;
            if (type == ModifierEntry.Type.LEGGINGS) return hasLeggingsConfig && stack.getItem() instanceof ArmorItem && ((ArmorItem) stack.getItem()).getSlot() == EquipmentSlot.LEGS;
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

            List<ModifierEntry> hasAttriGether = new ArrayList<>();
            for (ModifierEntry modifierAttriGether : modifierEntryMap.values()) {
                String id;
                for (int i = 0; true; i++) {
                    id = stack.getOrCreateTag().getString("exmodifier_armor_modifier_applied"+i);
                    if (id.isEmpty())break;
                    if (id.equals(modifierAttriGether.getId())) {
                        hasAttriGether.add(modifierAttriGether);
                        for (ModifierAttriGether modifierAttriGether1 : modifierAttriGether.attriGether) {
                            ItemAttrUtil.removeAttributeModifier(stack, modifierAttriGether1.getAttribute(), modifierAttriGether1.getModifier(), modifierAttriGether1.slot);
                        }
                        stack.getOrCreateTag().putString("exmodifier_armor_modifier_applied"+i,"");
                    }

                }
            }
        }
        @SubscribeEvent
        public static void armorChange(LivingEquipmentChangeEvent event){
            if (event.getEntity().level.isClientSide)return;
            if (event.getEntity() instanceof Player player){
            ItemStack stack = event.getTo();
            RandomEntry(stack,0,2);
                if (stack.getOrCreateTag().getBoolean("modifier_refresh")){
                    stack.getOrCreateTag().putString("exmodifier_armor_modifier_applied0","");
                    stack.getOrCreateTag().putString("exmodifier_armor_modifier_applied1","");
                    stack.getOrCreateTag().putString("exmodifier_armor_modifier_applied2","");
                    stack.getOrCreateTag().putBoolean("modifier_refresh", false);
                    stack.getOrCreateTag().putInt("exmodifier_armor_modifier_applied", 0);

                    ModifierHandle.CommonEvent.RandomEntry(stack, stack.getOrCreateTag().getInt("modifier_refresh_rarity"), stack.getOrCreateTag().getInt("modifier_refresh_add"));
                }
        }
        }
        @SubscribeEvent
        public static void atReload(AddReloadListenerEvent event) throws IOException {
            modifierEntryMap = new HashMap<>();
            readConfig();
        }

    }
    public static boolean hasHelmetConfig =false;
    public static boolean hasChestConfig = false;
    public static boolean hasLeggingsConfig = false;
    public static boolean hasBootsConfig = false;
    public static boolean hasSwordConfig = false;
    public static final Path WashingMaterialsConfigPath = FMLPaths.GAMEDIR.get().resolve("config/exmo/WashingMaterials.json");
    public static final Path ConfigPath = FMLPaths.GAMEDIR.get().resolve("config/exmo/modifier");
    public static List<moconfig> Foundmoconfigs = new ArrayList<>();
    public static List<WashingMaterials> materialsList = new ArrayList<>();
    public static Map<String,ModifierEntry> modifierEntryMap = new HashMap<>();
    public static void RegisterModifierEntry(ModifierEntry modifierEntry){
        if (!hasBootsConfig)if (modifierEntry.type == ModifierEntry.Type.BOOTS)hasBootsConfig=true;
        if (!hasLeggingsConfig)if (modifierEntry.type == ModifierEntry.Type.LEGGINGS)hasLeggingsConfig=true;
        if (!hasChestConfig)if (modifierEntry.type == ModifierEntry.Type.CHESTPLATE)hasChestConfig=true;
        if (!hasHelmetConfig)if (modifierEntry.type == ModifierEntry.Type.HELMET)hasHelmetConfig=true;
        if (!hasSwordConfig)if (modifierEntry.type == ModifierEntry.Type.SWORD)hasSwordConfig=true;
        modifierEntryMap.put(modifierEntry.type.toString().substring(0,2)+modifierEntry.id,modifierEntry);
        Exmodifier.LOGGER.debug("RegisterModifierEntry: Type:" + modifierEntry.type + " Target:" + modifierEntry.id  );
    }
    public static void listFiles(Path directory) throws IOException {
        try (Stream<Path> paths = Files.walk(directory)) {
            List<Path> files = paths.filter(Files::isRegularFile).toList();
            List<moconfig> moconfigs = new ArrayList<>();
            for (Path file : files){
                moconfig moconfig = new moconfig(file);
                if (moconfig.readSetting("type")!=null)moconfig.type = ModifierEntry.StringToType(moconfig.readSetting("type").getAsString());
                moconfigs.add(moconfig);
                Exmodifier.LOGGER.debug("Found Config: Type:" + moconfig.type + " Path:" + moconfig.configFile+" "  +moconfig.type );
            }
           Foundmoconfigs = moconfigs;
        }catch (Exception e){
            Exmodifier.LOGGER.error("Error while reading config file : not exists");
        }
    }
    public static void readConfig() throws IOException {
        for (Attribute attribute : ForgeRegistries.ATTRIBUTES) {
            Exmodifier.LOGGER.debug("Attribute: " + ForgeRegistries.ATTRIBUTES.getKey(attribute));

        }
        if (Files.exists(WashingMaterialsConfigPath)) {
            materialsList = new ArrayList<>();
            moconfig washingMaterialsConfig = new moconfig(WashingMaterialsConfigPath);

            for (Map.Entry<String, JsonElement> entry : washingMaterialsConfig.readEntrys()) {
                if (!entry.getValue().isJsonObject()) {
                    continue;
                }
                WashingMaterials materials;
                materials = new WashingMaterials(entry.getKey(), entry.getValue().getAsJsonObject().get("additionEntry").getAsInt(), ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry.getKey())), entry.getValue().getAsJsonObject().get("rarity").getAsInt());
                materialsList.add(materials);
                Exmodifier.LOGGER.debug("WashingMaterials: " + materials.ItemId + " additionEntry:" + materials.additionEntry + " rarity:" + materials.rarity);
            }
        }
        listFiles(ConfigPath);
        for (moconfig moconfig : Foundmoconfigs){
            List<ModifierEntry> entries = new ArrayList<>();
            for (Map.Entry<String, JsonElement> entry : moconfig.readEntrys()){
                String itemName = entry.getKey();
                JsonElement itemElement = entry.getValue();
                if (!itemElement.isJsonObject()) {
                    continue;
                }

                JsonObject itemObject = entry.getValue().getAsJsonObject();
                ModifierEntry modifierEntry = new ModifierEntry();

                modifierEntry.type = itemObject.has("type") ? ModifierEntry.StringToType(itemObject.get("type").getAsString()) : moconfig.type;
                modifierEntry.id = modifierEntry.type.toString().substring(0,2)+ entry.getKey();
                modifierEntry.isRandom = itemObject.has("isRandom") && itemObject.get("isRandom").getAsBoolean();
                modifierEntry.RandomNum = itemObject.has("Randomnum") ? itemObject.get("Randomnum").getAsInt() : 0;
                modifierEntry.weight = itemObject.has("weight") ? itemObject.get("weight").getAsFloat() : 1.0f;
                Exmodifier.LOGGER.debug(modifierEntry.id+" weight "+modifierEntry.weight);
                if (itemObject.has("attrGethers")) {
                    JsonObject attrGethers = itemObject.getAsJsonObject("attrGethers");
                    for (Map.Entry<String, JsonElement> attrGetherEntry : attrGethers.entrySet()) {
                        JsonObject attrGetherObj = attrGetherEntry.getValue().getAsJsonObject();
                        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attrGetherEntry.getKey()));
                        Exmodifier.LOGGER.debug("Attribute: " + attribute + " key: "+ attrGetherEntry.getKey());
                        double attrValue = attrGetherObj.get("value").getAsDouble();
                        if (attrGetherObj.has("uuid")) {
                        }
                        String modifierName = attrGetherObj.get("modifierName").getAsString();
                        String slotStr =null;

                        if (attrGetherObj.has("slot")){
                         slotStr = attrGetherObj.get("slot").getAsString();
                        }
                        UUID uuid = null;

                        if (attrGetherObj.has("autoUUID")){
                            if (attrGetherObj.get("autoUUID").getAsBoolean()){
                                uuid = exconfigHandle.autoUUid(exconfigHandle.autoUUID);
                            }
                        }else {
                            if (attrGetherObj.has("uuid")&& !attrGetherObj.get("uuid").getAsString().isEmpty()) {
                                uuid = UUID.fromString(attrGetherObj.get("uuid").getAsString());
                            }else {
                                uuid = exconfigHandle.autoUUid(exconfigHandle.autoUUID);
                            }
                        }
                        AttributeModifier.Operation operation = exconfigHandle.getOperation(attrGetherObj.get("operation").getAsString());
                        EquipmentSlot slot =null;
                        if (slotStr!=null&&!slotStr.equals("auto")) slot = exconfigHandle.getEquipmentSlot(slotStr);
                        AttributeModifier modifier = new AttributeModifier(uuid, modifierName, attrValue, operation);
                        ModifierAttriGether attrGether = new ModifierAttriGether(attribute, modifier, slot);
                        if (slotStr.equals("auto"))attrGether.IsAutoEquipmentSlot = true;
                        if (attrGethers.has("isAutoEquipmentSlot")){
                            attrGether.IsAutoEquipmentSlot = attrGethers.get("isAutoEquipmentSlot").getAsBoolean();
                        }
                        if (!attrGetherObj.has("uuid"))attrGether.hasUUID =false;
                        if (attrGetherObj.has("weight")){
                            attrGether.weight = attrGetherObj.get("weight").getAsFloat();
                        }
                        if (attrGetherObj.has("isRandom")){
                            attrGether.isRandom = attrGetherObj.get("isRandom").getAsBoolean();
                        }
                        if (attrGetherObj.has("type")){
                            modifierEntry.type = ModifierEntry.Type.valueOf(attrGetherObj.get("type").getAsString());
                        }else {
                            if (moconfig.type!= ModifierEntry.Type.UNKNOWN){
                                modifierEntry.type = moconfig.type;
                            }
                        }
                        Exmodifier.LOGGER.debug("ReadConfig: Type:" + moconfig.type + " Path:" + moconfig.configFile + " id:" + itemName + " attrGethers:" + modifierEntry.attriGether.size());
                        modifierEntry.attriGether.add(attrGether);

                        exconfigHandle.autoUUID++;
                    }
                }
                Exmodifier.LOGGER.debug("ReadConfig: Type:" + moconfig.type + " Path:" + moconfig.configFile + " id:" + itemName + " entries:" + entries.size());
                entries.add(modifierEntry);
            }
            Exmodifier.LOGGER.debug("ReadConfig Over: Type:" + moconfig.type + " Path:" + moconfig.configFile + " entries:" + entries.size());
            WeightedUtil weightedUtil = new WeightedUtil(entries.stream().collect(Collectors.toMap(ModifierEntry::getId, ModifierEntry::getWeight)));
            for (ModifierEntry entry : entries){
                RegisterModifierEntry(entry);
                Exmodifier.LOGGER.debug(entry.id + " 出现概率 " +weightedUtil.getProbability(entry.id)*100+"%");

            }

        }
        Exmodifier.LOGGER.debug("ReadConfig Over config count:"+Foundmoconfigs.size() );
        Exmodifier.LOGGER.debug("ReadConfig Over modifier count:"+modifierEntryMap.size() );
    }
}
