package me.contaria.seedqueue.compat;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import me.contaria.seedqueue.SeedQueue;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Wrapper for Minecrafts {@link Framebuffer} storing a previews last drawn image.
 * <p>
 * Stores {@link SeedQueuePreviewFrameBuffer#lastRenderData} as to only redraw the preview if it has changed.
 */
public class SeedQueuePreviewFrameBuffer {
    private static final List<Framebuffer> FRAMEBUFFER_POOL = new ArrayList<>();

    private final Framebuffer framebuffer;

    // stores a string unique to the current state of world rendering when writing to the framebuffer
    @Nullable
    private String lastRenderData;

    public SeedQueuePreviewFrameBuffer() {
        if (FRAMEBUFFER_POOL.isEmpty()) {
            this.framebuffer = new Framebuffer(
                    SeedQueue.config.simulatedWindowSize.width(),
                    SeedQueue.config.simulatedWindowSize.height(),
                    true
            );
        } else {
            this.framebuffer = FRAMEBUFFER_POOL.remove(0);
        }
    }

    public void beginWrite() {
        this.framebuffer.bind(true);
    }

    public void endWrite() {
        this.framebuffer.unbind();
    }

    public boolean updateRenderData(WorldRenderer worldRenderer) {
        return !Objects.equals(this.lastRenderData, this.lastRenderData = worldRenderer.getChunksDebugString() + "\n" + worldRenderer.getEntitiesDebugString());
    }

    /**
     * Draws the internal {@link Framebuffer} without setting {@link GlStateManager#ortho} and {@link GlStateManager#viewport}.
     */
    public void draw(int width, int height) {
        if (!GLX.supportsFbo()) {
            return;
        }

        GlStateManager.colorMask(true, true, true, false);
        GlStateManager.disableDepthTest();
        GlStateManager.depthMask(false);
        GlStateManager.enableTexture();
        GlStateManager.disableLighting();
        GlStateManager.disableAlphaTest();
        GlStateManager.disableBlend();
        GlStateManager.enableColorMaterial();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        this.framebuffer.beginRead();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(0.0, height, 0.0).texture(0.0f, 0.0f).color(255, 255, 255, 255).next();
        bufferBuilder.vertex(width, height, 0.0).texture(1.0f, 0.0f).color(255, 255, 255, 255).next();
        bufferBuilder.vertex(width, 0.0, 0.0).texture(1.0f, 1.0f).color(255, 255, 255, 255).next();
        bufferBuilder.vertex(0.0, 0.0, 0.0).texture(0.0f, 1.0f).color(255, 255, 255, 255).next();
        tessellator.draw();
        this.framebuffer.endRead();

        GlStateManager.depthMask(true);
        GlStateManager.colorMask(true, true, true, true);
    }

    public void discard() {
        FRAMEBUFFER_POOL.add(this.framebuffer);
    }

    public static void clearFramebufferPool() {
        for (Framebuffer framebuffer : FRAMEBUFFER_POOL) {
            framebuffer.delete();
        }
        FRAMEBUFFER_POOL.clear();
    }
}
