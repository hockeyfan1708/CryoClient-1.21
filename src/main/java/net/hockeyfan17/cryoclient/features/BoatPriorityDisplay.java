package net.hockeyfan17.cryoclient.features;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.hockeyfan17.cryoclient.CryoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class BoatPriorityDisplay {
    public static void register() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || !CryoConfig.INSTANCE.boatPriorityDisplayToggle) return;
            if (!(client.player.getVehicle() instanceof BoatEntity boat)) return;

            Vec3d vel = boat.getVelocity();
            if (vel.lengthSquared() < 1e-6) return;

            // --- determine dominant cardinal direction from velocity ---
            // NOTE: using X and Z axes (X -> East/West, Z -> South/North)
            Direction dir = getDominantCardinalFromVelocity(vel, true);
            Direction secondaryDir = getDominantCardinalFromVelocity(vel, false);

            // Use current entity position (no interpolation)
            Vec3d boatPos = boat.getLerpedPos(context.tickCounter().getTickDelta(true));

            double bx = boatPos.x;
            double by = boatPos.y;
            double bz = boatPos.z;
            double offset = 2.6;

            if (dir == Direction.WEST) bx += offset;
            if (dir == Direction.EAST) bx -= offset;
            if (dir == Direction.SOUTH) bz += offset;
            if (dir == Direction.NORTH) bz -= offset;

            Vec3d camPos = context.camera().getPos();
            double arrowX = bx - camPos.x;
            double arrowY = by + 0.02 - camPos.y; // raise arrow above boat
            double arrowZ = bz - camPos.z;

            MatrixStack matrices = context.matrixStack();
            assert matrices != null;
            matrices.push();

            // Move to boat
            matrices.translate(arrowX, arrowY, arrowZ);

            // Rotate so the arrow's +Z tip points to the chosen Direction
            float yawDeg = switch (dir) {
                case NORTH -> 180f;   // -Z
                case SOUTH -> 0f;     // +Z (no rotation)
                case EAST  -> -90f;   // +X
                case WEST  -> 90f;    // -X
                default    -> 0f;
            };
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yawDeg));

            Matrix4f mat = matrices.peek().getPositionMatrix();

            VertexConsumerProvider.Immediate vcp = client.getBufferBuilders().getEntityVertexConsumers();
            VertexConsumer vc = vcp.getBuffer(RenderLayer.getDebugQuads());

            // flat triangle arrow: base across X, tip along +Z
            float base = 0.3f;   // half-width of the base
            float length = 0.8f; // arrow length

            vc.vertex(mat, -base, 0f, 0f).color(1f, 0f, 0f, 1f).light(0xF000F0);
            vc.vertex(mat,  base, 0f, 0f).color(1f, 0f, 0f, 1f).light(0xF000F0);
            vc.vertex(mat,  0f,   0f, length).color(1f, 0f, 0f, 1f).light(0xF000F0);
            vc.vertex(mat, -base, 0f, 0f).color(1f, 0f, 0f, 1f).light(0xF000F0);

            vcp.draw();
            matrices.pop();

            if (secondaryDir != null && CryoConfig.INSTANCE.yellowArrowBoatPrioDisplay) {
                matrices.push();

                double bx2 = boatPos.x;
                double bz2 = boatPos.z;

                if (secondaryDir == Direction.WEST) bx2 += (offset + 0.2);
                if (secondaryDir == Direction.EAST) bx2 -= (offset + 0.2);
                if (secondaryDir == Direction.SOUTH) bz2 += (offset + 0.2);
                if (secondaryDir == Direction.NORTH) bz2 -= (offset + 0.2);

                double arrowX2 = bx2 - camPos.x;
                double arrowY2 = by + 0.02 - camPos.y;
                double arrowZ2 = bz2 - camPos.z;

                matrices.translate(arrowX2, arrowY2, arrowZ2);

                float yawDeg2 = switch (secondaryDir) {
                    case NORTH -> 180f;
                    case SOUTH -> 0f;
                    case EAST  -> -90f;
                    case WEST  -> 90f;
                    default    -> 0f;
                };
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yawDeg2));

                Matrix4f mat2 = matrices.peek().getPositionMatrix();

                VertexConsumer vc2 = vcp.getBuffer(RenderLayer.getDebugQuads());

                // Smaller yellow arrow
                float base2 = 0.2f;
                float length2 = 0.5f;

                vc2.vertex(mat2, -base2, 0f, 0f).color(1f, 1f, 0f, 1f).light(0xF000F0);
                vc2.vertex(mat2,  base2, 0f, 0f).color(1f, 1f, 0f, 1f).light(0xF000F0);
                vc2.vertex(mat2,  0f,   0f, length2).color(1f, 1f, 0f, 1f).light(0xF000F0);
                vc2.vertex(mat2, -base2, 0f, 0f).color(1f, 1f, 0f, 1f).light(0xF000F0);

                vcp.draw();
                matrices.pop();
            }
        });
    }

    private static Direction getDominantCardinalFromVelocity(Vec3d vel, boolean Type) {
        double absX = Math.abs(vel.x);
        double absZ = Math.abs(vel.z);

        Direction mainDir;
        Direction secondaryDir = null;

        if (absX >= absZ) {
            mainDir = (vel.x >= 0) ? Direction.WEST : Direction.EAST;

            // Check if Z is close enough to compete
            if (absZ / absX > 0.7) { // tweak threshold for sensitivity
                secondaryDir = (vel.z >= 0) ? Direction.SOUTH : Direction.NORTH;
            }
        } else {
            mainDir = (vel.z >= 0) ? Direction.SOUTH : Direction.NORTH;

            // Check if X is close enough to compete
            if (absX / absZ > 0.7) {
                secondaryDir = (vel.x >= 0) ? Direction.WEST : Direction.EAST;
            }
        }

        if (Type) return mainDir;
        else return secondaryDir;
    }
}
