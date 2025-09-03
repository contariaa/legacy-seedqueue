package me.contaria.seedqueue.mixin.worldpreview.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.contaria.seedqueue.worldpreview.WorldPreview;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
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

    @WrapWithCondition(
            method = "spawnEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/sound/SoundManager;play(Lnet/minecraft/client/sound/SoundInstance;)V"
            )
    )
    private boolean doNotPlayMinecartSoundOnPreview(SoundManager instance, SoundInstance sound) {
        return !WorldPreview.renderingPreview;
    }
}
