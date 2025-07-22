package me.contaria.seedqueue.mixin.client;

import me.contaria.seedqueue.SeedQueueEntry;
import me.contaria.seedqueue.interfaces.SQMinecraftClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.client.render.LoadingScreenRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.Session;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.net.SocketAddress;

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
    public void seedqueue$play(SeedQueueEntry entry) {
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
}
