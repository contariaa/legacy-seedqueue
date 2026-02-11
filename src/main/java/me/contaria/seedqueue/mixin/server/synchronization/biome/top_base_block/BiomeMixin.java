package me.contaria.seedqueue.mixin.server.synchronization.biome.top_base_block;

import me.contaria.seedqueue.interfaces.SQBiome;
import net.minecraft.block.Block;
import net.minecraft.world.biome.Biome;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Biome.class)
public abstract class BiomeMixin implements SQBiome {
    @Shadow
    public Block field_7204;
    @Shadow
    public Block field_7206;

    @Redirect(
            method = "method_6426",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/Biome;field_7204:Lnet/minecraft/block/Block;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private Block getThreadedTopBlock(Biome biome) {
        return this.seedQueue$getTopBlock();
    }

    @Redirect(
            method = "method_6426",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/Biome;field_7206:Lnet/minecraft/block/Block;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private Block getThreadedBaseBlock(Biome biome) {
        return this.seedQueue$getBaseBlock();
    }

    @Override
    public Block seedQueue$getTopBlock() {
        return this.field_7204;
    }

    @Override
    public void seedQueue$setTopBlock(Block state) {
        throw new RuntimeException("Tried to set un-synchronized Biome#topBlock!");
    }

    @Override
    public Block seedQueue$getBaseBlock() {
        return this.field_7206;
    }

    @Override
    public void seedQueue$setBaseBlock(Block state) {
        throw new RuntimeException("Tried to set un-synchronized Biome#baseBlock!");
    }
}
