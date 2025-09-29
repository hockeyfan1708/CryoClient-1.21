package net.hockeyfan17.cryoclient.features;

import net.hockeyfan17.cryoclient.CryoConfig;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.ArrayDeque;
import java.util.Queue;

public class IcePressurePlate {

    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final Queue<ChunkPos> chunkQueue = new ArrayDeque<>();

    private static final int radius = 100;
    private static final int verticalRange = 20;
    private static final int chunksPerTick = 10;

    public static void register() {
        ClientTickEvents.END_WORLD_TICK.register(world -> {
            if (client.player == null || world == null) return;

            if (!CryoConfig.INSTANCE.icePlatesToggle) return;

            BlockPos playerPos = client.player.getBlockPos();
            int playerChunkX = playerPos.getX() >> 4;
            int playerChunkZ = playerPos.getZ() >> 4;
            int chunkRadius = (radius + 15) / 16;

            if (chunkQueue.isEmpty()) {
                for (int cx = playerChunkX - chunkRadius; cx <= playerChunkX + chunkRadius; cx++) {
                    for (int cz = playerChunkZ - chunkRadius; cz <= playerChunkZ + chunkRadius; cz++) {
                        chunkQueue.add(new ChunkPos(cx, cz));
                    }
                }
            }

            int minY = Math.max(0, playerPos.getY() - verticalRange);
            int maxY = Math.min(world.getHeight() - 1, playerPos.getY() + verticalRange);

            for (int i = 0; i < chunksPerTick; i++) {
                ChunkPos cp = chunkQueue.poll();
                if (cp == null) break;

                Chunk chunk = world.getChunk(cp.x, cp.z, ChunkStatus.FULL, false);
                if (chunk == null) continue;

                int startX = Math.max(cp.x << 4, playerPos.getX() - radius);
                int endX = Math.min((cp.x << 4) + 15, playerPos.getX() + radius);
                int startZ = Math.max(cp.z << 4, playerPos.getZ() - radius);
                int endZ = Math.min((cp.z << 4) + 15, playerPos.getZ() + radius);

                for (int x = startX; x <= endX; x++) {
                    for (int z = startZ; z <= endZ; z++) {
                        for (int y = minY; y <= maxY; y++) {
                            BlockPos pos = new BlockPos(x, y, z);
                            BlockState state = world.getBlockState(pos);
                            Block block = state.getBlock();

                            if (block instanceof AbstractPressurePlateBlock) {
                                Block below = world.getBlockState(pos.down()).getBlock();
                                Block doublebelow = world.getBlockState(pos.down().down()).getBlock();
                                if (below == Blocks.ICE || below == Blocks.FROSTED_ICE ||
                                        below == Blocks.PACKED_ICE || below == Blocks.BLUE_ICE) {
                                    if (doublebelow instanceof CommandBlock) {
                                        below = below;
                                    } else {
                                        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }
}