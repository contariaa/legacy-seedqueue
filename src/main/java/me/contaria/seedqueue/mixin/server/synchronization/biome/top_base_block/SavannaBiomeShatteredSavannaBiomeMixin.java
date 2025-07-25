package me.contaria.seedqueue.mixin.server.synchronization.biome.top_base_block;

import me.contaria.seedqueue.interfaces.SQBiome;
import net.minecraft.block.BlockState;
import net.minecraft.world.biome.SavannaBiome;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SavannaBiome.ShatteredSavannaBiome.class)
public abstract class SavannaBiomeShatteredSavannaBiomeMixin {

    @Redirect(
            method = "method_6420",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/SavannaBiome$ShatteredSavannaBiome;topBlock:Lnet/minecraft/block/BlockState;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedTopBlock(SavannaBiome.ShatteredSavannaBiome biome, BlockState topBlock) {
        ((SQBiome) biome).seedQueue$setTopBlock(topBlock);
    }

    @Redirect(
            method = "method_6420",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/SavannaBiome$ShatteredSavannaBiome;baseBlock:Lnet/minecraft/block/BlockState;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedBaseBlock(SavannaBiome.ShatteredSavannaBiome biome, BlockState baseBlock) {
        ((SQBiome) biome).seedQueue$setBaseBlock(baseBlock);
    }
}
