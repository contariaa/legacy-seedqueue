package me.contaria.seedqueue.mixin.server.synchronization;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Biome.class)
public abstract class BiomeMixin {
    @Unique
    private final ThreadLocal<BiomeDecorator> threadedBiomeDecorator = ThreadLocal.withInitial(BiomeDecorator::new);

    @Redirect(
            method = "decorate",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/Biome;biomeDecorator:Lnet/minecraft/world/biome/BiomeDecorator;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private BiomeDecorator useThreadLocalBiomeDecorator(Biome biome) {
        return this.threadedBiomeDecorator.get();
    }
}
