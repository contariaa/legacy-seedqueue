package me.contaria.seedqueue.gui.wall;

import com.mojang.blaze3d.platform.GlStateManager;
import me.contaria.seedqueue.SeedQueue;
import me.contaria.seedqueue.SeedQueueEntry;
import me.contaria.seedqueue.compat.SeedQueuePreviewFrameBuffer;
import me.contaria.seedqueue.customization.LockTexture;
import me.contaria.seedqueue.worldpreview.WorldPreview;
import me.contaria.seedqueue.worldpreview.WorldPreviewProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class SeedQueuePreview extends DrawableHelper {
    private final SeedQueueWallScreen wall;
    private final SeedQueueEntry seedQueueEntry;
    private WorldPreviewProperties previewProperties;
    private WorldRenderer worldRenderer;

    private final MinecraftClient client;

    private final int width;
    private final int height;

    private final List<ButtonWidget> buttons;
    private final String seedString;
    private final LockTexture lockTexture;

    private long cooldownStart;
    private boolean allowInteractions;
    private int lastPreviewFrame;

    public SeedQueuePreview(SeedQueueWallScreen wall, SeedQueueEntry seedQueueEntry) {
        this.wall = wall;
        this.seedQueueEntry = seedQueueEntry;

        this.client = MinecraftClient.getInstance();

        // forceUnicodeFont is not being loaded from the settings cache because it is not included in SeedQueueSettingsCache.PREVIEW_SETTINGS
        int scale = SeedQueue.config.calculateSimulatedScaleFactor(
                MinecraftClient.getInstance().options.guiScale,
                MinecraftClient.getInstance().options.forcesUnicodeFont
        );
        this.width = (int) Math.ceil((double) SeedQueue.config.simulatedWindowSize.width() / scale);
        this.height = (int) Math.ceil((double) SeedQueue.config.simulatedWindowSize.height() / scale);

        this.buttons = WorldPreviewProperties.createMenu(this.width, this.height);

        /*
        if (Atum.inDemoMode()) {
            this.seedString = "North Carolina";
        } else if (Atum.getSeedProvider().shouldShowSeed()) {
            //noinspection DataFlowIssue
            this.seedString = ((ISeedStringHolder) (Object) this.seedQueueEntry.getLevelInfo()).atum$getSeedString();
        } else {
            this.seedString = "Set Seed";
        }

         */
        this.seedString = "";

        this.lockTexture = wall.getRandomLockTexture();

        this.updatePreviewProperties();
    }

    private void updatePreviewProperties() {
        if (this.previewProperties == (this.previewProperties = this.seedQueueEntry.getPreviewProperties())) {
            return;
        }
        if (this.previewProperties != null) {
            this.worldRenderer = SeedQueueWallScreen.getOrCreateWorldRenderer(this.previewProperties.world);
        } else {
            this.worldRenderer = null;
        }
    }

    public void render() {
        this.updatePreviewProperties();

        this.wall.setOrtho(this.width, this.height);
        if (!this.isPreviewReady()) {
            SeedQueuePreview.renderBackground(this.width, this.height);
            if (this.previewProperties != null) {
                this.buildChunks();
            }
        } else {
            this.renderPreview();
            this.allowInteractions = true;
        }

        if (!this.seedQueueEntry.isReady()) {
            this.renderLoading();
        }
        this.wall.resetOrtho();
    }

    private void renderPreview() {
        SeedQueuePreviewFrameBuffer frameBuffer = this.seedQueueEntry.getFrameBuffer();
        if (this.previewProperties != null) {
            if (this.shouldRedrawPreview() && (frameBuffer.updateRenderData(this.worldRenderer)) || SeedQueue.config.alwaysRedrawPreview) {
                this.redrawPreview(frameBuffer);
            } else {
                this.buildChunks();
            }
        }
        this.wall.setOrtho(this.width, this.height);
        frameBuffer.draw(this.width, this.height);
    }

    private void redrawPreview(SeedQueuePreviewFrameBuffer frameBuffer) {
        frameBuffer.beginWrite();
        // related to WorldRendererMixin#doNotClearOnWallScreen
        // the suppressed call usually renders a light blue overlay over the entire screen,
        // instead we draw it onto the preview ourselves
        DrawableHelper.fill(0, 0, this.width, this.height, -5323025);
        this.run(properties -> properties.render(0, 0, this.buttons, this.width, this.height));
        frameBuffer.endWrite();

        this.client.getFramebuffer().bind(false);
        this.wall.refreshViewport();
        this.lastPreviewFrame = this.wall.frame;
    }

    private void renderLoading() {
    }

    public void build() {
        this.updatePreviewProperties();
        if (this.previewProperties != null) {
            this.buildChunks();
        }
    }

    private void buildChunks() {
        this.run(properties -> {
            properties.tickPackets();
            properties.tickEntities();
            properties.buildChunks();
        });
    }

    private void run(Consumer<WorldPreviewProperties> consumer) {
        WorldRenderer worldRenderer = WorldPreview.worldRenderer;
        WorldPreviewProperties properties = WorldPreview.properties;
        try {
            WorldPreview.worldRenderer = this.worldRenderer;
            WorldPreview.properties = this.previewProperties;
            WorldPreview.properties.run(consumer);
        } finally {
            WorldPreview.worldRenderer = worldRenderer;
            WorldPreview.properties = properties;
        }
    }

    private boolean shouldRedrawPreview() {
        return this.lastPreviewFrame == 0 || this.wall.frame - this.lastPreviewFrame >= SeedQueue.config.wallFPS / SeedQueue.config.previewFPS;
    }

    private boolean isPreviewReady() {
        return this.seedQueueEntry.hasFrameBuffer() || (this.worldRenderer != null && this.getCompletedChunkCount() > 0);
    }

    private int getCompletedChunkCount() {
        String string = this.worldRenderer.getChunksDebugString();
        return Integer.parseInt(string.substring("C: ".length(), string.indexOf('/')));
    }

    public boolean isRenderingReady() {
        return this.isPreviewReady();
    }

    public boolean areInteractionsAllowed() {
        return this.allowInteractions;
    }

    protected boolean canReset(boolean ignoreLock, boolean ignoreResetCooldown) {
        return this.allowInteractions && (!this.seedQueueEntry.isLocked() || ignoreLock) && (this.isCooldownReady() || ignoreResetCooldown) && !this.seedQueueEntry.isLoaded();
    }

    protected void resetCooldown() {
        this.cooldownStart = Long.MAX_VALUE;
    }

    protected void populateCooldownStart(long cooldownStart) {
        if (this.allowInteractions && this.cooldownStart == Long.MAX_VALUE) {
            this.cooldownStart = cooldownStart;
        }
    }

    private boolean isCooldownReady() {
        return (System.nanoTime() / 1000000L) - this.cooldownStart >= SeedQueue.config.resetCooldown;
    }

    public void printDebug() {
        SeedQueue.LOGGER.warn("allowInteractions: {}, cooldownStart: {}, canReset(false, false): {}, isCooldownReady(): {}", allowInteractions, cooldownStart, canReset(false, false), isCooldownReady());
        if (this.worldRenderer != null) {
            SeedQueue.LOGGER.info("SeedQueue-DEBUG | Instance: {}, Seed: {}, World Gen %: {}, Chunks: {}, locked: {}, paused: {}, ready: {}", this.seedQueueEntry.getServer().getLevelName(), this.seedQueueEntry.getLevelInfo().getSeed(), this.seedQueueEntry.getProgressPercentage(), this.worldRenderer.getChunksDebugString(), this.seedQueueEntry.isLocked(), this.seedQueueEntry.isPaused(), this.seedQueueEntry.isReady());
        } else {
            SeedQueue.LOGGER.info("SeedQueue-DEBUG | Instance: {}, Seed: {}, World Gen %: {}", this.seedQueueEntry.getServer().getLevelName(), this.seedQueueEntry.getLevelInfo().getSeed(), this.seedQueueEntry.getProgressPercentage());
        }
    }

    public void printStacktrace() {
        SeedQueue.LOGGER.info("SeedQueue-DEBUG | Instance: {}, Stacktrace: {}", this.seedQueueEntry.getServer().getLevelName(), Arrays.toString(this.seedQueueEntry.getServer().getThread().getStackTrace()));
    }

    public SeedQueueEntry getSeedQueueEntry() {
        return this.seedQueueEntry;
    }

    public LockTexture getLockTexture() {
        return this.lockTexture;
    }

    // see Screen#renderBackground
    public static void renderBackground(int width, int height) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        MinecraftClient.getInstance().getTextureManager().bindTexture(OPTIONS_BACKGROUND_TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        buffer.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(0.0, height, 0.0).texture(0.0F, height / 32.0F).color(64, 64, 64, 255).next();
        buffer.vertex(width, height, 0.0).texture(width / 32.0F, height / 32.0F).color(64, 64, 64, 255).next();
        buffer.vertex(width, 0.0, 0.0).texture(width / 32.0F, 0.0F).color(64, 64, 64, 255).next();
        buffer.vertex(0.0, 0.0, 0.0).texture(0.0F, 0.0F).color(64, 64, 64, 255).next();
        tessellator.draw();
    }
}
