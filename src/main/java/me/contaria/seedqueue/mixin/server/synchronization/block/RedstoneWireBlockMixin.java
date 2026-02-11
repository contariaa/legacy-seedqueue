package me.contaria.seedqueue.mixin.server.synchronization.block;

import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.BlockPos;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashSet;
import java.util.Set;

@Mixin(RedstoneWireBlock.class)
public abstract class RedstoneWireBlockMixin {
    @Unique
    private final ThreadLocal<Boolean> threadedWireGivesPower = ThreadLocal.withInitial(() -> false);
    @Unique
    private final ThreadLocal<Set<BlockPos>> threadedAffectedNeighbors = ThreadLocal.withInitial(HashSet::new);

    private RedstoneWireBlockMixin() {
    }

    @Redirect(
            method = "method_371",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/RedstoneWireBlock;wiresGivePower:Z",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setWiresGivePower(RedstoneWireBlock block, boolean wiresGivePower) {
        this.threadedWireGivesPower.set(wiresGivePower);
    }

    @Redirect(
            method = {
                    "getStrongRedstonePower",
                    "getWeakRedstonePower",
                    "emitsRedstonePower"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/RedstoneWireBlock;wiresGivePower:Z",
                    opcode = Opcodes.GETFIELD
            )
    )
    private boolean getThreadedWireGivesPower(RedstoneWireBlock block) {
        return this.threadedWireGivesPower.get();
    }

    @Redirect(
            method = {
                    "method_371",
                    "method_375"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/RedstoneWireBlock;affectedNeighbors:Ljava/util/Set;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private Set<BlockPos> getThreadedAffectedNeighbors(RedstoneWireBlock block) {
        return this.threadedAffectedNeighbors.get();
    }
}
