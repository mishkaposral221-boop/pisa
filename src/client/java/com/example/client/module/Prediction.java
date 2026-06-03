package com.example.client.module;

import com.example.client.menu.data.ModModule;
import com.example.client.menu.data.Setting;
import com.ferra13671.cometrenderer.CometRenderer;
import com.ferra13671.cometrenderer.glsl.GlProgram;
import com.ferra13671.cometrenderer.minecraft.CRM;
import com.ferra13671.cometrenderer.minecraft.CustomDrawMode;
import com.ferra13671.cometrenderer.minecraft.CustomVertexElementTypes;
import com.ferra13671.cometrenderer.minecraft.CustomVertexFormats;
import com.ferra13671.cometrenderer.minecraft.RenderColor;
import com.ferra13671.cometrenderer.vertex.DrawMode;
import com.ferra13671.cometrenderer.vertex.format.VertexFormat;
import com.ferra13671.cometrenderer.vertex.mesh.IMesh;
import com.ferra13671.cometrenderer.vertex.mesh.Mesh;
import com.ferra13671.cometrenderer.vertex.mesh.MeshBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EggItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.Item;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.item.SnowballItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.item.TridentItem;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.DataComponentTypes;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;

public class Prediction {
    private ModModule moduleRef;
    private static Prediction INSTANCE;
    private float lastYaw;
    private float lastPitch;
    private int lastItemHash;
    private List<List<Vec3d>> cachedTrajectories;
    private int cacheFrameCounter = 0;
    private static final float DRAG = 0.99f;
    private static final int MAX_TICKS = 80;
    private static final int SKIP_FIRST = 3;

    public Prediction() {
        INSTANCE = this;
    }

    public static Prediction getInstance() {
        return INSTANCE;
    }

    public void setModuleRef(ModModule ref) {
        this.moduleRef = ref;
    }

    public boolean isEnabled() {
        return this.moduleRef != null && this.moduleRef.enabled;
    }

    public boolean showPearl() {
        return this.isEnabled() && this.getSetting(0).getBool();
    }

    public boolean showArrow() {
        return this.isEnabled() && this.getSetting(1).getBool();
    }

    public boolean showSnowball() {
        return this.isEnabled() && this.getSetting(2).getBool();
    }

    public boolean showPotion() {
        return this.isEnabled() && this.getSetting(3).getBool();
    }

    public boolean showTrident() {
        return this.isEnabled() && this.getSetting(4).getBool();
    }

    public boolean showEgg() {
        return this.isEnabled() && this.getSetting(5).getBool();
    }

