package net.hockeyfan17.cryoclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CryoTrackConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File TRACKS_FILE = new File(FabricLoader.getInstance().getConfigDir().resolve("cryoclient").toFile(), "tracks.json");

    public static CryoTrackConfig INSTANCE = new CryoTrackConfig();

    public List<String> trackList = new ArrayList<>();

    public void load() {
        try {
            if (TRACKS_FILE.exists()) {
                CryoTrackConfig loaded = GSON.fromJson(new FileReader(TRACKS_FILE), CryoTrackConfig.class);
                this.trackList = loaded.trackList;
            } else {
                save();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            TRACKS_FILE.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(TRACKS_FILE);
            GSON.toJson(this, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}