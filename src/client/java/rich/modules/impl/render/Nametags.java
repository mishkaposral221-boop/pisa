package rich.modules.impl.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector4f;
import rich.events.api.EventHandler;
import rich.events.impl.WorldRenderEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.util.c;

public class Nametags extends ModuleStructure {
    public BooleanSetting showArmor = new BooleanSetting("ShowArmor", "Display player armor info").setValue(true);

    public static Nametags getInstance() {
        return c.a(Nametags.class);
    }

    public Nametags() {
        super("Nametags", "Display player names above heads", ModuleCategory.VISUALS);
        this.settings(this.showArmor);
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (mc.world == null || mc.player == null) {
            return;
        }
        Quaternionf quat = new Quaternionf((Quaternionfc)mc.gameRenderer.getCamera().getRotation()).conjugate();
        Matrix4f viewMatrix = new Matrix4f().rotation((Quaternionfc)quat);
        float fov = ((Integer)mc.options.getFov().getValue()).intValue();
        Matrix4f projMatrix = mc.gameRenderer.getBasicProjectionMatrix(fov);
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        Vec3d camPos = mc.gameRenderer.getCamera().getCameraPos();
        
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || entity.isSpectator() || !(entity instanceof PlayerEntity)) continue;
            PlayerEntity target = (PlayerEntity)entity;
            double dist = mc.player.squaredDistanceTo(entity);
            if (dist > 4096.0) continue;
            double lerpX = entity.lastRenderX + (entity.getX() - entity.lastRenderX) * event.getPartialTicks();
            double lerpY = entity.lastRenderY + (entity.getY() - entity.lastRenderY) * event.getPartialTicks();
            double lerpZ = entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * event.getPartialTicks();
            double headY = lerpY + entity.getHeight() + 0.5;
            float[] screen = this.worldToScreen(lerpX, headY, lerpZ, camPos, viewMatrix, projMatrix, sw, sh);
            if (screen == null) continue;
            float screenX = Math.round(screen[0]);
            float screenY = Math.round(screen[1]);
            float distance = (float)Math.sqrt(dist);
            float scale = Math.max(0.5f, Math.min(1.0f, 1.0f - distance / 20.0f));
            this.renderNametag(target, screenX, screenY, scale, distance);
        }
    }

    private float[] worldToScreen(double wx, double wy, double wz, Vec3d camPos, Matrix4f viewMatrix, Matrix4f projMatrix, int sw, int sh) {
        float dx = (float)(wx - camPos.x);
        float dy = (float)(wy - camPos.y);
        float dz = (float)(wz - camPos.z);
        Vector4f pos = new Vector4f(dx, dy, dz, 1.0f);
        viewMatrix.transform(pos);
        projMatrix.transform(pos);
        if (pos.w() <= 0.001f) {
            return null;
        }
        float ndcX = pos.x() / pos.w();
        float ndcY = pos.y() / pos.w();
        float screenX = (ndcX + 1.0f) * 0.5f * (float)sw;
        float screenY = (-ndcY + 1.0f) * 0.5f * (float)sh;
        return new float[]{screenX, screenY};
    }

    private void renderNametag(PlayerEntity player, float cx, float cy, float scale, float distance) {
        float health = player.getHealth();
        float absorption = player.getAbsorptionAmount();
        float maxHp = Math.max(1.0f, player.getMaxHealth());
        float totalHealth = health + absorption;
        String name = player.getName().getString();
        String hpStr = String.format("%.0f", totalHealth);
        
        MutableText nameComp = Text.literal(name);
        MutableText hpComp = Text.literal(" " + hpStr);
        
        int nameW = mc.textRenderer.getWidth((StringVisitable)nameComp);
        int hpW = mc.textRenderer.getWidth((StringVisitable)hpComp);
        int totalW = nameW + hpW;
        
        float bgPad = 5.0f;
        float bgW = Math.max((float)totalW + bgPad * 2.0f, 60.0f);
        float bgH = 19;
        float bgX = -bgW / 2.0f;
        float bgY = 0.0f;
        
        // Note: This is a simplified version without actual DrawContext rendering
        // To render properly, we would need access to a DrawEvent with DrawContext
        // For now, just keep the calculation logic
    }
}
