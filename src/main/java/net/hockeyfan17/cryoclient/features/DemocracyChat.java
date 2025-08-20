package net.hockeyfan17.cryoclient.features;

import net.hockeyfan17.cryoclient.CryoConfig;
import net.hockeyfan17.cryoclient.CryoTrackConfig;
import net.minecraft.client.MinecraftClient;
import java.util.Objects;

public class DemocracyChat {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    public static String messageType2(String track){
        return " One cannot help but observe a certain... " +
                "creative stagnation in our voteraces of late. " +
                "Must we always resort to " +
                "\""+track+"\"? " +
                "A fine course, yes—but variety, dear friends, is the hallmark of refined taste.";
    }

    public static String messageType1(){
        return "The strongest argument against democracy is " +
                "frosthex players consistently voting for and picking the worst " +
                "race tracks on the server and then 80% of the players who voted " +
                "for the track leaving half way through the race after realizing their mistake.";
    }

    public static void democracyChatFunction(String rawMessage){
        if (CryoConfig.INSTANCE.democracyChatToggle && rawMessage.contains("--> Click to join a race on")) {
            String[] Array = rawMessage.split("Click to join a race on", 2);
            String[] Array1 = Array[1].split("\\(", 2);
            String trackName = Array1[0].replaceAll("\\s+", "").toLowerCase();

            for(String track : CryoTrackConfig.INSTANCE.trackList) {
                if(Objects.equals(track, trackName)) {
                    if(CryoConfig.INSTANCE.messageTypeToggle){
                        Objects.requireNonNull(client.getNetworkHandler()).sendChatMessage(messageType2(Array1[0]));
                    }
                    else {
                        Objects.requireNonNull(client.getNetworkHandler()).sendChatMessage(messageType1());
                    }
                    break;
                }
            }
        }
    }
}