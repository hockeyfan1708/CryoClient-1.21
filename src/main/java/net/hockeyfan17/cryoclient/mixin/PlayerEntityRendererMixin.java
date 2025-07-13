package net.hockeyfan17.cryoclient.mixin;

import net.hockeyfan17.cryoclient.CryoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(AbstractClientPlayerEntity player, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (shouldHide(player) && CryoConfig.INSTANCE.hidePassengersToggle) {
            ci.cancel();
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