package me.contaria.seedqueue.mixin.worldpreview.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.contaria.seedqueue.SeedQueue;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.BuiltChunk;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

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
}
