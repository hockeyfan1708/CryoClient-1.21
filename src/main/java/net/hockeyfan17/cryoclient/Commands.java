package net.hockeyfan17.cryoclient;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Iterator;
import java.util.Objects;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.hockeyfan17.cryoclient.features.BoatYaw.totalRotationNeeded;
import static net.hockeyfan17.cryoclient.features.DemocracyChat.messageType1;
import static net.hockeyfan17.cryoclient.features.DemocracyChat.messageType2;

public class Commands {

    public static MinecraftClient client = MinecraftClient.getInstance();

    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        if (client == null || client.player == null) return;

        var root = ClientCommandManager.literal("CryoClient");
        var alias = ClientCommandManager.literal("cc");

        // === Subcommands === //

        // HidePassengers //
        var hidePassengersCmd = ClientCommandManager.literal("HidePassengers")
                .executes(context -> {
                    CryoConfig.INSTANCE.hidePassengersToggle = !CryoConfig.INSTANCE.hidePassengersToggle;
                    Text message = Main.CryoClientName.copy()
                            .append(Text.literal("Hide Passengers ").formatted(Formatting.GRAY))
                            .append(Text.literal(CryoConfig.INSTANCE.hidePassengersToggle ? "Enabled" : "Disabled")
                                    .formatted(CryoConfig.INSTANCE.hidePassengersToggle ? Formatting.GREEN : Formatting.RED));
                    client.player.sendMessage(message);
                    return 1;
                });

        // BoatYaw //
        var boatYawCmd = ClientCommandManager.literal("BoatYaw")
                .executes(context -> {
                    CryoConfig.INSTANCE.boatYawToggle = !CryoConfig.INSTANCE.boatYawToggle;
                    Text message = Main.CryoClientName.copy()
                            .append("Boat Yaw ").formatted(Formatting.GRAY)
                            .append(Text.literal(CryoConfig.INSTANCE.boatYawToggle ? "Enabled" : "Disabled")
                                    .formatted(CryoConfig.INSTANCE.boatYawToggle ? Formatting.GREEN : Formatting.RED));
                    client.player.sendMessage(message);
                    return 1;
                });

        // RotationsNeeded //
        var rotationsNeededCmd = ClientCommandManager.literal("RotationsNeeded")
                .then(argument("angle", FloatArgumentType.floatArg())
                        .executes(context -> {
                            totalRotationNeeded(FloatArgumentType.getFloat(context, "angle"));
                            return 1;
                        })

                )
                .executes(context -> {
                    totalRotationNeeded(90.0);
                    return 1;
                });

        // DemocracyChat //
        var democracyChatCmd = ClientCommandManager.literal("DemocracyMessage")
                .then(literal("Toggle")
                        .executes(context -> {
                            CryoConfig.INSTANCE.democracyChatToggle = !CryoConfig.INSTANCE.democracyChatToggle;
                            Text message = Main.CryoClientName.copy()
                                    .append(Text.literal("Democracy Messages ").formatted(Formatting.GRAY))
                                    .append(Text.literal(CryoConfig.INSTANCE.democracyChatToggle ? "Enabled" : "Disabled")
                                            .formatted(CryoConfig.INSTANCE.democracyChatToggle ? Formatting.GREEN : Formatting.RED));
                            client.player.sendMessage(message);
                            return 1;
                        })
                )
                .then(literal("SwapMessage")
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
                        })
                )
                .then(literal("ShowMessage")
                        .executes(context -> {
                            String messageText = CryoConfig.INSTANCE.messageTypeToggle
                                    ? messageType2("(Track Name)")
                                    : messageType1();
                            client.player.sendMessage(Main.CryoClientName.copy()
                                    .append(Text.literal(messageText)).formatted(Formatting.GRAY));
                            return 1;
                        })
                )
                .then(literal("Track")
                        .then(literal("Add")
                                .then(argument("track", StringArgumentType.word())
                                        .executes(context -> {
                                            String trackName = StringArgumentType.getString(context, "track").toLowerCase();
                                            if (!CryoTrackConfig.INSTANCE.trackList.contains(trackName)) {
                                                CryoTrackConfig.INSTANCE.trackList.add(trackName);
                                                CryoTrackConfig.INSTANCE.save();
                                                client.player.sendMessage(Main.CryoClientName.copy()
                                                        .append(Text.literal(trackName + " added!!")).formatted(Formatting.GRAY));
                                            } else {
                                                client.player.sendMessage(Main.CryoClientName.copy()
                                                        .append(Text.literal(trackName + " already exists!")).formatted(Formatting.GRAY));
                                            }
                                            return 1;
                                        })
                                )
                        )
                        .then(literal("Remove")
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
                                        })
                                )
                        )
                        .then(literal("List")
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
                                })
                        )
                )
                .executes(context -> {
                    CryoConfig.INSTANCE.democracyChatToggle = !CryoConfig.INSTANCE.democracyChatToggle;
                    Text message = Main.CryoClientName.copy()
                            .append(Text.literal("Democracy Messages ").formatted(Formatting.GRAY))
                            .append(Text.literal(CryoConfig.INSTANCE.democracyChatToggle ? "Enabled" : "Disabled")
                                    .formatted(CryoConfig.INSTANCE.democracyChatToggle ? Formatting.GREEN : Formatting.RED));
                    client.player.sendMessage(message);
                    return 1;
                });

        // BoatTrail //
        var boatTrailCmd = ClientCommandManager.literal("BoatTrail")
                .executes(context -> {
                    CryoConfig.INSTANCE.boatTrailToggle = !CryoConfig.INSTANCE.boatTrailToggle;
                    Text message = Main.CryoClientName.copy()
                            .append("Boat Trail ").formatted(Formatting.GRAY)
                            .append(Text.literal(CryoConfig.INSTANCE.boatTrailToggle ? "Enabled" : "Disabled")
                                    .formatted(CryoConfig.INSTANCE.boatTrailToggle ? Formatting.GREEN : Formatting.RED));
                    client.player.sendMessage(message);
                    return 1;
                });

        // PitReminder //
        var pitReminderCmd = ClientCommandManager.literal("PitReminders")
                .executes(context -> {
                    CryoConfig.INSTANCE.pitReminderToggle = !CryoConfig.INSTANCE.pitReminderToggle;
                    Text message = Main.CryoClientName.copy()
                            .append(Text.literal("PitReminder ").formatted(Formatting.GRAY))
                            .append(Text.literal(CryoConfig.INSTANCE.pitReminderToggle ? "Enabled" : "Disabled")
                                    .formatted(CryoConfig.INSTANCE.pitReminderToggle ? Formatting.GREEN : Formatting.RED));
                    client.player.sendMessage(message);
                    return 1;
                });

        root.then(hidePassengersCmd);
        root.then(boatYawCmd);
        root.then(rotationsNeededCmd);
        root.then(democracyChatCmd);
        root.then(boatTrailCmd);
        root.then(pitReminderCmd);

        dispatcher.register(root);
        dispatcher.register(alias.redirect(root.build()));
    }
}
