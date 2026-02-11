package me.contaria.seedqueue.interfaces;

import net.minecraft.block.Block;
import net.minecraft.world.gen.feature.BigTreeFeature;

public interface SQBiome {
    Block seedQueue$getTopBlock();

    void seedQueue$setTopBlock(Block state);

    Block seedQueue$getBaseBlock();

    void seedQueue$setBaseBlock(Block state);

    BigTreeFeature seedQueue$getBigTreeFeature();
}
