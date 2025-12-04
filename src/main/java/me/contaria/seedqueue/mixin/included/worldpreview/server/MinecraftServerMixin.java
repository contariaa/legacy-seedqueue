package me.contaria.seedqueue.mixin.included.worldpreview.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import me.contaria.seedqueue.SeedQueue;
import me.contaria.seedqueue.interfaces.SQMinecraftServer;
import me.contaria.seedqueue.worldpreview.WorldPreview;
import me.contaria.seedqueue.worldpreview.WorldPreviewProperties;
import me.contaria.seedqueue.worldpreview.interfaces.WPServerChunkProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements SQMinecraftServer {
    @Shadow
    public ServerWorld[] worlds;

    @WrapOperation(
            method = "prepareWorlds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;getSpawnPos()Lnet/minecraft/util/math/BlockPos;"
            )
    )
    private BlockPos configureWorldPreview(ServerWorld world, Operation<BlockPos> original, @Share("properties") LocalRef<WorldPreviewProperties> properties) {
        if (this.shouldConfigurePreview()) {
            properties.set(WorldPreview.configure(world));
            return properties.get().player.getBlockPos();
        }
        return original.call(world);
    }

    @ModifyConstant(
            method = "prepareWorlds",
            constant = @Constant(
                    intValue = -192,
                    ordinal = 0
            )
    )
    private int reduceChunksNegativeX(int constant) {
        if (this.shouldGenerateFakePreview()) {
            return -SeedQueue.config.previewChunkDistance * 16;
        }
        return constant;
    }

    @ModifyConstant(
            method = "prepareWorlds",
            constant = @Constant(
                    intValue = -192,
                    ordinal = 1
            )
    )
    private int reduceChunksNegativeZ(int constant) {
        if (this.shouldGenerateFakePreview()) {
            return -16;
        }
        return constant;
    }

    @ModifyConstant(
            method = "prepareWorlds",
            constant = @Constant(intValue = 192)
    )
    private int reduceChunksPositive(int constant) {
        if (this.shouldGenerateFakePreview()) {
            return SeedQueue.config.previewChunkDistance * 16;
        }
        return constant;
    }

    @Inject(
            method = "prepareWorlds",
            at = @At("TAIL")
    )
    private void sendWorldPreviewData(CallbackInfo ci, @Share("properties") LocalRef<WorldPreviewProperties> properties) {
        if (this.shouldConfigurePreview()) {
            ((WPServerChunkProvider) this.worlds[0].getChunkProvider()).worldpreview$sendData(properties.get());
            this.seedQueue$getEntry().setPreviewProperties(properties.get());
        }
    }

    @Unique
    private boolean shouldConfigurePreview() {
        return SeedQueue.config.shouldUseWall() && this.seedQueue$inQueue() && !this.seedQueue$getEntry().isLocked();
    }

    @Unique
    private boolean shouldGenerateFakePreview() {
        return this.shouldConfigurePreview() && SeedQueue.config.generateFakePreview;
    }
}
