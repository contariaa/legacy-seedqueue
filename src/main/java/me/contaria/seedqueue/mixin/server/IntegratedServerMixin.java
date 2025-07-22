package me.contaria.seedqueue.mixin.server;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin extends MinecraftServerMixin {

    @ModifyExpressionValue(
            method = "setupWorld()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;getNetworkHandler()Lnet/minecraft/client/network/ClientPlayNetworkHandler;"
            )
    )
    private ClientPlayNetworkHandler doNotPauseBackgroundWorlds(ClientPlayNetworkHandler networkHandler) {
        if (this.seedQueue$inQueue()) {
            return null;
        }
        return networkHandler;
    }

    @ModifyExpressionValue(
            method = "setupWorld()V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/option/GameOptions;viewDistance:I"
            )
    )
    private int doNotChangeViewDistanceInQueue(int viewDistance) {
        if (this.seedQueue$inQueue()) {
            return this.getPlayerManager().getViewDistance();
        }
        return viewDistance;
    }

    @ModifyExpressionValue(
            method = "setupWorld()V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/MinecraftClient;world:Lnet/minecraft/client/world/ClientWorld;"
            )
    )
    private ClientWorld doNotChangeDifficultyInQueue(ClientWorld world) {
        if (this.seedQueue$inQueue()) {
            return null;
        }
        return world;
    }
}
