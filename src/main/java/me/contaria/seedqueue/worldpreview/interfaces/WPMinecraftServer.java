package me.contaria.seedqueue.worldpreview.interfaces;

import net.minecraft.util.math.Vec3d;

public interface WPMinecraftServer {
    void worldpreview$setPreviewSpawnPos(Vec3d pos);

    Vec3d worldpreview$getPreviewSpawnPos();

    void worldpreview$clearPreviewSpawnPos();
}
