package com.nickmod.mixin;

import com.nickmod.ClientNicks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    /**
     * In der 3rd-Person-Ansicht (F5) auch den eigenen Nametag anzeigen.
     * require = 0: Falls die Methode in deiner Version anders heisst, startet die Mod trotzdem
     * (nur der eigene Name in F5 fehlt dann).
     */
    @Inject(method = "shouldShowName", at = @At("RETURN"), cancellable = true, require = 0)
    private void nickmod$ownLabel(LivingEntity entity, double distSq, CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (!cir.getReturnValue()
                && entity == mc.player
                && !mc.options.getCameraType().isFirstPerson()
                && ClientNicks.own.enabled()
                && !mc.options.hideGui
                && !entity.isInvisible()) {
            cir.setReturnValue(true);
        }
    }
}
