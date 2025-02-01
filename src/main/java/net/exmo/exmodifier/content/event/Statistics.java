package net.exmo.exmodifier.content.event;

import net.exmo.exmodifier.config;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber
public class Statistics {
    public static final String ingotPath = FMLPaths.CONFIGDIR.get().resolve("exmo/ingots.txt").toString();
    public static final String attackablesPath = FMLPaths.CONFIGDIR.get().resolve("exmo/attackables.txt").toString();
    public static final String armorsPath = FMLPaths.CONFIGDIR.get().resolve("exmo/armors.txt").toString();
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!config.Statistics)return;
        if (!event.getEntity().level().isClientSide)return;
        Map<String, String> ingots = new HashMap<>();
        Map<String, List<Component>> armors = new HashMap<>();
        Map<String, List<Component>> attackables = new HashMap<>();
        ForgeRegistries.ITEMS.getEntries().forEach(
                v -> {
                    Item value = v.getValue();
                    String key = I18n.get(value.getDescriptionId()) ;
                    if (key.contains("锭") || key.contains("ingot") || key.contains("Ingot") ||v.getKey().location().toString().contains("ingot")) {
                        ingots.put(key, v.getKey().toString());
                    }
                    ItemStack defaultInstance = value.getDefaultInstance();
                    double sum = defaultInstance.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE).stream()
                            .mapToDouble(AttributeModifier::getAmount).sum();
                    if (sum >0){
                        attackables.put(key + " | "+ v.getKey().toString()+" | 攻击伤害:"+sum+" | 攻击速度:"+ defaultInstance.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_SPEED).stream()
                                .mapToDouble(AttributeModifier::getAmount).sum(), defaultInstance.getTooltipLines(event.getEntity(), TooltipFlag.ADVANCED));
                    }
                    if (value instanceof ArmorItem){
                        armors.put(key + " | "+ v.getKey().toString(), defaultInstance.getTooltipLines(event.getEntity(), TooltipFlag.ADVANCED));
                    }
                }
        );
        write2(attackables, attackablesPath);
        write2(armors, armorsPath);
        write(ingots,ingotPath);

    }

    private static void write2(Map<String, List<Component>> armors, String armorsPath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(armorsPath))) {
            for (Map.Entry<String, List<Component>> entry : armors.entrySet()) {
                writer.write(entry.getKey());
                writer.newLine();
                for (Component component : entry.getValue()){
                    writer.write(component.getString());
                    writer.newLine();
                }
                writer.newLine();
                writer.write("--------------------------------------------");
                writer.newLine();
            }
            System.out.println("Ingot data saved to " + armorsPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void write(Map<String, String> toWrite, String toWritePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(toWritePath))) {
            for (Map.Entry<String, String> entry : toWrite.entrySet()) {
                writer.write(entry.getKey() + " | " + entry.getValue());
                writer.newLine();
            }
            System.out.println("Ingot data saved to " + toWritePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
