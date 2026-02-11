package me.contaria.seedqueue.mixin.server.synchronization.biome.top_base_block;

import me.contaria.seedqueue.interfaces.SQBiome;
import net.minecraft.block.Block;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.LakesFeature;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LakesFeature.class)
public abstract class LakesFeatureMixin {

    @Redirect(
            method = "method_4028",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/Biome;field_7204:Lnet/minecraft/block/Block;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private Block getThreadedTopBlock(Biome biome) {
        return ((SQBiome) biome).seedQueue$getTopBlock();
    }
}
