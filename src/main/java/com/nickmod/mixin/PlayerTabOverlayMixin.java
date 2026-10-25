package com.nickmod.mixin;

import com.nickmod.ClientNicks;
import com.nickmod.NickData;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
    /** Tab-Liste: Name durch den animierten Nick ersetzen. */
    @Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
    private void nickmod$name(PlayerInfo info, CallbackInfoReturnable<Component> cir) {
        NickData d = ClientNicks.get(info.getProfile().id());
        if (d != null && d.enabled()) {
            cir.setReturnValue(d.render(System.currentTimeMillis()));
        }
    }
}
