package me.contaria.seedqueue.mixin.server.synchronization.biome.top_base_block;

import me.contaria.seedqueue.interfaces.SQBiome;
import net.minecraft.block.Block;
import net.minecraft.world.biome.ExtremeHillsBiome;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ExtremeHillsBiome.class)
public abstract class ExtremeHillsBiomeMixin extends BiomeMixin {
    @Unique
    private final ThreadLocal<Block> threadedTopBlock = ThreadLocal.withInitial(() -> this.field_7204);
    @Unique
    private final ThreadLocal<Block> threadedBaseBlock = ThreadLocal.withInitial(() -> this.field_7206);

    // adding this constructor fixes a weird mixin bug
    // with merging the field initializers
    public ExtremeHillsBiomeMixin() {
    }

    @Redirect(
            method = "method_6420",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/ExtremeHillsBiome;field_7204:Lnet/minecraft/block/Block;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedTopBlock(ExtremeHillsBiome biome, Block topBlock) {
        ((SQBiome) biome).seedQueue$setTopBlock(topBlock);
    }

    @Redirect(
            method = "method_6420",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/ExtremeHillsBiome;field_7206:Lnet/minecraft/block/Block;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedBaseBlock(ExtremeHillsBiome biome, Block baseBlock) {
        ((SQBiome) biome).seedQueue$setBaseBlock(baseBlock);
    }

    @Override
    public Block seedQueue$getTopBlock() {
        return this.threadedTopBlock.get();
    }

    @Override
    public void seedQueue$setTopBlock(Block state) {
        this.threadedTopBlock.set(state);
    }

    @Override
    public Block seedQueue$getBaseBlock() {
        return this.threadedBaseBlock.get();
    }

    @Override
    public void seedQueue$setBaseBlock(Block state) {
        this.threadedBaseBlock.set(state);
    }
}
