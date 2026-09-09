package me.contaria.seedqueue.mixin.client.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import me.contaria.seedqueue.SeedQueue;
import me.contaria.seedqueue.worldpreview.WorldPreview;
import net.minecraft.client.render.WorldRenderer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @ModifyExpressionValue(
            method = {
                    "method_1374",
                    "reload()V"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/option/GameOptions;viewDistance:I",
                    opcode = Opcodes.GETFIELD
            )
    )
    private int modifyViewDistance(int viewDistance) {
        if (SeedQueue.isOnWall()) {
            return SeedQueue.config.previewChunkDistance;
        }
        return viewDistance;
    }

    @WrapWithCondition(
            method = "method_1368",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/WorldRenderer;method_1366(ID)V"
            )
    )
    private boolean doNotRenderChunksWhileBuildingOnWall(WorldRenderer worldRenderer, int d, double v) {
        return !WorldPreview.buildingPreview;
    }
}
