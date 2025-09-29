package net.hockeyfan17.cryoclient.features;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.hockeyfan17.cryoclient.CryoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.*;
import java.util.List;

public class BoatTrail {

    public static MinecraftClient client = MinecraftClient.getInstance();
    private static final Map<Integer, List<TrailPoint>> boatTrails = new HashMap<>();
    private static final Map<Integer, float[]> boatColors = new HashMap<>();
    private static final Map<Integer, Float> boatYOffsetOffsets = new HashMap<>();
    private record TrailPoint(Vec3d pos, long time) {}
    private static final long MAX_GAP_MS = 100;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if ((!CryoConfig.INSTANCE.boatTrailToggle && !CryoConfig.INSTANCE.boatTrailUnderGlowToggle) || client.world == null) return;

            long now = System.currentTimeMillis();

            for (Entity entity : client.world.getEntities()) {
                if (entity instanceof AbstractBoatEntity boat) {
                    boatTrails
                            .computeIfAbsent(boat.getId(), id -> new ArrayList<>())
                            .add(new TrailPoint(
                                    new Vec3d(boat.getX(), boat.getY(), boat.getZ()),
                                    now
                            ));
                }
            }

            boatTrails.values().forEach(list -> list.removeIf(p -> now - p.time > CryoConfig.INSTANCE.trailDuration));
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            if (client.player == null || (!CryoConfig.INSTANCE.boatTrailToggle && !CryoConfig.INSTANCE.boatTrailUnderGlowToggle) || boatTrails.isEmpty()) return;

            MatrixStack matrices = context.matrixStack();
            Vec3d cameraPos = context.camera().getPos();
            long now = System.currentTimeMillis();

