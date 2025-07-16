package net.hockeyfan17.cryoclient.features;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.hockeyfan17.cryoclient.CryoConfig;
import net.hockeyfan17.cryoclient.CryoTrackConfig;
import net.hockeyfan17.cryoclient.Main;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class DemocracyChat {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    private static String messageType2(String track){
        return " One cannot help but observe a certain... " +
                "creative stagnation in our voteraces of late. " +
                "Must we always resort to " +
                "\""+track+"\"? " +
                "A fine course, yes—but variety, dear friends, is the hallmark of refined taste.";
    }

    private static String messageType1(){
        return "The strongest argument against democracy is " +
                "frosthex players consistently voting for and picking the worst " +
                "race tracks on the server and then 80% of the players who voted " +
                "for the track leaving half way through the race after realizing their mistake.";
    }

    public static void DemocracyChatCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        assert client.player != null;

        List<String> aliases = List.of("cc", "CryoClient");

        for (String alias : aliases) {
            LiteralArgumentBuilder<FabricClientCommandSource> root = literal(alias)
                    .then(literal("DemocracyMessage")
                            .then(literal("Toggle")
                                    .executes(context -> {
                                        CryoConfig.INSTANCE.democracyChatToggle = !CryoConfig.INSTANCE.democracyChatToggle;
                                        Text message = Main.CryoClientName.copy()
                                                .append(Text.literal("Democracy Messages ").formatted(Formatting.GRAY))
                                                .append(Text.literal(CryoConfig.INSTANCE.democracyChatToggle ? "Enabled" : "Disabled")
                                                        .formatted(CryoConfig.INSTANCE.democracyChatToggle ? Formatting.GREEN : Formatting.RED));
                                        client.player.sendMessage(message);
                                        return 1;
                                    }))
                            .then(literal("ChangeMessage")
                                    .executes(context -> {
                                        CryoConfig.INSTANCE.messageTypeToggle = !CryoConfig.INSTANCE.messageTypeToggle;
                                        Text message = Main.CryoClientName.copy()
                                                .append(Text.literal("Message type has been changed! ").formatted(Formatting.GRAY))
                                                .append(Text.literal("Show")
                                                        .setStyle(Style.EMPTY.withColor(0x54fbfc)
                                                                .withBold(true).withUnderline(true)
                                                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + alias + " DemocracyMessage ShowMessage"))));
                                        client.player.sendMessage(message);
                                        return 1;
                                    }))
                            .then(literal("ShowMessage")
                                    .executes(context -> {
                                        String messageText = CryoConfig.INSTANCE.messageTypeToggle
                                                ? messageType2("(Track Name)")
                                                : messageType1();
                                        client.player.sendMessage(Main.CryoClientName.copy()
                                                .append(Text.literal(messageText)).formatted(Formatting.GRAY));
                                        return 1;
                                    }))
                            .then(literal("AddTrack")
                                    .then(argument("track", StringArgumentType.word())
                                            .executes(context -> {
                                                String trackName = StringArgumentType.getString(context, "track").toLowerCase();
                                                if(!CryoTrackConfig.INSTANCE.trackList.contains(trackName)){
                                                    CryoTrackConfig.INSTANCE.trackList.add(trackName);
                                                    CryoTrackConfig.INSTANCE.save();
                                                    client.player.sendMessage(Main.CryoClientName.copy()
                                                            .append(Text.literal(trackName + " added!!")).formatted(Formatting.GRAY));
                                                }else{
                                                    client.player.sendMessage(Main.CryoClientName.copy()
                                                            .append(Text.literal(trackName + " already exists!")).formatted(Formatting.GRAY));
                                                }
                                                return 1;
                                            })))
                            .then(literal("RemoveTrack")
                                    .then(argument("track", StringArgumentType.word())
                                            .executes(context -> {
                                                String trackName = StringArgumentType.getString(context, "track").toLowerCase();
                                                Iterator<String> iterator = CryoTrackConfig.INSTANCE.trackList.iterator();
                                                boolean removed = false;

                                                while(iterator.hasNext()) {
                                                    if(Objects.equals(iterator.next(), trackName)){
                                                        iterator.remove();
                                                        CryoTrackConfig.INSTANCE.save();
                                                        client.player.sendMessage(Main.CryoClientName.copy()
                                                                .append(Text.literal(trackName + " has been removed!")).formatted(Formatting.GRAY));
                                                        removed = true;
                                                        break;
                                                    }
                                                }

                                                if(!removed){
                                                    client.player.sendMessage(Main.CryoClientName.copy()
                                                            .append(Text.literal(trackName + " not found")).formatted(Formatting.GRAY));
                                                }
                                                return 1;
                                            })))
                            .then(literal("ShowTrack")
                                    .executes(context -> {
                                        if(CryoTrackConfig.INSTANCE.trackList.isEmpty()){
                                            client.player.sendMessage(Main.CryoClientName.copy()
                                                    .append(Text.literal("Track list is empty.").formatted(Formatting.GRAY)));
                                        }else{
                                            Text message = Main.CryoClientName.copy()
                                                    .append(Text.literal("Current Tracks:").formatted(Formatting.AQUA));
                                            for (String track : CryoTrackConfig.INSTANCE.trackList) {
                                                message = message.copy().append(Text.literal("\n- " + track).formatted(Formatting.GRAY));
                                            }
                                            message = message.copy().append(Text.literal("\n"));
                                            client.player.sendMessage(message);
                                        }
                                        return 1;
                                    }))
                            .executes(context -> {
                                CryoConfig.INSTANCE.democracyChatToggle = !CryoConfig.INSTANCE.democracyChatToggle;
                                Text message = Main.CryoClientName.copy()
                                        .append(Text.literal("Democracy Messages ").formatted(Formatting.GRAY))
                                        .append(Text.literal(CryoConfig.INSTANCE.democracyChatToggle ? "Enabled" : "Disabled")
                                                .formatted(CryoConfig.INSTANCE.democracyChatToggle ? Formatting.GREEN : Formatting.RED));
                                client.player.sendMessage(message);
                                return 1;
                            }));

            dispatcher.register(root);
        }

        CryoTrackConfig.INSTANCE.load();
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

//    private static final List<String> trackList = new ArrayList<>();
//    private static final Path TRACK_FILE = Path.of("config/trackfile.json");
//    private static final Gson GSON = new Gson();
//
//    private static void loadTrackList() {
//        if (!Files.exists(TRACK_FILE)) {
//            client.inGameHud.getChatHud().addMessage(Text.literal("File does not exist"));
//            return;
//        }
//        try (Reader reader = new FileReader(TRACK_FILE.toFile())) {
//            Type listType = new TypeToken<List<String>>() {}.getType();
//            List<String> loaded = GSON.fromJson(reader, listType);
//            trackList.clear();
//            if (loaded != null) trackList.addAll(loaded);
//        } catch (IOException e) {
//            client.inGameHud.getChatHud().addMessage(Text.literal("Friend list could not be loaded"));
//        }
//    }
//
//    private static void saveTrackList() {
//        try {
//            Files.createDirectories(TRACK_FILE.getParent());
//            try (Writer writer = new FileWriter(TRACK_FILE.toFile())) {
//                GSON.toJson(trackList, writer);
//            }
//        } catch (IOException e) {
//            client.inGameHud.getChatHud().addMessage(Text.literal("File could not be saved"));
//        }
//    }
}