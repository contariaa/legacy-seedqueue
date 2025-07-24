package me.contaria.seedqueue.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import me.contaria.seedqueue.SeedQueue;
import me.contaria.seedqueue.SeedQueueEntry;
import me.contaria.seedqueue.gui.wall.SeedQueueWallScreen;
import me.contaria.seedqueue.interfaces.SQMinecraftClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.client.render.LoadingScreenRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.Session;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.server.integrated.IntegratedServer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;
import java.util.Optional;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin implements SQMinecraftClient {
    @Shadow
    private IntegratedServer server;

    @Shadow
    private boolean isIntegratedServerRunning;

    @Shadow
    public LoadingScreenRenderer loadingScreenRenderer;

    @Shadow
    private ClientConnection clientConnection;

    @Shadow
    public abstract void setScreen(Screen screen);

    @Shadow
    public abstract Session getSession();

    @Override
    public void seedQueue$play(SeedQueueEntry entry) {
        this.server = entry.getServer();
        //this.server.getThread().setPriority(Thread.NORM_PRIORITY);
        this.isIntegratedServerRunning = true;
        entry.load();

        this.loadingScreenRenderer.setTitle(I18n.translate("menu.loadingLevel"));

        while (!this.server.isLoading()) {
            String string = this.server.getServerOperation();
            if (string != null) {
                this.loadingScreenRenderer.setTask(I18n.translate(string));
            } else {
                this.loadingScreenRenderer.setTask("");
            }

            try {
                Thread.sleep(200L);
            } catch (InterruptedException ignored) {
            }
        }

        this.setScreen(new ProgressScreen());
        SocketAddress socketAddress = this.server.getNetworkIo().bindLocal();
        ClientConnection clientConnection = ClientConnection.connectLocal(socketAddress);
        clientConnection.setPacketListener(new ClientLoginNetworkHandler(clientConnection, MinecraftClient.getInstance(), null));
        clientConnection.send(new HandshakeC2SPacket(47, socketAddress.toString(), 0, NetworkState.LOGIN));
        clientConnection.send(new LoginHelloC2SPacket(this.getSession().getProfile()));
        this.clientConnection = clientConnection;
    }

    @ModifyExpressionValue(
            method = "runGameLoop",
            at = {
                    @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/client/option/GameOptions;debugEnabled:Z",
                            opcode = Opcodes.GETFIELD
                    ),
                    @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/client/option/GameOptions;debugProfilerEnabled:Z",
                            opcode = Opcodes.GETFIELD
                    )
            }
    )
    private boolean showDebugMenuOnWall(boolean enabled) {
        return enabled || (SeedQueue.isOnWall() && SeedQueue.config.showDebugMenu);
    }

    @ModifyExpressionValue(
            method = "runGameLoop",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/option/GameOptions;hudHidden:Z",
                    opcode = Opcodes.GETFIELD
            )
    )
    private boolean showDebugMenuOnWall2(boolean hudHidden) {
        return hudHidden && !(SeedQueue.isOnWall() && SeedQueue.config.showDebugMenu);
    }

    @WrapWithCondition(
            method = "runGameLoop",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Thread;yield()V"
            )
    )
    private boolean doNotYieldRenderThreadOnWall() {
        // because of the increased amount of threads when using SeedQueue,
        // not yielding the render thread results in a much smoother experience on the Wall Screen
        return !SeedQueue.isOnWall();
    }

    @Inject(
            method = "runGameLoop",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;updateDisplay()V",
                    shift = At.Shift.AFTER
            )
    )
    private void finishRenderingWall(CallbackInfo ci) {
        if (SeedQueue.isOnWall()) {
            SeedQueueWallScreen wall = (SeedQueueWallScreen) MinecraftClient.getInstance().currentScreen;
            wall.joinScheduledInstance();
            wall.populateResetCooldowns();
            wall.tickBenchmark();
        }
    }

    @Inject(
            method = "getMaxFramerate",
            at = @At("HEAD"),
            cancellable = true
    )
    private void modifyFPSOnWall(CallbackInfoReturnable<Integer> cir) {
        if (SeedQueue.isOnWall()) {
            cir.setReturnValue(SeedQueue.config.wallFPS);
        }
    }

    @Inject(
            method = "setScreen",
            at = @At("TAIL")
    )
    private void onSeedQueueReset(Screen screen, CallbackInfo ci) {
        if (!(screen instanceof TitleScreen && SeedQueue.isActive())) {
            return;
        }
        if (SeedQueue.config.useWall) {
            if (SeedQueue.config.bypassWall) {
                Optional<SeedQueueEntry> entry = SeedQueue.getEntryMatching(SeedQueueEntry::isLocked);
                if (entry.isPresent()) {
                    SeedQueue.playEntry(entry.get());
                    return;
                }
            }
            MinecraftClient.getInstance().setScreen(new SeedQueueWallScreen());
            return;
        }
        while (!SeedQueue.playEntry()) {
            SeedQueue.ping();
        }
    }

    @Inject(
            method = "stop",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;connect(Lnet/minecraft/client/world/ClientWorld;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void shutdownQueue(CallbackInfo ci) {
        SeedQueue.stop();
    }

    @Inject(
            method = "printCrashReport",
            at = @At("HEAD")
    )
    private static void shutdownQueueOnCrash(CallbackInfo ci) {
        // don't try to stop SeedQueue if Minecraft crashes before the client is initialized
        if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().isOnThread()) {
            SeedQueue.stop();
        }
    }
}
