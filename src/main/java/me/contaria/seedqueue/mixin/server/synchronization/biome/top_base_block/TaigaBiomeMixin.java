package me.contaria.seedqueue.mixin.server.synchronization.biome.top_base_block;

import me.contaria.seedqueue.interfaces.SQBiome;
import net.minecraft.block.BlockState;
import net.minecraft.world.biome.TaigaBiome;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TaigaBiome.class)
public abstract class TaigaBiomeMixin {

    @Redirect(
            method = "method_6420",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/TaigaBiome;topBlock:Lnet/minecraft/block/BlockState;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedTopBlock(TaigaBiome biome, BlockState topBlock) {
        ((SQBiome) biome).seedQueue$setTopBlock(topBlock);
    }

    @Redirect(
            method = "method_6420",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/TaigaBiome;baseBlock:Lnet/minecraft/block/BlockState;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedBaseBlock(TaigaBiome biome, BlockState baseBlock) {
        ((SQBiome) biome).seedQueue$setBaseBlock(baseBlock);
    }
}
