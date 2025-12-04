package me.contaria.seedqueue.mixin.client.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.contaria.seedqueue.SeedQueue;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.BuiltChunk;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @WrapOperation(
            method = "setupTerrain",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/WorldRenderer;isInChunk(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/world/BuiltChunk;)Z"
            )
    )
    private boolean alwaysDeferChunkUpdates(WorldRenderer worldRenderer, BlockPos pos, BuiltChunk chunk, Operation<Boolean> original) {
        return !SeedQueue.isOnWall() && original.call(worldRenderer, pos, chunk);
    }

    @ModifyArg(
            method = "reload()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/BuiltChunkStorage;<init>(Lnet/minecraft/world/World;ILnet/minecraft/client/render/WorldRenderer;Lnet/minecraft/client/render/world/ChunkRenderFactory;)V"
            ),
            index = 1
    )
    private int modifyViewDistance(int viewDistance) {
        if (SeedQueue.isOnWall()) {
            return SeedQueue.config.previewChunkDistance;
        }
        return viewDistance;
    }
}
