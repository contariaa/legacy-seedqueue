package me.contaria.seedqueue;

import me.contaria.seedqueue.interfaces.SQMinecraftServer;
import me.contaria.seedqueue.mixin.accessor.MinecraftServerAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.SaveHandler;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;

public class SeedQueueEntry {
    private final IntegratedServer server;

    private final SaveHandler saveHandler;
    private final LevelProperties levelProperties;
    private final LevelInfo levelInfo;

    private volatile boolean locked;
    private volatile boolean loaded;
    private volatile boolean discarded;
    private volatile boolean maxWorldGenerationReached;

    public SeedQueueEntry(IntegratedServer server, SaveHandler saveHandler, LevelProperties levelProperties, LevelInfo levelInfo) {
        this.server = server;
        this.saveHandler = saveHandler;
        this.levelProperties = levelProperties;
        this.levelInfo = levelInfo;

        ((SQMinecraftServer) server).seedQueue$setEntry(this);
    }

    public IntegratedServer getServer() {
        return this.server;
    }

    public SaveHandler getSaveHandler() {
        return this.saveHandler;
    }

    public LevelProperties getLevelProperties() {
        return this.levelProperties;
    }

    public LevelInfo getLevelInfo() {
        return this.levelInfo;
    }

    /**
     * Checks if this entry should pause.
     * <p>
     * Returns true if:
     * <p>
     * - the entry has finished world generation
     * <p>
     * - the entry has reached the {@link SeedQueueConfig#maxWorldGenerationPercentage} and is not locked
     * <p>
     * - the entry has been scheduled to pause by the {@link SeedQueueThread}
     *
     * @return If this entry's {@link MinecraftServer} should pause in its current state.
     * @see SQMinecraftServer#seedQueue$shouldPause
     */
    public boolean shouldPause() {
        return ((SQMinecraftServer) this.server).seedQueue$shouldPause();
    }

    /**
     * @return If the entry is currently paused.
     * @see SQMinecraftServer#seedQueue$isPaused
     * @see SeedQueueEntry#shouldPause
     */
    public boolean isPaused() {
        return ((SQMinecraftServer) this.server).seedQueue$isPaused();
    }

    /**
     * @return If the entry has been scheduled to pause by the {@link SeedQueueThread} but hasn't been paused yet.
     * @see SQMinecraftServer#seedQueue$isScheduledToPause
     * @see SeedQueueEntry#shouldPause
     */
    public boolean isScheduledToPause() {
        return ((SQMinecraftServer) this.server).seedQueue$isScheduledToPause();
    }

    /**
     * Schedules this entry to be paused.
     *
     * @see SQMinecraftServer#seedQueue$schedulePause
     */
    public void schedulePause() {
        ((SQMinecraftServer) this.server).seedQueue$schedulePause();
    }

    /**
     * @return True if the entry is not currently paused or scheduled to pause.
     */
    public boolean canPause() {
        return !this.isScheduledToPause() && !this.isPaused();
    }

    /**
     * Unpauses this entry.
     *
     * @see SQMinecraftServer#seedQueue$unpause
     */
    public void unpause() {
        ((SQMinecraftServer) this.server).seedQueue$unpause();
    }

    /**
     * An entry can be unpaused if:
     * <p>
     * - it was paused by reaching the {@link SeedQueueConfig#maxWorldGenerationPercentage} but has been locked since
     * <p>
     * - it was scheduled to be paused by the {@link SeedQueueThread}
     *
     * @return True if this entry is currently paused or scheduled to be paused and is allowed to be unpaused.
     */
    public boolean canUnpause() {
        return this.isScheduledToPause() || (this.isPaused() && !this.shouldPause());
    }

    /**
     * @return True if the entry was paused and has now been successfully unpaused.
     * @see SeedQueueEntry#unpause
     * @see SeedQueueEntry#canUnpause
     */
    public boolean tryToUnpause() {
        synchronized (this.server) {
            if (this.canUnpause()) {
                this.unpause();
                return true;
            }
            return false;
        }
    }

    /**
     * @return True if the {@link MinecraftServer} has fully finished generation and is ready to be joined by the player.
     */
    public boolean isReady() {
        return this.server.isLoading();
    }

    /**
     * @see SeedQueueEntry#lock
     */
    public boolean isLocked() {
        return this.locked;
    }

    /**
     * @return True if the {@link MinecraftServer} has not reached {@link SeedQueueConfig#maxWorldGenerationPercentage}.
     */
    public boolean isMaxWorldGenerationReached() {
        return this.maxWorldGenerationReached;
    }

    /**
     * Marks this entry as having reached {@link SeedQueueConfig#maxWorldGenerationPercentage}.
     */
    public void setMaxWorldGenerationReached() {
        this.maxWorldGenerationReached = true;
    }

    /**
     * Locks this entry from being mass-reset on the Wall Screen.
     * Mass Resets include Reset All, Focus Reset, Reset Row, Reset Column.
     *
     * @return True if the entry was not locked before.
     */
    public boolean lock() {
        if (!this.locked) {
            this.locked = true;
            SeedQueue.ping();
            return true;
        }
        return false;
    }

    /**
     * @see SeedQueueEntry#load
     */
    public boolean isLoaded() {
        return this.loaded;
    }

    /**
     * Marks this entry as loaded and discards its framebuffer.
     */
    public synchronized void load() {
        synchronized (this.server) {
            if (this.discarded) {
                throw new IllegalStateException("Tried to load \"" + this.server.getLevelName() + "\" but it has already been discarded!");
            }

            this.loaded = true;
            this.unpause();
        }
    }

    /**
     * @see SeedQueueEntry#discard
     */
    public boolean isDiscarded() {
        return this.discarded;
    }

    /**
     * Discards this entry and all the resources attached to it, including shutting down the {@link MinecraftServer}.
     */
    public synchronized void discard() {
        synchronized (this.server) {
            if (this.discarded) {
                SeedQueue.LOGGER.warn("Tried to discard \"{}\" but it has already been discarded!", this.server.getLevelName());
                return;
            }

            SeedQueue.LOGGER.info("Discarding \"{}\"...", this.server.getLevelName());

            this.discarded = true;

            ((MinecraftServerAccessor) this.server).seedQueue$setRunning(false);
            this.unpause();
        }
    }

    public int getProgressPercentage() {
        // TODO
        return 0;
    }
}
