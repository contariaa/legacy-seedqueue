package me.contaria.seedqueue.interfaces;

import me.contaria.seedqueue.SeedQueueEntry;

import java.util.Optional;

public interface SQMinecraftServer {

    Optional<SeedQueueEntry> seedQueue$getEntry();

    boolean seedQueue$inQueue();

    void seedQueue$setEntry(SeedQueueEntry entry);

    void seedQueue$tryPausingServer();

    boolean seedQueue$shouldPause();

    boolean seedQueue$isPaused();

    boolean seedQueue$isScheduledToPause();

    void seedQueue$schedulePause();

    void seedQueue$unpause();
}
