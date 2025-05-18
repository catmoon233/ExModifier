package web;

import com.google.common.util.concurrent.AtomicDouble;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.mojang.realmsclient.Unit;
import com.sun.management.OperatingSystemMXBean;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


import java.nio.charset.StandardCharsets;



import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.*;
import com.google.gson.*;
import net.exmo.exmodifier.Exmodifier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import static com.mojang.realmsclient.Unit.GB;

public class RealTimeWebServer {
    public static AtomicInteger entityCount = new AtomicInteger(0);
    public static AtomicReference<List<ServerPlayer>> onlinePlayers = new AtomicReference<>(new ArrayList<>());
    public static final AtomicDouble cpuUsage = new AtomicDouble(0);
    public static final AtomicDouble memoryUsage = new AtomicDouble(0);
    public static final AtomicReference<JsonArray> playersData = new AtomicReference<>(new JsonArray());
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final com.sun.management.OperatingSystemMXBean osBean =
            (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private static final Runtime runtime = Runtime.getRuntime();



    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);

        // 静态文件服务
// 在文件服务中添加异常处理
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";

            FileInputStream fileInputStream = new FileInputStream(String.valueOf(("G:\\MC\\ExModifier1.20.1\\static\\"+path)));
            InputStream is = fileInputStream;
            // FileReader fileReader = new FileReader(String.valueOf(("G:\\MC\\ExModifier1.20.1\\static")));
            if (is == null) {
                sendResponse(exchange, "404 Not Found", 404);
                return;
            }

            String mimeType = "text/html";
            if (path.endsWith(".css")) mimeType = "text/css";
            if (path.endsWith(".js")) mimeType = "application/javascript";

            exchange.getResponseHeaders().set("Content-Type", mimeType + "; charset=UTF-8");
            OutputStream os = exchange.getResponseBody();
            exchange.sendResponseHeaders(200, 0);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();
        });
        // 数据API端点
        server.createContext("/api/serverinfo", exchange -> {
            JsonObject data = new JsonObject();

            // 基础信息
            data.addProperty("entityCount", entityCount.get());
            data.addProperty("onlinePlayers", onlinePlayers.get().size());
            data.addProperty("cpuUsage", Math.round(cpuUsage.get() * 100) / 100.0);
            data.addProperty("memoryUsage", Math.round(memoryUsage.get() * 100) / 100.0);

            // 系统信息
            JsonObject systemInfo = new JsonObject();
            systemInfo.addProperty("os", System.getProperty("os.name"));
            systemInfo.addProperty("javaVersion", System.getProperty("java.version"));
            systemInfo.addProperty("serverTime", new Date().toString());
            data.add("system", systemInfo);

            // 玩家数据
            data.add("players", playersData.get());

            sendResponse(exchange, gson.toJson(data));
        });

        // 启动模拟数据更新线程
        new Thread(() -> {
            Random rand = new Random();
            while (!Thread.interrupted()) {
                try {
                    // 生成模拟数据



                    // CPU使用率（最近1分钟）
                    double cpuLoad = osBean.getCpuLoad() * 100;
                    cpuUsage.set(Double.isNaN(cpuLoad) ? 0 : cpuLoad);

                    // 内存使用（MB）
                    double usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024.0 / 1024.0;
                    double totalMemory = runtime.maxMemory() / 1024.0 / 1024.0;
                    memoryUsage.set((usedMemory / totalMemory) * 100);
                    
                    // 计算内存使用百分比
                    double memoryUsageValue = ((totalMemory - usedMemory)) * 100;

                    cpuUsage.set(Double.isNaN(cpuLoad) ? 0 : cpuLoad);
                    memoryUsage.set(memoryUsageValue);

                    // 生成玩家数据
                    JsonArray players = new JsonArray();
                    for (ServerPlayer player : onlinePlayers.get()) {
                        if (player == null) continue;

                        JsonObject playerJson = new JsonObject();
                        playerJson.addProperty("name", player.getScoreboardName());

                        // 位置信息
                        JsonObject position = new JsonObject();
                        position.addProperty("x", Math.round(player.getX()));
                        position.addProperty("y", Math.round(player.getY()));
                        position.addProperty("z", Math.round(player.getZ()));
                        playerJson.add("position", position);

                        // 装备信息
                        JsonArray equipment = new JsonArray();
                        for (ItemStack stack : player.getInventory().items) {
                            if (!stack.isEmpty()) {
                                JsonObject item = new JsonObject();
                                item.addProperty("name", stack.getHoverName().getString());
                                item.addProperty("id", ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
                                item.addProperty("durability", stack.getDamageValue());
                                item.addProperty("maxDurability", stack.getMaxDamage());
                                equipment.add(item);
                            }
                        }
                        playerJson.add("equipment", equipment);

                        // 添加更多玩家信息
                        playerJson.addProperty("health", player.getHealth());
                        playerJson.addProperty("dimension", player.serverLevel().dimension().location().toString());

                        players.add(playerJson);
                    }
                    playersData.set(players);
                    playersData.set(players);

                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();


        server.start();

        System.out.println("服务器运行在 http://localhost:8081");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop(0);
            Exmodifier.LOGGER.info("监控服务器已关闭");
        }));
    }

    private static void sendResponse(HttpExchange exchange, String content) throws IOException {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendResponse(HttpExchange exchange, String content, int code) throws IOException {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}