package net.hockeyfan17.cryoclient.features;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.hockeyfan17.cryoclient.CryoConfig;
import net.hockeyfan17.cryoclient.Main;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static org.joml.Math.floor;

public class PitReminder {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final Map<String, int[]> trackStats = new HashMap<>();// milliseconds
    private static int lapsCompleted;
    private static int pitsCompleted;
    private static int raceLaps;
    private static int racePits;
    private static long messageStartTime = -1;
    private static final long fadeDuration = 5000; // Milliseconds

    public static void PitReminderCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        assert client.player != null;
        List<String> CryoClient = List.of("cc", "CryoClient");

        for (String cc : CryoClient) {
            dispatcher.register(literal(cc)
                    .executes(context -> {
                        client.player.sendMessage(Text.literal("Missing Args").formatted(Formatting.RED));
                        return 1;
                    })
                    .then(literal("PitReminders")
                            .executes(context -> {
                                CryoConfig.INSTANCE.pitReminderToggle = !CryoConfig.INSTANCE.pitReminderToggle;
                                Text message = Main.CryoClientName.copy()
                                        .append(Text.literal("PitReminder ").formatted(Formatting.GRAY))
                                        .append(Text.literal(CryoConfig.INSTANCE.pitReminderToggle ? "Enabled" : "Disabled")
                                                .formatted(CryoConfig.INSTANCE.pitReminderToggle ? Formatting.GREEN : Formatting.RED));
                                client.player.sendMessage(message);
                                return 1;
                            })
                    )
            );
        }
    }
    public static void pitReminderFunction(String rawMessage) {
        trackListCounting(rawMessage);
        raceJoinFunction(rawMessage);
        if(raceLapCounting(rawMessage)) {
            messageStartTime = System.currentTimeMillis(); // Opens hud
        }
    }


    private static void raceJoinFunction(String rawMessage) {
        if(rawMessage.contains("--> Click to join a race on")){
            String[] Array = rawMessage.split("Click to join a race on", 2);
            String[] Array1 = Array[1].split("\\(", 2);
            String trackName = Array1[0].replaceAll("\\s+", "").toLowerCase();
            int[] finalTrackData = trackStats.get(trackName);
            raceLaps = (int) floor((float) finalTrackData[0] / finalTrackData[2]); // Starting lap not counted
            racePits = (int) floor((float) finalTrackData[1] / finalTrackData[2]);
            lapsCompleted = 0;
            pitsCompleted = 0;
            trackStats.clear();
        }
    }
    private static void trackListCounting(String rawMessage) {
        if (rawMessage.contains("just voted for a race on")) {
            String[] rawMessageArray = rawMessage.split(" ", 13);
            String messageTrack = rawMessageArray[7].toLowerCase().replace(" ", "");
            int messageLaps = Integer.parseInt(rawMessageArray[9].replace(" ", ""));
            int messagePits = Integer.parseInt(rawMessageArray[11].replace(" ", ""));


            trackStats.putIfAbsent(messageTrack, new int[]{0, 0, 0});
            int[] stats = trackStats.get(messageTrack);
            stats[0] += messageLaps; // Laps Count
            stats[1] += messagePits; // Pit Count
            stats[2]++; // vote count

        }
    }

    // return true when needing to pit //
    private static boolean raceLapCounting(String rawMessage){
        if(MinecraftClient.getInstance().player == null) return false; // fallback
        String playerName = MinecraftClient.getInstance().player.getName().getString();

        // If player does pit //
        if(rawMessage.contains(playerName + " completed pit") || rawMessage.contains(playerName + " has completed PigStop")){
            pitsCompleted++;
            return findIfPit();
        }

        // If player finished lap //
        if(rawMessage.contains(playerName + " new fastest lap") || rawMessage.contains("You finished lap in")){
            lapsCompleted++;
            return findIfPit();
        }

        return false;
    }

    // Setup Visuals for PitReminder
    public static void PitReminderHud(){
        HudRenderCallback.EVENT.register(( drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.player == null) return;

            // Compute how long it's been
            long elapsed = System.currentTimeMillis() - messageStartTime;
            if (elapsed > fadeDuration) return; // Done fading

            // Compute alpha from time (1.0 -> 0.0)
            float alpha = 1.0f - (elapsed / (float) fadeDuration);
            alpha = Math.max(0f, Math.min(1f, alpha)); // Clamp between 0 and 1

            // Scale alpha into ARGB
            int alphaInt = (int) (alpha * 255);
            int color = (alphaInt << 24) | 0xFF0000; // 0xAARRGGBB

            String displayText = "Pit Needed!";
            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();
            float scale = 2.5f; // Scale of text

            drawContext.getMatrices().push(); // Save the current state

            // Scale from the top-left corner — shift to desired position first
            drawContext.getMatrices().translate(screenWidth / 2f, screenHeight / 2f, 0);
            drawContext.getMatrices().scale(scale, scale, 1.0f);

            int textWidth = client.textRenderer.getWidth(displayText);
            int x = -textWidth / 2;
            int y = -4; // roughly half the text height

            drawContext.drawTextWithShadow(client.textRenderer, displayText, x, y, color);
        });
    }

    private static boolean findIfPit(){
        int lapsRemaining = raceLaps - lapsCompleted;
        int pitsRemaining = racePits - pitsCompleted;
        if(pitsRemaining == 0){
            return false;
        }
        if(lapsRemaining - 1 == pitsRemaining){
            return true;
        } else{
            return false;
        }
    }
}