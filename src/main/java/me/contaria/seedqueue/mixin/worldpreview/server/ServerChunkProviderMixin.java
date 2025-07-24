package me.contaria.seedqueue.mixin.worldpreview.server;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.contaria.seedqueue.SeedQueueEntry;
import me.contaria.seedqueue.interfaces.SQMinecraftServer;
import me.contaria.seedqueue.worldpreview.WorldPreview;
import me.contaria.seedqueue.worldpreview.WorldPreviewProperties;
import me.contaria.seedqueue.worldpreview.interfaces.WPServerChunkProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilterableList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ServerChunkProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.*;

@Mixin(ServerChunkProvider.class)
public abstract class ServerChunkProviderMixin implements WPServerChunkProvider {
    @Shadow
    private ServerWorld world;
    @Shadow
    private List<Chunk> chunks;

    @Unique
    private final Set<Long> sentChunks = new HashSet<>();
    @Unique
    private final Set<Long> sentEmptyChunks = new HashSet<>();
    @Unique
    private final Set<Integer> sentEntities = new HashSet<>();

    @ModifyExpressionValue(
            method = "getOrGenerateChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/ChunkProvider;getChunk(II)Lnet/minecraft/world/chunk/Chunk;"
            )
    )
    private Chunk sendDataAfterChunkGeneration(Chunk chunk) {
        // it's possible to optimize this by only sending the data for the new chunk
        // however that needs more careful thought and since this now only gets called 529 times
        // per world it isn't hugely impactful
        // stuff to consider:
        // - check all chunks on frustum update / initial sendData
        // - entities spawning in neighbouring chunks
        this.worldpreview$sendData();
        return chunk;
    }

    @Unique
    private List<Packet<?>> processChunk(Chunk chunk) {
        ChunkPos pos = chunk.getChunkPos();
        if (this.sentChunks.contains(ChunkPos.getIdFromCoords(pos.x, pos.z))) {
            return Collections.emptyList();
        }

        List<Packet<?>> chunkPackets = new ArrayList<>();

        chunkPackets.add(new ChunkDataS2CPacket(chunk, true, 65535));
        //chunkPackets.add(new LightUpdateS2CPacket(chunk.getPos(), chunk.getLightingProvider()));
        chunkPackets.addAll(this.processNeighborChunks(pos));

        this.sentChunks.add(ChunkPos.getIdFromCoords(pos.x, pos.z));

        return chunkPackets;
    }

    @Unique
    private List<Packet<?>> processNeighborChunks(ChunkPos pos) {
        List<Packet<?>> packets = new ArrayList<>();
        /*
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) {
                    continue;
                }
                long neighbor = ChunkPos.getIdFromCoords(pos.x + x, pos.z + z);
                Chunk neighborChunk = this.chunkStorage.get(neighbor);
                if (neighborChunk == null) {
                    continue;
                }

                if (this.sentChunks.contains(neighbor)) {
                    int[] lightUpdates = ((WPChunkHolder) neighborHolder).worldpreview$flushUpdates();
                    if (lightUpdates[0] != 0 || lightUpdates[1] != 0) {
                        packets.add(new LightUpdateS2CPacket(new ChunkPos(neighbor), neighborChunk.getLightingProvider(), lightUpdates[0], lightUpdates[1]));
                    }
                }
            }
        }

         */
        return packets;
    }

    @Unique
    private void sendData(Queue<Packet<?>> packetQueue, ClientPlayerEntity player, Chunk chunk) {
        ChunkPos pos = chunk.getChunkPos();
        ChunkPos playerPos = new ChunkPos(player.getBlockPos().getX() / 16, player.getBlockPos().getZ() / 16);
        if (Math.max(Math.abs(pos.x - playerPos.x), Math.abs(pos.z - playerPos.z)) > WorldPreview.config.chunkDistance) {
            return;
        }

        List<Packet<?>> chunkPackets = this.processChunk(chunk);

        List<Packet<?>> entityPackets = new ArrayList<>();
        for (TypeFilterableList<Entity> entities : chunk.getEntities()) {
            for (Entity entity : entities) {
                entityPackets.addAll(this.processEntity(entity));
            }
        }

        if (!entityPackets.isEmpty() && chunkPackets.isEmpty()) {
            if (!this.sentChunks.contains(ChunkPos.getIdFromCoords(pos.x, pos.z)) && this.sentEmptyChunks.add(ChunkPos.getIdFromCoords(pos.x, pos.z))) {
                chunkPackets = Collections.singletonList(this.createEmptyChunkPacket(chunk));
            }
        }

        packetQueue.addAll(chunkPackets);
        packetQueue.addAll(entityPackets);
    }

    @Unique
    private List<Packet<?>> processEntity(Entity entity) {
        int id = entity.getEntityId();
        if (this.sentEntities.contains(id)) {
            return Collections.emptyList();
        }

        List<Packet<?>> entityPackets = new ArrayList<>();
/*
        // ensure vehicles are processed before their passengers
        Entity vehicle = entity.vehicle;
        if (vehicle != null) {
            if (entity.chunkX != vehicle.chunkX || entity.chunkZ != vehicle.chunkZ) {
                WorldPreview.LOGGER.warn("Failed to send entity to preview! Entity and its vehicle are in different chunks.");
                return Collections.emptyList();
            }
            entityPackets.addAll(this.processEntity(vehicle));
        }

        this.entityTrackers.get(id).getEntry().sendPackets(entityPackets::add);
        // see EntityTrackerEntry#tick
        entityPackets.add(new EntityS2CPacket.Rotate(id, (byte) MathHelper.floor(entity.yaw * 256.0f / 360.0f), (byte) MathHelper.floor(entity.pitch * 256.0f / 360.0f), entity.onGround));
        entityPackets.add(new EntitySetHeadYawS2CPacket(entity, (byte) MathHelper.floor(entity.getHeadYaw() * 256.0f / 360.0f)));

 */

        this.sentEntities.add(id);
        return entityPackets;
    }

    @Unique
    private ChunkDataS2CPacket createEmptyChunkPacket(Chunk chunk) {
        Chunk empty = new Chunk(chunk.getWorld(), chunk.chunkX, chunk.chunkZ);
        empty.setBiomeArray(chunk.getBiomeArray());
        return new ChunkDataS2CPacket(empty, true, 65535);
    }

    @Unique
    private WorldPreviewProperties getWorldPreviewProperties() {
        return ((SQMinecraftServer) this.world.getServer()).seedQueue$getEntry().map(SeedQueueEntry::getPreviewProperties).orElse(null);
    }

    @Override
    public void worldpreview$sendData() {
        if (this.world.getServer().getTicks() > 0) {
            return;
        }

        WorldPreviewProperties properties = this.getWorldPreviewProperties();
        if (properties == null) {
            return;
        }

        if (this.world.dimension.getType() != properties.world.dimension.getType()) {
            return;
        }

        for (Chunk chunk : this.chunks) {
            this.sendData(properties.packetQueue, properties.player, chunk);
        }
    }
}
