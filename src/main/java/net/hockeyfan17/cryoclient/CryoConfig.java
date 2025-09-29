package net.hockeyfan17.cryoclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public boolean boatTrailUnderGlowToggle;
    public boolean icePlatesToggle;
    public boolean boatPriorityDisplayToggle;
    public boolean yellowArrowBoatPrioDisplay;
    public boolean pigStopDisplayToggle;
    public boolean pigStopDisplayHighlightSquareToggle;
    public boolean pigStopDisplayHideChatToggle = true;
    public int pigStopDisplaySize = 4;
    public int pigStopDisplayColor = 0xFF00a9f5;
    public long trailDuration = 30000L;
    public Float renderDistance = 100F;
    public int trailWidth = 3;
    public int icePlateRenderDistance = 100;
    public Map<String, Float> quickRaceTrackList = new HashMap<>() {{
        put("28g", 10.0F);
        put("CyberWorld2", 10.0F);
        put("NorthernWaterTribe", 10.0F);
        put("Mementos", 10.0F);
        put("Australia2024", 10.0F);
        put("Bahrain2024", 10.0F);
        put("Boingburg", 10.0F);

        put("4thLayerGFR", 8.0F);
        put("FuegoValley", 8.0F);

        put("Miami2024", 6.5F);
        put("Monza2023", 6.5F);
        put("EebreeManor", 6.5F);
        put("Brannheim", 6.5F);
        put("Anatidaephobia", 6.5F);

        put("MallardPark", 5.5F);
        put("DireWood", 5.5F);
        put("BCC25", 5.5F);

        put("BrimstoneSpeedway", 3.5F);
        put("MexicoFC1", 3.5F);

        put("Canada2024", 3.0F);
        put("Imola2024", 3.0F);
        put("CircuitoftheAmericas", 3.0F);
        put("Triton", 3.0F);
        put("IceColdSigma", 3.0F);
        put("Qatar2023", 3.0F);
        put("RedBullRing3D", 3.0F);

        put("SpaFrancorchamps2023", 2.5F);
        put("ContinentalIsland", 2.5F);

        put("BCC24", 2.0F);
        put("LasVegas", 2.0F);
        put("Weherua", 2.0F);

        put("Canada2023", 0.5F);
        put("Australia2023", 0.5F);
    }};
    public List<String> trackList = new ArrayList<>();

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
                this.boatTrailUnderGlowToggle = loaded.boatTrailUnderGlowToggle;
                this.quickRaceTrackList = loaded.quickRaceTrackList;
                this.trailWidth = loaded.trailWidth;
                this.icePlateRenderDistance = loaded.icePlateRenderDistance;
                this.icePlatesToggle = loaded.icePlatesToggle;
                this.trackList = loaded.trackList;
                this.boatPriorityDisplayToggle = loaded.boatPriorityDisplayToggle;
                this.yellowArrowBoatPrioDisplay = loaded.yellowArrowBoatPrioDisplay;
                this.pigStopDisplayToggle = loaded.pigStopDisplayToggle;
                this.pigStopDisplayColor = loaded.pigStopDisplayColor;
                this.pigStopDisplaySize = loaded.pigStopDisplaySize;
                this.pigStopDisplayHighlightSquareToggle = loaded.pigStopDisplayHighlightSquareToggle;
                this.pigStopDisplayHideChatToggle = loaded.pigStopDisplayHideChatToggle;
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