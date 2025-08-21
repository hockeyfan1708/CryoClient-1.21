package net.hockeyfan17.cryoclient.mixin;

import net.hockeyfan17.cryoclient.CryoConfig;
import net.hockeyfan17.cryoclient.util.PlayerRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class PlayerEntityRendererHideMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(LivingEntityRenderState state,
                          MatrixStack matrices,
                          VertexConsumerProvider vertexConsumers,
                          int light,
                          CallbackInfo ci) {

        PlayerEntity player = PlayerRenderContext.get();
        try {
            if (player != null && CryoConfig.INSTANCE.hidePassengersToggle && shouldHide(player)) {
                ci.cancel();
            }
        } finally {
            // always clear after use
            PlayerRenderContext.clear();
        }
    }

    private boolean shouldHide(PlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity self = client.player;
        if (self == null || !self.hasVehicle() || !(self.getVehicle() instanceof BoatEntity)) return false;

        return isPassengerRecursive(player, self);
    }

    private boolean isPassengerRecursive(Entity target, Entity vehicle) {
        for (Entity passenger : vehicle.getPassengerList()) {
            if (passenger == target) {
                return true;
            }
            if (isPassengerRecursive(target, passenger)) {
                return true;
            }
        }
        return false;
    }
}