    private Setting getSetting(int index) {
        if (this.moduleRef == null || index >= this.moduleRef.settings.length) {
            return new Setting("", true);
        }
        return this.moduleRef.settings[index];
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void renderWorld() {
        boolean needRecalc;
        MinecraftClient client = MinecraftClient.getInstance();
        if (!this.isEnabled() || client.player == null || client.world == null) {
            return;
        }
        ClientPlayerEntity player = client.player;
        Item mainItem = player.getMainHandStack().getItem();
        float gravity = 0.0f;
        float power = 0.0f;
        boolean shouldPredict = false;
        float pitchOffset = 0.0f;
        boolean multishot = false;
        if (mainItem instanceof EnderPearlItem && this.showPearl()) {
            gravity = 0.03f;
            power = 1.5f;
            shouldPredict = true;
        } else if (mainItem instanceof BowItem && this.showArrow()) {
            gravity = 0.05f;
            float charge = BowItem.getPullProgress((int)player.getItemUseTime());
            power = Math.max(charge * 3.0f, 0.1f);
            shouldPredict = player.isUsingItem();
        } else if (mainItem instanceof CrossbowItem && this.showArrow()) {
            gravity = 0.05f;
            power = 3.15f;
            shouldPredict = true;
            multishot = this.hasMultishot((PlayerEntity)player);
        } else if (mainItem instanceof SnowballItem && this.showSnowball()) {
            gravity = 0.03f;
            power = 1.5f;
            shouldPredict = true;
        } else if ((mainItem instanceof SplashPotionItem || mainItem instanceof LingeringPotionItem) && this.showPotion()) {
            gravity = 0.05f;
            power = 0.5f;
            shouldPredict = true;
            pitchOffset = -20.0f;
        } else if (mainItem instanceof TridentItem && this.showTrident()) {
            gravity = 0.05f;
            power = 2.5f;
            shouldPredict = true;
        } else if (mainItem instanceof EggItem && this.showEgg()) {
            gravity = 0.03f;
            power = 1.5f;
            shouldPredict = true;
        }
        if (!shouldPredict) {
            this.cachedTrajectories = null;
            return;
        }
        float curYaw = player.getYaw();
        float curPitch = player.getPitch();
        int curItemHash = System.identityHashCode(mainItem);
        ++this.cacheFrameCounter;
        boolean bl = needRecalc = this.cachedTrajectories == null || Math.abs(curYaw - this.lastYaw) > 0.1f || Math.abs(curPitch - this.lastPitch) > 0.1f || curItemHash != this.lastItemHash || this.cacheFrameCounter > 3;
        if (needRecalc) {
            this.lastYaw = curYaw;
            this.lastPitch = curPitch;
            this.lastItemHash = curItemHash;
            this.cacheFrameCounter = 0;
            this.cachedTrajectories = new ArrayList<List<Vec3d>>();
            this.cachedTrajectories.add(this.simulateTrajectory(client, (PlayerEntity)player, gravity, power, pitchOffset, 0.0f));
            if (multishot) {
                this.cachedTrajectories.add(this.simulateTrajectory(client, (PlayerEntity)player, gravity, power, pitchOffset, 10.0f));
                this.cachedTrajectories.add(this.simulateTrajectory(client, (PlayerEntity)player, gravity, power, pitchOffset, -10.0f));
            }
        }
        Vec3d camPos = client.gameRenderer.getCamera().getCameraPos();
        int prevProgram = GL11.glGetInteger((int)35725);
        boolean prevBlend = GL11.glIsEnabled((int)3042);
        boolean prevDepth = GL11.glIsEnabled((int)2929);
        try {
            CometRenderer.setCurrentProgram((GlProgram)CRM.getPrograms().POSITION_COLOR);
            CometRenderer.applyShaderColorUniform();
            CRM.applyMatrixUniform();
            GL11.glEnable((int)3042);
            GL14.glBlendFuncSeparate((int)770, (int)771, (int)1, (int)0);
            GL11.glDisable((int)2929);
            GL11.glDepthMask((boolean)false);
            MeshBuilder dotBuilder = Mesh.builder((DrawMode)CustomDrawMode.CUBE, (VertexFormat)CustomVertexFormats.POSITION_COLOR);
            MeshBuilder landBuilder = Mesh.builder((DrawMode)CustomDrawMode.CUBE, (VertexFormat)CustomVertexFormats.POSITION_COLOR);
            for (int t = 0; t < this.cachedTrajectories.size(); ++t) {
                List<Vec3d> traj = this.cachedTrajectories.get(t);
                if (traj.size() < 2) continue;
                int alpha = t == 0 ? 180 : 120;
                RenderColor dotColor = RenderColor.of((int)200, (int)160, (int)255, (int)alpha);
                int step = Math.max(2, traj.size() / 25);
                for (int i = 3; i < traj.size() - 1; i += step) {
                    Vec3d p = traj.get(i);
                    float px = (float)(p.x - camPos.x);
                    float py = (float)(p.y - camPos.y);
                    float pz = (float)(p.z - camPos.z);
                    float s = 0.03f;
                    this.addBoxVerts(dotBuilder, px - s, py - s, pz - s, px + s, py + s, pz + s, dotColor);
                }
                Vec3d land = traj.get(traj.size() - 1);
                float lx = (float)(land.x - camPos.x);
                float ly = (float)(land.y - camPos.y);
                float lz = (float)(land.z - camPos.z);
                float ms = 0.12f;
                this.addBoxVertsGradient(landBuilder, lx - ms, ly - ms, lz - ms, lx + ms, ly + ms, lz + ms, RenderColor.of((int)100, (int)30, (int)200, (int)60), RenderColor.of((int)180, (int)100, (int)255, (int)90));
            }
            Mesh dotMesh = dotBuilder.buildNullable();
            Mesh landMesh = landBuilder.buildNullable();
            try {
                if (dotMesh != null) {
                    CometRenderer.draw((IMesh)dotMesh);
                }
                if (landMesh != null) {
                    CometRenderer.draw((IMesh)landMesh);
                }
            }
            finally {
                if (dotMesh != null) {
                    dotMesh.close();
                }
                if (landMesh != null) {
                    landMesh.close();
                }
            }
        }
        finally {
            GL11.glDepthMask((boolean)true);
            if (prevDepth) {
                GL11.glEnable((int)2929);
            } else {
                GL11.glDisable((int)2929);
            }
            if (prevBlend) {
                GL11.glEnable((int)3042);
            } else {
                GL11.glDisable((int)3042);
            }
            GL20.glUseProgram((int)prevProgram);
        }
    }

    private boolean hasMultishot(PlayerEntity player) {
        try {
            ItemEnchantmentsComponent enchants = (ItemEnchantmentsComponent)player.getMainHandStack().getOrDefault(DataComponentTypes.ENCHANTMENTS, (Object)ItemEnchantmentsComponent.DEFAULT);
            for (Object2IntMap.Entry entry : enchants.getEnchantmentEntries()) {
                String name = ((RegistryEntry)entry.getKey()).getIdAsString();
                if (name == null || !name.contains("multishot")) continue;
                return true;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return false;
    }

    private List<Vec3d> simulateTrajectory(MinecraftClient client, PlayerEntity player, float gravity, float power, float pitchOffset, float yawOffset) {
        ArrayList<Vec3d> points = new ArrayList<Vec3d>();
        ClientWorld level = client.world;
        float yaw = player.getYaw() + yawOffset;
        float pitch = player.getPitch() + pitchOffset;
        double radYaw = Math.toRadians(yaw);
        double radPitch = Math.toRadians(pitch);
        Vec3d pos = player.getCameraPosVec(1.0f);
        double vx = -Math.sin(radYaw) * Math.cos(radPitch) * (double)power;
        double vy = -Math.sin(radPitch) * (double)power;
        double vz = Math.cos(radYaw) * Math.cos(radPitch) * (double)power;
        points.add(pos);
        for (int tick = 0; tick < 80; ++tick) {
            Vec3d nextPos = new Vec3d(pos.x + vx, pos.y + vy, pos.z + vz);
            BlockHitResult hit = level.raycast(new RaycastContext(pos, nextPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
            if (hit.getType() != HitResult.Type.MISS) {
                points.add(hit.getPos());
                break;
            }
            if (nextPos.y < (double)level.getBottomY()) {
                points.add(nextPos);
                break;
            }
            points.add(nextPos);
            pos = nextPos;
            vy -= (double)gravity;
            vx *= (double)0.99f;
            vy *= (double)0.99f;
            vz *= (double)0.99f;
        }
        return points;
    }

    private void addBoxVerts(MeshBuilder b, float x1, float y1, float z1, float x2, float y2, float z2, RenderColor c) {
        b.vertex(x2, y1, z1).element("Color", CustomVertexElementTypes.RENDER_COLOR, new RenderColor[]{c});
        b.vertex(x2, y1, z2).element("Color", CustomVertexElementTypes.RENDER_COLOR, new RenderColor[]{c});
        b.vertex(x1, y1, z2).element("Color", CustomVertexElementTypes.RENDER_COLOR, new RenderColor[]{c});
        b.vertex(x1, y1, z1).element("Color", CustomVertexElementTypes.RENDER_COLOR, new RenderColor[]{c});
        b.vertex(x1, y2, z1).element("Color", CustomVertexElementTypes.RENDER_COLOR, new RenderColor[]{c});
        b.vertex(x1, y2, z2).element("Color", CustomVertexElementTypes.RENDER_COLOR, new RenderColor[]{c});
        b.vertex(x2, y2, z2).element("Color", CustomVertexElementTypes.RENDER_COLOR, new RenderColor[]{c});
        b.vertex(x2, y2, z1).element("Color", CustomVertexElementTypes.RENDER_COLOR, new RenderColor[]{c});
    }

    private void addBoxVertsGradient(MeshBuilder b, float x1, float y1, float z1, float x2, float y2, float z2, RenderColor bottom, RenderColor top) {
        b.vertex(x2, y1, z1).element("Color", CustomVertexElementTypes.RENDER_COLOR, new RenderColor[]{bottom});
        b.vertex(x2, y1, z2).element("Color", CustomVertexElementTypes.RENDER_COLOR, new RenderColor[]{bottom});
        b.vertex(x1, y1, z2).element("Color", CustomVertexElementTypes.RENDER_COLOR, new RenderColor[]{bottom});
        b.vertex(x1, y1, z1).element("Color", CustomVertexElementTypes.RENDER_COLOR, new RenderColor[]{bottom});
        b.vertex(x1, y2, z1).element("Color", CustomVertexElementTypes.RENDER_COLOR, new RenderColor[]{top});
        b.vertex(x1, y2, z2).element("Color", CustomVertexElementTypes.RENDER_COLOR, new RenderColor[]{top});
        b.vertex(x2, y2, z2).element("Color", CustomVertexElementTypes.RENDER_COLOR, new RenderColor[]{top});
        b.vertex(x2, y2, z1).element("Color", CustomVertexElementTypes.RENDER_COLOR, new RenderColor[]{top});
    }
}

