package me.contaria.seedqueue.mixin.worldpreview.server;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.contaria.seedqueue.interfaces.SQMinecraftServer;
import me.contaria.seedqueue.worldpreview.WorldPreview;
import me.contaria.seedqueue.worldpreview.interfaces.WPMinecraftServer;
import me.contaria.seedqueue.worldpreview.interfaces.WPServerChunkProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements WPMinecraftServer {
    @Shadow
    public ServerWorld[] worlds;

    @Unique
    protected volatile boolean killed;
    @Unique
    private volatile boolean tooLateToKill;
    @Unique
    private boolean shouldConfigurePreview;

    @Inject(
            method = "prepareWorlds",
            at = @At("HEAD")
    )
    private void setShouldConfigurePreview(CallbackInfo ci) {
        this.shouldConfigurePreview = ((SQMinecraftServer) this).seedQueue$inQueue();
    }

    @Inject(
            method = "prepareWorlds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/ServerChunkProvider;getOrGenerateChunk(II)Lnet/minecraft/world/chunk/Chunk;",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void killWorldGen(CallbackInfo ci) {
        if (this.killed) {
            ci.cancel();
        }
    }

    @Inject(
            method = "prepareWorlds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;save()V"
            )
    )
    private void configureWorldPreview(CallbackInfo ci) {
        ServerWorld world = this.worlds[0];
        BlockPos pos = world.getSpawnPos();
        if (this.shouldConfigurePreview && world.chunkCache.chunkExists(pos.getX() >> 4, pos.getZ() >> 4)) {
            WorldPreview.configure(world);
            ((WPServerChunkProvider) world.getChunkProvider()).worldpreview$sendData();
            this.shouldConfigurePreview = false;
        }
    }

    @ModifyExpressionValue(
            method = "run",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;setupServer()Z"
            )
    )
    private synchronized boolean killServer(boolean original) {
        this.tooLateToKill = true;
        return original && !this.killed;
    }

    @Override
    public synchronized boolean worldpreview$kill() {
        if (this.tooLateToKill) {
            return false;
        }
        return this.killed = true;
    }
}
