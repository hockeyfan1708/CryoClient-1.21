package net.hockeyfan17.cryoclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.hockeyfan17.cryoclient.features.BoatYaw;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CryoConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().resolve("cryoclient").toFile(), "values.json");
    public static CryoConfig INSTANCE = new CryoConfig();
    public boolean boatYawToggle;
    public boolean hidePassengersToggle;
    public boolean democracyChatToggle;
    public boolean messageTypeToggle;
    public boolean pitReminderToggle;

    public void load() {
        try {
            if (CONFIG_FILE.exists()) {
                CryoConfig loaded = GSON.fromJson(new FileReader(CONFIG_FILE), CryoConfig.class);
                this.boatYawToggle = loaded.boatYawToggle;
                this.hidePassengersToggle = loaded.hidePassengersToggle;
                this.democracyChatToggle = loaded.democracyChatToggle;
                this.messageTypeToggle = loaded.messageTypeToggle;
                this.pitReminderToggle = loaded.pitReminderToggle;
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