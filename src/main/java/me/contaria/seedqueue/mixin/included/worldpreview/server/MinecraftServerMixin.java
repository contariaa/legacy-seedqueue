package me.contaria.seedqueue.mixin.included.worldpreview.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import me.contaria.seedqueue.SeedQueue;
import me.contaria.seedqueue.interfaces.SQMinecraftServer;
import me.contaria.seedqueue.worldpreview.WorldPreview;
import me.contaria.seedqueue.worldpreview.WorldPreviewProperties;
import me.contaria.seedqueue.worldpreview.interfaces.WPMinecraftServer;
import me.contaria.seedqueue.worldpreview.interfaces.WPServerChunkProvider;
import me.contaria.speedrunapi.config.SpeedrunConfigAPI;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements SQMinecraftServer, WPMinecraftServer {
    @Shadow
    public ServerWorld[] worlds;

    @Unique
    @Nullable
    private Vec3d previewSpawnPos;
    @Unique
    private int previewPerspective;

    @WrapOperation(
            method = "prepareWorlds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;getWorldSpawnPos()Lnet/minecraft/util/math/BlockPos;"
            )
    )
    private BlockPos configureWorldPreview(ServerWorld world, Operation<BlockPos> original) {
        if (this.shouldConfigurePreview()) {
            this.previewSpawnPos = this.calculatePreviewSpawnPos(world);
            this.previewPerspective = (int) SpeedrunConfigAPI.getConfigValueOptionally("standardsettings", "perspective").orElse(0);
            if (this.shouldGenerateFakePreview()) {
                return new BlockPos((int) this.previewSpawnPos.x, (int) this.previewSpawnPos.y, (int) this.previewSpawnPos.z);
            }
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
            if (this.previewPerspective != 2) {
                return -16;
            }
            return -SeedQueue.config.previewChunkDistance * 16;
        }
        return constant;
    }

    @ModifyConstant(
            method = "prepareWorlds",
            constant = @Constant(
                    intValue = 192,
                    ordinal = 1
            )
    )
    private int reduceChunksPositiveX(int constant) {
        if (this.shouldGenerateFakePreview()) {
            return SeedQueue.config.previewChunkDistance * 16;
        }
        return constant;
    }

    @ModifyConstant(
            method = "prepareWorlds",
            constant = @Constant(
                    intValue = 192,
                    ordinal = 2
            )
    )
    private int reduceChunksPositiveZ(int constant) {
        if (this.shouldGenerateFakePreview()) {
            if (this.previewPerspective == 2) {
                return 16;
            }
            return SeedQueue.config.previewChunkDistance * 16;
        }
        return constant;
    }

    @Inject(
            method = "prepareWorlds",
            at = @At("TAIL")
    )
    private void sendWorldPreviewData(CallbackInfo ci) {
        if (this.shouldConfigurePreview()) {
            ServerWorld world = this.worlds[0];
            WorldPreviewProperties properties = WorldPreview.configure(world, this.previewPerspective);
            ((WPServerChunkProvider) world.getChunkProvider()).worldpreview$sendData(properties);
            this.seedQueue$getEntry().setPreviewProperties(properties);
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

    @Unique
    private Vec3d calculatePreviewSpawnPos(ServerWorld world) {
        // see ServerPlayerEntity#<init>
        Random random = new Random();

        BlockPos spawnPos = world.getWorldSpawnPos();
        int x = spawnPos.x;
        int z = spawnPos.z;
        int y = spawnPos.y;

        int radius = Math.max(5, world.getServer().getSpawnProtectionRadius() - 6);
        x += random.nextInt(radius * 2) - radius;
        z += random.nextInt(radius * 2) - radius;
        // skip y increasing, since this calculation is only for fake preview spawn pos

        return Vec3d.of(x, y, z);
    }

    @Override
    public void worldpreview$setPreviewSpawnPos(Vec3d pos) {
        this.previewSpawnPos = pos;
    }

    @Override
    public Vec3d worldpreview$getPreviewSpawnPos() {
        return this.previewSpawnPos;
    }

    @Override
    public void worldpreview$clearPreviewSpawnPos() {
        this.previewSpawnPos = null;
    }
}
