package net.exmo.exmodifier.util;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.modifier.MoConfig;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class ExConfigHandle {
    public static int autoUUID = 0;
    public static int autoName = 0;
    public static enum type {
        Operation,
        Attribute,
        Modifier,
        EquipmentSlot,
        Name,
        UUID
    }
    public static Map<type,Map<String,Object>> valueByKey = new java.util.HashMap<>();
    static {
        valueByKey.put(type.Operation, Map.of(
                "add", AttributeModifier.Operation.ADDITION,
                "multiply", AttributeModifier.Operation.MULTIPLY_TOTAL,
                "multiply_base", AttributeModifier.Operation.MULTIPLY_BASE,
                "multiply_total", AttributeModifier.Operation.MULTIPLY_TOTAL
        ));
        valueByKey.put(type.EquipmentSlot, Map.of(
                "mainhand", EquipmentSlot.MAINHAND,
                "offhand", EquipmentSlot.OFFHAND,
                "head", EquipmentSlot.HEAD,
                "chest", EquipmentSlot.CHEST,
                "legs", EquipmentSlot.LEGS,
                "feet", EquipmentSlot.FEET

        ));
    }
    public static EquipmentSlot getEquipmentSlot(String key){
        return (EquipmentSlot) valueByKey.get(type.EquipmentSlot).get(key);
    }
    public static AttributeModifier.Operation getOperation(String key){
        return (AttributeModifier.Operation) valueByKey.get(type.Operation).get(key);
    }
    public static List<MoConfig> listFiles(Path directory) throws IOException {
        try (Stream<Path> paths = Files.walk(directory)) {
            List<Path> files = paths.filter(Files::isRegularFile).toList();
            List<MoConfig> moconfigs = new ArrayList<>();
            for (Path file : files){
                MoConfig moconfig = new MoConfig(file);
                if (moconfig.readSetting("type")!=null)moconfig.type = ModifierEntry.StringToType(moconfig.readSetting("type").getAsString());
                moconfigs.add(moconfig);
                Exmodifier.LOGGER.debug("Found Config: Type:" + moconfig.type + " Path:" + moconfig.configFile+" "  +moconfig.type );
            }
            return moconfigs;
        }catch (Exception e){
            Exmodifier.LOGGER.error("Error while reading config file : not exists");
        }
        return new ArrayList<>();
    }
    public static UUID autoUUid(int number) {
        // 将整数转换为十六进制字符串
        String hexNumber = Integer.toHexString(number);

        // 补齐到 8 位长度
        while (hexNumber.length() < 8) {
            hexNumber = "0" + hexNumber;
        }

        // 构建 UUID 字符串
        String uuidString = hexNumber.substring(0, 8) + "-" +
                "e89b" + "-" +
                "12d3" + "-" +
                "a456" + "-" +
                "426614174001".substring(0, 12);
        Exmodifier.LOGGER.debug("autoUUid: " + uuidString);

        return UUID.fromString(uuidString);

    }
}
