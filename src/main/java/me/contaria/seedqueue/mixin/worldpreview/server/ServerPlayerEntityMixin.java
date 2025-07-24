package me.contaria.seedqueue.mixin.worldpreview.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.contaria.seedqueue.worldpreview.WPFakeServerPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Unique
    private static final ThreadLocal<BlockPos> PREVIEW_SPAWNPOS = new ThreadLocal<>();

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/ServerPlayerEntity;refreshPositionAndAngles(Lnet/minecraft/util/math/BlockPos;FF)V"
            ),
            index = 0
    )
    private BlockPos setPreviewSpawnPos(BlockPos pos) {
        if (this.isWorldPreviewFakePlayer()) {
            PREVIEW_SPAWNPOS.set(pos);
            return pos;
        }
        BlockPos spawnPos = PREVIEW_SPAWNPOS.get();
        if (spawnPos != null) {
            PREVIEW_SPAWNPOS.remove();
            return spawnPos;
        }
        return pos;
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
