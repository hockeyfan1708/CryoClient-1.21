package net.hockeyfan17.cryoclient.features;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Field;

public class BoatReset {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static Field overlayField = null;
    private static boolean triedInit = false;
    private static String lastOverlay = null;
    public static Vec3d savedPos = new Vec3d(-35.5, 93.5, 2.0);
    public static Vec3d savedVel = new Vec3d(2.0, 0.0, 0.0);
    public static float savedYaw = 90.0F;   // facing east
    public static float savedPitch = 0.0F;
    public static float savedPlayerYaw = 90.0F;
    public static float savedPlayerPitch = 35.0F;

    public static void ResetBoat() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.world != null) {
            Entity vehicle = client.player.getVehicle();
            if (vehicle instanceof BoatEntity boat) {


                // Apply position and velocity
                boat.setPos(savedPos.x, savedPos.y + 0.04, savedPos.z);
                boat.setVelocity(savedVel);
                boat.velocityModified = true;

                // Apply rotation
                boat.setYaw(savedYaw);
                boat.setPitch(savedPitch);

                // Apply "rotational velocity" (custom, since MC doesn’t store it directly)
                boat.prevYaw = boat.getYaw();

                client.player.setPitch(savedPlayerPitch);
                client.player.setYaw(savedPlayerYaw);

                client.player.sendMessage(Text.literal("Boat reset to saved state!"), false);
            } else {
                client.player.sendMessage(Text.literal("You are not in a boat!"), false);
            }
        }
    }

    public static void SaveBoat() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.world != null) {
            Entity vehicle = client.player.getVehicle();
            if (vehicle instanceof BoatEntity boat) {
                savedPos = boat.getPos();
                savedPitch = boat.getPitch();
                savedYaw = boat.getYaw();
                savedVel = boat.getVelocity();
                savedPlayerPitch = client.player.getPitch();
                savedPlayerYaw = client.player.getYaw();
            } else {
                client.player.sendMessage(Text.literal("You are not in a boat!"), false);
            }
        }
    }

    public static void BoatResetHud(DrawContext context, RenderTickCounter tickCounter) {
        if (client.player == null) return;

        Text overlay = getOverlayMessage();
        if (overlay == null) {
            lastOverlay = null; // clear if nothing is shown
            return;
        }

        String raw = overlay.getString().trim();

        if (!raw.equals(lastOverlay) &&
                (raw.equals("00.000") || raw.equals("00.050") || raw.equals("00.100") || raw.equals("00.150"))) {
            lastOverlay = raw;
            SaveBoat();
//            client.player.sendMessage(
//                    Text.literal("Boat state saved at timer: " + raw),
//                    false
//            );
        }
    }

    private static Text getOverlayMessage() {
        if (!triedInit) {
            triedInit = true;
            try {
                // try the obvious field name first (yarn mappings)
                overlayField = InGameHud.class.getDeclaredField("overlayMessage");
                overlayField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                // fallback: search for a Text-typed field that likely is the overlay
                for (Field f : InGameHud.class.getDeclaredFields()) {
                    if (f.getType() == Text.class) {
                        String name = f.getName().toLowerCase();
                        if (name.contains("overlay") || name.contains("message") || name.contains("title")) {
                            f.setAccessible(true);
                            overlayField = f;
                            break;
                        }
                    }
                }
            } catch (Throwable t) {
                overlayField = null;
            }
        }

        if (overlayField == null) return null;

        try {
            return (Text) overlayField.get(client.inGameHud);
        } catch (IllegalAccessException e) {
            return null;
        }
    }
}
