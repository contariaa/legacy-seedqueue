package me.contaria.seedqueue.interfaces;

import net.minecraft.block.BlockState;

public interface SQBiome {
    BlockState seedQueue$getTopBlock();

    void seedQueue$setTopBlock(BlockState state);

    BlockState seedQueue$getBaseBlock();

    void seedQueue$setBaseBlock(BlockState state);
}
