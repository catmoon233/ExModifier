package net.exmo.exmodifier.util;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.modifier.MoConfig;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    /**
     * 从给定的字符串生成一个UUID。
     *
     * @param inputString 要转换成UUID的字符串
     * @return 生成的UUID
     */
    public static UUID generateUUIDFromString(String inputString) {
        try {
            // 使用SHA-256算法获取MessageDigest实例
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // 计算字符串的SHA-256哈希值
            byte[] hashBytes = md.digest(inputString.getBytes(StandardCharsets.UTF_8));

            // 只使用哈希的一部分来创建UUID
            long mostSignificantBits = ((long) (hashBytes[0] & 0xFF) << 56)
                    | ((long) (hashBytes[1] & 0xFF) << 48)
                    | ((long) (hashBytes[2] & 0xFF) << 40)
                    | ((long) (hashBytes[3] & 0xFF) << 32)
                    | ((long) (hashBytes[4] & 0xFF) << 24)
                    | ((hashBytes[5] & 0xFF) << 16)
                    | ((hashBytes[6] & 0xFF) << 8)
                    | (hashBytes[7] & 0xFF);

            long leastSignificantBits = ((long) (hashBytes[8] & 0xFF) << 56)
                    | ((long) (hashBytes[9] & 0xFF) << 48)
                    | ((long) (hashBytes[10] & 0xFF) << 40)
                    | ((long) (hashBytes[11] & 0xFF) << 32)
                    | ((long) (hashBytes[12] & 0xFF) << 24)
                    | ((hashBytes[13] & 0xFF) << 16)
                    | ((hashBytes[14] & 0xFF) << 8)
                    | (hashBytes[15] & 0xFF);

            return new UUID(mostSignificantBits, leastSignificantBits);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
//    public InputStream genResourcePath(String resourcePath)
//    {
//       return getClass().getResourceAsStream(resourcePath);
//    }
    public  void copyResourceToFile(String resourcePath, String targetPath) {
        // 获取Minecraft实例目录
        File mcDir = FMLPaths.GAMEDIR.get().toFile();

        // 构建目标目录
        File targetDir = new File(mcDir, targetPath).getParentFile();

        // 创建目录
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw new IllegalStateException("Could not create directory: " + targetDir.getAbsolutePath());
        }

        // 构建目标文件
        File targetFile = new File(targetDir, new File(targetPath).getName());

        // 使用Java NIO来复制文件，简化代码
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy file from " + resourcePath + " to " + targetFile, e);
        }
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
                if (moconfig.readSetting("type")!=null){
                    moconfig.type = ModifierEntry.StringToType(moconfig.readSetting("type").getAsString());
                }
                if (moconfig.type == ModifierEntry.Type.CURIOS &&moconfig.readSetting("type").getAsString().length()>6)moconfig.CuriosType=moconfig.readSetting("type").getAsString().substring(7);
                moconfigs.add(moconfig);
                Exmodifier.LOGGER.debug("Found Config: Type:" + moconfig.type + " Path:" + moconfig.configFile+" "  +moconfig.type +" CuriosType:"+moconfig.CuriosType);
            }
            return moconfigs;
        }catch (Exception e){
            Exmodifier.LOGGER.Logger.error("Error while reading config file : not exists");
        }
        return new ArrayList<>();
    }
    public static UUID autoUUid(int number) {
        // 将整数转换为十六进制字符串
        StringBuilder hexNumber = new StringBuilder(Integer.toHexString(number));

        // 补齐到 8 位长度
        while (hexNumber.length() < 8) {
            hexNumber.insert(0, "0");
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
