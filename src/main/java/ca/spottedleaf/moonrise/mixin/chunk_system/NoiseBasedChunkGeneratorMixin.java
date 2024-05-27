package ca.spottedleaf.moonrise.mixin.chunk_system;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin {

    /**
     * @reason Use the provided executor, chunk system sets this to something specific
     * @author Spottedleaf
     */
    @Redirect(
            method = "createBiomes",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
            )
    )
    private <U> CompletableFuture<U> redirectBiomesExecutor(final Supplier<U> supplier, final Executor badExecutor,
                                                            @Local(ordinal = 0, argsOnly = true) final Executor executor) {
        return CompletableFuture.supplyAsync(supplier, executor);
    }

    /**
     * @reason Use the provided executor, chunk system sets this to something specific
     * @author Spottedleaf
     */
    @Redirect(
            method = "fillFromNoise",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
            )
    )
    private <U> CompletableFuture<U> redirectNoiseExecutor(final Supplier<U> supplier, final Executor badExecutor,
                                                           @Local(ordinal = 0, argsOnly = true) final Executor executor) {
        return CompletableFuture.supplyAsync(supplier, executor);
    }
}
