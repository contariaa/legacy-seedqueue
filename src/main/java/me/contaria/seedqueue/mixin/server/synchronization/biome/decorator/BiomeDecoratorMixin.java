package me.contaria.seedqueue.mixin.server.synchronization.biome.decorator;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeDecorator;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(BiomeDecorator.class)
public abstract class BiomeDecoratorMixin {
    @Unique
    protected final ThreadLocal<World> threadedWorld = new ThreadLocal<>();
    @Unique
    protected final ThreadLocal<Random> threadedRandom = new ThreadLocal<>();
    @Unique
    protected final ThreadLocal<Integer> threadedX = new ThreadLocal<>();
    @Unique
    protected final ThreadLocal<Integer> threadedZ = new ThreadLocal<>();

    @Redirect(
            method = "method_3848",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/BiomeDecorator;world:Lnet/minecraft/world/World;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedWorld(BiomeDecorator decorator, World world) {
        this.threadedWorld.set(world);
    }

    @Redirect(
            method = "method_3848",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/BiomeDecorator;random:Ljava/util/Random;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedRandom(BiomeDecorator decorator, Random random) {
        this.threadedRandom.set(random);
    }

    @Redirect(
            method = "method_3848",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/BiomeDecorator;field_4689:I",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedX(BiomeDecorator decorator, int x) {
        this.threadedX.set(x);
    }

    @Redirect(
            method = "method_3848",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/BiomeDecorator;field_4690:I",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedZ(BiomeDecorator decorator, int z) {
        this.threadedZ.set(z);
    }

    @Inject(
            method = "method_3848",
            at = @At("TAIL")
    )
    private void removeThreadLocals(CallbackInfo ci) {
        this.threadedWorld.remove();
        this.threadedRandom.remove();
        this.threadedX.remove();
        this.threadedZ.remove();
    }

    @Redirect(
            method = {
                    "method_3848",
                    "generate",
                    "generateOre",
                    "method_3850"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/BiomeDecorator;world:Lnet/minecraft/world/World;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private World getThreadedWorld(BiomeDecorator decorator) {
        return this.threadedWorld.get();
    }

    @Redirect(
            method = {
                    "generate",
                    "generateOre",
                    "method_3850"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/BiomeDecorator;random:Ljava/util/Random;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private Random getThreadedRandom(BiomeDecorator decorator) {
        return this.threadedRandom.get();
    }

    @Redirect(
            method = {
                    "generate",
                    "generateOre",
                    "method_3850"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/BiomeDecorator;field_4689:I",
                    opcode = Opcodes.GETFIELD
            )
    )
    private int getThreadedX(BiomeDecorator decorator) {
        return this.threadedX.get();
    }

    @Redirect(
            method = {
                    "generate",
                    "generateOre",
                    "method_3850"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/BiomeDecorator;field_4690:I",
                    opcode = Opcodes.GETFIELD
            )
    )
    private int getThreadedZ(BiomeDecorator decorator) {
        return this.threadedZ.get();
    }
}
