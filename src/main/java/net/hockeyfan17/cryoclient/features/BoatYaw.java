package net.hockeyfan17.cryoclient.features;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.hockeyfan17.cryoclient.CryoConfig;
import net.hockeyfan17.cryoclient.Main;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BoatYaw {

    public static MinecraftClient client = MinecraftClient.getInstance();

    public static void BoatYawHud() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();

            if (CryoConfig.INSTANCE.boatYawToggle && client.player != null && client.player.getVehicle() instanceof BoatEntity boat) {
                double yaw = getBoatYaw(boat);
                String displayText = String.format("%.4f", yaw);
                int screenWidth = client.getWindow().getScaledWidth();
                int screenHeight = client.getWindow().getScaledHeight();
                int anchorX = (screenWidth / 2) + 20;
                int textWidth = client.textRenderer.getWidth(displayText);
                float x = anchorX - textWidth;
                float y = screenHeight - 85;

                drawContext.drawTextWithShadow(
                        client.textRenderer,
                        displayText,
                        (int) x,
                        (int) y,
                        0xFFAAAAAA
                );
            }
        });
    }

    static double boatAngle;

    public static double getBoatYaw(BoatEntity boat) {
        float rawYaw = boat.getYaw();
        boatAngle = Math.round(rawYaw * 10000.0) / 10000.0;
        return Math.round(rawYaw * 10000.0) / 10000.0;
    }

    static double totalRotation;

    public static void totalRotationNeeded(double baseTarget) {
        var client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        double startAngle = boatAngle;
        double step = 90.9091;
        int maxSteps = 50 * 360 / (int) step;

        double minDifference = Double.MAX_VALUE;
        int bestSteps = 0;
        double bestAngle = startAngle;

        for (int steps = -maxSteps; steps <= maxSteps; steps++) {
            double rotatedAngle = startAngle + steps * step;

            double k = Math.round((rotatedAngle - baseTarget) / 360.0);
            double nearestTarget = baseTarget + k * 360;

            double difference = Math.abs(rotatedAngle - nearestTarget);

            if (difference < minDifference) {
                minDifference = difference;
                bestSteps = steps;
                bestAngle = rotatedAngle;
            }
        }

        totalRotation = bestSteps * step;

        Text message = Main.CryoClientName.copy()
                .append(Text.literal("Closest angle to ").formatted(Formatting.GRAY))
                .append(Text.literal(String.valueOf(baseTarget)).formatted(Formatting.YELLOW))
                .append(Text.literal(" is: ").formatted(Formatting.GRAY))
                .append(Text.literal(String.valueOf(bestAngle)).formatted(Formatting.GREEN));
        client.player.sendMessage(message);
    }
}