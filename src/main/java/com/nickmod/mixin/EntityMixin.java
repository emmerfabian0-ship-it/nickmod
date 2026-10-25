package com.nickmod.mixin;

import com.nickmod.ClientNicks;
import com.nickmod.NickData;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    /** Name ueber dem Kopf (Nametag) von Spielern durch den Nick ersetzen. */
    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void nickmod$display(CallbackInfoReturnable<Component> cir) {
        if ((Object) this instanceof AbstractClientPlayer p) {
            NickData d = ClientNicks.get(p.getUUID());
            if (d != null && d.enabled()) {
                cir.setReturnValue(d.render(System.currentTimeMillis()));
            }
        }
    }
}
