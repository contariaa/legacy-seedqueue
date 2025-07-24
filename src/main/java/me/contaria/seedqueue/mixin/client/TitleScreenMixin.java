package me.contaria.seedqueue.mixin.client;

import me.contaria.seedqueue.SeedQueue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    @Unique
    private static final Identifier BUTTON_IMAGE = new Identifier("textures/items/diamond_boots.png");

    @Inject(
            method = "init",
            at = @At("TAIL")
    )
    private void addSeedQueueButton(CallbackInfo ci) {
        if (SeedQueue.isActive()) {
            return;
        }
        this.buttons.add(new ButtonWidget(420, this.width / 2 - 124, this.height / 4 + 48, 20, 20, "SQ") {
            @Override
            public void render(MinecraftClient client, int mouseX, int mouseY) {
                super.render(client, mouseX, mouseY);
                MinecraftClient.getInstance().getTextureManager().bindTexture(BUTTON_IMAGE);
                DrawableHelper.drawTexture(this.width / 2 - 124+2, this.height / 4 + 48 + 2, 0.0F, 0.0F, 16, 16, 16, 16);
            }
        });
    }

    @Inject(
            method = "buttonClicked",
            at = @At("TAIL")
    )
    private void startSeedQueueResets(ButtonWidget button, CallbackInfo ci) {
        if (button.id == 420) {
            SeedQueue.start();
            while (!SeedQueue.playEntry()) {
                SeedQueue.ping();
            }
        }
    }
}
