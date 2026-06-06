package rich.modules.impl.combat;

import java.util.Random;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import rich.events.api.EventHandler;
import rich.events.impl.WorldRenderEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.ModeSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.c;

public class AimAssist extends ModuleStructure {

    private final AimEngine engine = new AimEngine(this);
    private final Random random = new Random();

    // --- Settings ---
    public SliderSettings fov = new SliderSettings("FOV", "Угол обзора для аима")
            .setValue(45.0F).range(10.0F, 180.0F);
    public SliderSettings maxDistance = new SliderSettings("Distance", "Макс. дистанция до цели")
            .setValue(3.0F).range(1.0F, 6.0F);
    public SliderSettings smoothness = new SliderSettings("Smoothness", "Сила плавности (0=медл, 1=быстр)")
            .setValue(0.35F).range(0.05F, 1.0F);
    public BooleanSetting onlyWeapon = new BooleanSetting("OnlyWeapon", "Только с оружием")
            .setValue(true);
    public BooleanSetting onlyOnMove = new BooleanSetting("OnlyOnMouseMove",
            "Ассист только при движении мыши (антидетект)")
            .setValue(true);
    // Профиль ротации: SLOTH (легит), SMOOTH (средний), FUNTIME, HVH
    public ModeSetting profile = new ModeSetting("Profile", "Профиль ротации",
            "SMOOTH", "SLOTH", "SMOOTH", "FUNTIME", "HVH");

    public AimAssist() {
        super("AimAssist", "Помощь прицеливания", ModuleCategory.COMBAT);
        this.settings(fov, maxDistance, smoothness, onlyWeapon, onlyOnMove, profile);
    }

    public static AimAssist getInstance() { return c.a(AimAssist.class); }

    // Геттеры для AimEngine
    public float fov()          { return fov.getValue(); }
    public float maxDistance()  { return maxDistance.getValue(); }
    public float smoothness()   { return smoothness.getValue(); }
    public boolean onlyWeapon() { return onlyWeapon.isValue(); }
    public boolean onlyOnMove() { return onlyOnMove.isValue(); }
    public AimEngine.Profile getProfile() {
        return switch (profile.getValue()) {
            case "SLOTH"   -> AimEngine.Profile.SLOTH;
            case "FUNTIME" -> AimEngine.Profile.FUNTIME;
            case "HVH"     -> AimEngine.Profile.HVH;
            default        -> AimEngine.Profile.SMOOTH;
        };
    }
    public AimEngine getEngine() { return engine; }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        engine.onFrame(event.getPartialTicks());
    }

    /** Находит лучшую цель в FOV. Приоритет — наименьший угол, дистанция как тай-брейкер. */
    public Entity findTarget(MinecraftClient client) {
        if (client.player == null || client.world == null) return null;
        ClientPlayerEntity player = client.player;
        if (onlyWeapon() && !isHoldingWeapon(player)) return null;

        Entity best = null;
        double bestScore = Double.MAX_VALUE;
        for (Entity entity : client.world.getEntities()) {
            if (entity == player || !(entity instanceof PlayerEntity tp)) continue;
            if (!tp.isAlive() || tp.isSpectator()) continue;
            double dist = player.distanceTo(entity);
            if (dist > maxDistance() || dist < 0.5) continue;

            double[] diff = getAngleDiff(player, entity.getEyePos());
            double angleDist = Math.sqrt(diff[0]*diff[0] + diff[1]*diff[1]);
            if (angleDist > fov()) continue;

            // Наименьший угол — лучшая цель (не ближайшая игрок за спиной)
            double score = angleDist + dist * 0.1;
            if (score < bestScore) { bestScore = score; best = entity; }
        }
        return best;
    }

    public double[] getAngleDiff(PlayerEntity player, Vec3d target) {
        return getAngleDiffWithYawPitch(player, target, player.getYaw(), player.getPitch());
    }

    public double[] getAngleDiffWithYawPitch(PlayerEntity player, Vec3d target,
                                              float yaw, float pitch) {
        Vec3d eye = player.getEyePos();
        Vec3d d   = target.subtract(eye);
        double hd = Math.sqrt(d.x*d.x + d.z*d.z);
        double tYaw   = Math.toDegrees(Math.atan2(-d.x, d.z));
        double tPitch = Math.toDegrees(-Math.atan2(d.y, hd));
        return new double[]{
            MathHelper.wrapDegrees(tYaw   - yaw),
            MathHelper.wrapDegrees(tPitch - pitch)
        };
    }

    public Vec3d getNearestPoint(Entity entity, PlayerEntity player) {
        Box box = entity.getBoundingBox();
        Vec3d eye = player.getEyePos();
        float yR = player.getYaw()   * ((float)Math.PI/180);
        float pR = player.getPitch() * ((float)Math.PI/180);
        double lx = -Math.sin(yR)*Math.cos(pR), ly = -Math.sin(pR), lz = Math.cos(yR)*Math.cos(pR);
        double dist = eye.distanceTo(box.getCenter());
        Vec3d ct = eye.add(new Vec3d(lx,ly,lz).multiply(dist));
        return new Vec3d(
            MathHelper.clamp(ct.x, box.minX, box.maxX),
            MathHelper.clamp(ct.y, box.minY, box.maxY),
            MathHelper.clamp(ct.z, box.minZ, box.maxZ)
        );
    }

    private boolean isHoldingWeapon(PlayerEntity player) {
        Item i = player.getMainHandStack().getItem();
        return i.getRegistryEntry().isIn(ItemTags.SWORDS)
            || i.getRegistryEntry().isIn(ItemTags.AXES)
            || i.getRegistryEntry().isIn(ItemTags.PICKAXES)
            || i.getRegistryEntry().isIn(ItemTags.MELEE_WEAPON_ENCHANTABLE)
            || i instanceof RangedWeaponItem;
    }
}
