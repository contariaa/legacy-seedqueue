package me.contaria.seedqueue.mixin.server.synchronization.biome.feature;

import net.minecraft.block.Block;
import net.minecraft.world.gen.feature.FlowerPatchFeature;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FlowerPatchFeature.class)
public abstract class FlowerPatchFeatureMixin {
    @Shadow
    private Block field_7524;

    @Unique
    private final ThreadLocal<Block> threadedBlock = ThreadLocal.withInitial(() -> this.field_7524);
    @Unique
    private final ThreadLocal<Integer> threadedState = ThreadLocal.withInitial(() -> 0);

    // adding this constructor fixes a weird mixin bug
    // with merging the field initializers
    public FlowerPatchFeatureMixin() {
    }

    @Redirect(
            method = "method_6551",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/gen/feature/FlowerPatchFeature;field_7524:Lnet/minecraft/block/Block;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedBlock(FlowerPatchFeature feature, Block block) {
        this.threadedBlock.set(block);
    }

    @Redirect(
            method = "method_6551",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/gen/feature/FlowerPatchFeature;field_7525:I",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedState(FlowerPatchFeature feature, int state) {
        this.threadedState.set(state);
    }

    @Redirect(
            method = "method_4028",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/gen/feature/FlowerPatchFeature;field_7524:Lnet/minecraft/block/Block;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private Block getThreadedBlock(FlowerPatchFeature feature) {
        return this.threadedBlock.get();
    }

    @Redirect(
            method = "method_4028",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/gen/feature/FlowerPatchFeature;field_7525:I",
                    opcode = Opcodes.GETFIELD
            )
    )
    private int getThreadedState(FlowerPatchFeature feature) {
        return this.threadedState.get();
    }
}
