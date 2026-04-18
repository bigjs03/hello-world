package com.bygj.immersed.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public class ImmersedConfig {
    public float smallHeldItemsScale = 0.6f;
    public long bridgeIntervalMs = 220L;
    public String cloudSyncUrl = "";
    public boolean autoSyncOnStart = true;

    private static ImmersedConfig INSTANCE;
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("immersed-with-shading.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static ImmersedConfig get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    public static void load() {
        try {
            if (Files.exists(PATH)) {
                INSTANCE = GSON.fromJson(Files.readString(PATH), ImmersedConfig.class);
            }
        } catch (Exception ignored) {
        }
        if (INSTANCE == null) INSTANCE = new ImmersedConfig();
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(INSTANCE));
        } catch (IOException ignored) {
        }
    }

    public static boolean syncFromCloud() {
        ImmersedConfig cur = get();
        if (cur.cloudSyncUrl == null || cur.cloudSyncUrl.isBlank()) return false;
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            HttpRequest req = HttpRequest.newBuilder(URI.create(cur.cloudSyncUrl))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                ImmersedConfig remote = GSON.fromJson(resp.body(), ImmersedConfig.class);
                if (remote != null) {
                    String url = cur.cloudSyncUrl;
                    INSTANCE = remote;
                    INSTANCE.cloudSyncUrl = url;
                    save();
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
