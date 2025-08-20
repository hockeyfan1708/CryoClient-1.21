package net.hockeyfan17.cryoclient.features;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.hockeyfan17.cryoclient.CryoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import java.util.*;


public class BoatTrail {

    public static MinecraftClient client = MinecraftClient.getInstance();
    private static final long TRAIL_DURATION_MS = 30000;
    private static final Map<Integer, List<TrailPoint>> boatTrails = new HashMap<>();
    private record TrailPoint(Vec3d pos, long time) {}

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!CryoConfig.INSTANCE.boatTrailToggle || client.world == null) return;

            long now = System.currentTimeMillis();

            for (Entity entity : client.world.getEntities()) {
                if (entity instanceof BoatEntity boat) {
                    boatTrails
                            .computeIfAbsent(boat.getId(), id -> new ArrayList<>())
                            .add(new TrailPoint(
                                    new Vec3d(boat.getX(), boat.getY(), boat.getZ()),
                                    now
                            ));
                }
            }

            boatTrails.values().forEach(list -> list.removeIf(p -> now - p.time > TRAIL_DURATION_MS));
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            if (!CryoConfig.INSTANCE.boatTrailToggle || boatTrails.isEmpty()) return;

            MatrixStack matrices = context.matrixStack();
            Vec3d cameraPos = context.camera().getPos();
            long now = System.currentTimeMillis();

            VertexConsumerProvider.Immediate vcp = MinecraftClient.getInstance()
                    .getBufferBuilders().getEntityVertexConsumers();
            VertexConsumer vc = vcp.getBuffer(RenderLayer.getLines());

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.lineWidth(10.0f);

            for (List<TrailPoint> points : boatTrails.values()) {
                if (points.size() < 2) continue;

                for (int i = 1; i < points.size(); i++) {
                    TrailPoint p1 = points.get(i - 1);
                    TrailPoint p2 = points.get(i);

                    float alpha1 = MathHelper.clamp(1.0f - (now - p1.time) / (float) TRAIL_DURATION_MS, 0f, 1f);
                    float alpha2 = MathHelper.clamp(1.0f - (now - p2.time) / (float) TRAIL_DURATION_MS, 0f, 1f);

                    float offsetAmount = 0.00625f;
                    int numLines = 20;

                    Vec3d dir = p2.pos.subtract(p1.pos).normalize();
                    Vec3d perpendicular = new Vec3d(-dir.z, 0, dir.x).normalize().multiply(offsetAmount);

                    int half = numLines / 2;
                    for (int j = -half; j <= half; j++) {
                        Vec3d offset = perpendicular.multiply(j);

                        assert matrices != null;

                        vc.vertex(matrices.peek().getPositionMatrix(),
                                        (float)(p1.pos.x + offset.x - cameraPos.x),
                                        (float)(p1.pos.y - cameraPos.y),
                                        (float)(p1.pos.z + offset.z - cameraPos.z))
                                .color(1,0,0,alpha1)
                                .light(0xF000F0)
                                .normal(0,1,0);

                        vc.vertex(matrices.peek().getPositionMatrix(),
                                        (float)(p2.pos.x + offset.x - cameraPos.x),
                                        (float)(p2.pos.y - cameraPos.y),
                                        (float)(p2.pos.z + offset.z - cameraPos.z))
                                .color(1,0,0,alpha2)
                                .light(0xF000F0)
                                .normal(0,1,0);
                    }
                }
            }

            vcp.draw();
            RenderSystem.lineWidth(10.0f);
        });
    }
}