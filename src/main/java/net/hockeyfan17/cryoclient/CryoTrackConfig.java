package net.hockeyfan17.cryoclient;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.hockeyfan17.cryoclient.features.TrackDetector;
import net.minecraft.util.math.Vec3d;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CryoTrackConfig {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Vec3d.class, (JsonSerializer<Vec3d>) (src, typeOfSrc, context) ->
                    context.serialize(new double[]{src.x, src.y, src.z}))
            .registerTypeAdapter(Vec3d.class, (JsonDeserializer<Vec3d>) (json, typeOfT, context) -> {
                double[] arr = context.deserialize(json, double[].class);
                return new Vec3d(arr[0], arr[1], arr[2]);
            })
            .setPrettyPrinting()
            .create();
    public static CryoTrackConfig INSTANCE = new CryoTrackConfig();
    public Map<Integer, CheckpointData> checkpoints = new HashMap<>();
    public float finishTime;



    public String selectedTrack = null;
    public List<Vec3d> startPositions = new ArrayList<>();


    private File getTrackFile() {
        if (selectedTrack == null) return null;
        return new File(
                FabricLoader.getInstance().getConfigDir().resolve("cryoclient").resolve("tracks").toFile(),
                selectedTrack + ".json"
        );
    }

    public void load() {
        System.out.println("Load");
//        if (selectedTrack == null) return;
//        File file = getTrackFile();
//        assert file != null;
//        if (file.exists()) {
//            try (FileReader reader = new FileReader(file)) {
//                CryoTrackConfig loaded = GSON.fromJson(reader, CryoTrackConfig.class);
//                this.selectedTrack = loaded.selectedTrack;
//                this.startPositions = loaded.startPositions != null ? loaded.startPositions : new ArrayList<>();
//                this.checkpoints = loaded.checkpoints != null ? loaded.checkpoints : new HashMap<>();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            System.out.println("[CryoConfigLoad] Complete");
//        } else {
//            save();
//        }
    }

    public void save() {
        System.out.println("Save");
//        if (selectedTrack == null) return;
//        File file = getTrackFile();
//        file.getParentFile().mkdirs();
//        try (FileWriter writer = new FileWriter(file)) {
//            GSON.toJson(this, writer);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void addStartPos(Vec3d pos) {
        if (startPositions.size() >= 3) {
            startPositions.removeFirst();
        }
        startPositions.add(pos);
        save();
    }

    public void addCheckpointPos(int checkpointNumber, Vec3d pos) {
        // Get or create the checkpoint entry
        CheckpointData data = checkpoints.computeIfAbsent(
                checkpointNumber,
                k -> new CheckpointData(0, new ArrayList<>()) // time will be updated later
        );

        // Maintain max 3 positions for this checkpoint only
        if (data.positions.size() >= 3) {
            data.positions.removeFirst(); // remove oldest
        }
        data.positions.add(new double[]{pos.x, pos.y, pos.z});

        save();
    }


    public void addCheckpoint() {
        checkpoints.put(
                TrackDetector.checkpointNumber,
                new CheckpointData(
                        TrackDetector.checkpointTime,
                        checkpoints.getOrDefault(TrackDetector.checkpointNumber, new CheckpointData(0, new ArrayList<>())).positions
                )
        );
    }

    public static class CheckpointData {
        public float time;
        public List<double[]> positions;

        public CheckpointData(double time, List<double[]> posList) {
            this.time = (float) time;
            this.positions = new ArrayList<>(posList);
        }
    }
}