package rich.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.SectionRenderState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.NormalizedRelativePos;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.util.profiler.ProfilerFrameGraphHooks;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererFrameGraphAccountingMixin {
   @Unique
   private void richAccountBegin() {
      ProfilerFrameGraphHooks.beginKnownMainSection();
   }

   @Unique
   private void richAccountEnd() {
      ProfilerFrameGraphHooks.endKnownMainSection();
   }

   @Inject(method = "updateCamera", at = @At("HEAD"), require = 0)
   private void richAccountUpdateCameraStart(Camera camera, Frustum frustum, boolean spectator, CallbackInfo ci) {
      this.richAccountBegin();
   }

   @Inject(method = "updateCamera", at = @At("RETURN"), require = 0)
   private void richAccountUpdateCameraEnd(Camera camera, Frustum frustum, boolean spectator, CallbackInfo ci) {
      this.richAccountEnd();
   }

   @Inject(method = "updateChunks", at = @At("HEAD"), require = 0)
   private void richAccountUpdateChunksStart(Camera camera, CallbackInfo ci) {
      this.richAccountBegin();
   }

   @Inject(method = "updateChunks", at = @At("RETURN"), require = 0)
   private void richAccountUpdateChunksEnd(Camera camera, CallbackInfo ci) {
      this.richAccountEnd();
   }

   @Inject(method = "applyFrustum", at = @At("HEAD"), require = 0)
   private void richAccountApplyFrustumStart(Frustum frustum, CallbackInfo ci) {
      this.richAccountBegin();
   }

   @Inject(method = "applyFrustum", at = @At("RETURN"), require = 0)
   private void richAccountApplyFrustumEnd(Frustum frustum, CallbackInfo ci) {
      this.richAccountEnd();
   }

   @Inject(method = "renderBlockLayers", at = @At("HEAD"), require = 0)
   private void richAccountRenderBlockLayersStart(Matrix4fc matrix, double cameraX, double cameraY, double cameraZ, CallbackInfoReturnable<SectionRenderState> cir) {
      this.richAccountBegin();
   }

   @Inject(method = "renderBlockLayers", at = @At("RETURN"), require = 0)
   private void richAccountRenderBlockLayersEnd(Matrix4fc matrix, double cameraX, double cameraY, double cameraZ, CallbackInfoReturnable<SectionRenderState> cir) {
      this.richAccountEnd();
   }

   @Inject(method = "translucencySort", at = @At("HEAD"), require = 0)
   private void richAccountTranslucencySortStart(Vec3d cameraPos, CallbackInfo ci) {
      this.richAccountBegin();
   }

   @Inject(method = "translucencySort", at = @At("RETURN"), require = 0)
   private void richAccountTranslucencySortEnd(Vec3d cameraPos, CallbackInfo ci) {
      this.richAccountEnd();
   }

   @Inject(method = "scheduleChunkTranslucencySort", at = @At("HEAD"), require = 0)
   private void richAccountScheduleChunkTranslucencySortStart(ChunkBuilder.BuiltChunk chunk, NormalizedRelativePos relativePos, Vec3d cameraPos, boolean needsUpdate, boolean ignoreCameraAlignment, CallbackInfo ci) {
      this.richAccountBegin();
   }

   @Inject(method = "scheduleChunkTranslucencySort", at = @At("RETURN"), require = 0)
   private void richAccountScheduleChunkTranslucencySortEnd(ChunkBuilder.BuiltChunk chunk, NormalizedRelativePos relativePos, Vec3d cameraPos, boolean needsUpdate, boolean ignoreCameraAlignment, CallbackInfo ci) {
      this.richAccountEnd();
   }

   @Inject(method = "fillEntityRenderStates", at = @At("HEAD"), require = 0)
   private void richAccountFillEntityRenderStatesStart(Camera camera, Frustum frustum, RenderTickCounter tickCounter, WorldRenderState renderStates, CallbackInfo ci) {
      this.richAccountBegin();
   }

   @Inject(method = "fillEntityRenderStates", at = @At("RETURN"), require = 0)
   private void richAccountFillEntityRenderStatesEnd(Camera camera, Frustum frustum, RenderTickCounter tickCounter, WorldRenderState renderStates, CallbackInfo ci) {
      this.richAccountEnd();
   }

   @Inject(method = "pushEntityRenders", at = @At("HEAD"), require = 0)
   private void richAccountPushEntityRendersStart(MatrixStack matrices, WorldRenderState renderStates, OrderedRenderCommandQueue queue, CallbackInfo ci) {
      this.richAccountBegin();
   }

   @Inject(method = "pushEntityRenders", at = @At("RETURN"), require = 0)
   private void richAccountPushEntityRendersEnd(MatrixStack matrices, WorldRenderState renderStates, OrderedRenderCommandQueue queue, CallbackInfo ci) {
      this.richAccountEnd();
   }

   @Inject(method = "fillBlockEntityRenderStates", at = @At("HEAD"), require = 0)
   private void richAccountFillBlockEntityRenderStatesStart(Camera camera, float tickProgress, WorldRenderState renderStates, CallbackInfo ci) {
      this.richAccountBegin();
   }

   @Inject(method = "fillBlockEntityRenderStates", at = @At("RETURN"), require = 0)
   private void richAccountFillBlockEntityRenderStatesEnd(Camera camera, float tickProgress, WorldRenderState renderStates, CallbackInfo ci) {
      this.richAccountEnd();
   }

   @Inject(method = "renderBlockEntities", at = @At("HEAD"), require = 0)
   private void richAccountRenderBlockEntitiesStart(MatrixStack matrices, WorldRenderState renderStates, OrderedRenderCommandQueueImpl queue, CallbackInfo ci) {
      this.richAccountBegin();
   }

   @Inject(method = "renderBlockEntities", at = @At("RETURN"), require = 0)
   private void richAccountRenderBlockEntitiesEnd(MatrixStack matrices, WorldRenderState renderStates, OrderedRenderCommandQueueImpl queue, CallbackInfo ci) {
      this.richAccountEnd();
   }

   @Inject(method = "fillBlockBreakingProgressRenderState", at = @At("HEAD"), require = 0)
   private void richAccountFillBlockBreakingProgressStart(Camera camera, WorldRenderState renderStates, CallbackInfo ci) {
      this.richAccountBegin();
   }

   @Inject(method = "fillBlockBreakingProgressRenderState", at = @At("RETURN"), require = 0)
   private void richAccountFillBlockBreakingProgressEnd(Camera camera, WorldRenderState renderStates, CallbackInfo ci) {
      this.richAccountEnd();
   }

   @Inject(method = "renderBlockDamage", at = @At("HEAD"), require = 0)
   private void richAccountRenderBlockDamageStart(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, WorldRenderState renderStates, CallbackInfo ci) {
      this.richAccountBegin();
   }

   @Inject(method = "renderBlockDamage", at = @At("RETURN"), require = 0)
   private void richAccountRenderBlockDamageEnd(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, WorldRenderState renderStates, CallbackInfo ci) {
      this.richAccountEnd();
   }

   @Inject(method = "renderTargetBlockOutline", at = @At("HEAD"), require = 0)
   private void richAccountRenderTargetBlockOutlineStart(VertexConsumerProvider.Immediate immediate, MatrixStack matrices, boolean renderBlockOutline, WorldRenderState renderStates, CallbackInfo ci) {
      this.richAccountBegin();
   }

   @Inject(method = "renderTargetBlockOutline", at = @At("RETURN"), require = 0)
   private void richAccountRenderTargetBlockOutlineEnd(VertexConsumerProvider.Immediate immediate, MatrixStack matrices, boolean renderBlockOutline, WorldRenderState renderStates, CallbackInfo ci) {
      this.richAccountEnd();
   }

   @Inject(method = "renderSky", at = @At("HEAD"), require = 0)
   private void richAccountRenderSkyStart(FrameGraphBuilder frameGraphBuilder, Camera camera, GpuBufferSlice fogBuffer, CallbackInfo ci) {
      this.richAccountBegin();
   }

   @Inject(method = "renderSky", at = @At("RETURN"), require = 0)
   private void richAccountRenderSkyEnd(FrameGraphBuilder frameGraphBuilder, Camera camera, GpuBufferSlice fogBuffer, CallbackInfo ci) {
      this.richAccountEnd();
   }

   @Inject(method = "renderParticles", at = @At("HEAD"), require = 0)
   private void richAccountRenderParticlesStart(FrameGraphBuilder frameGraphBuilder, GpuBufferSlice fogBuffer, CallbackInfo ci) {
      this.richAccountBegin();
   }

   @Inject(method = "renderParticles", at = @At("RETURN"), require = 0)
   private void richAccountRenderParticlesEnd(FrameGraphBuilder frameGraphBuilder, GpuBufferSlice fogBuffer, CallbackInfo ci) {
      this.richAccountEnd();
   }

   @Inject(method = "renderClouds", at = @At("HEAD"), require = 0)
   private void richAccountRenderCloudsStart(FrameGraphBuilder frameGraphBuilder, CloudRenderMode mode, Vec3d cameraPos, long long2, float float2, int int2, float float3, CallbackInfo ci) {
      this.richAccountBegin();
   }

   @Inject(method = "renderClouds", at = @At("RETURN"), require = 0)
   private void richAccountRenderCloudsEnd(FrameGraphBuilder frameGraphBuilder, CloudRenderMode mode, Vec3d cameraPos, long long2, float float2, int int2, float float3, CallbackInfo ci) {
      this.richAccountEnd();
   }

   @Inject(method = "renderWeather", at = @At("HEAD"), require = 0)
   private void richAccountRenderWeatherStart(FrameGraphBuilder frameGraphBuilder, GpuBufferSlice gpuBufferSlice, CallbackInfo ci) {
      this.richAccountBegin();
   }

   @Inject(method = "renderWeather", at = @At("RETURN"), require = 0)
   private void richAccountRenderWeatherEnd(FrameGraphBuilder frameGraphBuilder, GpuBufferSlice gpuBufferSlice, CallbackInfo ci) {
      this.richAccountEnd();
   }

   @Inject(method = "renderLateDebug", at = @At("HEAD"), require = 0)
   private void richAccountRenderLateDebugStart(FrameGraphBuilder frameGraphBuilder, CameraRenderState cameraRenderState, GpuBufferSlice fogBuffer, Matrix4f matrix4f, CallbackInfo ci) {
      this.richAccountBegin();
   }

   @Inject(method = "renderLateDebug", at = @At("RETURN"), require = 0)
   private void richAccountRenderLateDebugEnd(FrameGraphBuilder frameGraphBuilder, CameraRenderState cameraRenderState, GpuBufferSlice fogBuffer, Matrix4f matrix4f, CallbackInfo ci) {
      this.richAccountEnd();
   }
}
