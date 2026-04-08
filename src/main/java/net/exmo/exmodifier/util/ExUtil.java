package net.exmo.exmodifier.util;

import com.google.common.base.CaseFormat;
import net.exmo.exmodifier.content.dynamicAttributes.DynamicAttribute;
import net.exmo.exmodifier.content.dynamicAttributes.DynamicAttributeRegister;
import net.exmo.exmodifier.content.helper.ModifierEntryHelper;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.specialEffects.SpecialEffect;
import net.exmo.exmodifier.content.specialEffects.SpecialEffectHandle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ExUtil {
    public static List<SpecialEffect> getSpecialModifierEntries(LivingEntity entity) {
        var specialEffects = new java.util.ArrayList<SpecialEffect>();
        for (var eq : EquipmentSlot.values()) {
            var item = entity.getItemBySlot(eq);
            specialEffects.addAll(ModifierEntryHelper.of(item).getModifierEntriesB().stream()
                    .flatMap(modifierEntry -> modifierEntry.specialTags.stream())
                    .map(SpecialEffectHandle::getSpecialEffect)
                    .toList());
        }
        return specialEffects;
    }

    public static boolean hasSpecialEffect(SpecialEffect effect, Player player){
        for (var eq : player.getInventory().armor){
            for (ModifierEntry modifierEntry : ModifierEntryHelper.of(eq).getModifierEntriesB()) {
                if (modifierEntry.hasSpecialEffect(effect)) {
                    return  true;
                }
            }
        }
        return false;
    }
    public static boolean hasSpecialEffect(String effect, Player player){
        for (var eq : player.getInventory().armor){
            for (ModifierEntry modifierEntry : ModifierEntryHelper.of(eq).getModifierEntriesB()) {
                if (modifierEntry.hasSpecialEffect(effect)) {
                    return  true;
                }
            }
        }
        return false;
    }
    public static boolean ifHasSpecialEffect(SpecialEffect effect, Player player, Consumer<ModifierEntry> consumer){
        for (var eq : player.getInventory().armor){
            for (ModifierEntry modifierEntry : ModifierEntryHelper.of(eq).getModifierEntriesB()) {
                if (modifierEntry.hasSpecialEffect(effect)) {
                    consumer.accept(modifierEntry);
                    return  true;
                }
            }
        }
        return false;
    }
    public static boolean ifHasSpecialEffect(String effect, Player player, Consumer<ModifierEntry> consumer){
        for (var eq : player.getInventory().armor){
            for (ModifierEntry modifierEntry : ModifierEntryHelper.of(eq).getModifierEntriesB()) {
                if (modifierEntry.hasSpecialEffect(effect)) {
                    consumer.accept(modifierEntry);
                    return  true;
                }
            }
        }
        return false;
    }
    public static boolean ifHasSpecialEffectAll(String effect, Player player, Consumer<ModifierEntry> consumer){
        for (var eq : EquipmentSlot.values()){
            var item = player.getItemBySlot(eq);
            for (ModifierEntry modifierEntry : ModifierEntryHelper.of(item).getModifierEntriesB()) {
                if (modifierEntry.hasSpecialEffect(effect)) {
                    consumer.accept(modifierEntry);
                    return  true;
                }
            }
        }
        return false;
    }
    public static boolean hasSpecialEffect(SpecialEffect effect, Player player, EquipmentSlot equipmentSlot){

            for (ModifierEntry modifierEntry : ModifierEntryHelper.of(player.getItemBySlot(equipmentSlot)).getModifierEntriesB()) {
                if (modifierEntry.hasSpecialEffect(effect)) {
                    return  true;
                }
            }

        return false;
    }
    public static boolean hasSpecialEffect(String effect, Player player, EquipmentSlot equipmentSlot){

            for (ModifierEntry modifierEntry : ModifierEntryHelper.of(player.getItemBySlot(equipmentSlot)).getModifierEntriesB()) {
                if (modifierEntry.hasSpecialEffect(effect)) {
                    return  true;
                }
            }

        return false;
    }
    public static boolean ifHasSpecialEffect(SpecialEffect effect, Player player, EquipmentSlot equipmentSlot,Consumer<ModifierEntry> consumer){

            for (ModifierEntry modifierEntry : ModifierEntryHelper.of(player.getItemBySlot(equipmentSlot)).getModifierEntriesB()) {
                if (modifierEntry.hasSpecialEffect(effect)) {
                    consumer.accept(modifierEntry);
                    return  true;
                }
            }

        return false;
    }
    public static boolean ifHasSpecialEffect(SpecialEffect effect, Player player,Consumer<ModifierEntry> consumer, EquipmentSlot... equipmentSlot){

        for (var e : equipmentSlot) {
            for (ModifierEntry modifierEntry : ModifierEntryHelper.of(player.getItemBySlot(e)).getModifierEntriesB()) {
                if (modifierEntry.hasSpecialEffect(effect)) {
                    consumer.accept(modifierEntry);
                    return true;
                }
            }
        }

        return false;
    }
    public static boolean ifHasSpecialEffect(String effect, Player player, EquipmentSlot equipmentSlot,Consumer<ModifierEntry> consumer){

            for (ModifierEntry modifierEntry : ModifierEntryHelper.of(player.getItemBySlot(equipmentSlot)).getModifierEntriesB()) {
                if (modifierEntry.hasSpecialEffect(effect)) {
                    consumer.accept(modifierEntry);
                    return  true;
                }
            }

        return false;
    }


    static HashMap<ResourceLocation, TagKey<ModifierEntry>> _IT = new HashMap<>();
    public static TagKey<ModifierEntry> createOrGetModifierTagKey(ResourceLocation key) {
        if (_IT.containsKey(key)) {
            return _IT.get(key);
        } else {
            TagKey<ModifierEntry> tagKey = TagKey.create(ModifierEntry.MODIFIER_KEY, key);
            _IT.put(key, tagKey);
            return tagKey;
        }
    }
    public static String classToString(Class<?> clazz) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, clazz.getSimpleName());
    }
    static HashMap<Item, String> _IS = new HashMap<>();

    public static String getItemID(Item item) {
        if (_IS.containsKey(item)) {
            return _IS.get(item);
        } else {
            String id = ForgeRegistries.ITEMS.getKey(item).toString();
            _IS.put(item, id);
            return id;
        }

    }

    public static String getItemID(RegistryObject<Item> registryObject) {
        return registryObject.getKey().toString();

    }
    public static String getItemID(ItemStack stack) {
        return getItemID(stack.getItem());

    }

    static Map<Attribute,String> _AS = new HashMap<>();
    public static String getAttributeID(Attribute attribute){
        if (attribute == null) return "";
        if (attribute instanceof DynamicAttribute dynamicAttribute)
        if (DynamicAttributeRegister.dynamicAttributes.containsValue(dynamicAttribute)) return dynamicAttribute.attributeId();
        if (_AS.containsKey(attribute)){
            return _AS.get(attribute);
        }else
        {
            String string = ForgeRegistries.ATTRIBUTES.getKey(attribute).toString();
            _AS.put(attribute, string);
            return string;
        }

    }


    public static Attribute getAttribute(String attribute){
        return _AS.values().stream().filter(s -> s.equals(attribute)).findFirst().map(s -> _AS.entrySet().stream().filter(entry -> entry.getValue().equals(s)).findFirst().get().getKey()).orElse(null);
    }


    public static String getAttributeID(RegistryObject<Attribute> attribute){
        return getAttributeID(attribute.get());
    }
}

