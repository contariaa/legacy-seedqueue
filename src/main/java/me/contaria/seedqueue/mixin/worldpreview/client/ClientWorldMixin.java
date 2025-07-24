package me.contaria.seedqueue.mixin.worldpreview.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {

    @WrapOperation(
            method = "getEntityById",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/ClientPlayerEntity;getEntityId()I"
            )
    )
    private int doNotAccessPlayerDuringPreviewConfiguration(ClientPlayerEntity player, Operation<Integer> original, int id) {
        if (!MinecraftClient.getInstance().isOnThread()) {
            return id + 1;
        }
        return original.call(player);
    }
}
