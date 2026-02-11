package me.contaria.seedqueue.mixin.client.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.contaria.seedqueue.SeedQueue;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

//    @WrapOperation(
//            method = "setupTerrain",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/client/render/WorldRenderer;isInChunk(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/world/BuiltChunk;)Z"
//            )
//    )
//    private boolean alwaysDeferChunkUpdates(WorldRenderer worldRenderer, BlockPos pos, BuiltChunk chunk, Operation<Boolean> original) {
//        return !SeedQueue.isOnWall() && original.call(worldRenderer, pos, chunk);
//    }

    @ModifyExpressionValue(
            method = {
                    "method_1374",
                    "reload()V"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/option/GameOptions;viewDistance:I"
            )
    )
    private int modifyViewDistance(int viewDistance) {
        if (SeedQueue.isOnWall()) {
            return SeedQueue.config.previewChunkDistance;
        }
        return viewDistance;
    }
}
