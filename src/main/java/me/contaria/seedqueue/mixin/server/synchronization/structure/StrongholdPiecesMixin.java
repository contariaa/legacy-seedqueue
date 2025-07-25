package me.contaria.seedqueue.mixin.server.synchronization.structure;

import net.minecraft.structure.StrongholdPieces;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(StrongholdPieces.class)
public abstract class StrongholdPiecesMixin {
    @Unique
    private static final ThreadLocal<List<?>> THREADED_POSSIBLE_PIECES = new ThreadLocal<>();
    @Unique
    private static final ThreadLocal<Class<?>> THREADED_ACTIVE_PIECE_TYPE = new ThreadLocal<>();
    @Unique
    private static final ThreadLocal<Integer> THREADED_TOTAL_WEIGHT = ThreadLocal.withInitial(() -> 0);

    @Redirect(
            method = "init",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/structure/StrongholdPieces;POSSIBLE_PIECES:Ljava/util/List;",
                    opcode = Opcodes.PUTSTATIC
            )
    )
    private static void setThreadedPossiblePieces(List<?> possiblePieces) {
        THREADED_POSSIBLE_PIECES.set(possiblePieces);
    }

    @Redirect(
            method = {
                    "init",
                    "pickPiece"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/structure/StrongholdPieces;ACTIVE_PIECE_TYPE:Ljava/lang/Class;",
                    opcode = Opcodes.PUTSTATIC
            )
    )
    private static void setThreadedActivePieceType(Class<?> activePieceType) {
        THREADED_ACTIVE_PIECE_TYPE.set(activePieceType);
    }

    @Redirect(
            method = "checkRemainingPieces",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/structure/StrongholdPieces;TOTAL_WEIGHT:I",
                    opcode = Opcodes.PUTSTATIC
            )
    )
    private static void setThreadedTotalWeight(int totalWeight) {
        THREADED_TOTAL_WEIGHT.set(totalWeight);
    }

    @Redirect(
            method = {
                    "init",
                    "checkRemainingPieces",
                    "pickPiece"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/structure/StrongholdPieces;POSSIBLE_PIECES:Ljava/util/List;",
                    opcode = Opcodes.GETSTATIC
            )
    )
    private static List<?> getThreadedPossiblePieces() {
        return THREADED_POSSIBLE_PIECES.get();
    }

    @Redirect(
            method = "pickPiece",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/structure/StrongholdPieces;ACTIVE_PIECE_TYPE:Ljava/lang/Class;",
                    opcode = Opcodes.GETSTATIC
            )
    )
    private static Class<?> getThreadedActivePieceType() {
        return THREADED_ACTIVE_PIECE_TYPE.get();
    }

    @Redirect(
            method = {
                    "checkRemainingPieces",
                    "pickPiece"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/structure/StrongholdPieces;TOTAL_WEIGHT:I",
                    opcode = Opcodes.GETSTATIC
            )
    )
    private static int getThreadedTotalWeight() {
        return THREADED_TOTAL_WEIGHT.get();
    }

    @Mixin(StrongholdPieces.SpiralStaircase.class)
    private abstract static class SpiralStaircaseMixin {
        @Redirect(
                method = "fillOpenings",
                at = @At(
                        value = "INVOKE",
                        target = "Lnet/minecraft/structure/StrongholdPieces;method_19(Ljava/lang/Class;)Ljava/lang/Class;"
                )
        )
        private static Class<?> setThreadedActivePieceType(Class<?> activePieceType) {
            THREADED_ACTIVE_PIECE_TYPE.set(activePieceType);
            return activePieceType;
        }
    }
}
