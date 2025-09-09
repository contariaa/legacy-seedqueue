package me.contaria.seedqueue.mixin.included.glacier;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import net.minecraft.client.render.chunk.ChunkOcclusionDataBuilder;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.*;

import java.util.BitSet;
import java.util.EnumSet;
import java.util.Set;

@Mixin(ChunkOcclusionDataBuilder.class)
public abstract class ChunkOcclusionDataBuilderMixin {
    @Unique
    private static final Direction[] DIRECTIONS = Direction.values();

    @Shadow
    @Final
    private BitSet closed;

    @Shadow
    protected abstract void addEdgeFaces(int pos, Set<Direction> openFaces);

    @Shadow
    protected abstract int offset(int pos, Direction direction);

    /**
     * @author contaria
     * @reason Backport modern version of this method
     */
    @Overwrite
    private Set<Direction> getOpenFaces(int pos) {
        Set<Direction> set = EnumSet.noneOf(Direction.class);
        IntPriorityQueue intPriorityQueue = new IntArrayFIFOQueue();
        intPriorityQueue.enqueue(pos);
        this.closed.set(pos, true);

        while (!intPriorityQueue.isEmpty()) {
            int i = intPriorityQueue.dequeueInt();
            this.addEdgeFaces(i, set);

            for (Direction direction : DIRECTIONS) {
                int j = this.offset(i, direction);
                if (j >= 0 && !this.closed.get(j)) {
                    this.closed.set(j, true);
                    intPriorityQueue.enqueue(j);
                }
            }
        }

        return set;
    }
}
