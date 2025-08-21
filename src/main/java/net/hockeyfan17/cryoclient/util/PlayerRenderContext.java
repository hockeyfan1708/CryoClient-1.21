package net.hockeyfan17.cryoclient.util;

import net.minecraft.entity.player.PlayerEntity;

public class PlayerRenderContext {
    private static final ThreadLocal<PlayerEntity> CURRENT = new ThreadLocal<>();

    public static void set(PlayerEntity player) {
        CURRENT.set(player);
    }

    public static PlayerEntity get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}