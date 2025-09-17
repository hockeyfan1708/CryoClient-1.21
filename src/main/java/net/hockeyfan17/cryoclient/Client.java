package net.hockeyfan17.cryoclient;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.hockeyfan17.cryoclient.features.BoatTrail;
import net.hockeyfan17.cryoclient.features.BoatYaw;
import net.hockeyfan17.cryoclient.features.DemocracyChat;
import net.hockeyfan17.cryoclient.features.PitReminder;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.util.Identifier;

public class Client implements ClientModInitializer {
    public static final Identifier BOAT_YAW_LAYER = Identifier.of("cryoclient", "boat_yaw");
    public static final Identifier PIT_REMINDER_LAYER = Identifier.of("cryoclient", "pit_reminder");

    @Override
    public void onInitializeClient() {

        BoatTrail.init();
        HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> {
            layeredDrawer.attachLayerBefore(IdentifiedLayer.CHAT, BOAT_YAW_LAYER, BoatYaw::BoatYawHud);
        });
        HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> {
            layeredDrawer.attachLayerBefore(IdentifiedLayer.CHAT, PIT_REMINDER_LAYER, PitReminder::PitReminderHud);
        });

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

        // Command Register //
        ClientCommandRegistrationCallback.EVENT.register(Commands::registerCommands);

        ClientReceiveMessageEvents.ALLOW_GAME.register((message, sender) -> {
            String rawMessage = message.getString();
            DemocracyChat.democracyChatFunction(rawMessage);
            if(CryoConfig.INSTANCE.pitReminderToggle) {PitReminder.pitReminderFunction(rawMessage);}
            return true;
        });
    }
}
