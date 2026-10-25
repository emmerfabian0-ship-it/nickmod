package com.nickmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Gemeinsamer Teil (laeuft auf Server und im Singleplayer): speichert und verteilt Nicknames. */
public class NickMod implements ModInitializer {
    private static final Map<UUID, NickData> NICKS = new ConcurrentHashMap<>();

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.serverboundPlay().register(SetNickC2S.TYPE, SetNickC2S.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(NickS2C.TYPE, NickS2C.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SetNickC2S.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            NickData data = payload.data().sanitized();
            if (data.enabled()) NICKS.put(player.getUUID(), data);
            else NICKS.remove(player.getUUID());
            broadcast(context.server(), player.getUUID(), data);
        });

        // Neuer Spieler bekommt alle bekannten Nicks
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer joined = handler.getPlayer();
            if (!ServerPlayNetworking.canSend(joined, NickS2C.TYPE)) return;
            for (Map.Entry<UUID, NickData> e : NICKS.entrySet()) {
                ServerPlayNetworking.send(joined, new NickS2C(e.getKey(), e.getValue()));
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID id = handler.getPlayer().getUUID();
            if (NICKS.remove(id) != null) broadcast(server, id, NickData.NONE);
        });
    }

    private static void broadcast(MinecraftServer server, UUID who, NickData data) {
        NickS2C packet = new NickS2C(who, data);
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (ServerPlayNetworking.canSend(p, NickS2C.TYPE)) ServerPlayNetworking.send(p, packet);
        }
    }
}
