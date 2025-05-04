package net.exmo.exmodifier.util;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;

public class ColorUtil {
    public static int toColor(String color){
        return Color.getColor(color).getRGB();
    }
    public static int getColorRGB(int red, int green, int blue){
        return new Color(red, green, blue).getRGB();
    }
    public static int getAverageColor(TextureAtlasSprite sprite) {
        try {
            // Create a BufferedImage for drawing
            int width = 16;
            int height = 16;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            // Populate the BufferedImage with pixel data from the TextureAtlasSprite
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    // Retrieve the pixel color (RGBA format)
                    int color =fixColor( sprite.getPixelRGBA(1,x, y));
                    String hex = Integer.toHexString(color);
                    String rgba = intToRGBA(color);
                    // Ensure you're using the correct x, y
                    image.setRGB(x, y, color);
                }
            }

            // Calculate average color
            AtomicInteger totalR = new AtomicInteger(0);
            AtomicInteger totalG = new AtomicInteger(0);
            AtomicInteger totalB = new AtomicInteger(0);
            AtomicInteger count = new AtomicInteger(0);

            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    int pixel = image.getRGB(x, y);
                    if ((pixel & 0xFF000000) != 0) { // Check if the pixel is not transparent
                        totalR.addAndGet((pixel >> 16) & 0xFF);
                        totalG.addAndGet((pixel >> 8) & 0xFF);
                        totalB.addAndGet(pixel & 0xFF);
                        count.incrementAndGet();
                    }
                }
            }

            // Prevent division by zero
            if (count.get() == 0) return 0;

            int r = totalR.get() / count.get();
            int g = totalG.get() / count.get();
            int b = totalB.get() / count.get();

            // Return the average color as an RGB int
            return (r << 16) | (g << 8) | b;

        } catch (Exception e) {
            e.printStackTrace();
            return 0; // Fallback color in case of error
        }
    }

    /**
     * 将整数颜色值转换为标准的ARGB格式。
     *
     * @param color 整数形式的颜色值
     * @return ARGB格式的颜色值
     */
    public static int fixColor(int color) {
        // 提取原始的R, G, B, A分量
        int originalRed = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        int alpha = (color >> 24) & 0xFF;

        // 交换R和B的位置以形成正确的ARGB颜色值
        return (alpha << 24) | (blue << 16) | (green << 8) | originalRed;
    }

    /**
     * 将整数颜色值转换为RGBA字符串表示。
     *
     * @param color 整数形式的颜色值
     * @return RGBA字符串表示
     */
    public static String intToRGBA(int color) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        int alpha = (color >> 24) & 0xFF;

        return String.format("rgba(%d, %d, %d, %d)", red, green, blue, alpha);
    }
    public static int rgbaToArgb(int rgba) {
        // 分离出R, G, B, A分量
        int red = (rgba >> 16) & 0xFF;
        int green = (rgba >> 8) & 0xFF;
        int blue = rgba & 0xFF;
        int alpha = (rgba >> 24) & 0xFF;

        // 组装成ARGB格式
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}
