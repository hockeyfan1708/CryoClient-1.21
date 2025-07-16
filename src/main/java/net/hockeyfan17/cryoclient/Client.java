package net.hockeyfan17.cryoclient;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.hockeyfan17.cryoclient.features.BoatYaw;
import net.hockeyfan17.cryoclient.features.DemocracyChat;
import net.hockeyfan17.cryoclient.features.HidePassengers;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class Client implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            CryoConfig.INSTANCE.load();
            CryoTrackConfig.INSTANCE.load();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            CryoConfig.INSTANCE.save();
            CryoTrackConfig.INSTANCE.save();
        });

        CryoConfig.INSTANCE.load();
        CryoTrackConfig.INSTANCE.load();

        // BoatYaw Command //
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            BoatYaw.BoatYawCommand(dispatcher);
        });

        BoatYaw.BoatYawHud();

        // RotationsNeeded Command //
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            BoatYaw.RotationsNeededCommand(dispatcher);
        });

        // Hide Passengers Command //
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            HidePassengers.HidePassengerCommand(dispatcher);
        });


        // Democracy Chat Command //
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            DemocracyChat.DemocracyChatCommand(dispatcher);
        });

        ClientReceiveMessageEvents.ALLOW_GAME.register((message, sender) -> {
            String rawMessage = message.getString();
            DemocracyChat.democracyChatFunction(rawMessage);
            return true;
        });
    }
}
