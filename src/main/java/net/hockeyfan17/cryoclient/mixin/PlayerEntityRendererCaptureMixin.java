package net.hockeyfan17.cryoclient.mixin;

import net.hockeyfan17.cryoclient.util.PlayerRenderContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererCaptureMixin {

    @Inject(method = "updateRenderState", at = @At("HEAD"))
    private void capturePlayer(AbstractClientPlayerEntity player,
                               PlayerEntityRenderState state,
                               float tickDelta,
                               CallbackInfo ci) {
        PlayerRenderContext.set(player);
    }
}