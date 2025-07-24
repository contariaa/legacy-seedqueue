package me.contaria.seedqueue.customization;

import me.contaria.seedqueue.SeedQueue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.TextureUtil;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LockTexture extends AnimatedTexture {
    private final int width;
    private final int height;

    public LockTexture(Identifier id) throws IOException {
        super(id);
        BufferedImage image = TextureUtil.create(MinecraftClient.getInstance().getResourceManager().getResource(id).getInputStream());
        this.width = image.getWidth();
        this.height = image.getHeight() / (this.animation != null ? this.animation.getIndices().size() : 1);
    }

    public double getAspectRatio() {
        return (double) this.width / this.height;
    }

    public static List<LockTexture> createLockTextures() {
        List<LockTexture> lockTextures = new ArrayList<>();
        Identifier lock = new Identifier("seedqueue", "textures/gui/wall/lock.png");
        do {
            try {
                lockTextures.add(new LockTexture(lock));
            } catch (IOException e) {
                SeedQueue.LOGGER.warn("Failed to read lock image texture: {}", lock, e);
            }
        } while (Layout.containsResource(lock = new Identifier("seedqueue", "textures/gui/wall/lock-" + lockTextures.size() + ".png")));
        return lockTextures;
    }
}
