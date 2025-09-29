package net.hockeyfan17.cryoclient.features;

import net.hockeyfan17.cryoclient.CryoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;
import java.util.Random;

public class QuickRace {
    public static void VoteTrack(int laps, int pits) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player != null) {
            Random random = new Random();

            Map<String, Float> trackMap = CryoConfig.INSTANCE.quickRaceTrackList;

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

                trackMap.remove(chosenTrack.getKey());
            }
        }
    }
}
