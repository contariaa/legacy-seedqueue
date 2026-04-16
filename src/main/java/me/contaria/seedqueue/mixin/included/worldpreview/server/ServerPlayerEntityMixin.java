package me.contaria.seedqueue.mixin.included.worldpreview.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.contaria.seedqueue.worldpreview.WPFakeServerPlayerEntity;
import me.contaria.seedqueue.worldpreview.interfaces.WPMinecraftServer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Shadow
    @Final
    public MinecraftServer server;

    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/ServerPlayerEntity;refreshPositionAndAngles(DDDFF)V"
            )
    )
    private void setPreviewSpawnPos(ServerPlayerEntity player, double x, double y, double z, float yaw, float pitch, Operation<Void> original) {
        WPMinecraftServer server = (WPMinecraftServer) this.server;
        Vec3d spawnPos = server.worldpreview$getPreviewSpawnPos();
        if (this.isWorldPreviewFakePlayer()) {
            original.call(player, spawnPos.x, spawnPos.y, spawnPos.z, yaw, pitch);
            return;
        }
        if (spawnPos != null) {
            server.worldpreview$clearPreviewSpawnPos();
            original.call(player, spawnPos.x, spawnPos.y, spawnPos.z, yaw, pitch);
            return;
        }
        original.call(player, x, y, z, yaw, pitch);
    }

    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;createStatHandler(Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/stat/ServerStatHandler;"
            )
    )
    private ServerStatHandler doNotCreateStatHandler(PlayerManager playerManager, PlayerEntity player, Operation<ServerStatHandler> original) {
        if (this.isWorldPreviewFakePlayer()) {
            return null;
        }
        return original.call(playerManager, player);
    }

    @Unique
    private boolean isWorldPreviewFakePlayer() {
        return (Object) this instanceof WPFakeServerPlayerEntity;
    }
}
