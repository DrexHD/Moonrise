package ca.spottedleaf.moonrise.mixin.chunk_tick_iteration;

import ca.spottedleaf.moonrise.common.list.ReferenceList;
import ca.spottedleaf.moonrise.patches.chunk_system.level.ChunkSystemServerLevel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

// TODO 1.21.2 broadcast chunks are collected different
@Mixin(ServerChunkCache.class)
abstract class ServerChunkCacheMixin extends ChunkSource {

    @Shadow
    @Final
    public ServerLevel level;

    @Mutable
    @Shadow
    @Final
    private List<LevelChunk> tickingChunks;
    @Unique
    private LevelChunk[] iterationCopy;

    /**
     * @reason Destroy old chunk cache field
     * @author Drex
     */
    @Inject(
        method = "<init>",
        at = @At(
            value = "RETURN"
        )
    )
    private void initHook(final CallbackInfo ci) {
        this.tickingChunks = null;
    }

    /**
     * @reason Initialise the list to contain only the ticking chunks.
     * @author Spottedleaf
     */
    @ModifyVariable(
            method = "tickChunks()V",
            at = @At(
                    value = "STORE",
                    opcode = Opcodes.ASTORE,
                    ordinal = 0
            )
    )
    private List<LevelChunk> initTickChunks(final List<LevelChunk> shouldBeNull) {
        final ReferenceList<LevelChunk> tickingChunks =
                ((ChunkSystemServerLevel)this.level).moonrise$getTickingChunks();

        final LevelChunk[] raw = tickingChunks.getRawDataUnchecked();
        final int size = tickingChunks.size();

        if (this.iterationCopy == null || this.iterationCopy.length < size) {
            this.iterationCopy = new LevelChunk[raw.length];
        }
        System.arraycopy(raw, 0, this.iterationCopy, 0, size);

        return ObjectArrayList.wrap(
                this.iterationCopy, size
        );

    }

    /**
     * @reason Ticking chunk collection is handled by moonrise
     * @author Drex
     */
    @Redirect(
        method = "tickChunks()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerChunkCache;collectTickingChunks(Ljava/util/List;)V"
        )
    )
    private void doNotCollect(ServerChunkCache instance, List<LevelChunk> list) {}
}
