package net.hockeyfan17.cryoclient.features;

import net.hockeyfan17.cryoclient.CryoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class QuickRace {
    private static final Map<String, Float> originalWeights = new HashMap<>();
    private static final Map<String, Integer> cooldowns = new HashMap<>();

    public static void VoteTrack(int laps, int pits) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player != null) {
            Random random = new Random();

            Map<String, Float> trackMap = new HashMap<>(CryoConfig.INSTANCE.quickRaceTrackList);

            // --- adjust weights based on cooldowns ---

            for (Map.Entry<String, Float> entry : trackMap.entrySet()) {
                String track = entry.getKey();
                float weight = entry.getValue();

                originalWeights.putIfAbsent(track, weight);

                if (cooldowns.containsKey(track)) {
                    int cd = cooldowns.get(track);
                    int total = 10; // cooldown length
                    int elapsed = total - cd;

                    float minWeight = originalWeights.get(track) * 0.1f; // punish hard at start
                    float progress = elapsed / (float) total;
                    float eased = progress * progress; // quadratic easing

                    float newWeight = minWeight + (originalWeights.get(track) - minWeight) * eased;
                    entry.setValue(newWeight);

                    if (cd > 0) {
                        cooldowns.put(track, cd - 1);
                    } else {
                        cooldowns.remove(track);
                    }
                }
            }

            // --- weighted random selection ---

            float totalWeight = 0.0F;

            for (float weight : trackMap.values()) {
                totalWeight += weight;
            }

            float roll = random.nextFloat() * totalWeight;

            Map.Entry<String, Float> chosenTrack = null;
            for (Map.Entry<String, Float> entry : trackMap.entrySet()) {
                roll -= entry.getValue();
                if (roll <= 0.0F) {
                    chosenTrack = entry;
                    break;
                }
            }

            if (chosenTrack == null && !trackMap.isEmpty()) {
                chosenTrack = trackMap.entrySet().iterator().next();
            }

            if (chosenTrack != null) {
                String trackName = chosenTrack.getKey();
                float trackWeight = chosenTrack.getValue();

                cooldowns.put(trackName, 10);

                Text message = Text.literal("Chosen Track: ").formatted(Formatting.WHITE).copy()
                        .append(Text.literal(trackName).setStyle(Style.EMPTY.withColor(Formatting.AQUA)
                                .withUnderline(true)))
                        .append(Text.literal(" (Laps: ").formatted(Formatting.WHITE))
                        .append(Text.literal(String.valueOf(laps)).setStyle(Style.EMPTY.withColor(Formatting.AQUA)
                                .withUnderline(true)))
                        .append(Text.literal(" Pits: ").formatted(Formatting.WHITE))
                        .append(Text.literal(String.valueOf(pits)).setStyle(Style.EMPTY.withColor(Formatting.AQUA)
                                .withUnderline(true)))
                        .append(Text.literal(" Weight: ").formatted(Formatting.WHITE))
                        .append(Text.literal(String.valueOf(trackWeight)).setStyle(Style.EMPTY.withColor(Formatting.AQUA)
                                .withUnderline(true)))
                        .append(Text.literal(")").formatted(Formatting.WHITE));
                client.player.sendMessage(message, false);

                player.networkHandler.sendChatCommand(
                        "voterace " + trackName + " " + laps + " " + pits
                );
            }
        }
    }
}
