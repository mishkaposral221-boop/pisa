package rich.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.gl.DynamicUniforms.ChunkSectionsValue;
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
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.IMinecraft;
import rich.modules.impl.render.ChunkAnimator;
import rich.modules.impl.render.NoRender;
import rich.util.profiler.FrameProfiler;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin implements IMinecraft {
   @ModifyArg(method = "renderBlockLayers", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0), index = 0, require = 0)
   private Object modifyChunkSectionsValue(Object var1) {
      if (var1 instanceof net.minecraft.client.gl.DynamicUniforms.ChunkSectionsValue var2) {
         ChunkAnimator var3 = ChunkAnimator.getInstance();
         if (var3 != null && var3.isState()) {
            float var4 = var2.visibility();
            if (var4 >= 1.0F) {
               return var1;
            }

            int var5 = (int)((1.0F - var4) * 100.0F);
            if (var5 == 0) {
               return var1;
            }

            int var6 = var2.y() - var5;
            return new net.minecraft.client.gl.DynamicUniforms.ChunkSectionsValue(var2.modelView(), var2.x(), var6, var2.z(), var2.visibility(), var2.textureAtlasWidth(), var2.textureAtlasHeight());
         }
      }

      return var1;
   }

   @Inject(method = "hasBlindnessOrDarkness", at = @At("HEAD"), cancellable = true, require = 0)
   private void onHasBlindnessOrDarkness(Camera var1, CallbackInfoReturnable<Boolean> var2) {
      NoRender var3 = NoRender.getInstance();
      if (var3 != null && var3.isState()) {
         if (var1.getFocusedEntity() instanceof LivingEntity var5) {
            boolean var6 = var5.hasStatusEffect(StatusEffects.BLINDNESS);
            boolean var7 = var5.hasStatusEffect(StatusEffects.DARKNESS);
            if (var3.modeSetting.isSelected("Bad Effects") && var6 && !var7) {
               var2.setReturnValue(false);
            }

            if (var3.modeSetting.isSelected("Darkness") && var7 && !var6) {
               var2.setReturnValue(false);
            }

            if (var3.modeSetting.isSelected("Bad Effects") && var3.modeSetting.isSelected("Darkness")) {
               var2.setReturnValue(false);
            }
         }
      }
   }

   @Unique
   private void richProfileBegin(String name) {
      FrameProfiler profiler = FrameProfiler.getInstance();
      if (profiler.isEnabled()) {
         profiler.begin(name);
      }
   }

   @Unique
   private void richProfileEnd() {
      FrameProfiler profiler = FrameProfiler.getInstance();
      if (profiler.isEnabled()) {
         profiler.end();
      }
   }

   @Inject(method = "render", at = @At("HEAD"), require = 0)
   private void richProfileWorldRendererRenderStart(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f basicProjectionMatrix, Matrix4f projectionMatrix, GpuBufferSlice fogBuffer, org.joml.Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/WorldRenderer.render");
   }

   @Inject(method = "render", at = @At("RETURN"), require = 0)
   private void richProfileWorldRendererRenderEnd(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f basicProjectionMatrix, Matrix4f projectionMatrix, GpuBufferSlice fogBuffer, org.joml.Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderMain", at = @At("HEAD"), require = 0)
   private void richProfileRenderMainStart(FrameGraphBuilder frameGraphBuilder, Frustum frustum, Matrix4f posMatrix, GpuBufferSlice fogBuffer, boolean renderBlockOutline, WorldRenderState state, RenderTickCounter tickCounter, Profiler profiler, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/WorldRenderer/renderMain");
   }

   @Inject(method = "renderMain", at = @At("RETURN"), require = 0)
   private void richProfileRenderMainEnd(FrameGraphBuilder frameGraphBuilder, Frustum frustum, Matrix4f posMatrix, GpuBufferSlice fogBuffer, boolean renderBlockOutline, WorldRenderState state, RenderTickCounter tickCounter, Profiler profiler, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "updateCamera", at = @At("HEAD"), require = 0)
   private void richProfileUpdateCameraStart(Camera camera, Frustum frustum, boolean spectator, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/WorldRenderer/updateCamera");
   }

   @Inject(method = "updateCamera", at = @At("RETURN"), require = 0)
   private void richProfileUpdateCameraEnd(Camera camera, Frustum frustum, boolean spectator, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "updateChunks", at = @At("HEAD"), require = 0)
   private void richProfileUpdateChunksStart(Camera camera, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/WorldRenderer/updateChunks");
   }

   @Inject(method = "updateChunks", at = @At("RETURN"), require = 0)
   private void richProfileUpdateChunksEnd(Camera camera, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "applyFrustum", at = @At("HEAD"), require = 0)
   private void richProfileApplyFrustumStart(Frustum frustum, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/WorldRenderer/applyFrustum");
   }

   @Inject(method = "applyFrustum", at = @At("RETURN"), require = 0)
   private void richProfileApplyFrustumEnd(Frustum frustum, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderBlockLayers", at = @At("HEAD"), require = 0)
   private void richProfileRenderBlockLayersStart(Matrix4fc matrix, double cameraX, double cameraY, double cameraZ, CallbackInfoReturnable<SectionRenderState> cir) {
      this.richProfileBegin("Minecraft/WorldRenderer/renderBlockLayers");
   }

   @Inject(method = "renderBlockLayers", at = @At("RETURN"), require = 0)
   private void richProfileRenderBlockLayersEnd(Matrix4fc matrix, double cameraX, double cameraY, double cameraZ, CallbackInfoReturnable<SectionRenderState> cir) {
      this.richProfileEnd();
   }

   @Inject(method = "translucencySort", at = @At("HEAD"), require = 0)
   private void richProfileTranslucencySortStart(Vec3d cameraPos, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/WorldRenderer/translucencySort");
   }

   @Inject(method = "translucencySort", at = @At("RETURN"), require = 0)
   private void richProfileTranslucencySortEnd(Vec3d cameraPos, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "scheduleChunkTranslucencySort", at = @At("HEAD"), require = 0)
   private void richProfileScheduleChunkTranslucencySortStart(ChunkBuilder.BuiltChunk chunk, NormalizedRelativePos relativePos, Vec3d cameraPos, boolean needsUpdate, boolean ignoreCameraAlignment, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/WorldRenderer/scheduleChunkTranslucencySort");
   }

   @Inject(method = "scheduleChunkTranslucencySort", at = @At("RETURN"), require = 0)
   private void richProfileScheduleChunkTranslucencySortEnd(ChunkBuilder.BuiltChunk chunk, NormalizedRelativePos relativePos, Vec3d cameraPos, boolean needsUpdate, boolean ignoreCameraAlignment, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "fillEntityRenderStates", at = @At("HEAD"), require = 0)
   private void richProfileFillEntityRenderStatesStart(Camera camera, Frustum frustum, RenderTickCounter tickCounter, WorldRenderState renderStates, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/WorldRenderer/entities/fillStates");
   }

   @Inject(method = "fillEntityRenderStates", at = @At("RETURN"), require = 0)
   private void richProfileFillEntityRenderStatesEnd(Camera camera, Frustum frustum, RenderTickCounter tickCounter, WorldRenderState renderStates, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "pushEntityRenders", at = @At("HEAD"), require = 0)
   private void richProfilePushEntityRendersStart(MatrixStack matrices, WorldRenderState renderStates, OrderedRenderCommandQueue queue, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/WorldRenderer/entities/pushRenders");
   }

   @Inject(method = "pushEntityRenders", at = @At("RETURN"), require = 0)
   private void richProfilePushEntityRendersEnd(MatrixStack matrices, WorldRenderState renderStates, OrderedRenderCommandQueue queue, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "fillBlockEntityRenderStates", at = @At("HEAD"), require = 0)
   private void richProfileFillBlockEntityRenderStatesStart(Camera camera, float tickProgress, WorldRenderState renderStates, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/WorldRenderer/blockEntities/fillStates");
   }

   @Inject(method = "fillBlockEntityRenderStates", at = @At("RETURN"), require = 0)
   private void richProfileFillBlockEntityRenderStatesEnd(Camera camera, float tickProgress, WorldRenderState renderStates, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderBlockEntities", at = @At("HEAD"), require = 0)
   private void richProfileRenderBlockEntitiesStart(MatrixStack matrices, WorldRenderState renderStates, OrderedRenderCommandQueueImpl queue, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/WorldRenderer/blockEntities/render");
   }

   @Inject(method = "renderBlockEntities", at = @At("RETURN"), require = 0)
   private void richProfileRenderBlockEntitiesEnd(MatrixStack matrices, WorldRenderState renderStates, OrderedRenderCommandQueueImpl queue, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "fillBlockBreakingProgressRenderState", at = @At("HEAD"), require = 0)
   private void richProfileFillBlockBreakingProgressStart(Camera camera, WorldRenderState renderStates, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/WorldRenderer/blockBreaking/fillState");
   }

   @Inject(method = "fillBlockBreakingProgressRenderState", at = @At("RETURN"), require = 0)
   private void richProfileFillBlockBreakingProgressEnd(Camera camera, WorldRenderState renderStates, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderBlockDamage", at = @At("HEAD"), require = 0)
   private void richProfileRenderBlockDamageStart(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, WorldRenderState renderStates, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/WorldRenderer/blockDamage/render");
   }

   @Inject(method = "renderBlockDamage", at = @At("RETURN"), require = 0)
   private void richProfileRenderBlockDamageEnd(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, WorldRenderState renderStates, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderTargetBlockOutline", at = @At("HEAD"), require = 0)
   private void richProfileRenderTargetBlockOutlineStart(VertexConsumerProvider.Immediate immediate, MatrixStack matrices, boolean renderBlockOutline, WorldRenderState renderStates, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/WorldRenderer/blockOutline/render");
   }

   @Inject(method = "renderTargetBlockOutline", at = @At("RETURN"), require = 0)
   private void richProfileRenderTargetBlockOutlineEnd(VertexConsumerProvider.Immediate immediate, MatrixStack matrices, boolean renderBlockOutline, WorldRenderState renderStates, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderSky", at = @At("HEAD"), require = 0)
   private void richProfileRenderSkyStart(FrameGraphBuilder frameGraphBuilder, Camera camera, GpuBufferSlice fogBuffer, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/WorldRenderer/sky");
   }

   @Inject(method = "renderSky", at = @At("RETURN"), require = 0)
   private void richProfileRenderSkyEnd(FrameGraphBuilder frameGraphBuilder, Camera camera, GpuBufferSlice fogBuffer, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderParticles", at = @At("HEAD"), require = 0)
   private void richProfileRenderParticlesStart(FrameGraphBuilder frameGraphBuilder, GpuBufferSlice fogBuffer, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/WorldRenderer/vanillaParticles");
   }

   @Inject(method = "renderParticles", at = @At("RETURN"), require = 0)
   private void richProfileRenderParticlesEnd(FrameGraphBuilder frameGraphBuilder, GpuBufferSlice fogBuffer, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderClouds", at = @At("HEAD"), require = 0)
   private void richProfileRenderCloudsStart(FrameGraphBuilder frameGraphBuilder, CloudRenderMode mode, Vec3d cameraPos, long long2, float float2, int int2, float float3, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/WorldRenderer/clouds");
   }

   @Inject(method = "renderClouds", at = @At("RETURN"), require = 0)
   private void richProfileRenderCloudsEnd(FrameGraphBuilder frameGraphBuilder, CloudRenderMode mode, Vec3d cameraPos, long long2, float float2, int int2, float float3, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderWeather", at = @At("HEAD"), require = 0)
   private void richProfileRenderWeatherStart(FrameGraphBuilder frameGraphBuilder, GpuBufferSlice gpuBufferSlice, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/WorldRenderer/weather");
   }

   @Inject(method = "renderWeather", at = @At("RETURN"), require = 0)
   private void richProfileRenderWeatherEnd(FrameGraphBuilder frameGraphBuilder, GpuBufferSlice gpuBufferSlice, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderLateDebug", at = @At("HEAD"), require = 0)
   private void richProfileRenderLateDebugStart(FrameGraphBuilder frameGraphBuilder, CameraRenderState cameraRenderState, GpuBufferSlice fogBuffer, Matrix4f matrix4f, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/WorldRenderer/debugLate");
   }

   @Inject(method = "renderLateDebug", at = @At("RETURN"), require = 0)
   private void richProfileRenderLateDebugEnd(FrameGraphBuilder frameGraphBuilder, CameraRenderState cameraRenderState, GpuBufferSlice fogBuffer, Matrix4f matrix4f, CallbackInfo ci) {
      this.richProfileEnd();
   }
}
