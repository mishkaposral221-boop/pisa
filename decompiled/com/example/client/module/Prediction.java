/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.ferra13671.cometrenderer.CometRenderer
 *  com.ferra13671.cometrenderer.glsl.GlProgram
 *  com.ferra13671.cometrenderer.minecraft.CRM
 *  com.ferra13671.cometrenderer.minecraft.CustomDrawMode
 *  com.ferra13671.cometrenderer.minecraft.CustomVertexElementTypes
 *  com.ferra13671.cometrenderer.minecraft.CustomVertexFormats
 *  com.ferra13671.cometrenderer.minecraft.RenderColor
 *  com.ferra13671.cometrenderer.vertex.DrawMode
 *  com.ferra13671.cometrenderer.vertex.format.VertexFormat
 *  com.ferra13671.cometrenderer.vertex.mesh.IMesh
 *  com.ferra13671.cometrenderer.vertex.mesh.Mesh
 *  com.ferra13671.cometrenderer.vertex.mesh.MeshBuilder
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_1753
 *  net.minecraft.class_1764
 *  net.minecraft.class_1771
 *  net.minecraft.class_1776
 *  net.minecraft.class_1792
 *  net.minecraft.class_1803
 *  net.minecraft.class_1823
 *  net.minecraft.class_1828
 *  net.minecraft.class_1835
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_3965
 *  net.minecraft.class_638
 *  net.minecraft.class_6880
 *  net.minecraft.class_746
 *  net.minecraft.class_9304
 *  net.minecraft.class_9334
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GL14
 *  org.lwjgl.opengl.GL20
 */
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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1753;
import net.minecraft.class_1764;
import net.minecraft.class_1771;
import net.minecraft.class_1776;
import net.minecraft.class_1792;
import net.minecraft.class_1803;
import net.minecraft.class_1823;
import net.minecraft.class_1828;
import net.minecraft.class_1835;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_638;
import net.minecraft.class_6880;
import net.minecraft.class_746;
import net.minecraft.class_9304;
import net.minecraft.class_9334;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;

@Environment(value=EnvType.CLIENT)
public class Prediction {
    private ModModule moduleRef;
    private static Prediction INSTANCE;
    private float lastYaw;
    private float lastPitch;
    private int lastItemHash;
    private List<List<class_243>> cachedTrajectories;
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
        class_310 client = class_310.method_1551();
        if (!this.isEnabled() || client.field_1724 == null || client.field_1687 == null) {
            return;
        }
        class_746 player = client.field_1724;
        class_1792 mainItem = player.method_6047().method_7909();
        float gravity = 0.0f;
        float power = 0.0f;
        boolean shouldPredict = false;
        float pitchOffset = 0.0f;
        boolean multishot = false;
        if (mainItem instanceof class_1776 && this.showPearl()) {
            gravity = 0.03f;
            power = 1.5f;
            shouldPredict = true;
        } else if (mainItem instanceof class_1753 && this.showArrow()) {
            gravity = 0.05f;
            float charge = class_1753.method_7722((int)player.method_6048());
            power = Math.max(charge * 3.0f, 0.1f);
            shouldPredict = player.method_6115();
        } else if (mainItem instanceof class_1764 && this.showArrow()) {
            gravity = 0.05f;
            power = 3.15f;
            shouldPredict = true;
            multishot = this.hasMultishot((class_1657)player);
        } else if (mainItem instanceof class_1823 && this.showSnowball()) {
            gravity = 0.03f;
            power = 1.5f;
            shouldPredict = true;
        } else if ((mainItem instanceof class_1828 || mainItem instanceof class_1803) && this.showPotion()) {
            gravity = 0.05f;
            power = 0.5f;
            shouldPredict = true;
            pitchOffset = -20.0f;
        } else if (mainItem instanceof class_1835 && this.showTrident()) {
            gravity = 0.05f;
            power = 2.5f;
            shouldPredict = true;
        } else if (mainItem instanceof class_1771 && this.showEgg()) {
            gravity = 0.03f;
            power = 1.5f;
            shouldPredict = true;
        }
        if (!shouldPredict) {
            this.cachedTrajectories = null;
            return;
        }
        float curYaw = player.method_36454();
        float curPitch = player.method_36455();
        int curItemHash = System.identityHashCode(mainItem);
        ++this.cacheFrameCounter;
        boolean bl = needRecalc = this.cachedTrajectories == null || Math.abs(curYaw - this.lastYaw) > 0.1f || Math.abs(curPitch - this.lastPitch) > 0.1f || curItemHash != this.lastItemHash || this.cacheFrameCounter > 3;
        if (needRecalc) {
            this.lastYaw = curYaw;
            this.lastPitch = curPitch;
            this.lastItemHash = curItemHash;
            this.cacheFrameCounter = 0;
            this.cachedTrajectories = new ArrayList<List<class_243>>();
            this.cachedTrajectories.add(this.simulateTrajectory(client, (class_1657)player, gravity, power, pitchOffset, 0.0f));
            if (multishot) {
                this.cachedTrajectories.add(this.simulateTrajectory(client, (class_1657)player, gravity, power, pitchOffset, 10.0f));
                this.cachedTrajectories.add(this.simulateTrajectory(client, (class_1657)player, gravity, power, pitchOffset, -10.0f));
            }
        }
        class_243 camPos = client.field_1773.method_19418().method_71156();
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
                List<class_243> traj = this.cachedTrajectories.get(t);
                if (traj.size() < 2) continue;
                int alpha = t == 0 ? 180 : 120;
                RenderColor dotColor = RenderColor.of((int)200, (int)160, (int)255, (int)alpha);
                int step = Math.max(2, traj.size() / 25);
                for (int i = 3; i < traj.size() - 1; i += step) {
                    class_243 p = traj.get(i);
                    float px = (float)(p.field_1352 - camPos.field_1352);
                    float py = (float)(p.field_1351 - camPos.field_1351);
                    float pz = (float)(p.field_1350 - camPos.field_1350);
                    float s = 0.03f;
                    this.addBoxVerts(dotBuilder, px - s, py - s, pz - s, px + s, py + s, pz + s, dotColor);
                }
                class_243 land = traj.get(traj.size() - 1);
                float lx = (float)(land.field_1352 - camPos.field_1352);
                float ly = (float)(land.field_1351 - camPos.field_1351);
                float lz = (float)(land.field_1350 - camPos.field_1350);
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