            VertexConsumerProvider.Immediate vcp = MinecraftClient.getInstance()
                    .getBufferBuilders().getEntityVertexConsumers();
            VertexConsumer vc = vcp.getBuffer(RenderLayer.getDebugQuads());

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            for (Map.Entry<Integer, List<TrailPoint>> entry : boatTrails.entrySet()) {
                List<TrailPoint> points = entry.getValue();
                if (points.size() < 2) continue;

                assert client.world != null;
                Entity boatEntity = client.world.getEntityById(entry.getKey());
                float[] color;

                if (boatEntity instanceof AbstractBoatEntity boat) {
                    color = getBoatColor(boat);
                } else {
                    color = boatColors.getOrDefault(entry.getKey(), new float[]{1f, 0f, 0f});
                }

                // Per-boat stitch memory
                Vec3d prevRibbonLeft = null, prevRibbonRight = null;
                Vec3d prevVerticalBottom = null, prevVerticalTop = null;
                Vec3d prevCarpetLeft = null, prevCarpetRight = null;

                for (int i = 1; i < points.size(); i++) {
                    TrailPoint p1 = points.get(i - 1);
                    TrailPoint p2 = points.get(i);

                    if (p2.time - p1.time > MAX_GAP_MS) {
                        prevRibbonLeft = prevRibbonRight = null;
                        prevVerticalBottom = prevVerticalTop = null;
                        prevCarpetLeft = prevCarpetRight = null;
                        continue;
                    }

                    double rd2 = CryoConfig.INSTANCE.renderDistance * CryoConfig.INSTANCE.renderDistance;
                    double dist1 = p1.pos.squaredDistanceTo(client.player.getPos());
                    double dist2 = p2.pos.squaredDistanceTo(client.player.getPos());
                    if (dist1 > rd2 && dist2 > rd2) {
                        prevRibbonLeft = prevRibbonRight = null;
                        prevVerticalBottom = prevVerticalTop = null;
                        prevCarpetLeft = prevCarpetRight = null;
                        continue;
                    }

                    float progress1 = (now - p1.time) / (float) CryoConfig.INSTANCE.trailDuration;
                    float progress2 = (now - p2.time) / (float) CryoConfig.INSTANCE.trailDuration;

                    float alpha1 = MathHelper.clamp(1.0f - (progress1 * progress1), 0f, 1f);
                    float alpha2 = MathHelper.clamp(1.0f - (progress2 * progress2), 0f, 1f);

                    float yOffset = 0.02f + boatYOffsetOffsets.getOrDefault(entry.getKey(), 0f);

                    Vec3d dir = p2.pos.subtract(p1.pos);
                    if (dir.lengthSquared() < 1.0e-8) {
                        prevRibbonLeft = prevRibbonRight = null;
                        prevVerticalBottom = prevVerticalTop = null;
                        prevCarpetLeft = prevCarpetRight = null;
                        continue;
                    }
                    dir = dir.normalize();

                    Vec3d perp = new Vec3d(-dir.z, 0, dir.x).normalize().multiply((double) (CryoConfig.INSTANCE.trailWidth) / 100);
                    assert matrices != null;
                    Matrix4f mat = matrices.peek().getPositionMatrix();

                    if (CryoConfig.INSTANCE.boatTrailToggle) {
                        Vec3d ribbonFrontLeft  = p2.pos.add(perp).add(0, yOffset, 0);
                        Vec3d ribbonFrontRight = p2.pos.subtract(perp).add(0, yOffset, 0);

                        Vec3d ribbonBackLeft, ribbonBackRight;
                        if (prevRibbonLeft != null && prevRibbonRight != null) {
                            ribbonBackLeft = prevRibbonLeft;
                            ribbonBackRight = prevRibbonRight;
                        } else {
                            ribbonBackLeft  = p1.pos.add(perp).add(0, yOffset, 0);
                            ribbonBackRight = p1.pos.subtract(perp).add(0, yOffset, 0);
                        }

                        Vec3d v1 = ribbonBackLeft.subtract(cameraPos);
                        Vec3d v2 = ribbonBackRight.subtract(cameraPos);
                        Vec3d v3 = ribbonFrontRight.subtract(cameraPos);
                        Vec3d v4 = ribbonFrontLeft.subtract(cameraPos);

                        vc.vertex(mat, (float)v1.x, (float)v1.y, (float)v1.z).color(color[0], color[1], color[2], alpha1).light(0xF000F0).normal(0,1,0);
                        vc.vertex(mat, (float)v2.x, (float)v2.y, (float)v2.z).color(color[0], color[1], color[2], alpha1).light(0xF000F0).normal(0,1,0);
                        vc.vertex(mat, (float)v3.x, (float)v3.y, (float)v3.z).color(color[0], color[1], color[2], alpha2).light(0xF000F0).normal(0,1,0);
                        vc.vertex(mat, (float)v4.x, (float)v4.y, (float)v4.z).color(color[0], color[1], color[2], alpha2).light(0xF000F0).normal(0,1,0);

                        prevRibbonLeft = ribbonFrontLeft;
                        prevRibbonRight = ribbonFrontRight;
                    }

                    if (CryoConfig.INSTANCE.boatTrailToggle) {
                        Vec3d verticalFrontBottom = p2.pos.add(0, yOffset - (double) (CryoConfig.INSTANCE.trailWidth) / 100, 0);
                        Vec3d verticalFrontTop    = p2.pos.add(0, yOffset + (double) (CryoConfig.INSTANCE.trailWidth) / 100, 0);

                        Vec3d verticalBackBottom, verticalBackTop;
                        if (prevVerticalBottom != null && prevVerticalTop != null) {
                            verticalBackBottom = prevVerticalBottom;
                            verticalBackTop = prevVerticalTop;
                        } else {
                            verticalBackBottom = p1.pos.add(0, yOffset - (double) (CryoConfig.INSTANCE.trailWidth) / 100, 0);
                            verticalBackTop    = p1.pos.add(0, yOffset + (double) (CryoConfig.INSTANCE.trailWidth) / 100, 0);
                        }

                        Vec3d v1v = verticalBackBottom.subtract(cameraPos);
                        Vec3d v2v = verticalBackTop.subtract(cameraPos);
                        Vec3d v3v = verticalFrontTop.subtract(cameraPos);
                        Vec3d v4v = verticalFrontBottom.subtract(cameraPos);

                        vc.vertex(mat, (float)v1v.x, (float)v1v.y, (float)v1v.z).color(color[0], color[1], color[2], alpha1).light(0xF000F0).normal(0,0,1);
                        vc.vertex(mat, (float)v2v.x, (float)v2v.y, (float)v2v.z).color(color[0], color[1], color[2], alpha1).light(0xF000F0).normal(0,0,1);
                        vc.vertex(mat, (float)v3v.x, (float)v3v.y, (float)v3v.z).color(color[0], color[1], color[2], alpha2).light(0xF000F0).normal(0,0,1);
                        vc.vertex(mat, (float)v4v.x, (float)v4v.y, (float)v4v.z).color(color[0], color[1], color[2], alpha2).light(0xF000F0).normal(0,0,1);

                        prevVerticalBottom = verticalFrontBottom;
                        prevVerticalTop = verticalFrontTop;
                    }

                    if (CryoConfig.INSTANCE.boatTrailUnderGlowToggle) {
                        float carpetHalfWidth = 0.7f;
                        float carpetYOffset = yOffset - 0.01f;
                        Vec3d carpetPerp = new Vec3d(-dir.z, 0, dir.x).normalize().multiply(carpetHalfWidth);

                        Vec3d frontLeft  = p2.pos.add(carpetPerp).add(0, carpetYOffset, 0);
                        Vec3d frontRight = p2.pos.subtract(carpetPerp).add(0, carpetYOffset, 0);

                        Vec3d backLeft, backRight;
                        if (prevCarpetLeft != null && prevCarpetRight != null) {
                            backLeft = prevCarpetLeft;
                            backRight = prevCarpetRight;
                        } else {
                            backLeft  = p1.pos.add(carpetPerp).add(0, carpetYOffset, 0);
                            backRight = p1.pos.subtract(carpetPerp).add(0, carpetYOffset, 0);
                        }

                        Vec3d c1 = backLeft.subtract(cameraPos);
                        Vec3d c2 = backRight.subtract(cameraPos);
                        Vec3d c3 = frontRight.subtract(cameraPos);
                        Vec3d c4 = frontLeft.subtract(cameraPos);

                        vc.vertex(mat, (float)c1.x, (float)c1.y, (float)c1.z)
                                .color(color[0], color[1], color[2], alpha1 * 0.5f).light(0xF000F0).normal(0,1,0);
                        vc.vertex(mat, (float)c2.x, (float)c2.y, (float)c2.z)
                                .color(color[0], color[1], color[2], alpha1 * 0.5f).light(0xF000F0).normal(0,1,0);
                        vc.vertex(mat, (float)c3.x, (float)c3.y, (float)c3.z)
                                .color(color[0], color[1], color[2], alpha2 * 0.5f).light(0xF000F0).normal(0,1,0);
                        vc.vertex(mat, (float)c4.x, (float)c4.y, (float)c4.z)
                                .color(color[0], color[1], color[2], alpha2 * 0.5f).light(0xF000F0).normal(0,1,0);

                        prevCarpetLeft = frontLeft;
                        prevCarpetRight = frontRight;
                    }
                }
            }

