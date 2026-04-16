package me.contaria.seedqueue.compat;

import com.mojang.blaze3d.platform.GLX;
import me.contaria.seedqueue.SeedQueue;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.Tessellator;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for Minecrafts {@link Framebuffer} storing a previews last drawn image.
 */
public class SeedQueuePreviewFrameBuffer {
    private static final List<Framebuffer> FRAMEBUFFER_POOL = new ArrayList<>();

    private final Framebuffer framebuffer;

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

    /**
     * Draws the internal {@link Framebuffer} without setting {@link GL11#glOrtho} and {@link GL11#glViewport}.
     */
    public void draw(int width, int height) {
        if (!GLX.supportsFbo()) {
            return;
        }

        GL11.glColorMask(true, true, true, false);
        GL11.glDisable(2929); // depth test
        GL11.glDepthMask(false);
        GL11.glEnable(3553); // texture
        GL11.glDisable(2896); // lighting
        GL11.glDisable(3008); // alpha test
        GL11.glDisable(3042); // blend
        GL11.glEnable(2903); // color material

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        double widthRatio = (double) this.framebuffer.viewportWidth / this.framebuffer.textureWidth;
        double heightRatio = (double) this.framebuffer.viewportHeight / this.framebuffer.textureHeight;

        this.framebuffer.beginRead();
        Tessellator tessellator = Tessellator.INSTANCE;
        tessellator.begin();
        tessellator.color(-1);
        tessellator.vertex(0.0, height, 0.0, 0.0, 0.0);
        tessellator.vertex(width, height, 0.0, widthRatio, 0.0);
        tessellator.vertex(width, 0.0, 0.0, widthRatio, heightRatio);
        tessellator.vertex(0.0, 0.0, 0.0, 0.0, heightRatio);
        tessellator.end();
        this.framebuffer.endRead();

        GL11.glDepthMask(true);
        GL11.glColorMask(true, true, true, true);
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
