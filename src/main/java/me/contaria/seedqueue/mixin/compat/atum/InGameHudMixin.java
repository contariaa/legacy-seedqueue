package me.contaria.seedqueue.mixin.compat.atum;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.contaria.seedqueue.SeedQueue;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(value = InGameHud.class, priority = 1500)
public abstract class InGameHudMixin {

    @Dynamic
    @TargetHandler(
            mixin = "me.voidxwalker.autoreset.mixin.gui.InGameHudMixin",
            name = "modifyRightText"
    )
    @ModifyExpressionValue(
            method = "@MixinSquared:Handler",
            at = @At(
                    value = "INVOKE",
                    target = "Lme/voidxwalker/autoreset/AtumConfig;getDebugText()Ljava/util/List;",
                    remap = false
            )
    )
    private List<String> addDebugText(List<String> lines) {
        lines.addAll(SeedQueue.getDebugText());
        return lines;
    }
}
