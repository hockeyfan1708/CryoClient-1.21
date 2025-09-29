package net.hockeyfan17.cryoclient.features;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.hockeyfan17.cryoclient.CryoTrackConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class TrackDetector {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static Field overlayField = null;
    private static boolean triedInit = false;
    private static String lastOverlay = null;
    public static int checkpointNumber;
    public static float checkpointTime;
    public static Double checkpointDelta = null;
    public static List<Vec3d> checkpointPositions = new ArrayList<>();
    public static Double finishDelta = null;


    public static void register() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String raw = message.getString().trim();

            Matcher m = TELEPORT_PATTERN.matcher(raw);
            if (m.find()) {
                CryoTrackConfig.INSTANCE.checkpoints.clear();
                CryoTrackConfig.INSTANCE.selectedTrack = m.group(1).trim().replaceAll("\\.$", "");
                CryoTrackConfig.INSTANCE.load();
                CryoTrackConfig.INSTANCE.selectedTrack = m.group(1).trim().replaceAll("\\.$", "");
                CryoTrackConfig.INSTANCE.save();
//                System.out.println("[TrackDetector] detected track -> " + CryoTrackConfig.INSTANCE.selectedTrack);
//                if (MinecraftClient.getInstance().player != null) {
//                    MinecraftClient.getInstance().player.sendMessage(
//                            Text.literal("Detected track: " + CryoTrackConfig.INSTANCE.selectedTrack),
//                            false
//                    );
//                }
            }

            Matcher cp = CHECKPOINT_PATTERN.matcher(raw);
            if (cp.find()) {
                if (client.player == null) return;
                checkpointNumber = Integer.parseInt(cp.group(1));
                checkpointDelta = cp.group(3) != null ? Double.parseDouble(cp.group(3)) : null;
                checkpointTime = (float) Math.round((checkpointDelta != null ? Double.parseDouble(cp.group(2)) - checkpointDelta : Double.parseDouble(cp.group(2))) * 100) / 100;
                CryoTrackConfig.INSTANCE.addCheckpointPos(checkpointNumber, client.player.getPos());

                CryoTrackConfig.INSTANCE.addCheckpoint();

//                System.out.println("[TrackDetector] Checkpoint " + checkpointNumber +
//                        " -> time: " + checkpointTime +
//                        (checkpointDelta != null ? " delta: " + (checkpointDelta >= 0 ? "+" : "") + checkpointDelta : ""));
            }

            Matcher fin = FINISH_PATTERN.matcher(raw);
            if (fin.find()) {
                finishDelta = fin.group(2) != null ? Double.parseDouble(fin.group(2)) : null;
                CryoTrackConfig.INSTANCE.finishTime = (float) Math.round((finishDelta != null ? Double.parseDouble(fin.group(1)) - finishDelta : Double.parseDouble(fin.group(1))) * 100) / 100;
                CryoTrackConfig.INSTANCE.save();
            }
        });
    }

    private static final java.util.regex.Pattern TELEPORT_PATTERN =
            java.util.regex.Pattern.compile("(?i)teleported you to\\s+(.+?)(?:\\s*\\(\\d+\\))?\\.?$"
            );

    private static final java.util.regex.Pattern CHECKPOINT_PATTERN =
            java.util.regex.Pattern.compile("(?i)checkpoint\\s+(\\d+)\\s*>\\s*(\\d+\\.\\d+)(?:\\s+([+-]\\d+\\.\\d+))?"
            );

    private static final java.util.regex.Pattern FINISH_PATTERN =
            java.util.regex.Pattern.compile(
                    "(?i)you finished\\s+.+?\\s+in\\s+(\\d+\\.\\d+)(?:\\s+([+-]?\\d+\\.\\d+))?.*"
            );


    public static void TimeTrailStartHUD(DrawContext context, RenderTickCounter tickCounter) {
        if (client.player == null) return;

        Text overlay = getOverlayMessage();
        if (overlay == null) {
            lastOverlay = null;
            return;
        }

        String raw = overlay.getString().trim();

        if (!raw.equals(lastOverlay) &&
                (raw.equals("00.000") || raw.equals("00.050") || raw.equals("00.100") || raw.equals("00.150"))) {
            lastOverlay = raw;

            client.player.sendMessage(
                    Text.literal("Boat state saved at timer: " + raw),
                    false
            );

            CryoTrackConfig.INSTANCE.addStartPos(client.player.getPos());
            CryoTrackConfig.INSTANCE.save();
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