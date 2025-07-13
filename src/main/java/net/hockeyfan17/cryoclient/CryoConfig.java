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

    public void load() {
        try {
            if (CONFIG_FILE.exists()) {
                CryoConfig loaded = GSON.fromJson(new FileReader(CONFIG_FILE), CryoConfig.class);
                this.boatYawToggle = loaded.boatYawToggle;
                this.hidePassengersToggle = loaded.hidePassengersToggle;
            } else {
                save();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Save to file
    public void save() {
        try {
            CONFIG_FILE.getParentFile().mkdirs(); // ensure dir exists
            FileWriter writer = new FileWriter(CONFIG_FILE);
            GSON.toJson(this, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}