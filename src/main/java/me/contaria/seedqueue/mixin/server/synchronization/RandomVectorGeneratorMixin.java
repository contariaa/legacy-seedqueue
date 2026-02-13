package me.contaria.seedqueue.mixin.server.synchronization;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.util.RandomVectorGenerator;
import net.minecraft.util.math.Vec3d;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RandomVectorGenerator.class)
public abstract class RandomVectorGeneratorMixin {

    @Inject(
            method = {
                    "method_2800",
                    "method_2801"
            },
            at = @At("HEAD")
    )
    private static void setTemporaryVec3d(CallbackInfoReturnable<Vec3d> cir, @Share("temp") LocalRef<Vec3d> temp) {
        temp.set(Vec3d.of(0.0, 0.0, 0.0));
    }

    @Redirect(
            method = {
                    "method_2800",
                    "method_2801"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/util/RandomVectorGenerator;field_3660:Lnet/minecraft/util/math/Vec3d;",
                    opcode = Opcodes.GETSTATIC
            )
    )
    private static Vec3d setTemporaryVec3d(@Share("temp") LocalRef<Vec3d> temp) {
        return temp.get();
    }
}
