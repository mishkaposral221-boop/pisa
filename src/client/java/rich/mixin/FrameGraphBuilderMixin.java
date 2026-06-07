package rich.mixin;

import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.render.FramePass;
import net.minecraft.client.util.ObjectAllocator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.util.profiler.FrameProfiler;
import rich.util.profiler.ProfilerFrameGraphHooks;

@Mixin(FrameGraphBuilder.class)
public abstract class FrameGraphBuilderMixin {
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

   @Inject(method = "createPass", at = @At("RETURN"), require = 0)
   private void richRegisterFrameGraphPass(String name, CallbackInfoReturnable<FramePass> cir) {
      ProfilerFrameGraphHooks.registerPass(cir.getReturnValue(), name);
   }

   @Inject(method = "run(Lnet/minecraft/client/util/ObjectAllocator;)V", at = @At("HEAD"), require = 0)
   private void richProfileRunStart(ObjectAllocator allocator, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/FrameGraph/run");
   }

   @Inject(method = "run(Lnet/minecraft/client/util/ObjectAllocator;)V", at = @At("RETURN"), require = 0)
   private void richProfileRunEnd(ObjectAllocator allocator, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "run(Lnet/minecraft/client/util/ObjectAllocator;Lnet/minecraft/client/render/FrameGraphBuilder$Profiler;)V", at = @At("HEAD"), require = 0)
   private void richProfileRunWithProfilerStart(ObjectAllocator allocator, FrameGraphBuilder.Profiler profiler, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/FrameGraph/runWithProfiler");
   }

   @Inject(method = "run(Lnet/minecraft/client/util/ObjectAllocator;Lnet/minecraft/client/render/FrameGraphBuilder$Profiler;)V", at = @At("RETURN"), require = 0)
   private void richProfileRunWithProfilerEnd(ObjectAllocator allocator, FrameGraphBuilder.Profiler profiler, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "collectPassesToVisit", at = @At("HEAD"), require = 0)
   private void richProfileCollectPassesStart(CallbackInfoReturnable<java.util.BitSet> cir) {
      this.richProfileBegin("Minecraft/FrameGraph/collectPassesToVisit");
   }

   @Inject(method = "collectPassesToVisit", at = @At("RETURN"), require = 0)
   private void richProfileCollectPassesEnd(CallbackInfoReturnable<java.util.BitSet> cir) {
      this.richProfileEnd();
   }

   @Inject(method = "checkResources", at = @At("HEAD"), require = 0)
   private void richProfileCheckResourcesStart(java.util.Collection<?> passes, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/FrameGraph/checkResources");
   }

   @Inject(method = "checkResources", at = @At("RETURN"), require = 0)
   private void richProfileCheckResourcesEnd(java.util.Collection<?> passes, CallbackInfo ci) {
      this.richProfileEnd();
   }
}
