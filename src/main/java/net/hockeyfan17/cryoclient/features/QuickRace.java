package net.hockeyfan17.cryoclient.features;

import net.hockeyfan17.cryoclient.CryoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Random;

public class QuickRace {
    public static void VoteTrack(int laps, int pits) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player != null) {
            Random random = new Random();
            String track = CryoConfig.INSTANCE.quickRaceTrackList[random.nextInt(CryoConfig.INSTANCE.quickRaceTrackList.length)];

            String command = "/voterace " + track + " " + laps + " " + pits;

            Text message = Text.literal("Voted for ").formatted(Formatting.WHITE).copy()
                    .append(Text.literal(command).setStyle(Style.EMPTY.withColor(Formatting.valueOf("AQUA"))
                            .withUnderline(true)
                    ));
            client.player.sendMessage(message, false);

            player.networkHandler.sendChatCommand(
                    "voterace " + track + " " + laps + " " + pits
            );
        }
    }
}
