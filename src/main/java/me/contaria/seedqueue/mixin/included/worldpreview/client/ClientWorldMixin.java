package me.contaria.seedqueue.mixin.included.worldpreview.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.contaria.seedqueue.worldpreview.WorldPreview;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.ControllablePlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {

    @WrapOperation(
            method = "getEntityById",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/ControllablePlayerEntity;getEntityId()I"
            )
    )
    private int doNotAccessPlayerDuringPreviewConfiguration(ControllablePlayerEntity player, Operation<Integer> original, int id) {
        if (!MinecraftClient.getInstance().method_6640()) {
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
