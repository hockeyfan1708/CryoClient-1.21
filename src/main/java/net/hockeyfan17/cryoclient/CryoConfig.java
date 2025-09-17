package net.hockeyfan17.cryoclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.hockeyfan17.cryoclient.features.BoatYaw;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CryoConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().resolve("cryoclient").toFile(), "values.json");
    public static CryoConfig INSTANCE = new CryoConfig();
    public boolean boatYawToggle;
    public boolean hidePassengersToggle;
    public boolean democracyChatToggle;
    public boolean messageTypeToggle;
    public boolean pitReminderToggle;
    public boolean boatTrailToggle;
    public boolean trailUnderGlow;
    public long trailDuration = 30000L;
    public Float renderDistance = 100F;
    public int trailWidth = 3;
    public String[] quickRaceTrackList = {
            "28g", "CyberWorld2", "NorthernWaterTribe", "Mementos",
            "Australia2024", "Bahrain2024", "Australia2023", "Miami2024",
            "Boingburg", "Monza2023", "Canada2024", "Imola2024",
            "CircuitoftheAmericas", "BCC24", "BCC25", "EebreeManor", "4thLayerGFR",
            "FuegoValley", "Brannheim", "BrimstoneSpeedway", "Triton", "IceColdSigma",
            "MallardPark", "LasVegas", "SpaFrancorchamps2023", "Qatar2023", "MexicoFC1",
            "RedBullRing3D", "Anatidaephobia", "Weherua", "DireWood", "Canada2023", "ContinentalIsland"
    };

//    public Map<UUID, float[]> playerColors = new HashMap<>();

    public void load() {
        try {
            if (CONFIG_FILE.exists()) {
                CryoConfig loaded = GSON.fromJson(new FileReader(CONFIG_FILE), CryoConfig.class);
                this.boatYawToggle = loaded.boatYawToggle;
                this.hidePassengersToggle = loaded.hidePassengersToggle;
                this.democracyChatToggle = loaded.democracyChatToggle;
                this.messageTypeToggle = loaded.messageTypeToggle;
                this.pitReminderToggle = loaded.pitReminderToggle;
                this.boatTrailToggle = loaded.boatTrailToggle;
                this.trailDuration = loaded.trailDuration;
                this.renderDistance = loaded.renderDistance;
                this.trailUnderGlow = loaded.trailUnderGlow;
                this.quickRaceTrackList = loaded.quickRaceTrackList;
                this.trailWidth = loaded.trailWidth;
//                this.playerColors = loaded.playerColors;
            } else {
                save();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(CONFIG_FILE);
            GSON.toJson(this, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}