package com.nickmod;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class NickModClient implements ClientModInitializer {
    private static KeyMapping openKey;

    @Override
    public void onInitializeClient() {
        ClientNicks.load();

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("nickmod", "main"));
        openKey = KeyMappingHelper.registerKeyMapping(
                                new KeyMapping("key.nickmod.open", InputConstants.KEY_N, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.consumeClick()) {
                if (client.gui.screen() == null) client.gui.setScreen(new NickScreen());
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(NickS2C.TYPE,
                (payload, context) -> ClientNicks.put(payload.uuid(), payload.data()));

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (ClientNicks.own.enabled()) ClientNicks.sendOwn();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientNicks.clearOthers());
    }
}
