package com.nickmod;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientNicks {
    private static final Map<UUID, NickData> OTHERS = new ConcurrentHashMap<>();
    public static NickData own = NickData.NONE;

    private ClientNicks() {}

    public static void put(UUID id, NickData d) {
        if (d.enabled()) OTHERS.put(id, d);
        else OTHERS.remove(id);
    }

    public static void clearOthers() {
        OTHERS.clear();
    }

    /** Eigener Nick kommt immer lokal, fremde vom Server. */
    public static NickData get(UUID id) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getUUID().equals(id)) return own;
        return OTHERS.get(id);
    }

    public static void setOwn(NickData d) {
        own = d.sanitized();
        save();
        sendOwn();
    }

    public static void sendOwn() {
        if (ClientPlayNetworking.canSend(SetNickC2S.TYPE)) {
            ClientPlayNetworking.send(new SetNickC2S(own));
        }
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("nickmod.json");
    }

    public static void save() {
        try {
            JsonObject o = new JsonObject();
            o.addProperty("enabled", own.enabled());
            o.addProperty("name", own.name());
            o.addProperty("color", own.color());
            o.addProperty("caseMode", own.caseMode());
            o.addProperty("effect", own.effect());
            Files.writeString(file(), o.toString());
        } catch (Exception ignored) {
        }
    }

    public static void load() {
        try {
            if (!Files.exists(file())) return;
            JsonObject o = JsonParser.parseString(Files.readString(file())).getAsJsonObject();
            own = new NickData(o.get("enabled").getAsBoolean(), o.get("name").getAsString(),
                    o.get("color").getAsInt(), o.get("caseMode").getAsInt(), o.get("effect").getAsInt()).sanitized();
        } catch (Exception e) {
            own = NickData.NONE;
        }
    }
}
