package me.contaria.seedqueue.mixin.server.synchronization.biome.top_base_block;

import me.contaria.seedqueue.interfaces.SQBiome;
import net.minecraft.block.Block;
import net.minecraft.world.biome.MesaBiome;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MesaBiome.class)
public abstract class MesaBiomeMixin {

    @Redirect(
            method = "method_6420",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/MesaBiome;field_7204:Lnet/minecraft/block/Block;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private Block getThreadedTopBlock(MesaBiome biome) {
        return ((SQBiome) biome).seedQueue$getTopBlock();
    }

    @Redirect(
            method = "method_6420",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/MesaBiome;field_7206:Lnet/minecraft/block/Block;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private Block getThreadedBaseBlock(MesaBiome biome) {
        return ((SQBiome) biome).seedQueue$getBaseBlock();
    }
}