    private boolean hasMultishot(class_1657 player) {
        try {
            class_9304 enchants = (class_9304)player.method_6047().method_58695(class_9334.field_49633, (Object)class_9304.field_49385);
            for (Object2IntMap.Entry entry : enchants.method_57539()) {
                String name = ((class_6880)entry.getKey()).method_55840();
                if (name == null || !name.contains("multishot")) continue;
                return true;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return false;
    }

    private List<class_243> simulateTrajectory(class_310 client, class_1657 player, float gravity, float power, float pitchOffset, float yawOffset) {
        ArrayList<class_243> points = new ArrayList<class_243>();
        class_638 level = client.field_1687;
        float yaw = player.method_36454() + yawOffset;
        float pitch = player.method_36455() + pitchOffset;
        double radYaw = Math.toRadians(yaw);
        double radPitch = Math.toRadians(pitch);
        class_243 pos = player.method_5836(1.0f);
        double vx = -Math.sin(radYaw) * Math.cos(radPitch) * (double)power;
        double vy = -Math.sin(radPitch) * (double)power;
        double vz = Math.cos(radYaw) * Math.cos(radPitch) * (double)power;
        points.add(pos);
        for (int tick = 0; tick < 80; ++tick) {
            class_243 nextPos = new class_243(pos.field_1352 + vx, pos.field_1351 + vy, pos.field_1350 + vz);
            class_3965 hit = level.method_17742(new class_3959(pos, nextPos, class_3959.class_3960.field_17558, class_3959.class_242.field_1348, (class_1297)player));
            if (hit.method_17783() != class_239.class_240.field_1333) {
                points.add(hit.method_17784());
                break;
            }
            if (nextPos.field_1351 < (double)level.method_31607()) {
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
        b.vertex(x2, y1, z1).element("Color", CustomVertexElementTypes.RENDER_COLOR, (Object[])new RenderColor[]{c});
        b.vertex(x2, y1, z2).element("Color", CustomVertexElementTypes.RENDER_COLOR, (Object[])new RenderColor[]{c});
        b.vertex(x1, y1, z2).element("Color", CustomVertexElementTypes.RENDER_COLOR, (Object[])new RenderColor[]{c});
        b.vertex(x1, y1, z1).element("Color", CustomVertexElementTypes.RENDER_COLOR, (Object[])new RenderColor[]{c});
        b.vertex(x1, y2, z1).element("Color", CustomVertexElementTypes.RENDER_COLOR, (Object[])new RenderColor[]{c});
        b.vertex(x1, y2, z2).element("Color", CustomVertexElementTypes.RENDER_COLOR, (Object[])new RenderColor[]{c});
        b.vertex(x2, y2, z2).element("Color", CustomVertexElementTypes.RENDER_COLOR, (Object[])new RenderColor[]{c});
        b.vertex(x2, y2, z1).element("Color", CustomVertexElementTypes.RENDER_COLOR, (Object[])new RenderColor[]{c});
    }

    private void addBoxVertsGradient(MeshBuilder b, float x1, float y1, float z1, float x2, float y2, float z2, RenderColor bottom, RenderColor top) {
        b.vertex(x2, y1, z1).element("Color", CustomVertexElementTypes.RENDER_COLOR, (Object[])new RenderColor[]{bottom});
        b.vertex(x2, y1, z2).element("Color", CustomVertexElementTypes.RENDER_COLOR, (Object[])new RenderColor[]{bottom});
        b.vertex(x1, y1, z2).element("Color", CustomVertexElementTypes.RENDER_COLOR, (Object[])new RenderColor[]{bottom});
        b.vertex(x1, y1, z1).element("Color", CustomVertexElementTypes.RENDER_COLOR, (Object[])new RenderColor[]{bottom});
        b.vertex(x1, y2, z1).element("Color", CustomVertexElementTypes.RENDER_COLOR, (Object[])new RenderColor[]{top});
        b.vertex(x1, y2, z2).element("Color", CustomVertexElementTypes.RENDER_COLOR, (Object[])new RenderColor[]{top});
        b.vertex(x2, y2, z2).element("Color", CustomVertexElementTypes.RENDER_COLOR, (Object[])new RenderColor[]{top});
        b.vertex(x2, y2, z1).element("Color", CustomVertexElementTypes.RENDER_COLOR, (Object[])new RenderColor[]{top});
    }
}

