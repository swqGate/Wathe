package dev.doctor4t.trainmurdermystery.mixin.client;

import dev.doctor4t.trainmurdermystery.client.TrainMurderMysteryClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(method = "renderStatusBars", at = @At("HEAD"), cancellable = true)
    private void tmm$removeStatusBars(DrawContext context, CallbackInfo ci) {
        if (TrainMurderMysteryClient.shouldRestrictPlayerOptions()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void tmm$removeExperienceBar(DrawContext context, int x, CallbackInfo ci) {
        if (TrainMurderMysteryClient.shouldRestrictPlayerOptions()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderExperienceLevel", at = @At("HEAD"), cancellable = true)
    private void tmm$removeExperienceLevel(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (TrainMurderMysteryClient.shouldRestrictPlayerOptions()) {
            ci.cancel();
        }
    }
}
