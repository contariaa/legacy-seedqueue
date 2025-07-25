package me.contaria.seedqueue.mixin.server.synchronization.biome.feature;

import me.contaria.seedqueue.interfaces.SQBiome;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.BigTreeFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Biome.class)
public abstract class BiomeMixin implements SQBiome {

    @Redirect(
            method = "method_3822",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/Biome;field_4631:Lnet/minecraft/world/gen/feature/BigTreeFeature;"
            )
    )
    private BigTreeFeature createBigTreeFeature(Biome biome) {
        return new BigTreeFeature(false);
    }
}