            vcp.draw();
        });
    }

    private static float[] getBoatColor(AbstractBoatEntity boat) {
        for (Entity passenger : boat.getPassengerList()) {
            if (passenger instanceof PlayerEntity player) {
                float[] color = uuidColor(player.getUuid());
                boatColors.put(boat.getId(), color);

                boatYOffsetOffsets.putIfAbsent(boat.getId(), getYOffsetOffset(player.getUuid()));

                return color;
            }
        }

        boatYOffsetOffsets.putIfAbsent(boat.getId(), 0f);
        return boatColors.getOrDefault(boat.getId(), new float[]{1f, 0f, 0f});
    }

    private static float getYOffsetOffset(UUID uuid) {
        Random rand = new Random(uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits());
        return (rand.nextFloat() - 0.5f) * 0.01f;
    }

    private static float[] uuidColor(UUID uuid) {
        Random rand = new Random(uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits());

        float hue;
        do {
            hue = rand.nextFloat();
        } while (hue > 0.45f && hue < 0.65f);

        float saturation = 0.8f + 0.2f * rand.nextFloat();
        float brightness = 0.8f + 0.2f * rand.nextFloat();

        int rgb = Color.HSBtoRGB(hue, saturation, brightness);
        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >> 8) & 0xFF) / 255f;
        float b = (rgb & 0xFF) / 255f;

        return new float[]{r, g, b};
    }
}
