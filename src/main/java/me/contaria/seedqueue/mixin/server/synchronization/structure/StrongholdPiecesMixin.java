package me.contaria.seedqueue.mixin.server.synchronization.structure;

import me.contaria.seedqueue.synchronization.ThreadedStrongholdPieces;
import net.minecraft.structure.StrongholdPieces;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(StrongholdPieces.class)
public abstract class StrongholdPiecesMixin {

    @Redirect(
            method = "init",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/structure/StrongholdPieces;POSSIBLE_PIECES:Ljava/util/List;",
                    opcode = Opcodes.PUTSTATIC
            )
    )
    private static void setThreadedPossiblePieces(List<?> possiblePieces) {
        ThreadedStrongholdPieces.THREADED_POSSIBLE_PIECES.set(possiblePieces);
    }

    @Redirect(
            method = {
                    "init",
                    "method_22"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/structure/StrongholdPieces;ACTIVE_PIECE_TYPE:Ljava/lang/Class;",
                    opcode = Opcodes.PUTSTATIC
            )
    )
    private static void setThreadedActivePieceType(Class<?> activePieceType) {
        ThreadedStrongholdPieces.THREADED_ACTIVE_PIECE_TYPE.set(activePieceType);
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
        ThreadedStrongholdPieces.THREADED_TOTAL_WEIGHT.set(totalWeight);
    }

    @Redirect(
            method = {
                    "init",
                    "checkRemainingPieces",
                    "method_22"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/structure/StrongholdPieces;POSSIBLE_PIECES:Ljava/util/List;",
                    opcode = Opcodes.GETSTATIC
            )
    )
    private static List<?> getThreadedPossiblePieces() {
        return ThreadedStrongholdPieces.THREADED_POSSIBLE_PIECES.get();
    }

    @Redirect(
            method = "method_22",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/structure/StrongholdPieces;ACTIVE_PIECE_TYPE:Ljava/lang/Class;",
                    opcode = Opcodes.GETSTATIC
            )
    )
    private static Class<?> getThreadedActivePieceType() {
        return ThreadedStrongholdPieces.THREADED_ACTIVE_PIECE_TYPE.get();
    }

    @Redirect(
            method = {
                    "checkRemainingPieces",
                    "method_22"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/structure/StrongholdPieces;TOTAL_WEIGHT:I",
                    opcode = Opcodes.GETSTATIC
            )
    )
    private static int getThreadedTotalWeight() {
        return ThreadedStrongholdPieces.THREADED_TOTAL_WEIGHT.get();
    }
}
