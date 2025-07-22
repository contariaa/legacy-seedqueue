package me.contaria.seedqueue.mixin.server.synchronization;

import net.minecraft.util.ScheduledTick;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.atomic.AtomicLong;

@Mixin(ScheduledTick.class)
public abstract class ScheduledTickMixin {
    @Unique
    private static final AtomicLong atomicIdCounter = new AtomicLong();

    @Mutable
    @Shadow
    private long id;

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/util/ScheduledTick;id:J",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void atomicIdCounter(ScheduledTick tick, long id) {
        this.id = atomicIdCounter.incrementAndGet();
    }
}
