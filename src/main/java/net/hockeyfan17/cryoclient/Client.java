package net.hockeyfan17.cryoclient;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.hockeyfan17.cryoclient.features.BoatYaw;

import net.fabricmc.api.ClientModInitializer;

public class Client implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        // BoatYaw Command //
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            BoatYaw.BoatYawCommand(dispatcher);
        });

        BoatYaw.BoatYawHud();

        // RotationsNeeded Command //
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            BoatYaw.RotationsNeededCommand(dispatcher);
        });
    }
}
